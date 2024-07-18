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

import au.org.democracydevelopers.raireservice.service.RaireServiceException;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Database retrieval and storage for Assertions.
 */
@Repository
public interface AssertionRepository extends JpaRepository<Assertion, Long> {

  Logger logger = LoggerFactory.getLogger(AssertionRepository.class);

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
  @Modifying
  long deleteByContestName(String contestName);

  /**
   * For the given collection of raire-java assertions, transform them into a form suitable
   * for storing in the corla database and save them to the database. Note that this method will
   * not verify that the provided array of candidate names are the candidates for the contest or
   * that the names themselves are valid, as stored in the database, or that a contest of the
   * given name exists.
   * @param contestName Name of the contest to which these assertions belong.
   * @param universeSize Number of ballots in the auditing universe for these assertions.
   * @param candidates Names of the candidates in the contest.
   * @param assertions Array of raire-java assertions for the contest.
   * @throws RaireServiceException with INTERNAL_ERROR if the caller supplies a non-positive
   * universe size, invalid margin, or invalid combination of winner, loser and list of assumed
   * continuing candidates, or if winner or loser indices in any of the raire-java assertions are
   * invalid with respect to the given array of candidates.  These are all internal errors by this
   * point because they should have been caught earlier.
   * Also translateAssertion may throw a RaireServiceException with WRONG_CANDIDATE_NAMES, which
   * is not caught.
   */
  @Modifying
  default void translateAndSaveAssertions(String contestName, long universeSize, String[] candidates,
      AssertionAndDifficulty[] assertions) throws RaireServiceException
  {
    final String prefix = "[translateAndSaveAssertions]";
    logger.debug(String.format("%s Translating and saving %s raire-java assertions to the " +
        "database. Additional parameters: contest name %s; universe size %d; and candidates %s.",
        prefix, assertions.length, contestName, universeSize, Arrays.toString(candidates)));

    List<Assertion> translated = new ArrayList<>();
    try {
      for (AssertionAndDifficulty a : assertions) {
        Assertion translateAssertion = translateAssertion(a, contestName, universeSize, candidates);
        translated.add(translateAssertion);
      }
      logger.debug(String.format("%s Translation complete.", prefix));
    } catch (IllegalArgumentException ex) {
           final String msg = String.format("%s Invalid arguments were supplied to " +
          "AssertionRepository::translateAndSaveAssertions or GenerateAssertionsSummaryRepository::update. " +
          "This is likely either a non-positive " +
          "universe size, invalid margin, or invalid combination of winner, loser and list of " +
          "assumed continuing candidates, or, for the summary, both a winner and an error, or neither. %s",
          prefix, ex.getMessage());
      logger.error(msg);
      throw new RaireServiceException(msg, RaireErrorCode.INTERNAL_ERROR);
    } catch (ArrayIndexOutOfBoundsException ex) {
           final String msg = String.format("%s Array index out of bounds access in " +
          "AssertionRepository::translateAndSaveAssertions. This was likely due to a winner " +
          "or loser index in a raire-java assertion being invalid with respect to the " +
          "candidates list for the contest. %s", prefix, ex.getMessage());
      logger.error(msg);
      throw new RaireServiceException(msg, RaireErrorCode.INTERNAL_ERROR);
    }



    // If all is good, save the assertions.
    logger.debug(String.format("%s (Database access) Proceeding to save generated assertions.",prefix));
    this.saveAll(translated);

    logger.debug(String.format("%s Save all complete.", prefix));
  }


  /**
   * Find and return the list of assertions generated for the given contest, throwing a
   * RaireServiceException with error code NO_ASSERTIONS_PRESENT when no assertions have been
   * generated for the contest.
   * @param contestName Name of the contest for which to return assertions.
   * @return The list of assertions generated for the contest with name 'contestName'
   * @throws RaireServiceException when no assertions have been generated for the given contest.
   */
  @Query
  default List<Assertion> getAssertionsThrowError(String contestName) throws RaireServiceException {
    final String prefix = "[getAssertionsThrowError]";
    logger.debug(String.format("%s (Database access) Retrieve all assertions for contest %s.",
        prefix, contestName));

    // Retrieve the assertions.
    List<Assertion> assertions = findByContestName(contestName);

    // If the contest has no assertions, return an error.
    if (assertions.isEmpty()) {
      final String msg = String.format("%s No assertions have been generated for the contest %s.",
          prefix, contestName);
      logger.error(msg);
      throw new RaireServiceException(msg, RaireErrorCode.NO_ASSERTIONS_PRESENT);
    }

    return assertions;
  }

  /**
   * Translate a raire-java::AssertionAndDifficulty into the corresponding raire-service::assertion,
   * of the correct type.
   */

  private Assertion translateAssertion(AssertionAndDifficulty a, String contestName, long universeSize, String[] candidates) throws RaireServiceException {
    if (a.assertion.isNEB()) {
      return new NEBAssertion(contestName, universeSize, a.margin, a.difficulty,
          candidates, (NotEliminatedBefore) a.assertion);
    } else {
      return new NENAssertion(contestName, universeSize, a.margin, a.difficulty,
          candidates, (NotEliminatedNext) a.assertion);
    }
  }
}
