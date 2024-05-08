/*
Copyright 2024 Democracy Developers

The Raire Service is designed to connect colorado-rla and its associated database to
the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).

This file is part of raire-service.

raire-service is free software: you can redistribute it and/or modify it under the terms
of the GNU Affero General Public License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with
raire-service. If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCodes;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Convert a contest's assertions, along with associated metadata, into a csv file for return via
 * the /get-assertions-csv endpoint.
 */
@Service
public class GetAssertionsCSVService {

  private final static Logger logger = LoggerFactory.getLogger(GetAssertionsService.class);

  private final AssertionRepository assertionRepository;

  /**
   * Row descriptors for the statistics for which we are computing max or min.
   */
  private final static String MARGIN = "Margin";
  private final static String DILUTED_MARGIN = "Diluted margin";
  private final static String DIFFICULTY = "Raire difficulty";
  private final static String CURRENT_RISK = "Current risk";
  private final static String OPTIMISTIC_SAMPLES = "Optimistic samples to audit";
  private final static String ESTIMATED_SAMPLES = "Estimated samples to audit";

  // List of statistics we find the minimum or maximum of.
  private final static List<String> statisticNames = List.of(MARGIN, DILUTED_MARGIN,
      DIFFICULTY, CURRENT_RISK, OPTIMISTIC_SAMPLES, ESTIMATED_SAMPLES);
  /**
   * Error allowed when comparing doubles.
   */
  private final static double EPS = 0.0000001;

  /**
   * All args constructor.
   * @param assertionRepository the assertion repository.
   */
  public GetAssertionsCSVService(AssertionRepository assertionRepository) {
    this.assertionRepository = assertionRepository;
  }

  /**
   * Generate CSV, main function. The csv includes
   * - a preface, with metadata about the contest and the assertions
   * - the header row for the csv columns
   * - the actual csv data, one row for each assertion
   * @param request the GetAssertionsRequest, which contains the contest name and candidates.
   * @return the csv as a string.
   * @throws RaireServiceException if assertion retrieval fails, or some other database-related error
   *         occurs.
   */
  public String generateCSV(GetAssertionsRequest request) throws RaireServiceException {
    try {
      // Retrieve the assertions.
      List<Assertion> assertions = assertionRepository.findByContestName(request.contestName);

      // If the contest has no assertions, return an error.
      if (assertions.isEmpty()) {
        String msg = "No assertions have been generated for the contest " + request.contestName;
        logger.error("GetAssertionsCSVService::generateCSV" + msg);
        throw new RaireServiceException(msg, RaireErrorCodes.NO_ASSERTIONS_PRESENT);
      }

      // Sort the assertions by ID. This may be redundant, but it guarantees that they are arranged
      // in a consistent order over multiple csv requests.
      List<Assertion> sortedAssertions
          = assertions.stream().sorted(Comparator.comparingLong(Assertion::getId)).toList();

      // Write metadata/summary at the top of the file, then the csv header row, then the assertion data.
      Map<String, List<Integer>> extrema = findExtrema(sortedAssertions);
      String preface = makePreface(request, extrema, sortedAssertions);
      String headers = makeHeaders();
      String contents = makeContents(sortedAssertions);

      return preface + "\n\n" + headers + '\n' + contents;

    } catch (Exception e) {
      String msg = "Error retrieving assertions for the contest " + request.contestName;
      logger.error("GetAssertionsCSVService::generateCSV" + msg);
      throw new RaireServiceException(msg, RaireErrorCodes.INTERNAL_ERROR);
    }
  }

