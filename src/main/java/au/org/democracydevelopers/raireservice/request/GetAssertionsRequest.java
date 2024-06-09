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

package au.org.democracydevelopers.raireservice.request;

import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import java.beans.ConstructorProperties;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request (expected to be json) identifying the contest for which assertions should be retrieved
 * from the database (expected to be exported as json).
 * This extends ContestRequest and uses the contest name, totalAuditable ballots and candidate list,
 * plus validations, from there. The time limit is set to a default and ignored.
 * A GetAssertionsRequest identifies a contest by name along with the candidate list
 * (which is necessary for producing the metadata for later visualization). riskLimit states the
 * risk limit for the audit. This is not actually used in raire-service computations,
 * but will be output later with the assertion export, so that it can be used in the assertion
 * visualizer. Validation consists only of checking that the request is reasonable, including calling
 * ContestRequest.Validate to check that the contest exists and is all IRV, and that the candidate
 * names are reasonable. GetAssertionsRequest.Validate then checks that the risk limit is non-negative.
 */
public class GetAssertionsRequest extends ContestRequest {

  private final static Logger logger = LoggerFactory.getLogger(GetAssertionsRequest.class);

  /**
   * Default time limit, in seconds. Currently ignored.
   */
  private final static double DEFAULT_TIME_LIMIT = 5;

  /**
   * The winner, as stated by the request. This is written into response metadata
   * _without_ being checked.
   */
  public final String winner;

  /**
   * The risk limit for the audit, expected to be in the range [0,1]. Defaults to zero, because
   * then we know we will never mistakenly claim the risk limit has been met.
   */
  public final BigDecimal riskLimit;

  /**
   * All args constructor.
   * @param contestName the name of the contest
   * @param candidates a list of candidates by name
   * @param riskLimit the risk limit for the audit, expected to be in the range [0,1].
   */
  @ConstructorProperties({"contestName", "totalAuditableBallots", "candidates", "winner", "riskLimit"})
  public GetAssertionsRequest(String contestName, int totalAuditableBallots, List<String> candidates,
      String winner, BigDecimal riskLimit) {

    super(contestName, totalAuditableBallots, DEFAULT_TIME_LIMIT, candidates);
    this.winner = winner;
    this.riskLimit = riskLimit;
  }

  /**
   * Validates the request to retrieve assertions for the contest, checking that the contest exists
   * and is an IRV contest, that the risk limit has a sensible value, and that there are candidates.
   * Note it does _not_ check whether the candidates are present in the CVRs.
   * @param contestRepository the repository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    final String prefix = "[Validate]";
    logger.debug(String.format("%s Validating a Get Assertions Request for contest %s " +
            "with specified candidates %s and risk limit %s.", prefix, contestName, candidates,
            riskLimit));
    super.Validate(contestRepository);

    // Check for a negative risk limit. Risk limits >1 are vacuous but not illegal.
    // Risk limits of exactly zero are unattainable but will not cause a problem.
    if (riskLimit == null || riskLimit.compareTo(BigDecimal.ZERO) < 0) {
      final String msg = String.format("%s Null or negative risk limit specified in request (%s). "
          + "Throwing a RequestValidationException.", prefix, riskLimit);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    // Check for null winner.
    if (winner == null) {
      final String msg = String.format("%s Null or absent winner specified in request. "
          + "Throwing a RequestValidationException.", prefix);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    logger.debug(String.format("%s Get Assertions Request validated.", prefix));
  }
}

