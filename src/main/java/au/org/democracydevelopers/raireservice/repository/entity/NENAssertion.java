/*
 * Sketch of NENAssertion class (following conventions of other CORLA classes).
 *
 */


package au.org.democracydevelopers.raireservice.repository.entity;

import java.util.Arrays;
import java.util.List;

import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.response.GetAssertionError;
import au.org.democracydevelopers.raireservice.response.GetAssertionException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic assertion for an assertion-based audit.
 *
 */
@Entity
@DiscriminatorValue("NEN")
@Slf4j
public class NENAssertion extends Assertion {

  /**
   * Construct an empty NEN assertion (for persistence).
   */
  public NENAssertion(){
    super();
  }

  /**
   * {@inheritDoc}
   */
  public NENAssertion(String contestName, String winner, String loser, int margin, long universeSize,
                      double difficulty, List<String> assumedContinuing) {
    super(contestName, winner, loser, margin, universeSize, difficulty, assumedContinuing);
  }

  /**
   * Return the raire-java style of this assertion, for export as json. The main difference is that candidates are
   * referred to by their index in the list of candidate names, rather than by name directly.
   * @param candidates The list of candidate names as strings.
   * @return A raire-java style NEN Assertion with the same data as this.
   * @throws RuntimeException if the data retrieved from the database is not consistent with a valid NEN assertion.
   */
  @Override
  public au.org.democracydevelopers.raire.assertions.Assertion makeRaireAssertion(List<String> candidates) throws GetAssertionException {

      // Find index of winner, loser, continuing candidates.
      int winnerIndex = candidates.indexOf(winner);
      int loserIndex = candidates.indexOf(loser);
       int[] continuing =  assumedContinuing.stream().mapToInt(candidates::indexOf).toArray();

      // If the winner, loser, and all continuing candidates were all valid, the assertion is valid.
      if (winnerIndex != -1 && loserIndex != -1 && !Arrays.stream(continuing).anyMatch(c -> c == -1)) {
         log.info(String.format("Valid NEN assertion retrieved from database: %s", this));
         return new NotEliminatedNext(winnerIndex, loserIndex, continuing);
      } else {
         // Otherwise, there's an inconsistency between the candidate list and the assertion we retrieved.
         log.error(String.format("Invalid NEN assertion retrieved from database: %s", this));
         throw new GetAssertionException(new GetAssertionError.ErrorRetrievingAssertions());
      }


  }

}
