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
import static au.org.democracydevelopers.raireservice.testUtils.defaultCountJson;
import static au.org.democracydevelopers.raireservice.testUtils.defaultWinnerJSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.testUtils;
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

/**
 * Tests for get-assertions endpoint with the csv request. This class automatically fires up the
 * RAIRE Microservice on a random port, then runs a series of tests.
 * The tests are essentially the same as those in GetAssertionsCSVTests.java, but we're checking for
 * correct API output rather than checking the service directly.
 * Contests which will be used for validity testing are preloaded into the database using
 * src/test/resources/simple_assertions_csv_challenges.sql.
 */

@ActiveProfiles("csv-challenges")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsAPICsvTests {

  private static final Logger logger = LoggerFactory.getLogger(GetAssertionsAPICsvTests.class);

  private final static HttpHeaders httpHeaders = new HttpHeaders();
  private final static String baseURL = "http://localhost:";
  private final static String getAssertionsEndpoint = "/raire/get-assertions-csv";
  private final static String candidatesAsJson = "\"candidates\":[\"Alice\",\"Bob\",\"Chuan\",\"Diego\"]}";
  private final static List<String> trickyCharacters
      = List.of("Annoying, Alice", "\"Breaking, Bob\"", "Challenging, Chuan", "O'Difficult, Diego");

  private final static String trickyCharactersAsJson =
      "\"candidates\":[\"Annoying, Alice\",\"\\\"Breaking, Bob\\\"\",\"Challenging, Chuan\",\"O'Difficult, Diego\"]}";

  @LocalServerPort
  private int port;


  @Autowired
  private TestRestTemplate restTemplate;

  @BeforeAll
  public static void before() {
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
  }

  /**
   * Test proper csv file generation of assertions when those assertions have lots of ties. These
   * maxima and minima have been manually computed to make sure they're correct.
   */
  @Test
  public void testValidRequestWithLotsOfTies() {
    testUtils.log(logger, "testValidRequestWithLotsOfTies");
    String url = baseURL + port + getAssertionsEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.10,\"contestName\":\"Lots of assertions with ties Contest\","
            + defaultCountJson + defaultWinnerJSON + candidatesAsJson;

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    String output = response.getBody();

    assertNotNull(output);
    assertTrue(output.contains("Contest name,Lots of assertions with ties Contest\n"));
    assertTrue(output.contains("Candidates,\"Alice,Bob,Chuan,Diego\"\n\n"));
    assertTrue(output.contains("Extreme item,Value,Assertion IDs"));
    assertTrue(output.contains("Margin,220,\"2, 5, 6\""));
    assertTrue(output.contains("Diluted margin,0.22,\"2, 5, 6\""));
    assertTrue(output.contains("Raire difficulty,3.1,\"3\""));
    assertTrue(output.contains("Current risk,0.23,\"2, 3\""));
    assertTrue(output.contains("Optimistic samples to audit,910,\"4\""));
    assertTrue(output.contains("Estimated samples to audit,430,\"2, 5\"\n\n"));
    assertTrue(output.contains(
        "ID,Type,Winner,Loser,Assumed continuing,Difficulty,Margin,Diluted margin,Risk,"
        + "Estimated samples to audit,Optimistic samples to audit,Two vote over count,"
        + "One vote over count,Other discrepancy count,One vote under count,"
        + "Two vote under count\n"
    ));
    assertTrue(output.contains("1,NEB,Alice,Bob,,2.1,320,0.32,0.04,110,100,0,0,0,0,0\n"));
    assertTrue(output.contains("2,NEB,Chuan,Bob,,1.1,220,0.22,0.23,430,200,0,0,0,0,0\n"));
    assertTrue(output.contains("3,NEB,Diego,Chuan,,3.1,320,0.32,0.23,50,110,0,0,0,0,0\n"));
    assertTrue(output.contains(
        "4,NEN,Alice,Bob,\"Alice,Bob,Chuan\",2.0,420,0.42,0.04,320,910,0,0,0,0,0\n"
    ));
    assertTrue(output.contains(
        "5,NEN,Alice,Diego,\"Alice,Diego\",1.1,220,0.22,0.07,430,210,0,0,0,0,0\n"
    ));
    assertTrue(output.contains(
        "6,NEN,Alice,Bob,\"Alice,Bob,Diego\",1.2,220,0.22,0.04,400,110,0,0,0,0,0\n"
    ));
  }

  /**
   * Test for difficult characters in candidate names, including ' and " and ,
   */
  @Test
  public void testCharacterEscapingForCSVExport() {
    testUtils.log(logger, "testCharacterEscapingForCSVExport");
    String url = baseURL + port + getAssertionsEndpoint;
    String requestAsJson =
        "{\"riskLimit\":0.10,\"contestName\":\"Lots of tricky characters Contest\","
            + defaultCountJson + defaultWinnerJSON + trickyCharactersAsJson;

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    String output = response.getBody();

    assertNotNull(output);
    assertTrue(StringUtils.containsIgnoreCase(output, trickyCharacters.get(0)));
    assertTrue(StringUtils.containsIgnoreCase(output, trickyCharacters.get(1)));
    assertTrue(StringUtils.containsIgnoreCase(output, trickyCharacters.get(2)));
    assertTrue(StringUtils.containsIgnoreCase(output, trickyCharacters.get(3)));
  }

  /**
   * A simple test for correct generation on a simple test case with one assertion of each type.
   */
  @Test
  public void testCSVDemoContest() {
    testUtils.log(logger, "testCSVDemoContest");
    String url = baseURL + port + getAssertionsEndpoint;
    String requestAsJson = "{\"riskLimit\":0.10,\"contestName\":\"CSV Demo Contest\","
        + defaultCountJson + defaultWinnerJSON + candidatesAsJson;

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    String output = response.getBody();

    assertNotNull(output);
    assertTrue(output.contains("Contest name,CSV Demo Contest\n"));
    assertTrue(output.contains("Candidates,\"Alice,Bob,Chuan,Diego\"\n\n"));
    assertTrue(output.contains("Extreme item,Value,Assertion IDs\n"));
    assertTrue(output.contains("Margin,100,\"2\"\n"));
    assertTrue(output.contains("Diluted margin,0.1,\"2\"\n"));
    assertTrue(output.contains("Raire difficulty,6.1,\"2\"\n"));
    assertTrue(output.contains("Current risk,0.06,\"1\"\n"));
    assertTrue(output.contains("Optimistic samples to audit,45,\"2\"\n"));
    assertTrue(output.contains("Estimated samples to audit,55,\"1\"\n"));
    assertTrue(output.contains(
        "ID,Type,Winner,Loser,Assumed continuing,Difficulty,Margin,Diluted margin,Risk,"
            + "Estimated samples to audit,Optimistic samples to audit,Two vote over count,"
            + "One vote over count,Other discrepancy count,One vote under count,"
            + "Two vote under count\n"));
    assertTrue(output.contains("1,NEB,Bob,Alice,,5.1,112,0.112,0.06,55,35,0,2,0,0,0\n"));
    assertTrue(output.contains(
        "2,NEN,Diego,Chuan,\"Alice,Chuan,Diego\",6.1,100,0.1,0.05,45,45,0,0,0,0,0\n"
    ));
  }

  /**
   * A request with candidates who are inconsistent with the assertions in the database is an error.
   */
  @Test
  public void wrongCandidatesIsAnError() {
    testUtils.log(logger, "wrongCandidatesIsAnError");
    String url = baseURL + port + getAssertionsEndpoint;

    String requestAsJson = "{\"riskLimit\":0.10,\"contestName\":\"CSV Demo Contest\","
        + defaultCountJson + defaultWinnerJSON
        + "\"candidates\":[\"Alicia\",\"Boba\",\"Chuan\",\"Diego\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(WRONG_CANDIDATE_NAMES.toString(),
        response.getHeaders().getFirst("error_code"));
  }
}