  /**
   * Find the maximum or minimum (whichever is meaningful) for the important statistics:
   * margin, diluted margin, (Raire estimated) difficulty, optimistic samples to audit,
   * estimated samples to audit.
   * Collect into a map the index numbers (in the sorted list) of the assertions that meet the
   * extrema. Note that there may be several that are tied.
   * For example, if there are several assertions with the same least margin, the map will have a
   * key "Margin" with a value listing the indices of all assertions that have that (minimum) margin.
   * @param sortedAssertions The assertions, assumed to be sorted by ID.
   * @return a map from the statistic name (margin, difficulty, etc) and the list of indices of
   * the assertions that have the extremal value for that statistic.
   */
  private Map<String, List<Integer>> findExtrema(List<Assertion> sortedAssertions) {
    // The map from statistic name to list of indices of sortedAssertions that have the extreme value.
    Map<String, List<Integer>> extrema = new HashMap<>();


    // If there are no assertions, there are no extreme statistics.
    if(sortedAssertions.isEmpty()) {
      return extrema;
    }

    // Now we know there is at least one assertion. Initialize all the extrema to the first one.
    for(String s : statisticNames) {
      extrema.put(s,  new ArrayList<>(List.of(0)));
    }

    // Starting from the second assertion, compare each assertion's statistics to the current extrema
    // The `extrema' hashmap's keys are the statistic names (margin, difficulty, etc).
    // Its values are the list of assertion indices that meet the extremal value (min or max as
    // appropriate) for that statistic.
    for(int i=1 ; i < sortedAssertions.size() ; i++ ) {

      // First see if any of its min statistics are smaller than the current minima
      // Margin
      int currentMinMargin = sortedAssertions.get(extrema.get(MARGIN).getFirst()).getMargin();
      if(sortedAssertions.get(i).getMargin() == currentMinMargin) {
        // This assertion has the same value as the current minimum. Add it to the list of
        // extremal assertions.
        extrema.get(MARGIN).add(i);
      } else if (sortedAssertions.get(i).getMargin() < currentMinMargin) {
        // This assertion has a strictly smaller value than the current minimum. Replace the
        // list of extremal assertions with this one only - it is (so far) the unique minimum.
        extrema.replace(MARGIN, new ArrayList<>(List.of(i)));
      }

      // Diluted Margin
      double currentMinDilutedMargin = sortedAssertions.get(extrema.get(DILUTED_MARGIN).getFirst())
          .getDilutedMargin();
      if(Math.abs(sortedAssertions.get(i).getDilutedMargin() - currentMinDilutedMargin) < EPS) {
        // This assertion has the same value as the current minimum. Add it to the list of
        // extremal assertions.
        extrema.get(DILUTED_MARGIN).add(i);
      } else if (sortedAssertions.get(i).getDilutedMargin() < currentMinDilutedMargin) {
        // Note the 'else if' is important here, (the 'else' part is redundant for the integer comparisons)
        // because if the new value is very slightly less than the current min, we only want to add
        // it once.

        // This assertion has a strictly smaller value than the current minimum. Replace the
        // list of extremal assertions with this one only - it is (so far) the unique minimum.
        extrema.replace(DILUTED_MARGIN, new ArrayList<>(List.of(i)));
      }

      // Difficulty
      double currentMaxDifficulty = sortedAssertions.get(extrema.get(DIFFICULTY).getFirst())
          .getDifficulty();
      if(Math.abs(sortedAssertions.get(i).getDifficulty() - currentMaxDifficulty) < EPS) {
        // This assertion has the same value as the current maximum. Add it to the list of
        // extremal assertions.
        extrema.get(DIFFICULTY).add(i);
      } else if (sortedAssertions.get(i).getDilutedMargin() > currentMinDilutedMargin) {
        // Note the 'else if' is important here, (the 'else' part is redundant for the integer comparisons)
        // because if the new value is very slightly less than the current min, we only want to add
        // it once.

        // This assertion has a strictly greater value than the current maximum. Replace the
        // list of extremal assertions with this one only - it is (so far) the unique maximum.
        extrema.replace(DIFFICULTY, new ArrayList<>(List.of(i)));
      }

      // Current risk
      BigDecimal currentRisk = sortedAssertions.get(extrema.get(CURRENT_RISK).getFirst())
          .getCurrentRisk();
      if(sortedAssertions.get(i).getCurrentRisk().compareTo(currentRisk) == 0) {
        // This assertion has the same value as the current maximum. Add it to the list of
        // extremal assertions.
        extrema.get(CURRENT_RISK).add(i);
      } else if (sortedAssertions.get(i).getCurrentRisk().compareTo(currentRisk) > 0) {

        // This assertion has a strictly greater value than the current maximum. Replace the
        // list of extremal assertions with this one only - it is (so far) the unique maximum.
        extrema.replace(CURRENT_RISK, new ArrayList<>(List.of(i)));
      }

      // Optimistic samples to audit
      double currentMaxOptimistic = sortedAssertions.get(extrema.get(OPTIMISTIC_SAMPLES).getFirst())
          .getOptimisticSamplesToAudit();
      if(sortedAssertions.get(i).getOptimisticSamplesToAudit() == currentMaxOptimistic) {
        // This assertion has the same value as the current maximum. Add it to the list of
        // extremal assertions.
        extrema.get(OPTIMISTIC_SAMPLES).add(i);
      } else if (sortedAssertions.get(i).getOptimisticSamplesToAudit() > currentMaxOptimistic) {
        // This assertion has a strictly greater value than the current maximum. Replace the
        // list of extremal assertions with this one only - it is (so far) the unique maximum.
        extrema.replace(OPTIMISTIC_SAMPLES, new ArrayList<>(List.of(i)));
      }

      // Estimated samples to audit
      double currentMaxEstimated = sortedAssertions.get(extrema.get(ESTIMATED_SAMPLES).getFirst())
          .getEstimatedSamplesToAudit();
      if(sortedAssertions.get(i).getEstimatedSamplesToAudit() == currentMaxEstimated) {
        // This assertion has the same value as the current maximum. Add it to the list of
        // extremal assertions.
        extrema.get(ESTIMATED_SAMPLES).add(i);
      } else if (sortedAssertions.get(i).getEstimatedSamplesToAudit() > currentMaxEstimated) {
        // This assertion has a strictly greater value than the current maximum. Replace the
        // list of extremal assertions with this one only - it is (so far) the unique maximum.
        extrema.replace(ESTIMATED_SAMPLES, new ArrayList<>(List.of(i)));
      }
    }

    return extrema;
  }

