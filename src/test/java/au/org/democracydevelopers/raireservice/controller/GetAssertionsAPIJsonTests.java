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
import static au.org.democracydevelopers.raireservice.testUtils.defaultCount;
import static au.org.democracydevelopers.raireservice.testUtils.baseURL;
import static au.org.democracydevelopers.raireservice.testUtils.correctAssertionData;
import static au.org.democracydevelopers.raireservice.testUtils.correctMetadata;
import static au.org.democracydevelopers.raireservice.testUtils.correctSolutionData;
import static au.org.democracydevelopers.raireservice.testUtils.defaultCountJson;
import static au.org.democracydevelopers.raireservice.testUtils.getAssertionsJSONEndpoint;
import static au.org.democracydevelopers.raireservice.testUtils.oneNEBAssertionContest;
import static au.org.democracydevelopers.raireservice.testUtils.oneNEBOneNENAssertionContest;
import static au.org.democracydevelopers.raireservice.testUtils.oneNENAssertionContest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.testUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
 * Tests for get-assertions endpoint. This class automatically fires up the RAIRE Microservice on a
 * random port, then runs a series of tests for correct responses to valid requests.
 * The list of tests is similar to - and in most cases identical to - the GetAssertionsJsonServiceTests.
 * Contests which will be used for validity testing are preloaded into the database using
 * src/test/resources/data.sql.
 */
@ActiveProfiles("simple-assertions")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsAPIJsonTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GetAssertionsAPIJsonTests.class);

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
   * The getAssertions endpoint, valid request. Currently just checking that the serialization
   * correctly ignores time_to_find_assertions.
   */
  @Test
  public void getAssertionsWithOneNEBContest() {
    testUtils.log(logger, "getAssertionsWithOneNEBContest");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    String requestAsJson = "{\"riskLimit\":0.05,\"contestName\":\"" + oneNEBAssertionContest + "\","
        + defaultCountJson + "," + "\"winner\":\"Bob\"" + "," + "\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertFalse(StringUtils.containsIgnoreCase(response.getBody(), "time_to_find_assertions"));
  }

  /**
   * Retrieve assertions for a contest that has one NEB assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNEBAssertion() {
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNEBAssertion");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    // Make the request.
    GetAssertionsRequest request = new GetAssertionsRequest(oneNEBAssertionContest, defaultCount,
        List.of("Bob","Alice"), BigDecimal.valueOf(0.1));
    ResponseEntity<RaireSolution> response
        = restTemplate.postForEntity(url, request, RaireSolution.class);

    // The metadata has been constructed appropriately. Note reversal of candidate names to check
    // the set, rather than the ordered list, is matched.
    assertNotNull(response.getBody());
    assertTrue(correctMetadata(List.of("Alice","Bob"), oneNEBAssertionContest, BigDecimal.valueOf(0.1),
        defaultCount, response.getBody().metadata, Double.class));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertNull(response.getBody().solution.Err);

    // Check the contents of the RaireResults within the RaireSolution.
    assertTrue(correctSolutionData(320,1.1, 2,1, 1,
        response.getBody().solution.Ok));

    // We expect one NEB assertion with the following data.
    assertTrue(correctAssertionData("NEB", 320, 1.1, 1,
        0, new ArrayList<>(), 1.0, response.getBody().solution.Ok.assertions[0]));
  }

  /**
   * Retrieve assertions for a contest that has one NEN assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENAssertion() {
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNENAssertion");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    GetAssertionsRequest request =  new GetAssertionsRequest(oneNENAssertionContest, defaultCount,
        List.of("Alice","Bob","Charlie","Diego"), BigDecimal.valueOf(0.1));
    ResponseEntity<RaireSolution> response = restTemplate.postForEntity(url, request, RaireSolution.class);

    // The metadata has been constructed appropriately
    assertNotNull(response.getBody());
    assertTrue(correctMetadata(List.of("Alice","Bob","Charlie","Diego"),oneNENAssertionContest,
        BigDecimal.valueOf(0.1), defaultCount, response.getBody().metadata, Double.class));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertNull(response.getBody().solution.Err);

    // Check the contents of the RaireResults within the RaireSolution.
    assertTrue(correctSolutionData(240, 3.01, 4, 0, 1, response.getBody().solution.Ok));

    // We expect one NEN assertion with the following data.
    assertTrue(correctAssertionData("NEN",240,3.01, 0,
        2, List.of(0,1,3,2), 1.0, response.getBody().solution.Ok.assertions[0]));
  }

  /**
   * Retrieve assertions for a contest where the request has been set up with incorrect
   * candidate names for the saved winner (which is none of them).
   * This is a valid request in the sense that it passes Request.Validate(), but should later fail.
   */
  @Test
  @Transactional
  void retrieveAssertionsIncorrectCandidateNamesIsAnError()  {
    testUtils.log(logger, "retrieveAssertionsIncorrectCandidateNamesIsAnError");
    String url = baseURL + port + getAssertionsJSONEndpoint;

    GetAssertionsRequest request =  new GetAssertionsRequest(oneNEBOneNENAssertionContest,
        defaultCount, List.of("Alice","Bob","Charlie","Diego"), BigDecimal.valueOf(0.1));
    ResponseEntity<String> response
        = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is5xxServerError());
    assertTrue(StringUtils.containsIgnoreCase(response.getBody(),
       "Inconsistent winner and candidate list"));
    assertEquals(WRONG_CANDIDATE_NAMES.toString(),
        Objects.requireNonNull(response.getHeaders().get("error_code")).getFirst());
  }

  // TODO make another test where the saved winner is OK but the other candidates aren't.
        // Should be "candidate list provided as parameter is inconsistent"));
  // Possibly needed at other points too.
}
