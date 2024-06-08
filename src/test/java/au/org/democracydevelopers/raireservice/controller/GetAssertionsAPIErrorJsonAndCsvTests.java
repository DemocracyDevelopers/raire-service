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

import static au.org.democracydevelopers.raireservice.testUtils.baseURL;
import static au.org.democracydevelopers.raireservice.testUtils.defaultCountJson;
import static au.org.democracydevelopers.raireservice.testUtils.defaultWinnerJSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.testUtils;
import static au.org.democracydevelopers.raireservice.testUtils.getAssertionsJSONEndpoint;
import static au.org.democracydevelopers.raireservice.testUtils.getAssertionsCSVEndpoint;
import java.util.Objects;
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
import org.apache.commons.lang3.StringUtils;

/**
 * Tests for get-assertions endpoint. This class automatically fires up the RAIRE Microservice on a
 * random port, then runs a series of basic tests of invalid input.
 * Each test runs on both the json and csv get-assertions endpoints.
 * The list of tests is similar to GetAssertionsRequestTests.java, and also to
 * GenerateAssertionsAPITests.java
 * when the same test is relevant to both endpoints.
 * Contests which will be used for validity testing are preloaded into the database using
 * src/test/resources/data.sql.
 */
@ActiveProfiles("test-containers")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsAPIErrorJsonAndCsvTests {

  private static final Logger logger = LoggerFactory.getLogger(GetAssertionsAPIErrorJsonAndCsvTests.class);

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
   * A valid request for a contest that exists but has no assertions. Returns the correct error
   * code and message. (JSON)
   */
  @Test
  public void testValidRequestWithNoAssertionsJSON() {
    testUtils.log(logger, "testValidRequestWithNoAssertionsJSON");
    String url = "http://localhost:" + port + getAssertionsJSONEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.05,\"contestName\":\"Ballina Mayoral\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT.toString(),
        Objects.requireNonNull(response.getHeaders().get("error_code")).getFirst());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "No assertions have been generated for the contest"));
  }

  /**
   * A valid request for a contest that exists but has no assertions. Returns the correct error
   * code and message. (CSV)
   */
  @Test
  public void testValidRequestWithNoAssertionsCSV() {
    testUtils.log(logger, "testValidRequestWithNoAssertionsCSV");
    String url = "http://localhost:" + port + getAssertionsCSVEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.05,\"contestName\":\"Ballina Mayoral\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT.toString(),
        Objects.requireNonNull(response.getHeaders().get("error_code")).getFirst());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "No assertions have been generated for the contest"));
  }

  /**
   * This is really just a test that the testing is working. There's no mapping for the plain
   * localhost response, so when the microservice is running it just returns a default error. We
   * check for 404.
   */
  @Test
  public void testErrorForNonFunctioningEndpoint() {
    testUtils.log(logger, "testErrorForNonFunctioningEndpoint");
    ResponseEntity<String> response = restTemplate.postForEntity(baseURL + port + "/",
        new HttpEntity<>("", httpHeaders), String.class);
    assertTrue(response.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404)));
  }

  /**
   * Calling the getAssertions endpoint with no header and no data produces an error. (JSON)
   */
  @Test
  public void getAssertionsNoBodyErrorJSON() {
    testUtils.log(logger, "getAssertionsNoBodyErrorJSON");
    ResponseEntity<String> response = restTemplate.postForEntity(baseURL + port +
        getAssertionsJSONEndpoint, new HttpEntity<>("", new HttpHeaders()), String.class);
    assertTrue(response.getStatusCode().is4xxClientError());
  }

  /**
   * Calling the getAssertions endpoint with no header and no data produces an error. (CSV)
   */
  @Test
  public void getAssertionsNoBodyErrorCSV() {
    testUtils.log(logger, "getAssertionsNoBodyErrorCSV");
    ResponseEntity<String> response = restTemplate.postForEntity(baseURL + port +
            getAssertionsCSVEndpoint, new HttpEntity<>("", new HttpHeaders()), String.class);
    assertTrue(response.getStatusCode().is4xxClientError());
  }

  /**
   * The getAssertions endpoint, with correct headers but no data, should produce "Bad Request".
   * (JSON)
   */
  @Test
  public void testGetAssertionsBadRequestJSON() {
    testUtils.log(logger, "testGetAssertionsBadRequestJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    HttpEntity<String> request = new HttpEntity<>("", httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(response.getBody()).contains("Bad Request"));
  }

  /**
   * The getAssertions endpoint, with correct headers but no data, should produce "Bad Request".
   * (CSV)
   */
  @Test
  public void testGetAssertionsBadRequestCSV() {
    testUtils.log(logger, "testGetAssertionsBadRequestCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    HttpEntity<String> request = new HttpEntity<>("", httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(Objects.requireNonNull(response.getBody()).contains("Bad Request"));
  }

  /**
   * The getAssertions endpoint, called with a nonexistent contest, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithNonExistentContestIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithNonExistentContestIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":\"Nonexistent Contest\","
      + defaultCountJson + defaultWinnerJSON +"\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No such contest"));

  }

  /**
   * The getAssertions endpoint, called with a nonexistent contest, returns a meaningful error.
   * (CSV)
   */
  @Test
  public void getAssertionsWithNonExistentContestIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithNonExistentContestIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":\"Nonexistent Contest\","
        + defaultCountJson + defaultWinnerJSON +"\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No such contest"));

  }

  /**
   * The getAssertions endpoint, called with a valid plurality contest, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithPluralityContestIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithPluralityContestIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.05,\"contestName\":\"Valid Plurality Contest\","
            + defaultCountJson + defaultWinnerJSON +"\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Not comprised of all IRV"));
  }

  /**
   * The getAssertions endpoint, called with a valid plurality contest, returns a meaningful error.
   * (CSV)
   */
  @Test
  public void getAssertionsWithPluralityContestIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithPluralityContestIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.05,\"contestName\":\"Valid Plurality Contest\","
        + defaultCountJson + defaultWinnerJSON +"\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Not comprised of all IRV"));
  }

  /**
   * The getAssertions endpoint, called with a mixed IRV and non-IRV contest, returns a meaningful
   * error. (JSON)
   */
  @Test
  public void getAssertionsWithMixedIRVPluralityContestIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithMixedIRVPluralityContestIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.05,\"contestName\":\"Invalid Mixed Contest\","
        + defaultCountJson + defaultWinnerJSON +"\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Not comprised of all IRV"));

  }

  /**
   * The getAssertions endpoint, called with a mixed IRV and non-IRV contest, returns a meaningful
   * error. (CSV)
   */
  @Test
  public void getAssertionsWithMixedIRVPluralityContestIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithMixedIRVPluralityContestIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.05,\"contestName\":\"Invalid Mixed Contest\","
        + defaultCountJson + defaultWinnerJSON + "\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Not comprised of all IRV"));

  }

  /**
   * The getAssertions endpoint, called with a missing contest name, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithMissingContestNameIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithMissingContestNameIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"candidates\":[\"Alice\",\"Bob\"]"
        + defaultCountJson + defaultWinnerJSON + "}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The getAssertions endpoint, called with a missing contest name, returns a meaningful error.
   * (CSV)
   */
  @Test
  public void getAssertionsWithMissingContestNameIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithMissingContestNameIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"candidates\":[\"Alice\",\"Bob\"],"
        + defaultCountJson + defaultWinnerJSON + "}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The getAssertions endpoint, called with a null contest name, returns a meaningful error. (JSON)
   */
  @Test
  public void getAssertionsWithNullContestNameIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithNullContestNameIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":null,"
        +"\"candidates\":[\"Alice\",\"Bob\"]," + defaultCountJson + defaultWinnerJSON + "}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The getAssertions endpoint, called with a null contest name, returns a meaningful error. (CSV)
   */
  @Test
  public void getAssertionsWithNullContestNameIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithNullContestNameIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":null,"
        + defaultCountJson + defaultWinnerJSON + "\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The getAssertions endpoint, called with an empty contest name, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithEmptyContestNameIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithEmptyContestNameIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":\"\","
        + defaultCountJson + defaultWinnerJSON + "\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The getAssertions endpoint, called with an empty contest name, returns a meaningful error.
   * (CSV)
   */
  @Test
  public void getAssertionsWithEmptyContestNameIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithEmptyContestNameIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":\"\","
        + defaultCountJson + defaultWinnerJSON + "\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The getAssertions endpoint, called with an all-whitespace contest name, returns a meaningful
   * error. (JSON)
   */
  @Test
  public void getAssertionsWithWhitespaceContestNameIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithWhitespaceContestNameIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":\"    \","
        + defaultCountJson + defaultWinnerJSON + "\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The getAssertions endpoint, called with an all-whitespace contest name, returns a meaningful
   * error. (CSV)
   */
  @Test
  public void getAssertionsWithWhitespaceContestNameIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithWhitespaceContestNameIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":\"    \","
        + defaultCountJson + defaultWinnerJSON + "\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "No contest name"));
  }

  /**
   * The getAssertions endpoint, called with a missing candidate list, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithMissingCandidateListIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithMissingCandidateListIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The getAssertions endpoint, called with a missing candidate list, returns a meaningful error.
   * (CSV)
   */
  @Test
  public void getAssertionsWithMissingCandidateListIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithMissingCandidateListIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The getAssertions endpoint, called with a null candidate list, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithNullCandidateListIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithNullCandidateListIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,,\"candidates\":null,"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The getAssertions endpoint, called with a null candidate list, returns a meaningful error.
   * (CSV)
   */
  @Test
  public void getAssertionsWithNullCandidateListIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithNullCandidateListIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"candidates\":null,"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The getAssertions endpoint, called with an empty candidate list, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithEmptyCandidateListIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithEmptyCandidateListIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"candidates\":[],"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The getAssertions endpoint, called with an empty candidate list, returns a meaningful error.
   * (CSV)
   */
  @Test
  public void getAssertionsWithEmptyCandidateListIsAnErrorCVS() {
    testUtils.log(logger, "getAssertionsWithEmptyCandidateListIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"candidates\":[],"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The getAssertions endpoint, called with a whitespace candidate name, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithWhitespaceCandidateNameIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithWhitespaceCandidateNameIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05, \"candidates\":[\"Alice\",\"    \"],"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The getAssertions endpoint, called with a whitespace candidate name, returns a meaningful error.
   * (CSV)
   */
  @Test
  public void getAssertionsWithWhitespaceCandidateNameIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithWhitespaceCandidateNameIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05, \"candidates\":[\"Alice\",\"    \"],"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Bad candidate list"));
  }

  /**
   * The getAssertions endpoint, called with a missing risk limit, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithMissingRiskLimitIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithMissingRiskLimitIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"candidates\":[\"Alice\",\"Bob\"],"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Null or negative risk limit"));
  }

  /**
   * The getAssertions endpoint, called with a missing risk limit, returns a meaningful error.
   * (CSV)
   */
  @Test
  public void getAssertionsWithMissingRiskLimitIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithMissingRiskLimitIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"candidates\":[\"Alice\",\"Bob\"],"
    + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Null or negative risk limit"));
  }

  /**
   * The getAssertions endpoint, called with a null risk limit, returns a meaningful error. (JSON)
   */
  @Test
  public void getAssertionsWithNullRiskLimitIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithNullRiskLimitIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":null,\"candidates\":[\"Alice\",\"Bob\"],"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Null or negative risk limit"));
  }

  /**
   * The getAssertions endpoint, called with a null risk limit, returns a meaningful error. (CSV)
   */
  @Test
  public void getAssertionsWithNullRiskLimitIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithNullRiskLimitIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":null,\"candidates\":[\"Alice\",\"Bob\"],"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Null or negative risk limit"));
  }

  /**
   * The getAssertions endpoint, called with a negative risk limit, returns a meaningful error.
   * (Note that a value >=1 is vacuously met but not invalid.) (JSON)
   */
  @Test
  public void getAssertionsWithNegativeRiskLimitIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithNegativeRiskLimitIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":-0.05,\"candidates\":[\"Alice\",\"Bob\"],"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Null or negative risk limit"));
  }
  /**
   * The getAssertions endpoint, called with a negative risk limit, returns a meaningful error.
   * (Note that a value >=1 is vacuously met but not invalid.) (CSV)
   */
  @Test
  public void getAssertionsWithNegativeRiskLimitIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithNegativeRiskLimitIsAnErrorCSV");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":-0.05,\"candidates\":[\"Alice\",\"Bob\"],"
        + defaultCountJson + defaultWinnerJSON + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Null or negative risk limit"));
  }
}
