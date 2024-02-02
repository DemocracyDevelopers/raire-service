/*
 * Sketch of NEBAssertion class (following conventions of other CORLA classes).
 *
 */

package au.org.democracydevelopers.raireservice.repository.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.*;


/**
 * Generic assertion for an assertion-based audit.
 *
 */
@Entity
@DiscriminatorValue("NEB")
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
}
