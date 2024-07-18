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

import static au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsSummary.UNKNOWN_WINNER;
import static au.org.democracydevelopers.raireservice.service.Metadata.CANDIDATES_HEADER;
import static au.org.democracydevelopers.raireservice.service.Metadata.CONTEST_NAME_HEADER;
import static au.org.democracydevelopers.raireservice.service.Metadata.CURRENT_RISK;
import static au.org.democracydevelopers.raireservice.service.Metadata.DIFFICULTY;
import static au.org.democracydevelopers.raireservice.service.Metadata.DILUTED_MARGIN;
import static au.org.democracydevelopers.raireservice.service.Metadata.ESTIMATED_SAMPLES;
import static au.org.democracydevelopers.raireservice.service.Metadata.MARGIN;
import static au.org.democracydevelopers.raireservice.service.Metadata.OPTIMISTIC_SAMPLES;
import static au.org.democracydevelopers.raireservice.service.Metadata.RISK_LIMIT_HEADER;
import static au.org.democracydevelopers.raireservice.service.Metadata.TOTAL_AUDITABLE_BALLOTS_HEADER;
import static au.org.democracydevelopers.raireservice.service.Metadata.WINNER_HEADER;
import static au.org.democracydevelopers.raireservice.service.Metadata.extremumHeaders;
import static au.org.democracydevelopers.raireservice.service.Metadata.csvHeaders;
import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.NO_ASSERTIONS_PRESENT;
import static au.org.democracydevelopers.raireservice.util.CSVUtils.escapeThenJoin;
import static au.org.democracydevelopers.raireservice.util.CSVUtils.intListToString;

import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsSummary;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.GenerateAssertionsSummaryRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.util.DoubleComparator;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Convert a contest's assertions, along with associated metadata, into a csv file for return via
 * the /get-assertions-csv endpoint.
 */
@Service
public class GetAssertionsCsvService {

  private final static Logger logger = LoggerFactory.getLogger(GetAssertionsCsvService.class);

  private final AssertionRepository assertionRepository;
  private final GenerateAssertionsSummaryRepository generateAssertionsSummaryRepository;

  /**
   * All args constructor.
   * @param assertionRepository the assertion repository.
   */
  public GetAssertionsCsvService(AssertionRepository assertionRepository, GenerateAssertionsSummaryRepository generateAssertionsSummaryRepository) {
    this.assertionRepository = assertionRepository;
    this.generateAssertionsSummaryRepository = generateAssertionsSummaryRepository;
  }

