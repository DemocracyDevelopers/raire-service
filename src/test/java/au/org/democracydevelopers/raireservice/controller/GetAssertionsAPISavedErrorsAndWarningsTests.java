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

package au.org.democracydevelopers.raireservice.controller;

import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.testUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.ERROR_CODE_KEY;
import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.NO_ASSERTIONS_PRESENT;
import static au.org.democracydevelopers.raireservice.testUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for get-assertions endpoint. This class automatically fires up the RAIRE Microservice on a
 * random port, then runs a series tests that simulate the situation after a failed attempt (or no
 * attempt) to generate assertions for the given contest.
 * Each test runs on both the json and csv get-assertions endpoints.
 * Tests include all possible errors and warnings that can be produced by assertion generation, plus
 * missing summaries and missing assertions.
 * Contests which will be used for validity testing are preloaded into the database using
 * src/test/resources/data.sql.
 */
@ActiveProfiles("summaries-generation-errors")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsAPISavedErrorsAndWarningsTests {

  private static final Logger logger
      = LoggerFactory.getLogger(GetAssertionsAPISavedErrorsAndWarningsTests.class);

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  /**
   * Test a variety of requests to ensure they elicit the appropriate errors, which are saved in
   * the database.
   *
   * @param type          which endpoint to use: JSON or CSV.
   * @param riskLimit     risk limit as a string.
   * @param contestName   the name of the contest.
   * @param totalBallots  the total ballots in the universe.
   * @param candidateList the list of candidate names.
   * @param errorMsg      the expected error message.
   */
  @ParameterizedTest
  @MethodSource("expectedErrorSummaries")
  public void testExpectedErrors(String type, BigDecimal riskLimit, String contestName,
                                 int totalBallots, List<String> candidateList, RaireErrorCode errorCode, String errorMsg) {
    testUtils.log(logger, "testExpectedErrors");
    String url = baseURL + port
        + (type.equals("JSON") ? getAssertionsJSONEndpoint : getAssertionsCSVEndpoint);

    GetAssertionsRequest request = new GetAssertionsRequest(contestName, totalBallots,
        candidateList, riskLimit);

    ResponseEntity<String> response = restTemplate.postForEntity(url, request,
        String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(errorCode.toString(),
        Objects.requireNonNull(response.getHeaders().get(ERROR_CODE_KEY)).getFirst());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), errorMsg));
  }

  private static Stream<Arguments> expectedErrorSummaries() {
    return Stream.of(
        Arguments.of("JSON", defaultRiskLimit, "Summary but no assertions Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "No assertions have been generated"),
        Arguments.of("CSV", defaultRiskLimit, "Summary but no assertions Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "No assertions have been generated"),
        Arguments.of("JSON", defaultRiskLimit, "No summary and no assertions Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "No assertion generation summary"),
        Arguments.of("CSV", defaultRiskLimit, "No summary and no assertions Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "No assertion generation summary"),
        Arguments.of("JSON", defaultRiskLimit, "No summary but some assertions Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "No assertion generation summary"),
        Arguments.of("CSV", defaultRiskLimit, "No summary but some assertions Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "No assertion generation summary"),
        // TODO think about the right way of dealing with, and testing, the timeout warning.
        // See Issue https://github.com/orgs/DemocracyDevelopers/projects/1?pane=issue&itemId=63398709
        Arguments.of("JSON", defaultRiskLimit, "Timeout trimming assertions Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, ""),
        Arguments.of("CSV", defaultRiskLimit, "Timeout trimming assertions Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, ""),
        // Tied winners
        Arguments.of("JSON", defaultRiskLimit, "Tied winners Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "Tied winners: Alice, Bob"),
        Arguments.of("CSV", defaultRiskLimit, "Tied winners Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "Tied winners: Alice, Bob"),
        // Time out generating assertions.
        Arguments.of("JSON", defaultRiskLimit, "Tied winners Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "Alice, Bob"),
        Arguments.of("CSV", defaultRiskLimit, "Tied winners Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "Alice, Bob"),
        // Time out finding assertions.
        Arguments.of("JSON", defaultRiskLimit, "Time out finding Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "Time out finding assertions"),
        Arguments.of("CSV", defaultRiskLimit, "Time out finding Contest", defaultCount,
            aliceAndBob, NO_ASSERTIONS_PRESENT, "Time out finding assertions")
        // The other errors are equivalent, and are not all specifically tested here.
    );
  }
}
