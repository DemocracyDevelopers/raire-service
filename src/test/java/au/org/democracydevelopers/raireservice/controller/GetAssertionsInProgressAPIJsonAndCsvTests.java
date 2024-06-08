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

import static au.org.democracydevelopers.raireservice.testUtils.defaultCount;
import static au.org.democracydevelopers.raireservice.testUtils.correctAssertionData;
import static au.org.democracydevelopers.raireservice.testUtils.correctMetadata;
import static au.org.democracydevelopers.raireservice.testUtils.correctSolutionData;
import static au.org.democracydevelopers.raireservice.testUtils.defaultWinner;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.testUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
 * Tests for get-assertions endpoint. This class automatically fires up the RAIRE Microservice on a
 * random port, then runs a series of tests for correct responses to valid requests.
 * The list of tests is similar to - and in most cases identical to - the tests in
 * GetAssertionsInProgressServiceTestsJsonAndCsv.java.
 * Contests which will be used for validity testing are preloaded into the database using
 * src/test/resources/data.sql.
 */
@ActiveProfiles("assertions-in-progress")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsInProgressAPIJsonAndCsvTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GetAssertionsInProgressAPIJsonAndCsvTests.class);

  private final static HttpHeaders httpHeaders = new HttpHeaders();
  private final static String baseURL = "http://localhost:";
  private final static String getAssertionsJsonEndpoint = "/raire/get-assertions-json";
  private final static String getAssertionsCsvEndpoint = "/raire/get-assertions-csv";
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
   * Retrieve assertions for a contest that has one NEB assertion (audit in progress). (JSON)
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNEBAssertionJSON() {
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNEBAssertionJSON");
    String url = baseURL + port + getAssertionsJsonEndpoint;

    GetAssertionsRequest request = new GetAssertionsRequest(oneNEBAssertionContest, defaultCount,
        List.of("Alice","Bob"), defaultWinner, BigDecimal.valueOf(0.1));

    ResponseEntity<RaireSolution> response = restTemplate.postForEntity(url, request,
        RaireSolution.class);

    // The metadata has been constructed appropriately
    assertNotNull(response.getBody());
    assertTrue(correctMetadata(List.of("Alice","Bob"), oneNEBAssertionContest,
        BigDecimal.valueOf(0.1), response.getBody().metadata, Double.class));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertNull(response.getBody().solution.Err);

    // Check the contents of the RaireResults within the RaireSolution.
    assertTrue(correctSolutionData(320,1.1, 1,
        response.getBody().solution.Ok));

    // We expect one NEB assertion with the following data.
    assertTrue(correctAssertionData("NEB", 320, 1.1, 0, 1,
        new ArrayList<>(), 0.5, response.getBody().solution.Ok.assertions[0]));

  }

  /**
   * Retrieve assertions for a contest that has one NEB assertion (audit in progress). (CSV)
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNEBAssertionCSV() {
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNEBAssertionCSV");
    String url = baseURL + port + getAssertionsCsvEndpoint;

    String requestAsJson = "{\"riskLimit\":0.10,\"contestName\":\"" +
        oneNEBAssertionContest+"\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    String csv = restTemplate.postForEntity(url, request, String.class).getBody();

    assertNotNull(csv);
    assertTrue(csv.contains("Contest name,One NEB Assertion Contest\n"));
    assertTrue(csv.contains("Candidates,\"Alice,Bob\""));
    assertTrue(csv.contains("Extreme item,Value,Assertion IDs\n"));
    assertTrue(csv.contains("Margin,320,\"1\"\n"));
    assertTrue(csv.contains("Diluted margin,0.32,\"1\"\n"));
    assertTrue(csv.contains("Raire difficulty,1.1,\"1\"\n"));
    assertTrue(csv.contains("Current risk,0.50,\"1\"\n"));
    assertTrue(csv.contains("Optimistic samples to audit,111,\"1\"\n"));
    assertTrue(csv.contains("Estimated samples to audit,111,\"1\"\n"));
    assertTrue(csv.contains(
        "ID,Type,Winner,Loser,Assumed continuing,Difficulty,Margin,Diluted margin,Risk,"
            + "Estimated samples to audit,Optimistic samples to audit,Two vote over count,"
            + "One vote over count,Other discrepancy count,One vote under count,"
            + "Two vote under count\n"));
    assertTrue(csv.contains("1,NEB,Alice,Bob,,1.1,320,0.32,0.50,111,111,0,0,0,0,0\n"));
  }

  /**
   * Retrieve assertions for a contest that has one NEN assertion (audit in progress). (JSON)
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENAssertionJSON() {
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNENAssertionJSON");
    String url = baseURL + port + getAssertionsJsonEndpoint;

    GetAssertionsRequest request = new GetAssertionsRequest(oneNENAssertionContest, defaultCount,
        List.of("Alice","Bob","Charlie","Diego"), defaultWinner, BigDecimal.valueOf(0.1));
    ResponseEntity<RaireSolution> response = restTemplate.postForEntity(url, request,
        RaireSolution.class);

    // The metadata has been constructed appropriately
    assertNotNull(response.getBody());
    assertTrue(
        correctMetadata(List.of("Alice", "Bob", "Charlie", "Diego"), oneNENAssertionContest,
            BigDecimal.valueOf(0.1), response.getBody().metadata, Double.class));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertNull(response.getBody().solution.Err);

    // Check the contents of the RaireResults within the RaireSolution.
    assertTrue(correctSolutionData(240, 3.01, 1,
        response.getBody().solution.Ok));

    // We expect one assertion with the following data.
    assertTrue(correctAssertionData("NEN", 240, 3.01, 0, 2,
        List.of(0, 1, 3, 2), 0.2, response.getBody().solution.Ok.assertions[0]));
  }

  /**
   * Retrieve assertions for a contest that has one NEN assertion (audit in progress). (CSV)
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENAssertionCSV() {
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNENAssertionCSV");
    String url = baseURL + port + getAssertionsCsvEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.10,\"contestName\":\"" + oneNENAssertionContest
            + "\",\"candidates\":[\"Alice\",\"Bob\",\"Charlie\",\"Diego\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    String csv = restTemplate.postForEntity(url, request, String.class).getBody();

    assertNotNull(csv);
    assertTrue(csv.contains("Contest name,One NEN Assertion Contest\n"));
    assertTrue(csv.contains("Candidates,\"Alice,Bob,Charlie,Diego\""));
    assertTrue(csv.contains("Extreme item,Value,Assertion IDs\n"));
    assertTrue(csv.contains("Margin,240,\"1\"\n"));
    assertTrue(csv.contains("Diluted margin,0.12,\"1\"\n"));
    assertTrue(csv.contains("Raire difficulty,3.01,\"1\"\n"));
    assertTrue(csv.contains("Current risk,0.20,\"1\"\n"));
    assertTrue(csv.contains("Optimistic samples to audit,201,\"1\"\n"));
    assertTrue(csv.contains("Estimated samples to audit,245,\"1\"\n"));
    assertTrue(csv.contains(
      "ID,Type,Winner,Loser,Assumed continuing,Difficulty,Margin,Diluted margin,Risk,"
      + "Estimated samples to audit,Optimistic samples to audit,Two vote over count,"
      + "One vote over count,Other discrepancy count,One vote under count,"
      + "Two vote under count\n"));
    assertTrue(csv.contains(
      "1,NEN,Alice,Charlie,\"Alice,Charlie,Diego,Bob\",3.01,240,0.12,0.20,245,201,0,1,2,0,0\n"
    ));
  }

  /**
   * Retrieve assertions for a contest that has one NEN and one NEB assertion (audit in progress).
   * (JSON)
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNENOneNEBAssertionInProgressJSON() {
    testUtils.log(logger, "retrieveAssertionsOneNENOneNEBAssertionInProgressJSON");
    String url = baseURL + port + getAssertionsJsonEndpoint;

    // Make the request.
    GetAssertionsRequest request = new GetAssertionsRequest(oneNEBOneNENAssertionContest,
        defaultCount, List.of("Liesl","Wendell","Amanda","Chuan"), defaultWinner, BigDecimal.valueOf(0.05));
    ResponseEntity<RaireSolution> response = restTemplate.postForEntity(url, request,
        RaireSolution.class);

    // The metadata has been constructed appropriately.
    assertNotNull(response.getBody());
    assertTrue(correctMetadata(List.of("Liesl", "Wendell", "Amanda", "Chuan"),
        oneNEBOneNENAssertionContest, BigDecimal.valueOf(0.05), response.getBody().metadata,
        Double.class));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertNull(response.getBody().solution.Err);

    // Check the contents of the RaireResults within the RaireSolution.
    assertTrue(correctSolutionData(112, 3.17, 2,
        response.getBody().solution.Ok));

    // We expect two assertions with the following data, but we don't necessarily know what order
    // they're in. So check for their presence at either position.
    assertTrue(
        correctAssertionData("NEB", 112, 0.1, 2, 0,
            new ArrayList<>(), 0.08, response.getBody().solution.Ok.assertions[0]) ||
            correctAssertionData("NEB", 112, 0.1, 2, 0,
                new ArrayList<>(), 0.08, response.getBody().solution.Ok.assertions[1])
    );

    assertTrue(
        correctAssertionData("NEN", 560, 3.17, 2, 1,
            List.of(0, 1, 2), 0.7, response.getBody().solution.Ok.assertions[0]) ||
            correctAssertionData("NEN", 560, 3.17, 2, 1,
                List.of(0, 1, 2), 0.7, response.getBody().solution.Ok.assertions[1])
    );
  }

  /**
   * Retrieve assertions for a contest that has one NEN and one NEB assertion (audit in progress).
   * (CSV)
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNENOneNEBAssertionInProgressCSV() {
    testUtils.log(logger, "retrieveAssertionsOneNENOneNEBAssertionInProgressCSV");
    String url = baseURL + port + getAssertionsCsvEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.05,\"contestName\":\"" + oneNEBOneNENAssertionContest
            + "\",\"candidates\":[\"Liesl\",\"Wendell\",\"Amanda\",\"Chuan\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    String csv = restTemplate.postForEntity(url, request, String.class).getBody();

    assertNotNull(csv);
    assertTrue(csv.contains("Contest name,One NEN NEB Assertion Contest\n"));
    assertTrue(csv.contains("Candidates,\"Liesl,Wendell,Amanda,Chuan\""));
    assertTrue(csv.contains("Extreme item,Value,Assertion IDs\n"));
    assertTrue(csv.contains("Margin,112,\"1\"\n"));
    assertTrue(csv.contains("Diluted margin,0.1,\"1\"\n"));
    assertTrue(csv.contains("Raire difficulty,3.17,\"2\"\n"));
    assertTrue(csv.contains("Current risk,0.70,\"2\"\n"));
    assertTrue(csv.contains("Optimistic samples to audit,200,\"2\"\n"));
    assertTrue(csv.contains("Estimated samples to audit,300,\"2\"\n"));
    assertTrue(csv.contains(
        "ID,Type,Winner,Loser,Assumed continuing,Difficulty,Margin,Diluted margin,Risk,"
            + "Estimated samples to audit,Optimistic samples to audit,Two vote over count,"
            + "One vote over count,Other discrepancy count,One vote under count,"
            + "Two vote under count\n"));
    assertTrue(csv.contains("1,NEB,Amanda,Liesl,,0.1,112,0.1,0.08,27,20,2,0,0,1,0\n"));
    assertTrue(csv.contains(
        "2,NEN,Amanda,Wendell,\"Liesl,Wendell,Amanda\",3.17,560,0.5,0.70,300,200,0,2,0,0,1\n"
    ));
  }
}