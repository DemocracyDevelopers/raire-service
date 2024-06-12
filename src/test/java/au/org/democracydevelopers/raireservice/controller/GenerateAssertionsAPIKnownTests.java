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

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.ERROR_CODE_KEY;
import static au.org.democracydevelopers.raireservice.testUtils.correctAssertionData;
import static au.org.democracydevelopers.raireservice.testUtils.correctMetadata;
import static au.org.democracydevelopers.raireservice.testUtils.defaultWinner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raireservice.request.ContestRequest;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.testUtils;
import au.org.democracydevelopers.raireservice.util.DoubleComparator;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

/**
 * Tests to validate the behaviour of the Assertion generation API on a collection of simple contest with
 * human-computable assertions. Relevant data is preloaded into the test database from
 * src/test/resources/known_testcases_votes.sql.
 * The test are a subset of those in GenerateAssertionsServiceKnownTests.java. They include
 * - The examples from the Guide To Raire Vol 2. Exact matching for Ex. 2 and some for Ex. 1.
 * - A cross-county version of the simple example.
 * - A request for the simple example with twice the totalAuditableBallots as ballots in the database,
 *   to test that the diluted margin and difficulties change by a factor of 2, but absolute margin
 *   stays the same
 * - A request for the simple example with fewer totalAuditableBallots than there are in the database,
 *   to check that there's an appropriate error response.
 * Note for anyone comparing this directly with GenerateAssertionsServiceKnownTests: the
 * test for wrong candidate names is in GenerateAssertionsAPIErrorTests, along with various other
 * tests involving invalid request data.
 * TODO The GenerateAssertionsServiceTest contains tests of proper overwriting when assertion
 * generation is requested but assertions are already in the database. This is not yet complete in
 * this class, pending a decision about how to block assertion regeneration when appropriate.
 * See (<a href="https://github.com/DemocracyDevelopers/raire-service/issues/70">...</a>)
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

    ContestRequest request = new ContestRequest(guideToRaireExample1,
        27, DEFAULT_TIME_LIMIT, Arrays.stream(aliceBobChuanDiego).toList());


    // Request for the assertions to be generated.
    ResponseEntity<GenerateAssertionsResponse> response = restTemplate.postForEntity(generateUrl,
        request, GenerateAssertionsResponse.class);

    // Check that generation is successful and we got the right winner.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertEquals(response.getBody().winner(), "Chuan");

    // Request the assertions
    GetAssertionsRequest getRequest = new GetAssertionsRequest(guideToRaireExample1, 27,
        Arrays.stream(aliceBobChuanDiego).toList(), defaultWinner, DEFAULT_RISK_LIMIT);
    ResponseEntity<RaireSolution> getResponse = restTemplate.postForEntity(getUrl, getRequest,
        RaireSolution.class);

    // Check for the right metadata
    assertTrue(getResponse.getStatusCode().is2xxSuccessful());
    assertNotNull(getResponse.getBody());
    assertTrue(correctMetadata(Arrays.stream(aliceBobChuanDiego).toList(), guideToRaireExample1,
        DEFAULT_RISK_LIMIT, 27, getResponse.getBody().metadata, Double.class));

    // There should be one NEB assertion: Chaun NEB Bob
    List<AssertionAndDifficulty> assertions = Arrays.stream(getResponse.getBody().solution.Ok.assertions).toList();
    Optional<AssertionAndDifficulty> nebMaybeAssertion = assertions.stream()
        .filter(a -> a.assertion instanceof NotEliminatedBefore).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    assertTrue(correctAssertionData("NEB", 8, 27/8.0, 2,1, List.of(), 1.0,
        nebMaybeAssertion.get()));
  }

  /**
   * Exact matching of the assertions described in the Guide to Raire Example 2.
   * The test data has 1/1000 of the votes, so divide margins by 1000.
   * The difficulties should be the same, because both numerator and denominator should be divided by 1000.
   */
  @Test
  @Transactional
  void testGuideToRairePart2Example2() {
    testUtils.log(logger, "testGuideToRairePart2Example2");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;
    String getUrl = baseURL + port + getAssertionsEndpoint;
    ContestRequest request = new ContestRequest(guideToRaireExample2,
        41, 5, Arrays.stream(aliceChuanBob).toList());

    // Request for the assertions to be generated.
    ResponseEntity<GenerateAssertionsResponse> response
        = restTemplate.postForEntity(generateUrl, request, GenerateAssertionsResponse.class);

    // Check that the response is successful and we got the right winner.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertEquals(response.getBody().winner(), "Chuan");

    // Request the assertions
    GetAssertionsRequest getRequest = new GetAssertionsRequest(guideToRaireExample2, 41,
        Arrays.stream(aliceChuanBob).toList(), defaultWinner, DEFAULT_RISK_LIMIT);
    ResponseEntity<RaireSolution> getResponse = restTemplate.postForEntity(getUrl, getRequest,
        RaireSolution.class);

    // Check for the right metadata.
    assertTrue(getResponse.getStatusCode().is2xxSuccessful());
    assertNotNull(getResponse.getBody());
    assertTrue(correctMetadata(Arrays.stream(aliceChuanBob).toList(), guideToRaireExample2,
        DEFAULT_RISK_LIMIT, 41, getResponse.getBody().metadata, Double.class));

    // Check for the right results: two assertions, margin 9 and difficulty 4.6.
    RaireResult raireResult = getResponse.getBody().solution.Ok;
    AssertionAndDifficulty[] assertions = raireResult.assertions;
    assertEquals(9, raireResult.margin);
    assertEquals(0, doubleComparator.compare(41.0/9, raireResult.difficulty));
    checkGuideToRaireExample2Assertions(assertions);
  }

  /**
   * Simple contest. The votes are
   * 2 (A,B)
   * 2 (B)
   * 1 (C,A).
   * The assertions should be
   * A NEB C
   * - Margin 1, diluted margin 1/5 = 0.2, difficulty 5/1 = 5.
   * A NEN B | {A,B} continuing.
   * - Margin 1, diluted margin 1/5 = 0.2, difficulty 5/1 = 5.
   * Note that A NEB B is not true.
   * This is the single-county case.
   */
  @Test
  @Transactional
  public void simpleContestSingleCounty() {
    testUtils.log(logger, "simpleContestSingleCounty");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;
    String getUrl = baseURL + port + getAssertionsEndpoint;

    ContestRequest request = new ContestRequest(simpleContest,
        5, 5, Arrays.stream(aliceChuanBob).toList());

    // Request for the assertions to be generated.
    ResponseEntity<GenerateAssertionsResponse> response
        = restTemplate.postForEntity(generateUrl, request, GenerateAssertionsResponse.class);

    // Check that the response is successful and we got the right winner.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertEquals(response.getBody().winner(), "Alice");

    // Request the assertions
    GetAssertionsRequest getRequest = new GetAssertionsRequest(simpleContest, 147,
        Arrays.stream(aliceChuanBob).toList(), defaultWinner, DEFAULT_RISK_LIMIT);
    ResponseEntity<RaireSolution> getResponse = restTemplate.postForEntity(getUrl, getRequest,
        RaireSolution.class);

    // Check for the right metadata.
    // Note that this metadata - particularly the totalAuditable ballots, but also the candidate
    // name order - matches the GetAssertionsRequest (not the request for generation).
    assertTrue(getResponse.getStatusCode().is2xxSuccessful());
    assertNotNull(getResponse.getBody());
    assertTrue(correctMetadata(Arrays.stream(aliceChuanBob).toList(), simpleContest,
        DEFAULT_RISK_LIMIT, 147, getResponse.getBody().metadata, Double.class));

    // Check for the right results: two assertions, margin 9 and difficulty 4.6.
    RaireResult raireResult = getResponse.getBody().solution.Ok;
    AssertionAndDifficulty[] assertions = raireResult.assertions;
    assertEquals(1, raireResult.margin);
    assertEquals(0, doubleComparator.compare(5.0, raireResult.difficulty));
    checkSimpleContestAssertions(assertions, 1);
  }

  /**
   * The same simple contest, but across two counties. Nothing should change.
   */
  @Test
  @Transactional
  public void simpleContestCrossCounty() {
    testUtils.log(logger, "simpleContestCrossCounty");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;
    String getUrl = baseURL + port + getAssertionsEndpoint;

    ContestRequest request = new ContestRequest(crossCountySimpleContest,
        5, 5, Arrays.stream(aliceChuanBob).toList());

    // Request for the assertions to be generated.
    ResponseEntity<GenerateAssertionsResponse> response
        = restTemplate.postForEntity(generateUrl, request, GenerateAssertionsResponse.class);

    // Check that the response is successful and we got the right winner.
    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertNotNull(response.getBody());
    assertEquals(response.getBody().winner(), "Alice");

    // Request the assertions
    GetAssertionsRequest getRequest = new GetAssertionsRequest(crossCountySimpleContest, 5,
        Arrays.stream(aliceChuanBob).toList(), defaultWinner, DEFAULT_RISK_LIMIT);
    ResponseEntity<RaireSolution> getResponse = restTemplate.postForEntity(getUrl, getRequest,
        RaireSolution.class);

    // Check for the right metadata.
    assertTrue(getResponse.getStatusCode().is2xxSuccessful());
    assertNotNull(getResponse.getBody());
    assertTrue(correctMetadata(Arrays.stream(aliceChuanBob).toList(), crossCountySimpleContest,
        DEFAULT_RISK_LIMIT, 5, getResponse.getBody().metadata, Double.class));

    // Check for the right results: two assertions, margin 9 and difficulty 4.6.
    RaireResult raireResult = getResponse.getBody().solution.Ok;
    AssertionAndDifficulty[] assertions = raireResult.assertions;
    assertEquals(1, raireResult.margin);
    assertEquals(0, doubleComparator.compare(5.0, raireResult.difficulty));
    checkSimpleContestAssertions(assertions, 1);
  }

  /**
   * Single-county simple contest again.
   * Doubling the totalAuditableBallots to 10 doubles the difficulty, and halves the diluted margin,
   * but does not change the absolute margins.
   * The actual test data is still the same, with 5 ballots - we just set totalAuditableBallots in
   * the request to 10.
   * We now have 10 totalAuditableBallots, so we expect:
   * A NEB B: Margin 1, diluted margin 1/10 = 0.1, difficulty 10/1 = 10.
   * A NEN B | {A,B} continuing: Margin 1, diluted margin 1/10 = 0.1, difficulty 10/1 = 10.
   * Exactly the same as the simpleContest test above, but now we have 10 totalAuditableBallots
   * and a difficultyFactor=2 in the call to checkSimpleContestAssertions.
   */
  @Test
  @Transactional
  public void simpleContestSingleCountyDoubleBallots() {
    testUtils.log(logger, "simpleContestSingleCountyDoubleBallots");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;
    String getUrl = baseURL + port + getAssertionsEndpoint;

     // Tell raire that the totalAuditableBallots is double the number in the database
     // for this contest.
     ContestRequest request = new ContestRequest(simpleContest,
         10, 5, Arrays.stream(aliceChuanBob).toList());

     // Request for the assertions to be generated.
     ResponseEntity<GenerateAssertionsResponse> response
         = restTemplate.postForEntity(generateUrl, request, GenerateAssertionsResponse.class);

     // Check that the response is successful and we got the right winner.
     assertTrue(response.getStatusCode().is2xxSuccessful());
     assertNotNull(response.getBody());
     assertEquals(response.getBody().winner(), "Alice");

     // Request the assertions
     GetAssertionsRequest getRequest = new GetAssertionsRequest(simpleContest, 10,
         Arrays.stream(aliceChuanBob).toList(), defaultWinner, DEFAULT_RISK_LIMIT);
     ResponseEntity<RaireSolution> getResponse = restTemplate.postForEntity(getUrl, getRequest,
         RaireSolution.class);

     // Check for the right metadata.
     assertTrue(getResponse.getStatusCode().is2xxSuccessful());
     assertNotNull(getResponse.getBody());
     assertTrue(correctMetadata(Arrays.stream(aliceChuanBob).toList(), simpleContest,
         DEFAULT_RISK_LIMIT, 10, getResponse.getBody().metadata, Double.class));

     // Check for the right results: two assertions, margin 9 and difficulty 4.6.
     RaireResult raireResult = getResponse.getBody().solution.Ok;
     AssertionAndDifficulty[] assertions = raireResult.assertions;
     assertEquals(1, raireResult.margin);
     assertEquals(0, doubleComparator.compare(10.0, raireResult.difficulty));

     // Difficulty factor 2 to match the doubled totalAuditableBallots.
     checkSimpleContestAssertions(assertions, 2);
  }

  /**
   * Insufficient totalAuditableBallots causes the right raire error to be returned.
   * This test case has 5 ballots, so 2 totalAuditableBallots is an error.
   */
  @Test
  @Transactional
  public void simpleContestSingleCountyInsufficientBallotsError() {
    testUtils.log(logger, "simpleContestSingleCountyInsufficientBallotsError");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;

    ContestRequest notEnoughBallotsRequest = new ContestRequest(simpleContest,
        2, 5, Arrays.stream(aliceChuanBob).toList());

    // Request for the assertions to be generated.
    ResponseEntity<String> response
        = restTemplate.postForEntity(generateUrl, notEnoughBallotsRequest, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(RaireErrorCode.INVALID_TOTAL_AUDITABLE_BALLOTS.toString(),
        response.getHeaders().getFirst(ERROR_CODE_KEY));
  }

  /**
    * Check the data for the simple contests. Note that the winner and loser indices
    * are dependent on the order of the input candidate list, which in all the test
    * that use it are Alice, Chuan, Bob.
    * @param assertionAndDifficulties the assertions returned by API
    * @param difficultyFactor factor by which the difficulty should be multiplied (because of larger
    *                         universe size).
    */
  private void checkSimpleContestAssertions(AssertionAndDifficulty[] assertionAndDifficulties,
      double difficultyFactor) {
    assertEquals(2, assertionAndDifficulties.length);
    int nebIndex;

    if(assertionAndDifficulties[0].assertion instanceof NotEliminatedBefore) {
      nebIndex = 0;
    } else {
      nebIndex = 1;
    }

    // There should be one NEB assertion: Alice NEB Chuan
    // Margin 1, diluted margin 0.2, difficulty 5.
    assertTrue(correctAssertionData("NEB", 1, 5*difficultyFactor,
        0,1, List.of(), 1.0, assertionAndDifficulties[nebIndex]));

    // There should be one NEN assertion: Chuan > Bob if only {Chuan,Bob} remain.
    // Margin is 9,000, but data is divided by 1000, so 9. Difficulty is 41/9 = 4.5555...,
    // rounded to 4.6 in the Guide.
    // Diluted margin is 9/41 = 0.219512195...
    assertTrue(correctAssertionData("NEN", 1, 5*difficultyFactor,
        0, 2, List.of(0,2), 1.0, assertionAndDifficulties[1-nebIndex]));
  }

  /**
   * Checks for exact match with the Guide To Raire Part 2, example 2.
   * Same as GenerateAssertionsServiceKnownTests::checkGuideToRaireExample2Assertions, except that
   * it inputs an array of raire-java::AssertionAndDifficulty.
   */
  private void checkGuideToRaireExample2Assertions(AssertionAndDifficulty @NotNull [] assertionAndDifficulties) {
    assertEquals(2, assertionAndDifficulties.length);
    int nebIndex;

    if(assertionAndDifficulties[0].assertion instanceof NotEliminatedBefore) {
      nebIndex = 0;
    } else {
      nebIndex = 1;
    }

    // There should be one NEB assertion: Chaun NEB Alice
    // Margin is 10,000, but data is divided by 1000, so 10. Difficulty is 4.1 as in the Guide.
    // Diluted Margin is 10/41.
    assertTrue(correctAssertionData("NEB", 10, 4.1,
        1,0, List.of(), 1.0, assertionAndDifficulties[nebIndex]));

    // There should be one NEN assertion: Chuan > Bob if only {Chuan,Bob} remain.
    // Margin is 9,000, but data is divided by 1000, so 9. Difficulty is 41/9 = 4.5555...,
    // rounded to 4.6 in the Guide.
    // Diluted margin is 9/41 = 0.219512195...
    assertTrue(correctAssertionData("NEN", 9, 41.0/9,
        1, 2, List.of(1,2), 1.0, assertionAndDifficulties[1-nebIndex]));
  }
}