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

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * Database retrieval for contests, either by name or by (CountyID, contestID) pairs.
 */
@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

  /**
   * Select contests by contest name
   * Not used except in isAllIRV.
   * Spring syntactic sugar for the obvious SELECT query.
   * @param contestName the name of the contest.
   * @return the contests with that name, as retrieved from the database.
   */
  List<Contest> findByName(String contestName);

  /**
   * Find the first contest by name
   * @param contestName the name of the contest.
   * @return the first of that name,
   */
  Optional<Contest> findFirstByName(String contestName);


  /**
   * Select contests by contest ID and county ID.
   * Contest ID is unique, so at most one result is possible.
   * @param contestID the ID of the contest
   * @param countyID the ID of the county
   * @return the (singleton or empty) matching contest.
   */
  @Query(value = "select c from Contest c where c.id = :contestID and c.countyID = :countyID")
  Optional<Contest> findByContestAndCountyID(@Param("contestID") long contestID,
      @Param("countyID") long countyID);

  /**
   * Check whether all the contests of the given name have description 'IRV'.
   * @param contestName the name of the contest
   * @return false if there are any non-IRV descriptions for a contest of that name.
   * Note it does _not_ test for existence - use findFirstByName for that.
   */
  default boolean isAllIRV(String contestName) {
    List<Contest> contests = findByName(contestName);

    return contests.stream().allMatch(contest -> contest.getDescription().equals("IRV"));
  }
}