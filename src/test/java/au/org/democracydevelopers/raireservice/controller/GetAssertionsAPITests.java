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
 * Tests for get-assertions endpoint. This class automatically fires up the RAIRE Microservice on a random port, then runs
 * a series of (at this stage) very basic tests. Currently we check for proper input validation, and
 * check that one valid trivial request succeeds for each endpoint.
 * Note that you have to run the *whole class*. Individual tests do not work separately because they don't initiate the
 * microservice on their own.
 */

@ActiveProfiles("test-containers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetAssertionsAPITests {

  private final Gson gson = new Gson();
  private final static HttpHeaders httpHeaders = new HttpHeaders();

  private final static String baseURL = "http://localhost:";
  private final static String getAssertionsEndpoint = "/raire/get-assertions";
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
   * This is really just a test that the testing is working. There's no mapping for the plain
   * localhost response, so when the microservice is running it just returns a default error. We
   * check for 404.
   */
  @Test
  public void testErrorForNonFunctioningEndpoint() {
    ResponseEntity<String> response = restTemplate.postForEntity(baseURL + port + "/",
        new HttpEntity<>("", httpHeaders), String.class);
    assertTrue(response.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404)));
  }

  /**
   * Calling the getAssertions endpoint with no header and no data produces an error.
   */
  @Test
  public void getAssertionsNoBodyError() {
    ResponseEntity<String> response = restTemplate.postForEntity(baseURL + port + getAssertionsEndpoint,
        new HttpEntity<>("", new HttpHeaders()), String.class);
    assertTrue(response.getStatusCode().is4xxClientError());
  }

  /**
   * The getAssertions endpoint, with correct headers but no data, should produce "Bad Request".
   */
  @Test
  public void testGetAssertionsBadRequest() {
    String url = baseURL + port + getAssertionsEndpoint;

    HttpEntity<String> request = new HttpEntity<>("", httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(response.getBody()).contains("Bad Request"));
  }

  /**
   * The getAssertions endpoint, called with a non-IRV contest, returns an error.
   */
  @Test
  public void getAssertionsWithNonIRVContestIsAnError() {
    String url = baseURL + port + getAssertionsEndpoint;

    GetAssertionsRequest getAssertions = new GetAssertionsRequest(
        invalidMixed,
        List.of("Alice", "Bob"),
        BigDecimal.valueOf(0.03)
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(getAssertions), httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(response.getBody()).contains("Not all IRV"));

  }

  /**
   * The generateAssertions endpoint, called with a non-IRV contest, returns an error.
   */
  @Test
  public void testTrivialGetAssertionsExample() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String url = "http://localhost:" + port + getAssertionsEndpoint;

    GetAssertionsRequest getAssertions = new GetAssertionsRequest(
        ballina,
        List.of("Alice", "Bob"),
        new BigDecimal(0.05)
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(getAssertions), headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is2xxSuccessful());
  }
}