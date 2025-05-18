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

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.service.Metadata;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.testUtils;
import au.org.democracydevelopers.raireservice.util.DoubleComparator;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static au.org.democracydevelopers.raireservice.NSWValues.BallotCount_12;
import static au.org.democracydevelopers.raireservice.NSWValues.choicesContest_12;
import static au.org.democracydevelopers.raireservice.controller.GenerateAssertionsAPIWickedTests.ByronMayoral;
import static au.org.democracydevelopers.raireservice.service.GenerateAssertionsServiceMultipleCallsTests.ByronNormalTimeoutRequest;
import static au.org.democracydevelopers.raireservice.service.RaireServiceException.ERROR_CODE_KEY;
import static au.org.democracydevelopers.raireservice.testUtils.correctAssertionData;
import static au.org.democracydevelopers.raireservice.testUtils.correctMetadata;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to validate the behaviour of the Assertion generation API when called successively.
 * These are the same as the tests in GenerateAssertionsServiceMultipleCallsTests.java, but use
 * the API instead of a call to the service.
 * Relevant data is preloaded into the test database from
 * src/test/resources/known_testcases_votes.sql.
 * These tests include
 * 1. Calling the endpoint twice with the same contest name. The state at the end of the second call
 * should be the same as at the end of the first.
 * 2. Calling the endpoint first with an extremely small time limit that causes assertion generation
 * to fail, then calling it again with a longer time limit that succeeds. This should store
 * assertions and replace the summary.
 * 3. The same as (2), but in the opposite order.
 * In each case, the test
 * - makes a request for assertion generation through the API,
 * - checks for the right winner or the expected error,
 * - requests the assertion data through the get-assertions API (JSON),
 * - checks whether there are assertions for the contest.
 */
@ActiveProfiles("known-testcases")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsAPIMultipleCallsTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsAPIMultipleCallsTests.class);
  private final static String baseURL = "http://localhost:";
  private final static String generateAssertionsEndpoint = "/raire/generate-assertions";

  // Get assertions endpoint - used for testing that they were generated properly.
  private final static String getAssertionsEndpoint = "/raire/get-assertions-json";
  private static final int DEFAULT_TIME_LIMIT = 5;
  private static final BigDecimal DEFAULT_RISK_LIMIT = BigDecimal.valueOf(0.03);
  private static final DoubleComparator doubleComparator = new DoubleComparator();

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  /**
   * Names of contests, to match preloaded data.
   */
  private static final String guideToRaireExample1 = "Guide To Raire Example 1";
  private static final String guideToRaireExample2 = "Guide To Raire Example 2";
  private static final String simpleContest = "Simple Contest";
  private static final String crossCountySimpleContest = "Cross-county Simple Contest";

  /**
   * Array of candidates: Alice, Bob, Chuan, Diego.
   */
  private static final String[] aliceBobChuanDiego = {"Alice", "Bob", "Chuan", "Diego"};

  /**
   * Array of candidates: Alice, Chuan, Bob.
   */
  private static final String[] aliceChuanBob = {"Alice", "Chuan", "Bob"};

  private static final GetAssertionsRequest ByronGetAssertionsRequest
      = new GetAssertionsRequest(ByronMayoral, BallotCount_12, choicesContest_12, DEFAULT_RISK_LIMIT);

  /**
   * Run Byron Mayoral assertion generation twice; sanity-check results.
   */
  @Test
  @Transactional
  public void ByronRunsTwiceSameResults() {
    testUtils.log(logger, "ByronRunsTwiceSameResults");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;
    String getUrl = baseURL + port + getAssertionsEndpoint;

    // Request for the assertions to be generated.
    ResponseEntity<GenerateAssertionsResponse> response = restTemplate.postForEntity(generateUrl,
        ByronNormalTimeoutRequest, GenerateAssertionsResponse.class);

    // Check that generation is successful, with no retry.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().succeeded());
    assertFalse(response.getBody().retry());

    // Request the assertions
    ResponseEntity<RaireSolution> getResponse
        = restTemplate.postForEntity(getUrl, ByronGetAssertionsRequest, RaireSolution.class);

    // Check for success
    assertTrue(getResponse.getStatusCode().is2xxSuccessful());
    assertNotNull(getResponse.getBody());
    int firstAssertionCount = getResponse.getBody().solution.Ok.assertions.length;

    // Repeat the request
    restTemplate.postForEntity(generateUrl, ByronNormalTimeoutRequest, GenerateAssertionsResponse.class);

    // Check that generation is successful, with no retry.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().succeeded());
    assertFalse(response.getBody().retry());

    // Request the assertions again
    getResponse = restTemplate.postForEntity(getUrl, ByronGetAssertionsRequest, RaireSolution.class);

    // Check for success, and that we have the same number of assertions.
    assertTrue(getResponse.getStatusCode().is2xxSuccessful());
    assertNotNull(getResponse.getBody());
    assertEquals(firstAssertionCount, getResponse.getBody().solution.Ok.assertions.length);
  }
}
