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
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.orm.jpa.JpaSystemException;

/**
 * Database retrieval of vote data associated with a specific contests on a CVR.
 */
public interface CVRContestInfoRepository extends JpaRepository<CVRContestInfo, CVRContestInfoId> {

  /**
   * Retrieve the ranked choice data associated with a specific contest in a specific county
   * across all CVRS in the database. If a record in cvr_contest_info has a malformed choices
   * entry (ie. a non-empty string that is not a Json representation for a list), then a
   * JPASystemException will be thrown indicating that an error has occurred in attribute
   * conversion. If a choices column has nulls/blank strings, then a JPASystemException will be
   * also be thrown -- this indicates problems in the database data.
   * @param contestId the ID of the contest
   * @param countyId the ID of the county
   * @return a List of List<String> where each List<String> is a list of ranked choices.
   * @throws org.springframework.orm.jpa.JpaSystemException when an error has occurred in the
   * conversion of ranked choice vote entries to an array of strings (most likely because the
   * entry was either null or blank or not a JSON representation of a list).
   * @throws org.springframework.dao.DataAccessException when a database error has occurred, such
   * as a connection failure.
   */
  @Query(value = "select ci.choices from CVRContestInfo ci " +
      " where ci.contestId = :contestId and ci.countyId = :countyId")
  List<String[]> getCVRs(@Param("contestId") long contestId, @Param("countyId") long countyId)
      throws JpaSystemException, DataAccessException;

}
