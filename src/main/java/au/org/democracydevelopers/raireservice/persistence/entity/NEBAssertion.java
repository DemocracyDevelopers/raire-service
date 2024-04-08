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
import java.util.ArrayList;

@Entity
@DiscriminatorValue("NEB")
public class NEBAssertion extends Assertion {

  /**
   * {@inheritDoc}
   */
  public NEBAssertion() {
    super();
  }

  /**
   * Construct a NEBAssertion give a raire-java NotEliminatedBefore construct.
   * @param contestName Name of the contest to which this assertion belongs.
   * @param universeSize Number of ballots in the auditing universe for the assertion.
   * @param margin Absolute margin of the assertion.
   * @param difficulty Difficulty of the assertion, as computed by raire-java.
   * @param candidates Names of the candidates in this assertion's contest.
   * @param neb Raire-java NotEliminatedBefore assertion to be transformed into a NENAssertion.
   * @throws IllegalStateException if the caller supplies a non-positive universe size.
   * @throws ArrayIndexOutOfBoundsException if the winner or loser indices in the raire-java
   * assertion are invalid with respect to the given array of candidates.
   */
  public NEBAssertion(String contestName, long universeSize, int margin, double difficulty,
      String[] candidates, au.org.democracydevelopers.raire.assertions.NotEliminatedBefore neb)
      throws IllegalStateException, ArrayIndexOutOfBoundsException
  {
    super(contestName, candidates[neb.winner], candidates[neb.loser], margin, universeSize,
        difficulty, new ArrayList<>());
  }

}
