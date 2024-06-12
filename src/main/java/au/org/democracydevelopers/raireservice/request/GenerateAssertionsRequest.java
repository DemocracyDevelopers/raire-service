/*
Democracy Developers IRV extensions to colorado-rla.

@copyright 2024 Colorado Department of State

These IRV extensions are designed to connect to a running instance of the raire 
service (https://github.com/DemocracyDevelopers/raire-service), in order to 
generate assertions that can be audited using colorado-rla.

The colorado-rla IRV extensions are free software: you can redistribute it and/or modify it under the terms
of the GNU Affero General Public License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

The colorado-rla IRV extensions are distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
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
 * Request (expected to be json) derived from ContestRequest, identifying a contest by name and
 * listing other data:
 * - the candidates (by name),
 * - the total auditable ballots in the universe (used to calculate difficulty in raire),
 * - the time limit allowed to raire.
 * This is used for requesting assertion generation.
 * The only significant method is a verification method for checking that the data items are
 * present and have reasonable values.
 */
public class GenerateAssertionsRequest extends ContestRequest {

  /**
   * The elapsed time allowed to raire to generate the assertions, in seconds.
   */
  public final double timeLimitSeconds;

  /**
   * Class-wide logger.
   */
  private final static Logger logger = LoggerFactory.getLogger(ContestRequest.class);

  /**
   * All args constructor.
   * @param contestName the name of the contest
   * @param totalAuditableBallots the total auditable ballots in the universe under audit.
   * @param timeLimitSeconds the elapsed time allowed for RAIRE to generate assertions, in seconds.
   * @param candidates the list of candidates by name
   */
  @ConstructorProperties({"contestName", "totalAuditableBallots", "timeLimitSeconds","candidates"})
  public GenerateAssertionsRequest(String contestName, int totalAuditableBallots, double timeLimitSeconds,
      List<String> candidates) {
    super(contestName, totalAuditableBallots, candidates);
    this.timeLimitSeconds = timeLimitSeconds;
  }

  /**
   * Validates the GenerateAssertionsRequest,
   * super::Validate() checks that the contest exists and is an IRV contest, that
   * the total ballots has a sensible value, and that the contest has candidates.
   * Note it does _not_ check whether the candidates are present in the CVRs.
   * This function adds a test that the timeLimitSeconds has a sensible value.
   * @param contestRepository the respository for getting Contest objects from the database.
   * @throws RequestValidationException if the request is invalid.
   */
  public void Validate(ContestRepository contestRepository) throws RequestValidationException {
    final String prefix = "[Validate]";
    logger.debug(String.format("%s Validating a GenerateAssertionsRequest for contest %s " +
        "with time limit %f seconds.", prefix, contestName, timeLimitSeconds));

    super.Validate(contestRepository);

    if (timeLimitSeconds <= 0) {
      final String msg = String.format("%s Non-positive time limit on assertion generation (%f). " +
          "Throwing a RequestValidationException.", prefix, timeLimitSeconds);
      logger.error(msg);
      throw new RequestValidationException(msg);
    }

    logger.debug(String.format("%s Generate Assertions Request validated.", prefix));
  }
}
