/*
  Copyright 2024 Democracy Developers
  The Raire Service is designed to connect colorado-rla and its associated database to
  the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).
  
  This file is part of raire-service.
  raire-service is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with raire-service.  If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.request;

import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.ReadOnlyProperty;

/**
 * Request (expected to be json) describing the contest for which assertions should be generated.
 */
public class GenerateAssertionsRequest {

  Logger logger = LoggerFactory.getLogger(GenerateAssertionsRequest.class);

  /**
   * The name of the contest
   */
  @ReadOnlyProperty
  private String contestName;

  /**
   * The total number of ballots in the universe under audit.
   * This may not be the same as the number of ballots or CVRs in the contest, if the contest
   * is available only to a subset of voters in the universe.
   */
  @ReadOnlyProperty
  private int totalAuditableBallots;

  /**
   * The elapsed time allowed to raire to generate the assertions, in seconds.
   */
  @ReadOnlyProperty
  private float timeLimitSeconds;

  /**
   * List of candidate names.
   */
  @ReadOnlyProperty
  private List<String> candidates;

  /**
   * List of pairs of (CountyID, contestID) that define this contest.
   * TODO will not be necessary after switching to contest name-based queries.
   */
  @ReadOnlyProperty
  private List<CountyAndContestID> countyAndContestIDs;

  /**
   * No args constructor. Used for serialization.
   */
  public GenerateAssertionsRequest() {
  }

  /**
   * All args constructor.
   * @param contestName the name of the contest
   * @param totalAuditableBallots the total auditable ballots in the universe under audit.
   * @param timeLimitSeconds the elapsed time allowed for RAIRE to generate assertions, in seconds.
   * @param candidates the list of candidates by name
   * @param countyAndContestIDs the pairs of (CountyID, contestID) that define this contest.
   */
  public GenerateAssertionsRequest(String contestName, int totalAuditableBallots,
      float timeLimitSeconds, List<String> candidates,
      List<CountyAndContestID> countyAndContestIDs) {
    this.contestName = contestName;
    this.totalAuditableBallots = totalAuditableBallots;
    this.timeLimitSeconds = timeLimitSeconds;
    this.candidates = candidates;
    this.countyAndContestIDs = countyAndContestIDs;
  }

  /**
   * Validates the generate assertions request, checking that the contest exists and is an
   * IRV contest, that the total ballots and time limit have sensible values, and that
   * the contest has candidates. Note it does _not_ check whether the candidates are present in
   * the CVRs.
   *
   * @param contestRepository the respository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    {
      if(contestName == null || contestName.isBlank()) {
        logger.error("No contest name.");
        throw new RequestValidationException("No contest name.");
      }

      if(candidates == null || candidates.isEmpty() || candidates.stream().anyMatch(String::isBlank)) {
        logger.error("Request for contest "+contestName+
            ". Bad candidate list: "+(candidates==null ? "" : candidates));
        throw new RequestValidationException("Bad candidate list.");
      }

      if(totalAuditableBallots <= 0) {
        logger.error("Request for contest "+contestName+
          ". Non-positive total auditable ballots: "+totalAuditableBallots);
        throw new RequestValidationException("Non-positive total auditable ballots.");
      }

      if(timeLimitSeconds <= 0) {
        logger.error("Request for contest "+contestName+
            ". Non-positive time provision for result: "+ timeLimitSeconds);
        throw new RequestValidationException("Non-positive time provision for result.");
      }

      if(contestRepository.findFirstByName(contestName).isEmpty()) {
        logger.error("Request for contest "+contestName+ ". No such contest in database.");
        throw new RequestValidationException("No such contest: "+contestName);
      }

      if(!contestRepository.isAllIRV(contestName)) {
        logger.error("Request for contest "+contestName+ ". Not all IRV contests.");
        throw new RequestValidationException("Not all IRV: "+contestName);
      }

      // TODO Add validation to check whether the contest name matches all the (countyID, contestID)
      // pairs in the database.
      // Unnecessary if we change to request by contest name.
    }


  }

}