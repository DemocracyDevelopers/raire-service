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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request (expected to be json) identifying the contest for which assertions should be generated.
 * This extends ContestRequest and uses the contest name and candidate list, plus validations,
 * from there. A GenerateAssertionsRequest identifies a contest by name along with the candidate list
 * (which is necessary for producing the metadata for later visualization).
 * TotalAuditableBallots states the total number of ballots in the universe, which may _not_ be the
 * same as the number of CVRs that mention the contest.
 * TimeLimitSeconds is a limit on the elapsed time that RAIRE has to do assertion generation.
 * Validation consists only of checking that the request is reasonable, including calling
 * ContestRequest.Validate to check that the contest exists and is all IRV, and that the candidate
 * names are reasonable. GenerateAssertionsRequest.Validate then checks that the two numbers,
 * totalAuditableBallots and timeLimitSeconds are positive.
 */
public class GenerateAssertionsRequest extends ContestRequest {

  private final static Logger logger = LoggerFactory.getLogger(GenerateAssertionsRequest.class);

  /**
   * The total number of ballots in the universe under audit.
   * This may not be the same as the number of ballots or CVRs in the contest, if the contest
   * is available only to a subset of voters in the universe.
   */
  public final int totalAuditableBallots;

  /**
   * The elapsed time allowed to raire to generate the assertions, in seconds.
   */
  public final double timeLimitSeconds;

  /**
   * All args constructor.
   * @param contestName the name of the contest
   * @param totalAuditableBallots the total auditable ballots in the universe under audit.
   * @param timeLimitSeconds the elapsed time allowed for RAIRE to generate assertions, in seconds.
   * @param candidates the list of candidates by name
   */
  @ConstructorProperties({"contestName", "totalAuditableBallots", "timeLimitSeconds","candidates"})
  public GenerateAssertionsRequest(String contestName, int totalAuditableBallots,
      double timeLimitSeconds, List<String> candidates) {
    super(contestName, candidates);
    this.totalAuditableBallots = totalAuditableBallots;
    this.timeLimitSeconds = timeLimitSeconds;
  }

  /**
   * Validates the generate assertions request, checking that the contest exists and is an
   * IRV contest, that the total ballots and time limit have sensible values, and that
   * the contest has candidates. Note it does _not_ check whether the candidates are present in
   * the CVRs.
   * @param contestRepository the repository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    final String prefix = "[Validate]";
    logger.debug(String.format("%s Validating a Generate Assertions Request for contest %s " +
        "with specified candidates %s, total number of auditable ballots %d, and time limit " +
        "on assertion generation of %fs.", prefix, contestName, candidates, totalAuditableBallots,
        timeLimitSeconds));

    super.Validate(contestRepository);

    if(totalAuditableBallots <= 0) {
      final String msg = String.format("%s Non-positive total auditable ballots (%d). Throwing a " +
          "RequestValidationException.", prefix, totalAuditableBallots);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }
    if(timeLimitSeconds <= 0) {
      final String msg = String.format("%s Non-positive time limit on assertion generation (%f). " +
          "Throwing a RequestValidationException.", prefix, timeLimitSeconds);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    logger.debug(String.format("%s Generate Assertions Request validated.", prefix));
  }
}