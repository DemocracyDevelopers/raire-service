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

/**
 * Abstract class that serves as a parent class for particular requests to raire. This class
 * identifies a contest by name and includes
 * - the candidates (by name), and
 * - the total auditable ballots in the universe (used to calculate difficulty in raire).
 * The only significant method is a verification method for checking that the data items are
 * present and have reasonable values.
 * GenerateAssertionsRequest and GetAssertionsRequest inherit from this class and add some other
 * fields and validations.
 */
public abstract class ContestRequest {

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
   * List of candidate names.
   */
  public final List<String> candidates;

  /**
   * Class-wide logger.
   */
  private final static Logger logger = LoggerFactory.getLogger(ContestRequest.class);

  /**
   * All args constructor.
   * @param contestName the name of the contest
   * @param totalAuditableBallots the total auditable ballots in the universe under audit.
   * @param candidates the list of candidates by name
   */
  protected ContestRequest(String contestName, int totalAuditableBallots, List<String> candidates) {
    this.contestName = contestName;
    this.totalAuditableBallots = totalAuditableBallots;
    this.candidates = candidates;
  }

  /**
   * Validates the contest request, checking that the contest exists and is an IRV contest, that
   * the total ballots has a sensible value, and that the contest has candidates.
   * Note it does _not_ check whether the candidates are present in the CVRs.
   * @param contestRepository the repository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    final String prefix = "[Validate]";
    logger.debug(String.format("%s Validating a Contest Request for contest %s " +
        "with specified candidates %s, total number of auditable ballots %d.", prefix, contestName,
        candidates, totalAuditableBallots));

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
  }
}