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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.testUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
 * automatically fires up the RAIRE Microservice on a random port. Currently, we check for
 * proper input validation.
 * The list of tests is similar to GenerateAssertionsRequestTests.java, and also to
 * GetAssertionsAPITests.java when the same test is relevant to both endpoints.
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
  private final static String baseURL = "http://localhost:";
  private final static String generateAssertionsEndpoint = "/raire/generate-assertions";

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
   * The generateAssertions endpoint, called with a nonexistent contest, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNonExistentContestIsAnError() {
    testUtils.log(logger, "generateAssertionsWithNonExistentContestIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"NonExistentContest\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No such contest"));
  }

  /**
   * The generateAssertions endpoint, called with a valid plurality contest,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithPluralityContestIsAnError() {
    testUtils.log(logger, "generateAssertionsWithPluralityContestIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"Valid Plurality Contest\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Not comprised of all IRV"));
  }

  /**
   * The generateAssertions endpoint, called with a mixed IRV and non-IRV contest,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithMixedIRVPluralityContestIsAnError() {
    testUtils.log(logger, "generateAssertionsWithMixedIRVPluralityContestIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"Invalid Mixed Contest\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Not comprised of all IRV"));
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
   * The generateAssertions endpoint, called with a null contest name, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNullContestNameIsAnError() {
    testUtils.log(logger, "generateAssertionsWithNullContestNameIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":null,\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The generateAssertions endpoint, called with an empty contest name, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithEmptyContestNameIsAnError() {
    testUtils.log(logger, "generateAssertionsWithEmptyContestNameIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The generateAssertions endpoint, called with an all-whitespace contest name,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithWhitespaceContestNameIsAnError() {
    testUtils.log(logger, "generateAssertionsWithWhitespaceContestNameIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"     \",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The generateAssertions endpoint, called with a null candidate list, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNullCandidateListIsAnError() {
    testUtils.log(logger, "generateAssertionsWithNullCandidateListIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":5.0,\"totalAuditableBallots\":100,"
            +"\"candidates\":null,\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
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
   * The generateAssertions endpoint, called with an empty candidate list, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithEmptyCandidateListIsAnError() {
    testUtils.log(logger, "generateAssertionsWithEmptyCandidateListIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":5.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"Ballina Mayoral\",\"candidates\":[]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The generateAssertions endpoint, called with a whitespace candidate name,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithWhiteSpaceCandidateNameIsAnError() {
    testUtils.log(logger, "generateAssertionsWithWhiteSpaceCandidateNameIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":5.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"Ballina Mayoral\",\"candidates\":[\"Alice\",\"     \"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The generateAssertions endpoint, called with null/missing total auditable ballots, returns a
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
   * The generateAssertions endpoint, called with zero total auditable ballots,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithZeroAuditableBallotsIsAnError() {
    testUtils.log(logger, "generateAssertionsWithZeroAuditableBallotsIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":0,"
            +"\"contestName\":\"Ballina Mayoral\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "Non-positive total auditable ballots"));
  }

  /**
   * The generateAssertions endpoint, called with negative total auditable ballots,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNegativeAuditableBallotsIsAnError() {
    testUtils.log(logger, "generateAssertionsWithNegativeAuditableBallotsIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":-10,"
            +"\"contestName\":\"Ballina Mayoral\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "Non-positive total auditable ballots"));
  }



  /**
   * The generateAssertions endpoint, called with null/missing time limit, returns a meaningful error.
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
   * The generateAssertions endpoint, called with zero time limit, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithZeroTimeLimitIsAnError() {
    testUtils.log(logger, "generateAssertionsWithZeroTimeLimitIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":0.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"Ballina Mayoral\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),"Non-positive time limit"));
  }

  /**
   * The generateAssertions endpoint, called with negative time limit, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNegativeTimeLimitIsAnError() {
    testUtils.log(logger, "generateAssertionsWithNegativeTimeLimitIsAnError");
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":-50.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"Ballina Mayoral\",\"candidates\":[\"Alice\",\"Bob\"]}";

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
    String url = "http://localhost:" +port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"totalAuditableBallots\":100,"
            +"\"contestName\":\"Ballina One Vote Contest\",\"candidates\":[\"Alice\",\"Bob\",\"Chuan\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(WRONG_CANDIDATE_NAMES.toString(),
        response.getHeaders().getFirst("error_code"));
  }

}