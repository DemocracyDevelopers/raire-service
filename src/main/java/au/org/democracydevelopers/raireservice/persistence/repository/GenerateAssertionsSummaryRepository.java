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

import au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsSummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Database retrieval and storage for the results of generating assertions (the winner, and possibly
 * an error or warning).
 */
@Repository
public interface GenerateAssertionsSummaryRepository extends JpaRepository<GenerateAssertionsSummary, Long> {

  Logger logger = LoggerFactory.getLogger(GenerateAssertionsSummaryRepository.class);

  /**
   * Retrieve all GenerateAssertionsSummary records from the database belonging to the contest with
   * the given name. There will be at most one, because contest name is unique.
   * @param contestName Name of the contest whose data is being retrieved.
   */
  @Query(value="select a from GenerateAssertionsSummary a where a.contestName = :contestName")
  Optional<GenerateAssertionsSummary> findByContestName(@Param("contestName") String contestName);

  /**
   * Delete all summaries (there should be at most one) belonging to the contest with the given name
   * from the database. This is Spring syntactic sugar for the corresponding 'delete' query.
   * @param contestName The name of the contest whose assertions are to be deleted.
   */
  @Query(value="delete from GenerateAssertionsSummary a where a.contestName = :contestName")
  @Modifying
  @Transactional
  void deleteByContestName(@Param("contestName") String contestName);
}
