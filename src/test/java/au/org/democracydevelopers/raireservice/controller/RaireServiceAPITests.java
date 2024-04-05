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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;


/*
  * Tests for Springboot functioning. This class automatically fires up the RAIRE Microservice on a random port, then runs
  * a series of (at this stage) very basic tests.
  * Note that you have to run the *whole class*. Individual tests do not work separately because they don't initiate the
  * microservice on their own.
  */



@ActiveProfiles("test-containers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RaireServiceAPITests {

  private final Gson gson = new Gson();

  private final static String getAssertionsEndpoint = "/raire/get-assertions";
  private final static String generateAssertionsEndpoint = "/raire/generate-assertions";
  private final static String ballina = "Ballina Mayoral";

  @LocalServerPort
  private int port;


  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void contextLoads() {
  }

  /*
   * This is really just a test that the testing is working.
   * There's no mapping for the plain localhost response, so when the microservice is running it just returns
   * a default error. We check for 404 because that appears in the default error text.
   */
  @Test
  public void testErrorForNonFunctioningEndpoint() {
    assertTrue((restTemplate.getForObject("http://localhost:" + port + "/",
        String.class)).contains("404"));
  }

  /*
   * Check that calling the right endpoint with no header and no data produces a sensible error message.
   */
  @Test
  public void testMethodNotAllowed() {
    assertTrue((restTemplate.getForObject("http://localhost:" + port + getAssertionsEndpoint,
        String.class)).contains("405"));
    assertTrue((restTemplate.getForObject("http://localhost:" + port + getAssertionsEndpoint,
        String.class)).contains("Method Not Allowed"));
    assertTrue((restTemplate.getForObject("http://localhost:" + port + generateAssertionsEndpoint,
        String.class)).contains("405"));
    assertTrue((restTemplate.getForObject("http://localhost:" + port + generateAssertionsEndpoint,
        String.class)).contains("Method Not Allowed"));
  }

  /*
   * The right endpoint, with correct headers but no data, should produce "Bad Request".
   */
  @Test
  public void testGetAssertionsBadRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String url = "http://localhost:" + port + generateAssertionsEndpoint;

    HttpEntity<String> request = new HttpEntity<>("", headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(response.getBody().contains("Bad Request"));
  }

  /*
   * The right endpoint, with correct headers but no data, should produce "Bad Request".
   */
  @Test
  public void testGenerateAssertionsBadRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String url = "http://localhost:" + port + getAssertionsEndpoint;

    HttpEntity<String> request = new HttpEntity<>("", headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(response.getBody().contains("Bad Request"));
  }

  @Test
  public void testTrivialGetAssertionsExample() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String url = "http://localhost:" +port + getAssertionsEndpoint;

    GetAssertionsRequest getAssertions = new GetAssertionsRequest(
        ballina,
        List.of("Alice","Bob"),
        new BigDecimal(0.05)
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(getAssertions), headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class );

    assertTrue(response.getStatusCode().is2xxSuccessful());
  }

  @Test
  public void testTrivialGenerateAssertionsExample() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    String url = "http://localhost:" +port + generateAssertionsEndpoint;

    GenerateAssertionsRequest generateAssertions = new GenerateAssertionsRequest(
        ballina,
        100,
        5,
        List.of("Alice","Bob")
    );

    HttpEntity<String> request = new HttpEntity<>(gson.toJson(generateAssertions), headers);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class );

    assertTrue(response.getStatusCode().is2xxSuccessful());
  }
}