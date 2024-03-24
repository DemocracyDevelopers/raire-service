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

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import java.math.BigDecimal;
import java.util.List;
import jdk.jfr.DataAmount;
import org.springframework.data.annotation.ReadOnlyProperty;

public class GenerateAssertionsRequest {

  @ReadOnlyProperty
  public String contestName;
  @ReadOnlyProperty
  // This may not be the same as the number of ballots or CVRs in the contest, if the contest
  // is available only to a subset of voters in the universe.
  public int totalAuditableBallots;
  @ReadOnlyProperty
  public int timeProvisionForResult;
  @ReadOnlyProperty
  public List<String> candidates;
  @ReadOnlyProperty
  public List<CountyAndContestID> countyAndContestIDs;

  // No args constructor. Used for serialization.
  public GenerateAssertionsRequest() {
  }

  // All args constructor.
  public GenerateAssertionsRequest(String contestName, int totalAuditableBallots,
      int timeProvisionForResult,
      List<String> candidates, List<CountyAndContestID> countyAndContestIDs) {
    this.contestName = contestName;
    this.totalAuditableBallots = totalAuditableBallots;
    this.timeProvisionForResult = timeProvisionForResult;
    this.candidates = candidates;
    this.countyAndContestIDs = countyAndContestIDs;
  }

  /**
   * Validates the request, checking that the contest exists and is an IRV contest, that the total
   * ballots and time limit have sensible values, and that there are candidates. Note it does _not_
   * check whether the candidates are present in the CVRs.
   *
   * @param contestRepository the respository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    {
      if(contestName == null || contestName.isBlank()) {
        throw new RequestValidationException("No contest name.");
      }

      if(candidates == null || candidates.isEmpty() || candidates.stream().anyMatch(String::isBlank)) {
        throw new RequestValidationException("Bad candidate list.");
      }

      if(totalAuditableBallots <= 0) {
        throw new RequestValidationException("Non-positive total auditable ballots.");
      }

      if(timeProvisionForResult <= 0) {
        throw new RequestValidationException("Non-positive time provision for result.");
      }

      if(contestRepository.findFirstByName(contestName).isEmpty()) {
        throw new RequestValidationException("No such contest: "+contestName);
      }

      if(!contestRepository.isAllIRV(contestName)) {
        throw new RequestValidationException("Not all IRV: "+contestName);
      }

      // TODO Add validation to check whether the contest name matches all the (countyID, contestID)
      // pairs in the database.
      // Unnecessary if we change to request by contest name.
    }


  }

}