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
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Convert a contests's assertions, along with associated metadata, into a csv file for return via
 * the /get-assertions-csv endpoint.
 */
@Service
public class GetAssertionsCSVService {

  private final static Logger logger = LoggerFactory.getLogger(GetAssertionsService.class);

  private final AssertionRepository assertionRepository;

  // Row descriptors for the statistics for which we are computing max or min.
  private final static String MARGIN = "Margin";
  private final static String DILUTED_MARGIN = "Diluted margin";
  private final static String DIFFICULTY = "Raire difficulty";
  private final static String OPTIMISTIC_SAMPLES = "Optimistic samples to audit";
  private final static String ESTIMATED_SAMPLES = "Estimated samples to audit";

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

    // List of statistics we find the minimum of.
    List<String> minHeaders = List.of(MARGIN, DILUTED_MARGIN);
    // List of statistics we find the maximum of.
    List<String> maxHeaders = List.of(DIFFICULTY, OPTIMISTIC_SAMPLES, ESTIMATED_SAMPLES);

    // If there are no assertions, there are no extreme statistics.
    if(sortedAssertions.isEmpty()) {
      return extrema;
    }

    // Now we know there is at least one assertion. Initialize all the extrema to the first one.
    for(String s : minHeaders) {
      extrema.put(s, List.of(0));
    }
    for(String s : maxHeaders) {
      extrema.put(s,List.of(0));
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
        extrema.replace(MARGIN, List.of(i));
      }

      // Diluted Margin
      double currentMinDilutedMargin = sortedAssertions.get(extrema.get(DILUTED_MARGIN).getFirst())
          .getDilutedMargin();
      if(sortedAssertions.get(i).getDilutedMargin() == currentMinDilutedMargin) {
        // This assertion has the same value as the current minimum. Add it to the list of
        // extremal assertions.
        extrema.get(DILUTED_MARGIN).add(i);
      } else if (sortedAssertions.get(i).getDilutedMargin() < currentMinDilutedMargin) {
        // This assertion has a strictly smaller value than the current minimum. Replace the
        // list of extremal assertions with this one only - it is (so far) the unique minimum.
        extrema.replace(DILUTED_MARGIN, List.of(i));
      }

      // Difficulty
      double currentMaxDifficulty = sortedAssertions.get(extrema.get(DIFFICULTY).getFirst())
          .getDifficulty();
      if(sortedAssertions.get(i).getDifficulty() == currentMaxDifficulty) {
        // This assertion has the same value as the current maximum. Add it to the list of
        // extremal assertions.
        extrema.get(DIFFICULTY).add(i);
      } else if (sortedAssertions.get(i).getDilutedMargin() > currentMinDilutedMargin) {
        // This assertion has a strictly greater value than the current maximum. Replace the
        // list of extremal assertions with this one only - it is (so far) the unique maximum.
        extrema.replace(DIFFICULTY, List.of(i));
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
        extrema.replace(OPTIMISTIC_SAMPLES, List.of(i));
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
        extrema.replace(ESTIMATED_SAMPLES, List.of(i));
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
   * @param request    the GetAssertionsRequest, used for contest name and candidate list.
   * @param assertions the assertions to be written out.
   * @return a preface to the CSV file.
   * @throws RaireServiceException if some of the maxima or minima (for margin, difficulty, etc.)
   *                               don't exist. This should never happen, because if there are any
   *                               assertions in the database all the extrema should have a value,
   *                               and if there are no assertions we check for that first.
   */
  private String makePreface(GetAssertionsRequest request, List<Assertion> assertions)
      throws RaireServiceException {
    try {
      // Compute the assertions with the greatest difficulty and least margin. These are always the same
      // for Colorado's Super Simple style of audit, but are computed separately here in case the audit
      // style changes.
      Assertion mostDifficult = assertions.stream()
          .max(Comparator.comparingDouble(Assertion::getDifficulty)).get();
      Assertion smallestMargin = assertions.stream()
          .max(Comparator.comparingInt(Assertion::getMargin)).get();

      // Find the assertions with the maximum optimistic and estimated samples to audit. Initially,
      // these will match mostDifficult, but as errors are detected, some assertions' optimistic and
      // estimated samples will increase more than others'.
      Assertion maxOptimistic = assertions.stream()
          .max(Comparator.comparingDouble(Assertion::getOptimisticSamplesToAudit)).get();
      Assertion maxEstimated = assertions.stream()
          .max(Comparator.comparingInt(Assertion::getEstimatedSamplesToAudit)).get();
      return
          "Contest name," + StringEscapeUtils.escapeCsv(request.contestName) + '\n'
              + "Candidates," + String.join(",", request.candidates.stream()
              .map(StringEscapeUtils::escapeCsv).toList()) + "\n\n"
              + "\"Extreme item\",Value,\"Assertion ID\"" + '\n'
              + "Raire difficulty," + mostDifficult.getDifficulty() + ","
              + (assertions.indexOf(mostDifficult) + 1) + "\n"
              + "Margin," + smallestMargin.getMargin() + ","
              + (assertions.indexOf(smallestMargin) + 1) + "\n"
              + "Diluted margin," + smallestMargin.getDilutedMargin() + ","
              + (assertions.indexOf(smallestMargin) + 1) + "\n"
              + "Optimistic samples to audit," + maxOptimistic.getOptimisticSamplesToAudit() + ","
              + (assertions.indexOf(maxOptimistic) + 1) + "\n"
              + "Estimated samples to audit," + maxEstimated.getEstimatedSamplesToAudit() + ","
              + (assertions.indexOf(maxEstimated) + 1);
    } catch (NoSuchElementException e) {
      // Throw an exception if some of the maxes and mins did not exist.
      // This should never happen, because we check that the assertion list is nonempty, and any nonempty
      // list should have a (possibly not unique) max and min.
      String msg = "Error building CSV for contest " + request.contestName;
      logger.error("GetAssertionsCSVService::makePreface:" + msg);
      throw new RaireServiceException(msg, RaireErrorCodes.INTERNAL_ERROR);
    }
  }

  private String makeHeaders() {
    return "ID,"
        + "Type,"
        + "Winner,"
        + "Loser,"
        + "\"Assumed continuing\","
        + "Difficulty,"
        + "Margin,"
        + "Risk,"
        + "\"Diluted margin\","
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
         case NENAssertion a -> assertionType = "NEN";
         case NEBAssertion a -> assertionType = "NEB";
         // TODO seal Assertion class to obviate need for default case.
         default -> throw new IllegalStateException("Unexpected value: " + assertion);
       }

       contents.append(
           index++ + ","
           + assertionType + ','
           + StringEscapeUtils.escapeCsv(assertion.getWinner()) + ','
           + StringEscapeUtils.escapeCsv(assertion.getLoser()) + ','
           + String.join(",", assertion.getAssumedContinuing().stream()
           .map(StringEscapeUtils::escapeCsv).toList()) + ','
           + assertion.getDifficulty() + ','
           + assertion.getMargin() + ','
           + assertion.getDilutedMargin() + ','
           + assertion.getCurrentRisk() + ','
           + assertion.getEstimatedSamplesToAudit() + ','
           + assertion.getOptimisticSamplesToAudit() + ','
           + assertion.getTwoVoteOverCount() + ','
           + assertion.getOneVoteOverCount() + ','
           + assertion.getOtherCount() + ','
           + assertion.getOneVoteUnderCount() + ','
           + assertion.getTwoVoteUnderCount() + '\n'
       );
    }
    return String.valueOf(contents);
  }
}
