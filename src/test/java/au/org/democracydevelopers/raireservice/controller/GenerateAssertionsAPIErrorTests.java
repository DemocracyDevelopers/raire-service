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

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.WRONG_CANDIDATE_NAMES;
import static au.org.democracydevelopers.raireservice.testUtils.aliceAndBob;
import static au.org.democracydevelopers.raireservice.testUtils.ballinaMayoral;
import static au.org.democracydevelopers.raireservice.testUtils.baseURL;
import static au.org.democracydevelopers.raireservice.testUtils.generateAssertionsEndpoint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.request.ContestRequest;
import au.org.democracydevelopers.raireservice.testUtils;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for appropriate responses to bad requests to the generate-assertions endpoint. This class
 * automatically fires up the RAIRE Microservice on a random port. These sorts of errors are _not_
 * supposed to happen - they indicate programming errors or problems with databases etc.
 * Currently, we check for proper input validation and inconsistent input.
 * The list of tests is similar to GenerateAssertionsRequestTests.java, and also to
 * GetAssertionsAPIErrorTests.java when the same test is relevant to both endpoints.
 * Contests which will be used for validity testing are
 * preloaded into the database using src/test/resources/data.sql.
 * Tests include:
 * - null, missing or whitespace contest name,
 * - non-IRV contests, mixed IRV-plurality contests or contests not in the database,
 * - null, missing or whitespace candidate names,
 * - candidate names that are valid but do not include all the candidates mentioned in votes in the
 *   database,
 * - missing, negative or zero values for numerical inputs (totalAuditableBallots and
 *   timeLimitSeconds).
 */