  /**
   * Construct the preface of the csv file, including contest metadata (name and list of
   * candidates), along with data about the extreme values in the set of assertions: the maximum
   * difficulty (as estimated by raire), the minimum margin and diluted margin, the maximum
   * optimistic samples to audit and estimated samples to audit.
   *
   * @param request the GetAssertionsRequest, used for contest name and candidate list.
   * @param extrema the map from the name of a statistic to the list of indices of assertions that
   *                meet the extreme value (max or min as appropriate).
   * @return a preface to the CSV file.
   */
  private String makePreface(GetAssertionsRequest request, Map<String, List<Integer>> extrema,
      List<Assertion> sortedAssertions) {
      return "Contest name," + StringEscapeUtils.escapeCsv(request.contestName) + '\n'
          + "Candidates," + String.join(",", request.candidates.stream()
            .map(StringEscapeUtils::escapeCsv).toList()) + "\n\n"
          + "\"Extreme item\",Value,\"Assertion IDs\"" + '\n'
          // Print out the name of the extremum statistic, the extreme value, and the list of
          // indices meeting the extremum. The list is indexed 0..size-1, but humans want to read
          // lists as 1..size, so add 1 to index values before printing.
          + MARGIN + ","
              + sortedAssertions.get(extrema.get(MARGIN).getFirst()).getMargin() + ","
              + String.join(",",
                  extrema.get(MARGIN).stream().map(i -> (i+1)+"").toList()) + "\n"
          + DILUTED_MARGIN + ","
              + sortedAssertions.get(extrema.get(DILUTED_MARGIN).getFirst()).getDilutedMargin() + ","
              + String.join(",",
                  extrema.get(DILUTED_MARGIN).stream().map(i -> (i+1)+"").toList()) + "\n"
          + DIFFICULTY + ","
              + sortedAssertions.get(extrema.get(DIFFICULTY).getFirst()).getDifficulty() + ","
              + String.join(",",
                  extrema.get(DIFFICULTY).stream().map(i -> (i+1)+"").toList()) + "\n"
          + CURRENT_RISK + ","
              + sortedAssertions.get(extrema.get(CURRENT_RISK).getFirst()).getCurrentRisk() + ","
              + String.join(",",
                  extrema.get(CURRENT_RISK).stream().map(i -> (i+1)+"").toList()) + "\n"
          + OPTIMISTIC_SAMPLES + ","
              + sortedAssertions.get(extrema.get(OPTIMISTIC_SAMPLES).getFirst()).getOptimisticSamplesToAudit() + ","
              + String.join(",",
                  extrema.get(OPTIMISTIC_SAMPLES).stream().map(i -> (i+1)+"").toList()) + "\n"
          + ESTIMATED_SAMPLES + ","
              + sortedAssertions.get(extrema.get(ESTIMATED_SAMPLES).getFirst()).getEstimatedSamplesToAudit() + ","
              + String.join(",",
                  extrema.get(ESTIMATED_SAMPLES).stream().map(i -> (i+1)+"").toList());
  }

  private String makeHeaders() {
    return "ID,"
        + "Type,"
        + "Winner,"
        + "Loser,"
        + "\"Assumed continuing\","
        + "Difficulty,"
        + "Margin,"
        + "\"Diluted margin\","
        + "Risk,"
        + "\"Estimated samples to audit\","
        + "\"Optimistic samples to audit\","
        + "\"Two vote over count\","
        + "\"One vote over count\","
        + "\"Other discrepancy count\","
        + "\"One vote under count\","
        + "\"Two vote under count\"";
  }


  private String makeContents(List<Assertion> assertions) {

    // Write out the data as a string, with a newline at the end of each assertion's data.
    int index = 1;
    StringBuilder contents = new StringBuilder();
    String assertionType;
    for (Assertion assertion : assertions) {
       switch (assertion) {
         case NENAssertion ignored -> assertionType = "NEN";
         case NEBAssertion ignored -> assertionType = "NEB";
         // Ideally we would use the 'sealed' concept on the Assertion class to tell the compiler
         // that there was no need for this default, but that interacted badly with persistence.
         default -> throw new IllegalStateException("Unexpected value: " + assertion);
       }

       contents.append(
           index++ + ","
           + assertionType + ","
           + StringEscapeUtils.escapeCsv(assertion.getWinner()) + ","
           + StringEscapeUtils.escapeCsv(assertion.getLoser()) + ","
           + StringEscapeUtils.escapeCsv(String.join(",", assertion.getAssumedContinuing())) + ","
           + assertion.getDifficulty() + ","
           + assertion.getMargin() + ","
           + assertion.getDilutedMargin() + ","
           + assertion.getCurrentRisk() + ","
           + assertion.getEstimatedSamplesToAudit() + ","
           + assertion.getOptimisticSamplesToAudit() + ","
           + assertion.getTwoVoteOverCount() + ","
           + assertion.getOneVoteOverCount() + ","
           + assertion.getOtherCount() + ","
           + assertion.getOneVoteUnderCount() + ","
           + assertion.getTwoVoteUnderCount()
       );
    }
    return String.valueOf(contents);
  }
}
