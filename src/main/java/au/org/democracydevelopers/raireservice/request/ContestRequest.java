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

package au.org.democracydevelopers.raireservice.request;

import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.ConstructorProperties;

/**
 * Request (expected to be json) identifying a contest by name and listing other data:
 * - the candidates (by name),
 * - the total auditable ballots in the universe (used to calculate difficulty in raire),
 * - the time limit allowed to raire.
 * This is used directly for requesting assertion generation.
 * The only significant method is a verification method for checking that the data items are
 * present and have reasonable values.
 * The get assertions request type inherits from this class and adds some other fields and
 * validations.
 */
public class ContestRequest {

  private final static Logger logger = LoggerFactory.getLogger(ContestRequest.class);

  /**
   * The name of the contest
   */
  public final String contestName;

  /**
   * The total number of ballots in the universe under audit.
   * This may not be the same as the number of ballots or CVRs in the contest, if the contest
   * is available only to a subset of voters in the universe.
   */
  public final int totalAuditableBallots;

  /**
   * The elapsed time allowed to raire to generate the assertions, in seconds.
   * Ignored for GetAssertionsRequests.
   */
  public final double timeLimitSeconds;

  /**
   * List of candidate names.
   */
  public final List<String> candidates;

  /**
   * All args constructor.
   * @param contestName the name of the contest
   * @param totalAuditableBallots the total auditable ballots in the universe under audit.
   * @param timeLimitSeconds the elapsed time allowed for RAIRE to generate assertions, in seconds.
   * @param candidates the list of candidates by name
   */
  @ConstructorProperties({"contestName", "totalAuditableBallots", "timeLimitSeconds","candidates"})
  public ContestRequest(String contestName, int totalAuditableBallots, double timeLimitSeconds,
      List<String> candidates) {
    this.contestName = contestName;
    this.totalAuditableBallots = totalAuditableBallots;
    this.timeLimitSeconds = timeLimitSeconds;
    this.candidates = candidates;
  }

  /**
   * Validates the contest request, checking that the contest exists and is an IRV contest, that
   * the total ballots and time limit have sensible values, and that the contest has candidates.
   * Note it does _not_ check whether the candidates are present in the CVRs.
   * @param contestRepository the respository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    final String prefix = "[Validate]";
    logger.debug(String.format("%s Validating a Contest Request for contest %s " +
        "with specified candidates %s, total number of auditable ballots %d, and time limit " +
        "on assertion generation of %fs.", prefix, contestName, candidates, totalAuditableBallots,
        timeLimitSeconds));

    if(contestName == null || contestName.isBlank()) {
      final String msg = String.format("%s No contest name specified. " +
          "Throwing a RequestValidationException.", prefix);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    if(candidates == null || candidates.isEmpty() || candidates.stream().anyMatch(String::isBlank)) {
      final String msg = String.format("%s Request for contest %s with a bad candidate list %s. " +
          "Throwing a RequestValidationException.", prefix, contestName,
          (candidates==null ? "" : candidates));
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    if(contestRepository.findFirstByName(contestName).isEmpty()) {
      final String msg = String.format("%s Request for contest %s. No such contest in database. " +
          "Throwing a RequestValidationException.", prefix, contestName);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    if(!contestRepository.isAllIRV(contestName)) {
      final String msg = String.format("%s Request for contest %s: not comprised of all IRV " +
          "contests. Throwing a RequestValidationException.", prefix, contestName);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    if(totalAuditableBallots <= 0) {
      final String msg = String.format("%s Non-positive total auditable ballots (%d). Throwing a " +
          "RequestValidationException.", prefix, totalAuditableBallots);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }
    if(timeLimitSeconds <= 0) {
      final String msg = String.format("%s Non-positive time limit on assertion generation (%f). " +
          "Throwing a RequestValidationException.", prefix, timeLimitSeconds);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    logger.debug(String.format("%s Request for contest information valid.", prefix));
  }
}