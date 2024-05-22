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

import static au.org.democracydevelopers.raireservice.testUtils.correctAssertionData;
import static au.org.democracydevelopers.raireservice.testUtils.correctMetadata;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.testUtils;
import au.org.democracydevelopers.raireservice.util.DoubleComparator;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

/**
 * Tests to validate the behaviour of the Assertion generation API on a collection of simple contest with
 * human-computable assertions. Relevant data is preloaded into the test database from
 * src/test/resources/known_testcases_votes.sql.
 * The test are the same as those in GenerateAssertionsServiceKnownTests.java. They include
 * - The examples from the Guide To Raire Vol 2. Exact matching for Ex. 2 and some for Ex. 1.
 * - A very simple example test with two obvious assertions (an NEN and NEB), described below.
 * - A cross-county version of the simple example.
 * - A request for the simple example with twice the totalAuditableBallots as ballots in the database,
 *   to test that the diluted margin and difficulties change by a factor of 2, but absolute margin
 *   stays the same
 * - A request for the simple example with fewer totalAuditableBallots than there are in the database,
 *   to check that there's an appropriate error response.
 * - A request for the simple example with the wrong candidate names, to check that there's an
 *   appropriate error response.
 * In each case, the test
 * - makes a request for assertion generation through the API,
 * - checks for the right winner,
 * - requests the assertion data through the get-assertions API (JSON),
 * - verifies the expected difficulty,
 * and checks for the expected data.
 */
@ActiveProfiles("known-testcases")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsAPIKnownTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsAPIKnownTests.class);
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
  private static final String oneNEBAssertionContest = "Sanity Check NEB Assertion Contest";
  private static final String oneNENAssertionContest = "Sanity Check NEN Assertion Contest";
  private static final String NEBNENAssertionContest = "Sanity Check NEB NEN Assertion Contest";
  private static final String ThreeAssertionContest = "Sanity Check 3 Assertion Contest";
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

  /**
   * Test the first example in the Guide to Raire Part 2.
   * The test data has 1/500 of the votes, so divide margins by 500.
   * The difficulties should be the same, because both numerator and denominator should be divided by 500.
   * We do not test the NEN assertions because the ones in the Guide have some redundancy.
   * Test assertion: Chuan NEB Bob.
   * Margin is 4000, but data is divided by 500, so 8. Difficulty is 3.375 as in the Guide.
   * Diluted margin is 8/27 = 0.296...
   */
  @Test
  @Transactional
  public void testGuideToRairePart2Example1() {
    testUtils.log(logger, "testGuideToRairePart2Example1");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;
    String getUrl = baseURL + port + getAssertionsEndpoint;

    GenerateAssertionsRequest request = new GenerateAssertionsRequest(guideToRaireExample1,
        27, DEFAULT_TIME_LIMIT, Arrays.stream(aliceBobChuanDiego).toList());


    // Request for the assertions to be generated.
    ResponseEntity<GenerateAssertionsResponse> response = restTemplate.postForEntity(generateUrl,
        request, GenerateAssertionsResponse.class);

    // Check that generation is successful and we got the right winner.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertEquals(response.getBody().winner(), "Chuan");

    // Request the assertions
    GetAssertionsRequest getRequest = new GetAssertionsRequest(guideToRaireExample1,
        Arrays.stream(aliceBobChuanDiego).toList(), DEFAULT_RISK_LIMIT);
    ResponseEntity<RaireSolution> getResponse = restTemplate.postForEntity(getUrl, getRequest,
        RaireSolution.class);

    // Check for the right metadata
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(getResponse.getBody());
    assertTrue(correctMetadata(Arrays.stream(aliceBobChuanDiego).toList(), guideToRaireExample1,
        DEFAULT_RISK_LIMIT.doubleValue(), getResponse.getBody().metadata));

    // There should be one NEB assertion: Chaun NEB Bob
    List<AssertionAndDifficulty> assertions = Arrays.stream(getResponse.getBody().solution.Ok.assertions).toList();
    Optional<AssertionAndDifficulty> nebMaybeAssertion = assertions.stream()
        .filter(a -> a.assertion instanceof NotEliminatedBefore).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    assertTrue(correctAssertionData("NEB", 8, 27/8.0, 2,1, List.of(), 1.0,
        nebMaybeAssertion.get()));
  }

  // TODO Add Example 2 and the simple contests.
}