@ActiveProfiles("test-containers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsAPIErrorTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsAPIErrorTests.class);

  private final static HttpHeaders httpHeaders = new HttpHeaders();

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeAll
  public static void before() {
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
  }

  /**
   * This is really just a test that the testing is working.
   * There's no mapping for the plain localhost response, so when the microservice is running it
   * just returns a default error. We check for 404.
   */
  @Test
  public void testErrorForNonFunctioningEndpoint() {
    testUtils.log(logger, "testErrorForNonFunctioningEndpoint");
    ResponseEntity<String> response = restTemplate.postForEntity(baseURL + port + "/",
        new HttpEntity<>("", httpHeaders), String.class);
    assertTrue(response.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404)));
  }

  /**
   * Calling the generateAssertions endpoint with no header and no data produces an error.
   */
  @Test
  public void generateAssertionsNoBodyError() {
    testUtils.log(logger, "generateAssertionsNoBodyError");
    ResponseEntity<String> response = restTemplate.postForEntity(baseURL + port +
        generateAssertionsEndpoint, new HttpEntity<>("", new HttpHeaders()), String.class);
    assertTrue(response.getStatusCode().is4xxClientError());
  }

  /**
   * The generateAssertions endpoint, with correct headers but no data, should produce "Bad Request".
   */
  @Test
  public void testGenerateAssertionsBadRequest() {
    testUtils.log(logger, "testGenerateAssertionsBadRequest");
    String url = baseURL + port + generateAssertionsEndpoint;

    HttpEntity<String> request = new HttpEntity<>("", httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad request"));
  }

  /**
   * The generateAssertions endpoint, called with a valid IRV contest for which no votes are present,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsFromNoVotesIsAnError() {
    testUtils.log(logger, "generateAssertionsFromNoVotesIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    ContestRequest request = new ContestRequest("No CVR Mayoral", 100,
        10, aliceAndBob);

    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No votes present for contest"));
  }

  /**
   * @param contestName the name of the contest.
   * @param totalBallots the total auditable ballots.
   * @param timeLimit the time limit in seconds.
   * @param candidateList the list of candidate names.
   * @param errorMsg the expected error message.
   */
  @ParameterizedTest
  @MethodSource("expectedBadRequestErrors")
  public void testExpectedErrors(String contestName, int totalBallots, double timeLimit,
      List<String> candidateList, String errorMsg) {
    testUtils.log(logger, "testExpectedErrors");
    String url = baseURL + port + generateAssertionsEndpoint;

    ContestRequest request = new ContestRequest(contestName, totalBallots, timeLimit, candidateList);

    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), errorMsg));
  }

  /**
   * The actual data for the testExpectedErrors function.
   * @return the data to be tested.
   */
  private static Stream<Arguments> expectedBadRequestErrors() {
    final int ballotCount = 100;
    final int timeLimit = 10;
    return Stream.of(
        // generateAssertions, called with a nonexistent contest.
        Arguments.of("NonExistentContest", ballotCount, timeLimit, aliceAndBob,
            "No such contest"),
        // generateAssertions, called with a valid plurality contest.
        Arguments.of("Valid Plurality Contest", ballotCount, timeLimit, aliceAndBob,
            "Not comprised of all IRV"),
        // generateAssertions endpoint, called with a mixed IRV and non-IRV contest.
        Arguments.of("Invalid Mixed Contest", ballotCount, timeLimit, aliceAndBob,
            "Not comprised of all IRV"),
        // generateAssertions endpoint, called with a null contest name.
        Arguments.of(null, ballotCount, timeLimit, aliceAndBob, "No contest name"),
        // generateAssertions endpoint, called with an empty contest name.
        Arguments.of("", ballotCount, timeLimit, aliceAndBob, "No contest name"),
        // generateAssertions endpoint, called with an all-whitespace contest name.
        Arguments.of("     ", ballotCount, timeLimit, aliceAndBob, "No contest name"),
        // generateAssertions endpoint, called with a null candidate list.
        Arguments.of(ballinaMayoral, ballotCount, timeLimit, null,
            "Bad candidate list"),
        // generateAssertions endpoint, called with an empty candidate list.
        Arguments.of(ballinaMayoral, ballotCount, timeLimit, List.of(),
            "Bad candidate list"),
        // generateAssertions endpoint, called with a whitespace candidate name.
        Arguments.of(ballinaMayoral, ballotCount, timeLimit, List.of("Alice", "  "),
            "Bad candidate list"),
        // generateAssertions endpoint, called with zero total auditable ballots.
        Arguments.of(ballinaMayoral, 0, timeLimit, aliceAndBob,
            "Non-positive total auditable ballots"),
        // generateAssertions endpoint, called with negative total auditable ballots.
        Arguments.of(ballinaMayoral, -10, timeLimit, aliceAndBob,
            "Non-positive total auditable ballots"),
        // generateAssertions endpoint, called with zero time limit.
        Arguments.of(ballinaMayoral, ballotCount, 0, aliceAndBob,
            "Non-positive time limit"),
        // generateAssertions endpoint, called with negative time limit.
        Arguments.of(ballinaMayoral, ballotCount, -4.0, aliceAndBob,
            "Non-positive time limit")
    );
  }

  /**
   * The generateAssertions endpoint, called with a missing contest name, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithMissingContestNameIsAnError() {
    testUtils.log(logger, "generateAssertionsWithMissingContestNameIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":100,"
            +"\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The generateAssertions endpoint, called with a missing candidate list, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithMissingCandidateListIsAnError() {
    testUtils.log(logger, "generateAssertionsWithMissingCandidateListIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":5.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The generateAssertions endpoint, called with missing total auditable ballots, returns a
   * meaningful error.
   */
  @Test
  public void generateAssertionsWithNullAuditableBallotsIsAnError() {
    testUtils.log(logger, "generateAssertionsWithNullAuditableBallotsIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"contestName\":\"Ballina Mayoral\","
            +"\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "Non-positive total auditable ballots"));
  }

  /**
   * The generateAssertions endpoint, called with missing time limit, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNullTimeLimitIsAnError() {
    testUtils.log(logger, "generateAssertionsWithNullTimeLimitIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"totalAuditableBallots\":100,\"contestName\":\"Ballina Mayoral\","
            +"\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),"Non-positive time limit"));
  }

  /**
   * A GenerateAssertions request with a candidate list that is valid, but the votes in the database
   * contain at least one candidate who is not in the expected candidate list. This is an error.
   */
  @Test
  public void wrongCandidatesIsAnError() {
    testUtils.log(logger, "wrongCandidatesIsAnError");
    String url = "http://localhost:" + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"Ballina One Vote Contest\",\"candidates\":[\"Alice\",\"Bob\",\"Chuan\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(WRONG_CANDIDATE_NAMES.toString(),
        response.getHeaders().getFirst("error_code"));
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "was not on the list of candidates"));
  }

}