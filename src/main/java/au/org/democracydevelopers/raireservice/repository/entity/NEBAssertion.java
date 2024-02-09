/*
 * Sketch of NEBAssertion class (following conventions of other CORLA classes).
 *
 */

package au.org.democracydevelopers.raireservice.repository.entity;

import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raireservice.response.GetAssertionError;
import au.org.democracydevelopers.raireservice.response.GetAssertionException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


/**
 * Generic assertion for an assertion-based audit.
 *
 */
@Entity
@DiscriminatorValue("NEB")
@Slf4j
public class NEBAssertion extends Assertion  {

  /**
   * Construct an empty NEB assertion (for persistence).
   */
  public NEBAssertion(){
    super();
  }


  /**
   * {@inheritDoc}
   */
  public NEBAssertion(String contestName, String winner, String loser, int margin, long universeSize,
                      double difficulty) {
    super(contestName, winner, loser, margin, universeSize, difficulty,  new ArrayList<>());
  }

  /**
   * Return the raire-java style of this assertion, for export as json. The main difference is that candidates are
   * referred to by their index in the list of candidate names, rather than by name directly.
   * @param candidates The list of candidate names as strings.
   * @return A raire-java style NEB Assertion with the same data as this.
   * @throws RuntimeException if the data retrieved from the database is not consistent with a valid NEN assertion.
   */
  @Override
  public au.org.democracydevelopers.raire.assertions.Assertion makeRaireAssertion(List<String> candidates) throws GetAssertionException {

      // Find index of winner, loser.
      int winnerIndex = candidates.indexOf(winner);
      int loserIndex = candidates.indexOf(loser);

      if (winnerIndex != -1 && loserIndex != -1 && assumedContinuing.isEmpty()) {
         log.info(String.format("Valid NEB assertion retrieved from database: %s", this));
         return new NotEliminatedBefore(winnerIndex, loserIndex);
      } else {
         log.error(String.format("Invalid NEB assertion retrieved from database: %s", this));
         throw new GetAssertionException(new GetAssertionError.ErrorRetrievingAssertions()) ;
      }


  }
}
