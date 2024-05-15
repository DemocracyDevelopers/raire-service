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
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raireservice.service.Metadata;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Not Eliminated Before assertion (or NEB) says that a candidate _winner_ will always have
 * a higher tally than a candidate _loser_. What this means is that the minimum possible tally
 * that _winner_ will have at any stage of tabulation is greater than the maximum possible
 * tally _loser_ can ever achieve. For more detail on NEB assertions, refer to the Guide to RAIRE.
 * The constructor for this class takes a raire-java NEB assertion construct (NotEliminatedBefore)
 * and translates it into a NEBAssertion entity, suitable for storage in the corla database.
 */
@Entity
@DiscriminatorValue("NEB")
public class NEBAssertion extends Assertion {

  private static final Logger logger = LoggerFactory.getLogger(NEBAssertion.class);

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
   * @throws IllegalArgumentException if the caller supplies a non-positive universe size, invalid
   * margin, or invalid combination of winner, loser and list of assumed continuing candidates.
   * @throws ArrayIndexOutOfBoundsException if the winner or loser indices in the raire-java
   * assertion are invalid with respect to the given array of candidates.
   */
  public NEBAssertion(String contestName, long universeSize, int margin, double difficulty,
      String[] candidates, au.org.democracydevelopers.raire.assertions.NotEliminatedBefore neb)
      throws IllegalStateException, ArrayIndexOutOfBoundsException
  {
    super(contestName, candidates[neb.winner], candidates[neb.loser], margin, universeSize,
        difficulty, new ArrayList<>());

    final String prefix = "[all args constructor]";
    logger.debug(String.format("%s Constructed NEB assertion with winner (%d) and loser (%d) " +
            "indices with respect to candidate list %s: %s. " +
            "Parameters: contest name %s; margin %d; universe size %d; and difficulty %f.", prefix,
            neb.winner, neb.loser, Arrays.toString(candidates), this.getDescription(),
            contestName, margin, universeSize, difficulty));
  }

  /**
   * {@inheritDoc}
   */
  public AssertionAndDifficulty convert(List<String> candidates) throws IllegalArgumentException {

    final String prefix = "[convert]";
    logger.debug(String.format("%s Constructing a raire-java AssertionAndDifficulty for the " +
        "assertion %s with candidate list parameter %s.", prefix, this.getDescription(), candidates));

    int w = candidates.indexOf(winner);
    int l = candidates.indexOf(loser);

    logger.debug(String.format("%s Winner index %d, Loser index %d.", prefix, w, l));
    if(w != -1 && l != -1) {
      Map<String,Object> status = new HashMap<>();
      status.put(Metadata.STATUS_RISK, currentRisk);

      logger.debug(String.format("%s Constructing AssertionAndDifficulty, current risk %f.",
          prefix, currentRisk));
      return new AssertionAndDifficulty(new NotEliminatedBefore(w, l), difficulty, margin, status);
    }
    else{
      final String msg = String.format("%s Candidate list provided as parameter is inconsistent " +
              "with assertion (winner or loser not present).", prefix);
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }
  }

  /**
   * Print the assertion type. Used for CSV file output.
   * @return The string "NEB"
   */
  @Override
  public String gettAssertionType() {
    return "NEB";
  }

  /**
   * {@inheritDoc}
   */
  public String getDescription(){
    return String.format("%s NEB %s with diluted margin %f", winner, loser, dilutedMargin);
  }
}