  /**
   * Generate CSV, main function. The csv includes:
   * - a preface, with metadata about the contest and the assertions
   * - the header row for the csv columns
   * - the actual csv data, one row for each assertion
   * @param request the GetAssertionsRequest, which contains the contest name and candidates.
   * @return the csv as a string.
   * @throws RaireServiceException if assertion retrieval fails, or some other database-related error
   *         occurs.
   */
  public String generateCSV(GetAssertionsRequest request) throws RaireServiceException {
    final String prefix = "[generateCSV]";
    try {
      logger.debug(String.format("%s Preparing to export assertions as CSV for contest %s.",
          prefix, request.contestName));

      // Make the metadata/summary for the top of the file,
      String preface = makePreface(request);

      // Retrieve the assertions.
      List<Assertion> assertions = assertionRepository.getAssertionsThrowError(request.contestName);

      // Sort the assertions by ID. This may be redundant, but it guarantees that they are arranged
      // in a consistent order over multiple csv requests.
      List<Assertion> sortedAssertions
          = assertions.stream().sorted(Comparator.comparingLong(Assertion::getId)).toList();

      // make the extrema data, then the csv header row, then the assertion data.
      logger.debug(String.format("%s Converting %d assertions into csv format.", prefix,
          assertions.size()));
      String extrema = findExtrema(sortedAssertions);
      String headers = escapeThenJoin(csvHeaders);
      String contents = makeContents(sortedAssertions, request.candidates);

      logger.debug(String.format("%s %d assertions translated to csv.", prefix, assertions.size()));
      return preface + extrema + "\n\n" + headers + "\n" + contents;

    } catch(RaireServiceException ex) {
      logger.error(String.format("%s RaireServiceException caught. Passing to caller: %s",
          prefix, ex.getMessage()));
      throw ex;
    } catch (Exception e) {
      logger.error(String.format("%s Generic exception caught. Passing to caller: %s",
          prefix, e.getMessage()));
      throw new RaireServiceException(e.getMessage(), RaireErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * Find a single extremum (min or max) and return the appropriate extremumResult structure
   * (detailing the value of the extremum and the indices of the assertions that attain it).
   * @param sortedAssertions the assertions, in the sorted order that the indices will reference.
   *                         These must be non-empty.
   * @param statisticName e.g. margin, diluted margin, estimated samples to audit, current risk.
   * @param type MAX or MIN
   * @param getter the getter for extracting the relevant statistic from a particular assertion.
   * @param comparator an appropriate comparator for the type.
   * @return the extremumResult, including all the data required to make the csv row.
   * @param <T> the type of the statistic. Can be anything on which an appropriate comparator can
   *           be defined, but for all the known examples it's a numeric type.
   * @throws java.util.NoSuchElementException if called on an empty list of assertions.
   */
  private <T> extremumResult<T> findExtremum(List<Assertion> sortedAssertions, String statisticName,
      extremumType type, Function<Assertion,T> getter, Comparator<T> comparator)
      throws NoSuchElementException {

    // Now we know there is at least one assertion. Initialize the extremum with the first
    // assertion's values. Throws NoSuchElementException if the assertion list is empty.
    List<Integer> extremalAssertions = new ArrayList<>(List.of(1));
    T extremalValue = getter.apply(sortedAssertions.getFirst());

    // Starting from the second assertion, compare each assertion's statistics to the current
    // extremum.
    for(int i=1 ; i < sortedAssertions.size() ; i++) {
      // Is this assertion's value for this statistic more extremal than our current extremum?
      T statistic = getter.apply(sortedAssertions.get(i));
      var comparison = comparator.compare(statistic, extremalValue);

      // If this assertion has the same value as the current minimum.
      if (comparison == 0) {
        // Add it to the list of extremal assertions.
        // The human-readable indices start at 1, so we have to add 1.
        extremalAssertions.add(i+1);

        // If we're looking for MAX and this assertion has a higher value than the current max,
        // or we're looking for MIM and this assertion has a lower value than the current min,
      } else if ((type == extremumType.MAX && comparison > 0) ||
                 (type == extremumType.MIN && comparison < 0)) {

        // replace the extremal value and the list of extremal assertions with this one (only) -
        // it is (so far) the unique extremum.
        // The human-readable indices start at 1, so we have to add 1.
        extremalValue = statistic;
        extremalAssertions = new ArrayList<>(List.of(i+1));
      }
    }

    return new extremumResult<>(statisticName, type, extremalValue, extremalAssertions);
  }

  /**
   * The result of finding the extremum, for one statistic.
   * @param statisticName the name of the statistic.
   * @param type whether this is a max or min.
   * @param value the value of the extremum (max or min).
   * @param indices the indices of the assertions that attain the extremum.
   */
  private record extremumResult<T>(
      String statisticName,
      extremumType type,
      T value,
      List<Integer> indices
  ){

    /**
     * Make the appropriate CSV row for the extremum result: comma-separated statistic name, value,
     * and indices of assertions that produced the extremum.
     * @return a CSV row with the relevant data, as a string.
     */
    String toCSVRow() {
      return escapeThenJoin(List.of(statisticName, value.toString()))+","+intListToString(indices);
    }
  }

  /**
   * Extremum type: max or min, so we know whether to search for the biggest or smallest thing.
   */
  private enum extremumType {MAX, MIN}

  /**
   * Find the maximum or minimum (whichever is meaningful) for the important statistics: margin,
   * diluted margin, (raire-java estimated) difficulty, optimistic samples to audit, estimated
   * samples to audit.
   *
   * @param sortedAssertions The assertions, assumed to be sorted by ID.
   * @return the CSV rows for all the extrema: margin, diluted margin, difficulty, etc, along with
   *         all relevant data (the extremal value and the indices of assertions that attain it).
   */
  private String findExtrema(List<Assertion> sortedAssertions) {

    List<String> csvRows = new ArrayList<>();
    DoubleComparator doubleComparator = new DoubleComparator();

    // Minimum margin.
    csvRows.add(findExtremum(sortedAssertions,
        MARGIN, extremumType.MIN, Assertion::getMargin,  Integer::compare
    ).toCSVRow());

    // Minimum diluted margin.
    csvRows.add(findExtremum(sortedAssertions,
        DILUTED_MARGIN, extremumType.MIN, Assertion::getDilutedMargin, doubleComparator
    ).toCSVRow());

    // Maximum difficulty.
    csvRows.add(findExtremum(sortedAssertions,
        DIFFICULTY, extremumType.MAX, Assertion::getDifficulty, doubleComparator
    ).toCSVRow());

    // Maximum current risk.
    csvRows.add(findExtremum(sortedAssertions,
        CURRENT_RISK, extremumType.MAX, Assertion::getCurrentRisk, BigDecimal::compareTo
    ).toCSVRow());

    // Maximum optimistic samples to audit.
    csvRows.add(findExtremum(sortedAssertions,
        OPTIMISTIC_SAMPLES, extremumType.MAX, Assertion::getOptimisticSamplesToAudit, Integer::compare
    ).toCSVRow());

    // Maximum estimated samples to audit.
    csvRows.add(findExtremum(sortedAssertions,
        ESTIMATED_SAMPLES, extremumType.MAX, Assertion::getEstimatedSamplesToAudit,  Integer::compare
    ).toCSVRow());

    return String.join("\n", csvRows);
  }

  /**
   * Construct the preface of the csv file, including contest metadata (contest name, list of
   * candidates, winner, total auditable ballots and risk limit) and headers for the extreme values.
   * @param request the GetAssertionsRequest, used for contest name and candidate list.
   * @return a preface to the CSV file.
   */
  private String makePreface(GetAssertionsRequest request) throws RaireServiceException {
    final String prefix = "[makePreface]";

    Map<String,String> metadata = new HashMap<>();
    metadata.put(CONTEST_NAME_HEADER, request.contestName);
    metadata.put(CANDIDATES_HEADER, escapeThenJoin(request.candidates));
    metadata.put(TOTAL_AUDITABLE_BALLOTS_HEADER, ""+request.totalAuditableBallots);
    metadata.put(RISK_LIMIT_HEADER, request.riskLimit.toString());

    // Throw an exception if there is no summary record in the database.
    Optional<GenerateAssertionsSummary> summary
        = generateAssertionsSummaryRepository.findByContestName(request.contestName);
    if (summary.isEmpty()) {
      String msg = String.format("%s No assertion generation summary for contest %s.", prefix,
          request.contestName);
      logger.error(msg);
      throw new RaireServiceException("No assertion generation summary", NO_ASSERTIONS_PRESENT);
    } else if (summary.get().winner.isBlank()) {
      String msg = String.format("%s Failed assertion generation in contest %s. Error %s, message %s.", prefix,
          request.contestName, summary.get().error, summary.get().message);
      logger.error(msg);
      throw new RaireServiceException(summary.get().message, NO_ASSERTIONS_PRESENT);
    }

    // Put the winner from the database into metadata, or put UNKNOWN_WINNER if it is blank.
    metadata.put(WINNER_HEADER, summary.get().winner.isBlank() ? summary.get().winner : UNKNOWN_WINNER);

    List<String> prefaceHeaders = List.of(CONTEST_NAME_HEADER, CANDIDATES_HEADER, WINNER_HEADER,
        TOTAL_AUDITABLE_BALLOTS_HEADER, RISK_LIMIT_HEADER);

    return String.join("\n", prefaceHeaders.stream()
        .map(h -> escapeThenJoin(List.of(h, metadata.get(h)))).toList())
        + "\n\n"
        + escapeThenJoin(extremumHeaders) + "\n";
  }

  /**
   * Generate the actual csv data rows for a list of assertions. Each row is prepended with an index
   * number (not related to the database's index) that begins at 1 and increments by 1 with each row.
   * @param assertions a list of assertions
   * @return their concatenated csv rows.
   * @throws RaireServiceException if the candidate names in any of the assertions are inconsistent
   *         with the request's candidate list.
   */
  private String makeContents(List<Assertion> assertions, List<String> candidates)
      throws RaireServiceException {

    int index = 1;
    List<String> rows = new ArrayList<>();

    for (Assertion assertion : assertions) {
      rows.add(index++ + "," + escapeThenJoin(assertion.asCSVRow(candidates)));
    }

    return String.join("\n", rows) + "\n";
  }
}
