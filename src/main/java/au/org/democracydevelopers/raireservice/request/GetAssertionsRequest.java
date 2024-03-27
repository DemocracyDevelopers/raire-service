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
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.ReadOnlyProperty;

/**
 * Request (expected to be json) describing the contest for which assertions should be retrieved
 * from the database (expected to be exported as json).
 */
public class GetAssertionsRequest {

  Logger logger = LoggerFactory.getLogger(GenerateAssertionsRequest.class);

  /**
   * The name of the contest
   */
  @ReadOnlyProperty
  public String contestName;

  /**
   * List of candidate names.
   */
  @ReadOnlyProperty
  private List<String> candidates;

  /**
   * The risk limit for the audit, expected to be in the range [0,1].
   */
  @ReadOnlyProperty
  private BigDecimal riskLimit;

  /**
   * No args constructor. Needed for serialization.
   */
  public GetAssertionsRequest() {
  }

  /**
   * All args constructor.
   * @param contestName the name of the contest
   * @param candidates a list of candidates by name
   * @param riskLimit the risk limit for the audit, expected to be in the range [0,1].
   */
  public GetAssertionsRequest(String contestName, List<String> candidates, BigDecimal riskLimit) {
    this.contestName = contestName;
    this.candidates = candidates;
    this.riskLimit = riskLimit;
  }

  /**
   * Validates the request to retrieve assertions for the contest, checking that the contest exists
   * and is an IRV contest, that the risk limit has a sensible value, and that there are candidates.
   * Note it does _not_ check whether the candidates are present in the CVRs.
   * @param contestRepository the respository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    if (contestName == null || contestName.isBlank()) {
      logger.error("No contest name.");
      throw new RequestValidationException("No contest name.");
    }

    if (candidates == null || candidates.isEmpty() || candidates.stream().anyMatch(String::isBlank)) {
      logger.error("Request for contest "+contestName+
          ". Bad candidate list: "+(candidates==null ? "" : candidates));
      throw new RequestValidationException("Bad candidate list.");
    }

    // Check for a negative risk limit. Risk limits >1 are vacuous but not illegal.
    // Risk limits of exactly zero are unattainable but will not cause a problem.
    if (riskLimit == null || riskLimit.compareTo(BigDecimal.ZERO) < 0) {
      logger.error("Request for contest "+contestName+
          ". Null or negative risk limit: "+(riskLimit==null ? "" : riskLimit));
      throw new RequestValidationException("Null or negative risk limit.");
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

