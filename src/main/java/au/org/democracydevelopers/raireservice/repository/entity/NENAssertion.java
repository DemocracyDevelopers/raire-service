/*
 * Sketch of NENAssertion class (following conventions of other CORLA classes).
 *
 */


package au.org.democracydevelopers.raireservice.repository.entity;

import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
/**
 * Generic assertion for an assertion-based audit.
 *
 */
@Entity
@DiscriminatorValue("NEN")
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
}
