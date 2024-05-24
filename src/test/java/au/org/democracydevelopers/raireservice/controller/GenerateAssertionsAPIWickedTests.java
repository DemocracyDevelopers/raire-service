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

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.TIED_WINNERS;
import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.TIMEOUT_CHECKING_WINNER;
import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.TIMEOUT_FINDING_ASSERTIONS;
import static au.org.democracydevelopers.raireservice.service.RaireServiceException.errorCodeString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.testUtils;
import au.org.democracydevelopers.raireservice.util.DoubleComparator;
import java.math.BigDecimal;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests to validate the behavior of Assertion generation on a collection of particularly nasty
 * test cases designed to elicit errors. These kinds of errors _are_ expected to happen occasionally
 * in normal operation, if the input data is particularly challenging.
 * This has the same tests as GenerateAssertionsServiceWickedTests.java. Relevant data is preloaded
 * into the test database from src/test/resources/known_testcases_votes.sql.
 * This includes
 * - a contest with tied winners,
 * - a contest that times out trying to find the winners (there are 20 and they are all tied),
 * - a contest (Byron Mayor '21) that has enough candidates to time out generating assertions (when
 *   given a very short timeout).
 */
@ActiveProfiles("known-testcases")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsAPIWickedTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GenerateAssertionsAPIWickedTests.class);
  private final static String baseURL = "http://localhost:";
  private final static String generateAssertionsEndpoint = "/raire/generate-assertions";

  /**
   * Names of contests, to match preloaded data.
   */
  private static final String tiedWinnersContest = "Tied Winners Contest";
  private static final String ByronMayoral = "Byron Mayoral";
  private static final String timeOutCheckingWinnersContest = "Time out checking winners contest";

  /**
   * Candidate lists for the preloaded contests.
   */
  private static final List<String> aliceChuanBob = List.of("Alice", "Chuan", "Bob");
  private static final List<String> choicesByron = List.of("HUNTER Alan", "CLARKE Bruce",
      "COOREY Cate", "ANDERSON John", "MCILRATH Christopher", "LYON Michael", "DEY Duncan",
      "PUGH Asren", "SWIVEL Mark");
  private static final List<String> timeoutCheckingWinnersChoices = List.of("A", "B", "C", "D", "E",
      "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T");


  /**
   * The API requests appropriate for each preloaded contest. Those intended to produce a timeout
   * have a particularly small timeLimit.
   */
  private final static GenerateAssertionsRequest tiedWinnersRequest
      = new GenerateAssertionsRequest(tiedWinnersContest, 2, 5,
      aliceChuanBob);
  private final static GenerateAssertionsRequest ByronShortTimeoutRequest
      = new GenerateAssertionsRequest(ByronMayoral, 18165, 0.001,
      choicesByron);
  private final static GenerateAssertionsRequest checkingWinnersTimeoutRequest
      = new GenerateAssertionsRequest(timeOutCheckingWinnersContest, 20,
      0.001, timeoutCheckingWinnersChoices);

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  /**
   * Tied winners results in raire-java returning a TiedWinners RaireError. This is a super-simple
   * election with two candidates with one vote each.
   */
  @Test
  @Transactional
  void tiedWinnersGivesTiedWinnersError() {
    testUtils.log(logger, "tiedWinnersGivesTiedWinnersError");

    String generateUrl = baseURL + port + generateAssertionsEndpoint;

    // Request for the assertions to be generated.
    ResponseEntity<String> response = restTemplate.postForEntity(generateUrl, tiedWinnersRequest,
        String.class);

    // Check that generation is successful and we got the right winner.
    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(TIED_WINNERS.toString(), response.getHeaders().getFirst(errorCodeString));
  }

   /**
    * A huge number of tied winners results in raire-java returning a TimeOutCheckingWinners
    * error. This election has 20 candidates who are all tied.
   */
  @Test
  @Transactional
  void twentyTiedWinnersGivesTimeOutCheckingWinnersError() {
    testUtils.log(logger, "twentyTiedWinnersGivesTimeOutCheckingWinnersError");

    String generateUrl = baseURL + port + generateAssertionsEndpoint;

    // Request for the assertions to be generated.
    ResponseEntity<String> response = restTemplate.postForEntity(generateUrl,
        checkingWinnersTimeoutRequest, String.class);

    // Check that generation is successful and we got the right winner.
    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(TIMEOUT_CHECKING_WINNER.toString(), response.getHeaders().getFirst(errorCodeString));
  }

   /**
   * Byron Mayoral times out generating assertions when given a very very short timeout.
   */
  @Test
  @Transactional
  void ByronWithShortTimeoutGivesTimeoutGeneratingAssertionsError() {
    testUtils.log(logger, "ByronWithShortTimeoutGivesTimeoutGeneratingAssertionsError");
    String generateUrl = baseURL + port + generateAssertionsEndpoint;

    // Request for the assertions to be generated.
    ResponseEntity<String> response = restTemplate.postForEntity(generateUrl,
        ByronShortTimeoutRequest, String.class);

    // Check that generation is successful and we got the right winner.
    assertTrue(response.getStatusCode().is5xxServerError());
    assertEquals(TIMEOUT_FINDING_ASSERTIONS.toString(), response.getHeaders().getFirst(errorCodeString));
  }
}