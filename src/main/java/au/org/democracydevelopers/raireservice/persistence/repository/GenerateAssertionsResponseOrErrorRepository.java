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

import au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsResponseOrError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Database retrieval and storage for the results of generating assertions (the winner, and possibly
 * an error).
 */
@Repository
public interface GenerateAssertionsResponseOrErrorRepository
    extends JpaRepository<GenerateAssertionsResponseOrError, Long> {

  Logger logger = LoggerFactory.getLogger(GenerateAssertionsResponseOrErrorRepository.class);

  /**
   * Retrieve all GenerateAssertionsResponseOrError records (there should be at most one) from the
   * database belonging to the contest with the given name.
   * @param contestName Name of the contest whose data is being retrieved.
   */
  @Query(value="select a from GenerateAssertionsResponseOrError a where a.contestName = :contestName")
  List<GenerateAssertionsResponseOrError> findByContestName(@Param("contestName") String contestName);

}
