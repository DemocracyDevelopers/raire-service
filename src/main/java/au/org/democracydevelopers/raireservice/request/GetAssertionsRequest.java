/*
  Copyright 2024 Democracy Developers
  The Raire Service is designed to connect colorado-rla and its associated database to
  the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).
  
  This file is part of raire-service.
  raire-service is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.request;

import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import java.math.BigDecimal;
import java.util.List;

public class GetAssertionsRequest {
  private String contestName;
  private List<String> candidates;
  private BigDecimal riskLimit;

  // No args constructor. Needed for serialization.
  public GetAssertionsRequest() {

  }

  // All args constructor.
  public GetAssertionsRequest(String contestName, List<String> candidates, BigDecimal riskLimit) {
    this.contestName = contestName;
    this.candidates = candidates;
    this.riskLimit = riskLimit;
  }

  /**
   * Validates the request, checking that the contest exists and is an IRV contest,
   * that the risk limit has a sensible value, and that there are candidates.
   * Note it does _not_ check whether the candidates are present in the CVRs.
   * @param contestRepository the respository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    if (contestName == null || contestName.isBlank()) {
      throw new RequestValidationException("No contest name.");
    }

    if (candidates == null || candidates.isEmpty() || candidates.stream().anyMatch(String::isBlank)) {
      throw new RequestValidationException("Bad candidate list.");
    }

    // Check for a negative risk limit. Risk limits >1 are vacuous but not illegal.
    // Risk limits of exactly zero are unattainable but will not cause a problem.
    if (riskLimit == null || riskLimit.compareTo(BigDecimal.ZERO) < 0) {
      throw new RequestValidationException("Null or negative risk limit.");
    }

    List<Contest> contests = contestRepository.findByName(contestName);
    if (contests == null || contests.isEmpty()) {
      throw new RequestValidationException("No such contest.");
    }

    if (contests.stream().anyMatch(c -> !c.description.equals("IRV"))) {
      throw new RequestValidationException("Contest " + contestName + " are not all IRV contests.");
    }
  }
}

