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
import java.util.Comparator;
import java.util.List;
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
   * @throws RaireServiceException if assertion retrieval fails, or some other internal error occurs.
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
      String preface = makePreface(request, sortedAssertions);
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
   * Construct the preface of the csv file, including contest metadata (name and list of candidates),
   * along with data about the extreme values in the set of assertions: the maximum difficulty (as
   * estimated by raire), the minimum margin and diluted margin, the maximum optimistic samples to
   * audit and estimated samples to audit.
   * TODO at the moment this just gives the first extremum for each data type - fix it so that if there
   * are multiple matches to the same thing, it returns a list of them all.
   * @param request the GetAssertionsRequest, used for contest name and candidate list.
   * @param assertions the assertions to be written out.
   * @return a preface to the CSV file.
   * @throws RaireServiceException if some of the maxima or minima (for margin, difficulty, etc) don't
   *         exist. This should never happen, because if there are any assertions in the database all
   *         the extrema should have a value, and if there are no assertions we check for that first.
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
