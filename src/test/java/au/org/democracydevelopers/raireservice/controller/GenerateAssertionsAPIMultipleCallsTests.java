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
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.testUtils;
import org.apache.commons.lang3.StringUtils;
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

import static au.org.democracydevelopers.raireservice.NSWValues.BallotCount_12;
import static au.org.democracydevelopers.raireservice.NSWValues.choicesContest_12;
import static au.org.democracydevelopers.raireservice.controller.GenerateAssertionsAPIWickedTests.ByronMayoral;
import static au.org.democracydevelopers.raireservice.controller.GenerateAssertionsAPIWickedTests.ByronShortTimeoutRequest;
import static au.org.democracydevelopers.raireservice.service.GenerateAssertionsServiceMultipleCallsTests.ByronNormalTimeoutRequest;
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
  private static final BigDecimal DEFAULT_RISK_LIMIT = BigDecimal.valueOf(0.03);

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

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

  /**
  * Run assertion generation on Byron Mayoral, first with a too-short timeout, then again with a
  * workable timeout. Verify that the error state is returned first, then replaced with success.
  * This test doesn't do exhaustive search of the data - it just sanity-checks that first the
  * failure result with no assertions, then the success result with some assertions, are stored.
  */
  @Test
  @Transactional
  public void ByronTimeoutsSuccessReplacesFailure() {
    testUtils.log(logger, "ByronTimeoutsSuccessReplacesFailure");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;
    String getUrl = baseURL + port + getAssertionsEndpoint;

    // Request for the assertions to be generated, short timeout.
    ResponseEntity<GenerateAssertionsResponse> response = restTemplate.postForEntity(generateUrl,
        ByronShortTimeoutRequest, GenerateAssertionsResponse.class);

    // Check that the request is successful, but it tells us generation failed and we should retry.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().succeeded());
    assertTrue(response.getBody().retry());

    // Request the assertions
    ResponseEntity<String> getResponse
        = restTemplate.postForEntity(getUrl, ByronGetAssertionsRequest, String.class);

    // Check there are none.
    assertFalse(getResponse.getStatusCode().is2xxSuccessful());
    assertTrue(StringUtils.containsIgnoreCase(getResponse.getBody(), "No assertions"));

    // Repeat the request, with a normal timeout.
    response = restTemplate.postForEntity(generateUrl, ByronNormalTimeoutRequest, GenerateAssertionsResponse.class);

    // Check that generation is successful, with no retry.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().succeeded());
    assertFalse(response.getBody().retry());

    // Request the assertions again
    ResponseEntity<RaireSolution> getResponse2 = restTemplate.postForEntity(getUrl, ByronGetAssertionsRequest, RaireSolution.class);

    // Check for success, and that we have some assertions.
    assertTrue(getResponse2.getStatusCode().is2xxSuccessful());
    assertNotNull(getResponse2.getBody());
    assertTrue(getResponse2.getBody().solution.Ok.assertions.length > 0);
  }

  /**
   * The same as ByronTimeoutsSuccessReplacesFailure, but in the opposite order: success first,
   * to be replaced by failure.
   */
  @Test
  @Transactional
  public void ByronTimeoutsFailureReplacesSuccess() {
    testUtils.log(logger, "ByronTimeoutsFailureReplacesSuccess");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;
    String getUrl = baseURL + port + getAssertionsEndpoint;

    // Request for the assertions to be generated, normal timeout.
    ResponseEntity<GenerateAssertionsResponse> response = restTemplate.postForEntity(generateUrl,
        ByronNormalTimeoutRequest, GenerateAssertionsResponse.class);

    // Check that generation is successful, with no retry.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().succeeded());
    assertFalse(response.getBody().retry());

    // Request the assertions
    ResponseEntity<RaireSolution> getResponseFirst = restTemplate.postForEntity(getUrl, ByronGetAssertionsRequest, RaireSolution.class);

    // Check for success, and that we have some assertions.
    assertTrue(getResponseFirst.getStatusCode().is2xxSuccessful());
    assertNotNull(getResponseFirst.getBody());
    assertTrue(getResponseFirst.getBody().solution.Ok.assertions.length > 0);

    // Repeat the request, with a short timeout.
    response = restTemplate.postForEntity(generateUrl, ByronShortTimeoutRequest, GenerateAssertionsResponse.class);

    // Check that the request is successful, but it tells us generation failed and we should retry.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().succeeded());
    assertTrue(response.getBody().retry());

    // Request the assertions
    ResponseEntity<String> getResponse
        = restTemplate.postForEntity(getUrl, ByronGetAssertionsRequest, String.class);

    // Check there are none.
    assertFalse(getResponse.getStatusCode().is2xxSuccessful());
    assertTrue(StringUtils.containsIgnoreCase(getResponse.getBody(), "No assertions"));
  }
}
