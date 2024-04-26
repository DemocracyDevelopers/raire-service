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

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   * @throws IllegalArgumentException if the caller supplies a non-positive universe size, invalid
   * margin, or invalid combination of winner, loser and list of assumed continuing candidates.
   * @throws ArrayIndexOutOfBoundsException if the winner or loser indices in the raire-java
   * assertion are invalid with respect to the given array of candidates.
   */
  public NENAssertion(String contestName, long universeSize, int margin, double difficulty,
      String[] candidates, au.org.democracydevelopers.raire.assertions.NotEliminatedNext nen)
      throws IllegalArgumentException, ArrayIndexOutOfBoundsException
  {
    super(contestName, candidates[nen.winner], candidates[nen.loser], margin, universeSize,
        difficulty, Arrays.stream(nen.continuing).mapToObj(i -> candidates[i]).toList());

    if(!assumedContinuing.contains(winner) || !assumedContinuing.contains(loser)){
      String msg = "The winner and loser of an assertion must also be continuing candidates.";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
  }

  /**
   * {@inheritDoc}
   */
  public AssertionAndDifficulty convert(List<String> candidates) throws IllegalArgumentException {
    int w = candidates.indexOf(winner);
    int l = candidates.indexOf(loser);
    int[] continuing =  assumedContinuing.stream().mapToInt(candidates::indexOf).toArray();

    // Check for validity of the assertion with respect to the given list of candidate names
    if (w != -1 && l != -1 && Arrays.stream(continuing).noneMatch(c -> c == -1)) {
      Map<String,Object> status = new HashMap<>();
      status.put(STATUS_RISK, currentRisk);

      return new AssertionAndDifficulty(new NotEliminatedNext(w, l, continuing), difficulty,
          margin, status);
    }
    else{
      final String msg = "Candidate list is inconsistent with assertion.";
      logger.error("NENAssertion::convert " + msg);
      throw new IllegalArgumentException(msg);
    }
  }
}
