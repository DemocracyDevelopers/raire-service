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

import static au.org.democracydevelopers.raireservice.testUtils.ballinaMayoral;
import static au.org.democracydevelopers.raireservice.testUtils.defaultCount;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.testUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests on GetAssertionsRequests, particularly focusing on the validation step.
 * Contests which will be used to test the validity of the GenerateAssertionsRequest are
 * preloaded into the database using src/test/resources/data.sql.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GetAssertionsRequestTests {

  private static final Logger logger = LoggerFactory.getLogger(GetAssertionsRequestTests.class);

  @Autowired
  ContestRepository contestRepository;

  /**
   * A valid request for an IRV contest is valid.
   */
  @Test
  public void validRequestForIRVContestIsValid() {
    testUtils.log(logger, "validRequestForIRVContestIsValid");
    GetAssertionsRequest validRequest = new GetAssertionsRequest("Ballina Mayoral",
        defaultCount, List.of("Alice"), BigDecimal.valueOf(0.03));
    assertDoesNotThrow(() -> validRequest.Validate(contestRepository));
  }

  /**
   * Collection of tests for proper catching of invalid requests, including non-IRV contests and
   * various invalid input parameters.
   * @param contestName the name of the contest.
   * @param totalBallots the total number of ballots.
   * @param candidateList the list of candidate names.
   * @param riskLimit the risk limit.
   * @param errorMsg the expected error message.
   */
  @ParameterizedTest
  @MethodSource("expectedExceptionMessages")
  public void testExpectedErrors(String contestName, int totalBallots, List<String> candidateList,
      BigDecimal riskLimit, String errorMsg) {
    testUtils.log(logger, "testExpectedErrors");
    GetAssertionsRequest invalidRequest = new GetAssertionsRequest(contestName, totalBallots,
        candidateList, riskLimit);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> invalidRequest.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), errorMsg));
  }

  /**
   * The actual data for the testExpectedErrors function.
   * @return the data to be tested.
   */
  private static Stream<Arguments> expectedExceptionMessages() {
    BigDecimal defaultRiskLimit = BigDecimal.valueOf(0.03);
    List<String> alice = List.of("Alice");
    return Stream.of(
        // A request for a contest that doesn't exist is invalid.
        Arguments.of("NonExistentContest", defaultCount, alice,
            defaultRiskLimit, "No such contest"),
        // A request for a plurality contest is invalid, because we have assertions only for IRV.
        Arguments.of("Valid Plurality Contest", defaultCount, alice,
            defaultRiskLimit, "not comprised of all IRV"),
        // A request for mixed IRV-plurality contests is invalid.
        // Note that this is a data problem - contests should have a consistent description.
        Arguments.of("Invalid Mixed Contest", defaultCount, alice,
            defaultRiskLimit, "not comprised of all IRV"),
        // A request with null contest name is invalid.
        Arguments.of(null, defaultCount, alice,
            defaultRiskLimit, "No contest name"),
        // A request with empty contest name is invalid.
        Arguments.of("", defaultCount, alice,
            defaultRiskLimit, "No contest name"),
        // A request with all-whitespace contest name is invalid.
        Arguments.of("    ", defaultCount, alice,
            defaultRiskLimit, "No contest name"),
        // A request with null candidate list is invalid.
        Arguments.of("IRVContestName", defaultCount, null,
            defaultRiskLimit, "bad candidate list"),
        // A request with empty candidate list is invalid.
        Arguments.of("IRVContestName", defaultCount, List.of(),
            defaultRiskLimit, "bad candidate list"),
        // A request with an all-whitespace candidate name is invalid.
        Arguments.of("IRVContestName", defaultCount, List.of("Alice","    "),
            defaultRiskLimit, "bad candidate list"),
        // A request with a null risk limit is invalid.
        Arguments.of(ballinaMayoral, defaultCount, alice,
            null, "risk limit"),
        // A request with a negative risk limit is invalid.
        Arguments.of(ballinaMayoral, defaultCount, alice,
            BigDecimal.valueOf(-0.03), "risk limit"),
        // A request with a negative totalAuditableBallots is invalid.
        // (Note that a request with zero auditable ballots is strange but valid.)
        Arguments.of(ballinaMayoral, -10, alice,
            defaultRiskLimit, "Non-positive total auditable ballots")
    );
  }
}
