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
import org.springframework.data.annotation.ReadOnlyProperty;

/**
 * Request (expected to be json) identifying a contest by name and listing its candidates.
 * This is an abstract class containing only the core input & validation for contests -
 * just the contest name and list of candidates, plus basic methods to check that they are
 * present, non-null and IRV.
 * Every actual request type inherits from this class and adds some other fields and/or validations.
 */
public abstract class ContestRequest {

  // Transient hides it for json serialization used for testing.
  protected final transient Logger logger = LoggerFactory.getLogger(ContestRequest.class);

  /**
   * The name of the contest
   */
  @ReadOnlyProperty
  protected String contestName;

  /**
   * List of candidate names.
   */
  @ReadOnlyProperty
  protected List<String> candidates;

  /**
   * No args constructor. Used for serialization.
   */
  public ContestRequest() {
  }

  /**
   * All args constructor.
   * @param contestName the name of the contest
   * @param candidates the list of candidates by name
   */
  public ContestRequest(String contestName, List<String> candidates) {
    this.contestName = contestName;
    this.candidates = candidates;
  }

  /**
   * Set the contest name. Used for deserialization.
   * @param contestName the name of the contest.
   */
  public void setContestName(String contestName) {
    this.contestName = contestName;
  }
  /**
   * Set the candidate list. Used for deserialization.
   * @param candidates the list of candidate names as strings.
   */
  public void setCandidates(List<String> candidates) {
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

      if(contestName == null || contestName.isBlank()) {
        logger.error("No contest name.");
        throw new RequestValidationException("No contest name.");
      }

      if(candidates == null || candidates.isEmpty() || candidates.stream().anyMatch(String::isBlank)) {
        logger.error("Request for contest "+contestName+
            ". Bad candidate list: "+(candidates==null ? "" : candidates));
        throw new RequestValidationException("Bad candidate list.");
      }

      if(contestRepository.findFirstByName(contestName).isEmpty()) {
        logger.error("Request for contest "+contestName+ ". No such contest in database.");
        throw new RequestValidationException("No such contest: "+contestName);
      }

      if(!contestRepository.isAllIRV(contestName)) {
        logger.error("Request for contest "+contestName+ ". Not all IRV contests.");
        throw new RequestValidationException("Not all IRV: "+contestName);
      }
    }
}