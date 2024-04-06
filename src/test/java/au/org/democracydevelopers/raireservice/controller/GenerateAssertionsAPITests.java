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

import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import com.google.gson.Gson;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;


/**
 * Tests for generate-assertions endpoint. This class automatically fires up the RAIRE Microservice on a random port, then runs
 * a series of (at this stage) very basic tests. Currently we check for proper input validation, and
 * check that one valid trivial request succeeds for each endpoint.
 * Note that you have to run the *whole class*. Individual tests do not work separately because they don't initiate the
 * microservice on their own.
 */

@ActiveProfiles("test-containers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
  }

  /**
   * The generateAssertions endpoint, called with a non-IRV contest, returns a meaningful error message.
   */
  @Test
  public void generateAssertionsWithNonIRVContestIsAnError() {
    String url = baseURL + port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        invalidMixed,
        100,
        5,
        List.of("Alice","Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(response.getBody()).contains("Not all IRV"));
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
}