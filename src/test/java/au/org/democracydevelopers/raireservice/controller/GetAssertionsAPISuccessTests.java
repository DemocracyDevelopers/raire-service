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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
 * Tests for get-assertions endpoint. This class automatically fires up the RAIRE Microservice on a random
 * port, then runs a series of (at this stage) very basic tests. Currently we check for proper input
 * validation, and check that one valid trivial request succeeds for each endpoint.
 * The list of tests is similar to GetAssertionsRequestTests.java, and also to GenerateAssertionsAPITests.java
 * when the same test is relevant to both endpoints.
 * Note that you have to run the *whole class*. Individual tests do not work separately because they don't
 * initiate the microservice on their own.
 * Contests which will be used for validity testing are pre-loaded into the database using
 * src/test/resources/data.sql.
 */

@ActiveProfiles("simple-assertions")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsAPISuccessTests {

  private final static HttpHeaders httpHeaders = new HttpHeaders();
  private final static String baseURL = "http://localhost:";
  private final static String getAssertionsEndpoint = "/raire/get-assertions";

  private final static String oneNEBAssertionContest = "One NEB Assertion Contest";
  private final static String oneNENAssertionContest = "One NEN Assertion Contest";
  private final static String oneNEBOneNENAssertionContest = "One NEN NEB Assertion Contest";
  private final static String multiCountyContest = "Multi-County Contest 1";

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
   * The getAssertions endpoint, valid request. Currently just checking that the serialization correctly
   * ignores time_to_find_assertions.
   */
  @Test
  public void getAssertionsWithOneNEBContest() {
    String url = baseURL + port + getAssertionsEndpoint;

    String requestAsJson =
        "{\"riskLimit\":0.05,\"contestName\":\""+oneNEBAssertionContest+"\",\"candidates\":[\"Alice\",\"Bob\"]}";

    HttpEntity<String> request = new HttpEntity<>(requestAsJson, httpHeaders);
    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

    assertTrue(response.getStatusCode().is2xxSuccessful());
    assertFalse(StringUtils.containsIgnoreCase(response.getBody(), "time_to_find_assertions"));
  }
}
