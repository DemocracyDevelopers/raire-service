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

import static au.org.democracydevelopers.raireservice.NSWValues.expectedSolutionData;
import static au.org.democracydevelopers.raireservice.testUtils.baseURL;
import static au.org.democracydevelopers.raireservice.testUtils.generateAssertionsEndpoint;
import static au.org.democracydevelopers.raireservice.testUtils.getAssertionsJSONEndpoint;
import static org.junit.jupiter.api.Assertions.*;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raireservice.NSWValues.Expected;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.testUtils;
import au.org.democracydevelopers.raireservice.util.DoubleComparator;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.EnabledIf;

/**
 * Tests to validate the behaviour of Assertion generation on NSW 2021 Mayoral election data.
 * Data is loaded in from src/test/resources/NSW2021Data/
 * These tests all pass, but can be disabled because loading in all the NSW data takes a long time.
 */
@ActiveProfiles("nsw-testcases")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@EnabledIf(value = "${test-strategy.run-nsw-tests}", loadContext = true)
public class GenerateAssertionsAPINSWTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsAPINSWTests.class);
  private static final int DEFAULT_TIME_LIMIT = 5;
  private static final BigDecimal DEFAULT_RISK_LIMIT = BigDecimal.valueOf(0.03);
  private static final DoubleComparator doubleComparator = new DoubleComparator();

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  /**
   * Iterate through all the NSW example data,
   * - request assertion generation through the API,
   * - check for the right winner,
   * - request the assertion data through the get-assertions API (JSON)
   * - verify the expected difficulty.
   */
  @Test
  public void checkAllNSWByAPI() {
    testUtils.log(logger, "checkAllNSWByAPI");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;
    String getUrl = baseURL + port + getAssertionsJSONEndpoint;

    for(Expected expected : expectedSolutionData) {
      testUtils.log(logger, "checkAllNSWByAPI: contest "+expected.contestName());
      GenerateAssertionsRequest generateRequest = new GenerateAssertionsRequest(
          expected.contestName(), expected.ballotCount(), DEFAULT_TIME_LIMIT, expected.choices());

      // Request for the assertions to be generated.
      ResponseEntity<GenerateAssertionsResponse> response = restTemplate.postForEntity(generateUrl,
          generateRequest, GenerateAssertionsResponse.class);

      // Check that generation is successful, with no retry.
      assertTrue(response.getStatusCode().is2xxSuccessful());
      assertNotNull(response.getBody());
      assertTrue(response.getBody().succeeded());
      assertFalse(response.getBody().retry());

      // Request the assertions
      GetAssertionsRequest getRequest = new GetAssertionsRequest(expected.contestName(),
          expected.ballotCount(), expected.choices(), DEFAULT_RISK_LIMIT);
      ResponseEntity<RaireSolution> getResponse = restTemplate.postForEntity(getUrl, getRequest,
          RaireSolution.class);

      // Check that the retrieved assertions have the right overall difficulty.
      assertTrue(response.getStatusCode().is2xxSuccessful());
      assertNotNull(getResponse.getBody());
      assertEquals(0, doubleComparator.compare(expected.difficulty(),
          getResponse.getBody().solution.Ok.difficulty));
    }
  }
}