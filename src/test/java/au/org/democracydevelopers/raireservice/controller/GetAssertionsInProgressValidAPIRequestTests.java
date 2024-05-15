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

import static au.org.democracydevelopers.raireservice.testUtils.correctIndexedAPIAssertionData;
import static au.org.democracydevelopers.raireservice.testUtils.correctMetadata;
import static au.org.democracydevelopers.raireservice.testUtils.correctSolutionData;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.testUtils;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for get-assertions endpoint. This class automatically fires up the RAIRE Microservice on a random
 * port, then runs a series of tests for correct responses to valid requests.
 * The list of tests is similar to - and in most cases identical to - the GetAssertionsJsonServiceTests.
 * Note that you have to run the *whole class*. Individual tests do not work separately because they don't
 * initiate the microservice on their own.
 * Contests which will be used for validity testing are pre-loaded into the database using
 * src/test/resources/data.sql.
 */
@ActiveProfiles("assertions-in-progress")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsInProgressValidAPIRequestTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GetAssertionsInProgressValidAPIRequestTests.class);

  private final static HttpHeaders httpHeaders = new HttpHeaders();
  private final static String baseURL = "http://localhost:";
  private final static String getAssertionsJsonEndpoint = "/raire/get-assertions-json";
  private final static String oneNEBAssertionContest = "One NEB Assertion Contest";
  private final static String oneNENAssertionContest = "One NEN Assertion Contest";
  private final static String oneNEBOneNENAssertionContest = "One NEN NEB Assertion Contest";

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeAll
  public static void before() {
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
  }


  /**
   * Retrieve assertions for a contest that has one NEB assertion (audit in progress).
   */
  @Test
  @Transactional
  void retrieveAssertionsAsJsonExistentContestOneNEBAssertion() {
    testUtils.log(logger, "retrieveAssertionsAsJsonExistentContestOneNEBAssertion");
    String url = baseURL + port + getAssertionsJsonEndpoint;

    String requestAsJson = "{\"riskLimit\":0.10,\"contestName\":\"" +
        oneNEBAssertionContest+"\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    // The metadata has been constructed appropriately
    assertTrue(correctMetadata(List.of("Alice","Bob"), oneNEBAssertionContest, 0.1,
        response.getBody()));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertFalse(StringUtils.containsIgnoreCase(response.getBody(), "Error"));

    // Check the contents of the RaireResults within the RaireSolution.
    assertTrue(correctSolutionData(320,1.1, 1, response.getBody()));

    // We expect one assertion with the following data.
    assertTrue(correctIndexedAPIAssertionData("NEB", 320, 1.1, 0,
        1, new ArrayList<>(), 0.5, response.getBody(),0));

  }

  /**
   * Retrieve assertions for a contest that has one NEN assertion (audit in progress).
   */
  @Test
  @Transactional
  void retrieveAssertionsAsJsonExistentContestOneNENAssertion() {
    testUtils.log(logger, "retrieveAssertionsAsJsonExistentContestOneNENAssertion");
    String url = baseURL + port + getAssertionsJsonEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.10,\"contestName\":\"" + oneNENAssertionContest
            + "\",\"candidates\":[\"Alice\",\"Bob\",\"Charlie\",\"Diego\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    // The metadata has been constructed appropriately
    assertTrue(
        correctMetadata(List.of("Alice", "Bob", "Charlie", "Diego"), oneNENAssertionContest, 0.1,
            response.getBody()));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertFalse(StringUtils.containsIgnoreCase(response.getBody(), "Error"));

    // Check the contents of the RaireResults within the RaireSolution.
    assertTrue(correctSolutionData(240, 3.01, 1, response.getBody()));

    // We expect one assertion with the following data.
    assertTrue(correctIndexedAPIAssertionData("NEN", 240, 3.01, 0, 2,
        List.of(0, 1, 3, 2), 0.2, response.getBody(), 0));
  }

  /**
   * Retrieve assertions for a contest that has one NEN and one NEB assertion (audit in progress).
   */
  @Test
  @Transactional
  void retrieveAssertionsAsJsonOneNENOneNEBAssertionInProgress() {
    testUtils.log(logger, "retrieveAssertionsAsJsonOneNENOneNEBAssertionInProgress");
    String url = baseURL + port + getAssertionsJsonEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.05,\"contestName\":\"" + oneNEBOneNENAssertionContest
            + "\",\"candidates\":[\"Liesl\",\"Wendell\",\"Amanda\",\"Chuan\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    // The metadata has been constructed appropriately
    assertTrue(correctMetadata(List.of("Liesl", "Wendell", "Amanda", "Chuan"),
        oneNEBOneNENAssertionContest, 0.05,
        response.getBody()));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertFalse(StringUtils.containsIgnoreCase(response.getBody(), "Error"));

    // Check the contents of the RaireResults within the RaireSolution.
    assertTrue(correctSolutionData(112, 3.17, 2, response.getBody()));

    // We expect two assertions with the following data, but we don't necessarily know what order they're in.
    // So check for their presence at either position.
    assertTrue(
        correctIndexedAPIAssertionData("NEB", 112, 0.1, 2, 0,
            new ArrayList<>(), 0.08, response.getBody(), 0) ||
            correctIndexedAPIAssertionData("NEB", 112, 0.1, 2, 0,
                new ArrayList<>(), 0.08, response.getBody(), 1)
    );

    assertTrue(
        correctIndexedAPIAssertionData("NEN", 560, 3.17, 2, 1,
            List.of(0, 1, 2), 0.7, response.getBody(), 0) ||
            correctIndexedAPIAssertionData("NEN", 560, 3.17, 2, 1,
                List.of(0, 1, 2), 0.7, response.getBody(), 1)
    );
  }
}