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
 * Request (expected to be json) identifying a contest by name and listing its candidates.
 * This is an abstract class containing only the core input & validation for contests -
 * just the contest name and list of candidates, plus basic methods to check that they are
 * present, non-null and IRV.
 * Every actual request type inherits from this class and adds some other fields and/or validations.
 */
public abstract class ContestRequest {

  private final static Logger logger = LoggerFactory.getLogger(ContestRequest.class);

  /**
   * The name of the contest
   */
  public final String contestName;

  /**
   * List of candidate names.
   */
  public final List<String> candidates;

  /**
   * All args constructor.
   * @param contestName the name of the contest
   * @param candidates the list of candidates by name
   */
  @ConstructorProperties({"contestName", "candidates"})
  public ContestRequest(String contestName, List<String> candidates) {
    this.contestName = contestName;
    this.candidates = candidates;
  }

  /**
   * Validates the contest request, checking that the contest exists and is an IRV contest, and
   * that the contest request has candidates. Note it does _not_ check whether the candidates are
   * present in the CVRs.
   * @param contestRepository the respository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    final String prefix = "[Validate]";
    logger.debug(String.format("%s Validating a request to retrieve contest information from the "+
        "database for contest %s with specified candidates %s.", prefix, contestName, candidates));

    if(contestName == null || contestName.isBlank()) {
      final String msg = String.format("%s No contest name specified. " +
          "Throwing a RequestValidationException.", prefix);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    if(candidates == null || candidates.isEmpty() || candidates.stream().anyMatch(String::isBlank)) {
      final String msg = String.format("%s Request for contest %s with a bad candidate list %s. " +
          "Throwing a RequestValidationException.", prefix, contestName, (candidates==null ? "" : candidates));
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

    logger.debug(String.format("%s Request for contest information valid.", prefix));
  }
}