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
import static au.org.democracydevelopers.raireservice.testUtils.defaultCount;
import static au.org.democracydevelopers.raireservice.testUtils.defaultCountJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.testUtils;
import static au.org.democracydevelopers.raireservice.testUtils.getAssertionsJSONEndpoint;
import static au.org.democracydevelopers.raireservice.testUtils.getAssertionsCSVEndpoint;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
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

  private static final Logger logger
      = LoggerFactory.getLogger(GetAssertionsAPIErrorJsonAndCsvTests.class);

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

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":\"Ballina Mayoral\","
        + defaultCountJson + "," + "\"candidates\":[\"Bob\",\"Chuan\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT.toString(),
        Objects.requireNonNull(response.getHeaders().get("error_code")).getFirst());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "No generate assertions summary"));
  }

  // TODO Add a test for the case where there's a summary record but no assertions. Should be.
  // No assertions have been generated for the contest"));

  /**
   * Test a variety of bad requests to ensure they elicit the appropriate error message.
   * @param type which endpoint to use: JSON or CSV.
   * @param riskLimit risk limit as a string.
   * @param contestName the name of the contest.
   * @param totalBallots the total ballots in the universe.
   * @param candidateList the list of candidate names.
   * @param errorMsg the expected error message.
   */
  @ParameterizedTest
  @MethodSource("expectedBadRequestErrors")
  public void testExpectedErrors(String type, String riskLimit, String contestName,
      String totalBallots, String candidateList, String errorMsg) {
    testUtils.log(logger, "testExpectedErrors");
    String url = baseURL + port
        + (type.equals("JSON") ? getAssertionsJSONEndpoint : getAssertionsCSVEndpoint);

    String requestAsJson = "{"
        + String.join(",", List.of(
          "\"riskLimit\":"+riskLimit,
          "\"contestName\":"+contestName,
          "\"totalAuditableBallots\":"+totalBallots,
          "\"candidates\":"+candidateList
        ))
        + "}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), errorMsg));
  }


  /**
   * The actual data for the testExpectedErrors function.
   * @return the data to be tested.
   */
  private static Stream<Arguments> expectedBadRequestErrors() {
    String aliceAndBobJSON = "[\"Alice\",\"Bob\"]";
    String ballotCount = Integer.toString(defaultCount);
    String ballinaMayoral = "\"Ballina Mayoral\"";
    String riskLimit = "0.05";

    return Stream.of(
        // getAssertions, called with a nonexistent contest, returns a meaningful error.
        Arguments.of("JSON", riskLimit, "\"Nonexistent Contest\"", ballotCount,
            aliceAndBobJSON, "No such contest"),
        Arguments.of("CSV", riskLimit, "\"Nonexistent Contest\"", ballotCount,
            aliceAndBobJSON, "No such contest"),
        // getAssertions, called with a valid plurality contest, returns a meaningful error.
        Arguments.of("JSON", riskLimit,  "\"Valid Plurality Contest\"", ballotCount,
            aliceAndBobJSON,  "Not comprised of all IRV"),
        Arguments.of("CSV", riskLimit,  "\"Valid Plurality Contest\"", ballotCount,
            aliceAndBobJSON,  "Not comprised of all IRV"),
        // getAssertions, called with a mixed IRV and non-IRV contest, returns a meaningful error.
        Arguments.of("JSON", riskLimit, "\"Invalid Mixed Contest\"", ballotCount,
            aliceAndBobJSON,  "Not comprised of all IRV"),
        Arguments.of("CSV", riskLimit, "\"Invalid Mixed Contest\"", ballotCount,
            aliceAndBobJSON,  "Not comprised of all IRV"),
        // getAssertions, called with a null contest name, returns a meaningful error.
        Arguments.of("JSON", riskLimit, null, ballotCount,
            aliceAndBobJSON, "No contest name"),
        Arguments.of("CSV", riskLimit, null, ballotCount,
            aliceAndBobJSON,  "No contest name"),
        // getAssertions, called with an empty contest name, returns a meaningful error.
        Arguments.of("JSON", riskLimit, "\"\"", ballotCount,
            aliceAndBobJSON, "No contest name"),
        Arguments.of("CSV", riskLimit, "\"\"", ballotCount,
            aliceAndBobJSON,  "No contest name"),
        // getAssertions, called with an all-whitespace contest name, returns a meaningful error.
        Arguments.of("JSON", riskLimit, "\"    \"", ballotCount,
            aliceAndBobJSON, "No contest name"),
        Arguments.of("CSV", riskLimit, "\"     \"", ballotCount,
            aliceAndBobJSON,  "No contest name"),
        // getAssertions, called with a null candidate list, returns a meaningful error.
        Arguments.of("JSON", riskLimit, ballinaMayoral, ballotCount,
            null, "Bad candidate list"),
        Arguments.of("CSV", riskLimit, ballinaMayoral, ballotCount,
            null,  "Bad candidate list"),
        // getAssertions endpoint, called with an empty candidate list, returns a meaningful error.
        Arguments.of("JSON", riskLimit, ballinaMayoral, ballotCount,
            "[]", "Bad candidate list"),
        Arguments.of("CSV", riskLimit, ballinaMayoral, ballotCount,
            "[]",  "Bad candidate list"),
        // getAssertions, called with a whitespace candidate name, returns a meaningful error.
        Arguments.of("JSON", riskLimit, ballinaMayoral, ballotCount,
            "[\"Alice\",\"    \"]", "Bad candidate list"),
        Arguments.of("CSV", riskLimit, ballinaMayoral, ballotCount,
            "[\"Alice\",\"    \"]",  "Bad candidate list"),
        // getAssertions, called with a null risk limit, returns a meaningful error. (JSON)
        Arguments.of("JSON", null, ballinaMayoral, ballotCount,
            aliceAndBobJSON, "Null or negative risk limit"),
        Arguments.of("CSV", null, ballinaMayoral, ballotCount,
            aliceAndBobJSON,   "Null or negative risk limit"),
        // getAssertions, called with a negative risk limit, returns a meaningful error.
        // (Note that a value >=1 is vacuously met but not invalid.)
        Arguments.of("JSON", "-0.05", ballinaMayoral, ballotCount,
            aliceAndBobJSON, "Null or negative risk limit"),
        Arguments.of("CSV", "-0.05", ballinaMayoral, ballotCount,
            aliceAndBobJSON,   "Null or negative risk limit"),
        // A request with a negative totalAuditableBallots is invalid.
        // (Note that a request with zero auditable ballots is strange but valid.)
        Arguments.of("JSON", riskLimit, ballinaMayoral, "-10",
            aliceAndBobJSON, "Non-positive total auditable ballots"),
        Arguments.of("CSV", riskLimit, ballinaMayoral, "-10",
            aliceAndBobJSON, "Non-positive total auditable ballots")
        // TODO Add a test for when the stored winner isn't on the list of candidates.
    );
  }

  /**
   * A valid request for a contest that exists but has no assertions. Returns the correct error
   * code and message. (CSV)
   */
  @Test
  public void testValidRequestWithNoAssertionsCSV() {
    testUtils.log(logger, "testValidRequestWithNoAssertionsCSV");
    String url = "http://localhost:" + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":\"Ballina Mayoral\","
        + defaultCountJson + "," + "\"candidates\":[\"Bob\",\"Chuan\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT.toString(),
        Objects.requireNonNull(response.getHeaders().get("error_code")).getFirst());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "No assertions have been generated"));
  }

  // TODO Add a test for when there are assertions but no summary. Should be
  //  "No assertions have been generated for the contest" if there's a summary but no assertions, and
  //  "No generate assertions summary" if there's no summary. JSON makes the distinction; CSV doesn't.));

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
   * The getAssertions endpoint, called with a missing contest name, returns a meaningful error.
   * (JSON)
   */
  @Test
  public void getAssertionsWithMissingContestNameIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithMissingContestNameIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"candidates\":[\"Alice\",\"Bob\"],"
        + defaultCountJson + "}";

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
        + defaultCountJson + "}";

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
        + defaultCountJson + "," + "\"contestName\":\"Ballina Mayoral\"}";

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
        + defaultCountJson + "," + "\"contestName\":\"Ballina Mayoral\"}";

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
        + defaultCountJson + "," + "\"contestName\":\"Ballina Mayoral\"}";

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
    + defaultCountJson + "," + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(), "Null or negative risk limit"));
  }

  /**
   * The getAssertions endpoint, called with a missing totalAuditableBallots, returns a meaningful
   * error. (JSON)
   */
  @Test
  public void getAssertionsWithMissingBallotCountIsAnErrorJSON() {
    testUtils.log(logger, "getAssertionsWithMissingBallotCountIsAnErrorJSON");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"candidates\":[\"Alice\",\"Bob\"],\"riskLimit\":0.05,"
        + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "Non-positive total auditable ballots"));
  }

  /**
   * The getAssertions endpoint, called with a missing totalAuditableBallots, returns a meaningful
   * error. (CSV)
   */
  @Test
  public void getAssertionsWithMissingBallotCountIsAnErrorCSV() {
    testUtils.log(logger, "getAssertionsWithMissingBallotCountIsAnErrorJSON");
    String url = baseURL + port + getAssertionsCSVEndpoint;

    String requestAsJson = "{\"candidates\":[\"Alice\",\"Bob\"],\"riskLimit\":0.05,"
        + "\"contestName\":\"Ballina Mayoral\"}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is4xxClientError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
        "Non-positive total auditable ballots"));
  }
}
