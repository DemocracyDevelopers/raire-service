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

import static au.org.democracydevelopers.raireservice.util.StringUtils.containsIgnoreCase;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import com.google.gson.Gson;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.context.ActiveProfiles;


/**
 * Tests for generate-assertions endpoint. This class automatically fires up the RAIRE Microservice on a
 * random port, then runs a series of (at this stage) very basic tests. Currently we check for proper
 * input validation, and check that one valid trivial request succeeds.
 * The list of tests is similar to GenerateAssertionsRequestTests.java, and also to GetAssertionsAPITests.java
 * when the same test is relevant to both endpoints.
 * Note that you have to run the *whole class*. Individual tests do not work separately because they don't
 * initiate the microservice on their own.
 * Contests which will be used for validity testing are pre-loaded into the database using
 * src/test/resources/data.sql.
 */

@ActiveProfiles("test-containers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GenerateAssertionsAPITests {

  private final Gson gson = new Gson();
  private final static HttpHeaders httpHeaders = new HttpHeaders();

  private final static String baseURL = "http://localhost:";
  private final static String generateAssertionsEndpoint = "/raire/generate-assertions";
  private final static String ballina = "Ballina Mayoral";
  private final static String invalidMixed = "Invalid Mixed Contest";
  private final static String validPlurality = "Valid Plurality Contest";

  @LocalServerPort
  private int port;



  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeAll
  public static void before() {
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
  }

  @Test
  void contextLoads() {
  }

  /**
   * A trivial example of a valid generate assertions request. Simply tests that it returns an HTTP
   * success status.
   */
  @Test
  public void testTrivialGenerateAssertionsExample() {
    String url = "http://localhost:" +port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        ballina,
        100,
        5,
        List.of("Alice","Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class );

    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  /**
   * This is really just a test that the testing is working.
   * There's no mapping for the plain localhost response, so when the microservice is running it just returns
   * a default error. We check for 404.
   */
  @Test
  public void testErrorForNonFunctioningEndpoint() {
    ResponseEntity<String> response = restTemplate.postForEntity(baseURL + port + "/",
        new HttpEntity<>("", httpHeaders), String.class);
    assertTrue(response.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404)));
  }

  /**
   * Calling the generateAssertions endpoint with no header and no data produces an error.
   */
  @Test
  public void generateAssertionsNoBodyError() {
    ResponseEntity<String> response = restTemplate.postForEntity(baseURL + port + generateAssertionsEndpoint,
                new HttpEntity<>("", new HttpHeaders()), String.class);
    assertTrue(response.getStatusCode().is4xxClientError());
  }

  /**
   * The generateAssertions endpoint, with correct headers but no data, should produce "Bad Request".
   */
  @Test
  public void testGenerateAssertionsBadRequest() {
    String url = baseURL + port + generateAssertionsEndpoint;

    HttpEntity<String> request = new HttpEntity<>("", httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(response.getBody()).contains("Bad Request"));
    assertTrue(containsIgnoreCase(response.getBody(), "Bad request"));
  }


  /**
   * The generateAssertions endpoint, called with a nonexistent contest, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNonExistentContestIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        "NonExistentContest",
        100,
        5,
        List.of("Alice", "Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(response.getBody()).contains("No such contest"));
    assertTrue(containsIgnoreCase(response.getBody(), "No such contest"));
  }
  /**
   * The generateAssertions endpoint, called with a valid plurality contest, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithPluralityContestIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        validPlurality,
        100,
        5,
        List.of("Alice", "Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(response.getBody()).contains("Not all IRV"));
    assertTrue(containsIgnoreCase(response.getBody(), "Not all IRV"));
  }

  /**
   * The generateAssertions endpoint, called with a mixed IRV and non-IRV contest, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithMixedIRVPluralityContestIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        invalidMixed,
        100,
        5,
        List.of("Alice", "Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(response.getBody()).toLowerCase()
        .contains("Not all IRV".toLowerCase()));
    assertTrue(containsIgnoreCase(response.getBody(), "Not all IRV"));
  }

  /**
   * The generateAssertions endpoint, called with an empty contest name, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithEmptyContestNameIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        "",
        100,
        5,
        List.of("Alice", "Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The generateAssertions endpoint, called with an all-whitespace contest name, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithWhitespaceContestNameIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        "       ",
        100,
        5,
        List.of("Alice","Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The generateAssertions endpoint, called with a null candidate list, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNullCandidateListIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        ballina,
        100,
        5,
        null
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The generateAssertions endpoint, called with an empty candidate list, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithEmptyCandidateListIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        ballina,
        100,
        5,
        List.of()
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The generateAssertions endpoint, called with a whitespace candidate name, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithWhiteSpaceCandidateNameIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        ballina,
        100,
        5,
        List.of("Alice", "    ")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The generateAssertions endpoint, called with null/missing total auditable ballots, returns a
   * meaningful error.
   */
  @Test
  public void generateAssertionsWithNullAuditableBallotsIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"timeLimitSeconds\":10.0,\"contestName\":\"Ballina Mayoral\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(),"Non-positive total auditable ballots"));
  }

  /**
   * The generateAssertions endpoint, called with zero total auditable ballots, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithZeroAuditableBallotsIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        ballina,
        0,
        5,
        List.of("Alice", "Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(),"Non-positive total auditable ballots"));
  }

  /**
   * The generateAssertions endpoint, called with negative total auditable ballots, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNegativeAuditableBallotsIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        ballina,
        -10,
        5,
        List.of("Alice", "Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(),"Non-positive total auditable ballots"));
  }



  /**
   * The generateAssertions endpoint, called with null/missing time limit, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNullTimeLimitIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    String requestAsJson =
        "{\"totalAuditableBallots\":100,\"contestName\":\"Ballina Mayoral\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(),"Non-positive time limit"));
  }

  /**
   * The generateAssertions endpoint, called with zero time limit, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithZeroTimeLimitIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        ballina,
        100,
        0,
        List.of("Alice", "Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(),"Non-positive time limit"));
  }

  /**
   * The generateAssertions endpoint, called with negative time limit, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNegativeTimeLimitIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        ballina,
        100,
        -5,
        List.of("Alice", "Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(containsIgnoreCase(response.getBody(),"Non-positive time limit"));
  }
}