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

package au.org.democracydevelopers.raireservice.persistence.repository;

import au.org.democracydevelopers.raireservice.persistence.entity.CVRContestInfoId;
import au.org.democracydevelopers.raireservice.persistence.entity.CVRContestInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Database retrieval of vote data associated with a specific contests on a CVR.
 */
public interface CVRContestInfoRepository extends JpaRepository<CVRContestInfo, CVRContestInfoId> {

  /**
   * Retrieve the ranked choice data associated with a specific contest in a specific county
   * across all CVRS in the database. If a record in cvr_contest_info has a malformed choices
   * entry (ie. a non-empty string that is not a Json representation for a list), then a
   * JPASystemException will be thrown indicating that an error has occurred in attribute
   * conversion. If the choices columns has nulls, then null elements may be present in the
   * return list of vote data.
   * @param contestId the ID of the contest
   * @param countyId the ID of the county
   * @return a List of List<String> where each List<String> is a list of ranked choices.
   * @throws org.springframework.orm.jpa.JpaSystemException
   * @throws org.springframework.dao.DataAccessException
   */
  @Query(value = "select ci.choices from CVRContestInfo ci " +
      " where ci.contestId = :contestId and ci.countyId = :countyId")
  List<String[]> getCVRs(@Param("contestId") long contestId, @Param("countyId") long countyId);

}
