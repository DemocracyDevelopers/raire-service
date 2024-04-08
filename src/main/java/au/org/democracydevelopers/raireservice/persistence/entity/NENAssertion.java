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

package au.org.democracydevelopers.raireservice.persistence.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.Arrays;

/**
 * A Not Eliminated Next assertion asserts that a _winner_ beats a _loser_ in an audit when all
 * candidates other that those in a specified _assumed to be continuing_ list have been removed.
 *
 * In particular, this means that _winner_ can not be the next candidate eliminated.
 *
 * This assertion type is also referred to as an NEN assertion in A Guide to RAIRE.
 *
 * The constructor for this class takes a raire-java NEN assertion construct (NotEliminatedNext)
 * and translates it into a NENAssertion entity, suitable for storage in the corla database.
 */
@Entity
@DiscriminatorValue("NEN")
public class NENAssertion extends Assertion {

  /**
   * {@inheritDoc}
   */
  public NENAssertion() {
    super();
  }

  /**
   * Construct a NENAssertion give a raire-java NotEliminatedNext construct.
   * @param contestName Name of the contest to which this assertion belongs.
   * @param universeSize Number of ballots in the auditing universe for the assertion.
   * @param margin Absolute margin of the assertion.
   * @param difficulty Difficulty of the assertion, as computed by raire-java.
   * @param candidates Names of the candidates in this assertion's contest.
   * @param nen Raire-java NotEliminatedNext assertion to be transformed into a NENAssertion.
   * @throws IllegalStateException if the caller supplies a non-positive universe size.
   * @throws ArrayIndexOutOfBoundsException if the winner or loser indices in the raire-java
   * assertion are invalid with respect to the given array of candidates.
   */
  public NENAssertion(String contestName, long universeSize, int margin, double difficulty,
      String[] candidates, au.org.democracydevelopers.raire.assertions.NotEliminatedNext nen)
      throws IllegalStateException, ArrayIndexOutOfBoundsException
  {
    super(contestName, candidates[nen.winner], candidates[nen.loser], margin, universeSize,
        difficulty, Arrays.stream(nen.continuing).mapToObj(i -> candidates[i]).toList());
  }


}
