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

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Database retrieval and storage for Assertions.
 */
@Repository
public interface AssertionRepository extends JpaRepository<Assertion, Long> {

  /**
   * Retrieve all Assertions from the database belonging to the contest with the given name.
   * @param contestName Name of the contest whose assertions being retrieved.
   */
  @Query(value="select a from Assertion a where a.contestName = :contestName")
  List<Assertion> findByContestName(@Param("contestName") String contestName);

  /**
   * Delete all Assertions belonging to the contest with the given name from the database. This
   * is Spring syntactic sugar for the corresponding 'delete' query.
   * @param contestName The name of the contest whose assertions are to be deleted.
   * @return The number of records deleted from the database.
   */
  long deleteByContestName(String contestName);

  /**
   * For the given collection of raire-java assertions, transform them into a form suitable
   * for storing in the corla database and save them to the database.
   * @param contestName Name of the contest to which these assertions belong.
   * @param universeSize Number of ballots in the auditing universe for these assertions.
   * @param candidates Names of the candidates in the contest.
   * @param assertions Array of raire-java assertions for the contest.
   * @throws IllegalStateException if the caller supplies a non-positive universe size.
   * @throws ArrayIndexOutOfBoundsException if the winner or loser indices in any of the raire-java
   * assertions are invalid with respect to the given array of candidates.
   */
  default void storeAssertions(String contestName, long universeSize, String[] candidates,
      AssertionAndDifficulty[] assertions)
      throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
      List<Assertion> translated = Arrays.stream(assertions).map(a -> createAssertion(contestName,
          universeSize, candidates, a)).toList();

      this.saveAll(translated);
  }

  /**
   * Translates a raire-java assertion into an Assertion, suitable for saving to the database.
   * @param contestName Name of the contest to which this assertion belongs.
   * @param universeSize Number of ballots in the auditing universe for the assertion.
   * @param candidates Names of the candidates in the contest.
   * @param aad The raire-java AssertionAndDifficulty to be translated to an Assertion.
   * @return Assertion object, ready for saving to the database.
   * @throws IllegalStateException if the caller supplies a non-positive universe size.
   * @throws ArrayIndexOutOfBoundsException if the winner or loser indices in the raire-java
   * assertion are invalid with respect to the given array of candidates.
   */
  private Assertion createAssertion(String contestName, long universeSize, String[] candidates,
      au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty aad)
      throws IllegalStateException, ArrayIndexOutOfBoundsException
  {
      if(aad.assertion.isNEB()){
        return new NEBAssertion(contestName, universeSize, aad.margin, aad.difficulty,
            candidates, (NotEliminatedBefore) aad.assertion);
      }
      else{
        return new NENAssertion(contestName, universeSize, aad.margin, aad.difficulty,
            candidates, (NotEliminatedNext) aad.assertion);
      }
  }

}