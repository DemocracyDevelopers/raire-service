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

package au.org.democracydevelopers.raireservice.service;

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import au.org.democracydevelopers.raire.RaireError.TiedWinners;
import au.org.democracydevelopers.raire.RaireError.TimeoutCheckingWinner;
import au.org.democracydevelopers.raire.RaireError.TimeoutFindingAssertions;
import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsSummary;
import au.org.democracydevelopers.raireservice.persistence.repository.GenerateAssertionsSummaryRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.testUtils;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests to validate the behavior of Assertion generation on a collection of particularly nasty
 * test cases designed to elicit errors. These kinds of errors _are_ expected to happen occasionally
 * in normal operation, if the input data is particularly challenging.
 * This has the same tests as GenerateAssertionsAPIWickedTests.java. Relevant data is preloaded into
 * the test database from src/test/resources/known_testcases_votes.sql.
 * This includes
 * - a contest with tied winners,
 * - a contest that times out trying to find the winners (there are 20 and they are all tied),
 * - a contest (Byron Mayor '21) that has enough candidates to time out generating assertions (when
 *   given a very short timeout).
 */
@ActiveProfiles("known-testcases")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsServiceWickedTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GenerateAssertionsServiceWickedTests.class);

  @Autowired
  GenerateAssertionsService generateAssertionsService;

  @Autowired
  GenerateAssertionsSummaryRepository summaryRepository;

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
  private static final List<String> choicesByron = List.of("HUNTER Alan","CLARKE Bruce",
      "COOREY Cate","ANDERSON John","MCILRATH Christopher","LYON Michael","DEY Duncan",
      "PUGH Asren","SWIVEL Mark");
  private static final List<String> timeoutCheckingWinnersChoices = List.of("A","B","C","D","E",
      "F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T");


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

  /**
   * Tied winners results in raire-java returning a TiedWinners RaireError. This is a super-simple
   * election with two candidates (Alice and Bob) with one first-preference vote each.
   */
  @Test
  @Transactional
  void tiedWinnersThrowsTiedWinnersErrorAndStoresIt() throws RaireServiceException {
    testUtils.log(logger, "tiedWinnersThrowsTiedWinnersErrorAndStoresIt");
    RaireResultOrError result = generateAssertionsService.generateAssertions(tiedWinnersRequest);

    assertNull(result.Ok);
    assertNotNull(result.Err);

    assertInstanceOf(TiedWinners.class, result.Err);

    generateAssertionsService.persistAssertionsOrErrors(result, tiedWinnersRequest);
    Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(tiedWinnersContest);
    assertTrue(optSummary.isPresent());
    // Check that Alice and Bob are the tied winners
    assertTrue(optSummary.get().equalData(tiedWinnersContest, GenerateAssertionsSummary.UNKNOWN_WINNER,
        TIED_WINNERS.toString(), "", "Alice"));
    assertTrue(optSummary.get().equalData(tiedWinnersContest, GenerateAssertionsSummary.UNKNOWN_WINNER,
        TIED_WINNERS.toString(), "", "Bob"));
    // and that Chuan is not
    assertFalse(optSummary.get().equalData(tiedWinnersContest, GenerateAssertionsSummary.UNKNOWN_WINNER,
        TIED_WINNERS.toString(), "", "Chuan"));
  }

  /**
   * A huge number of tied winners results in raire-java returning a TimeOutCheckingWinners
   * RaireError. This election has 20 candidates who are all tied.
   */
  @Test
  @Transactional
  void twentyTiedWinnersThrowsTimeOutCheckingWinnersErrorAndStoresIt() throws RaireServiceException {
    testUtils.log(logger, "twentyTiedWinnersThrowsTimeOutCheckingWinnersErrorAndStoresIt");
    RaireResultOrError result = generateAssertionsService
        .generateAssertions(checkingWinnersTimeoutRequest);

    assertNull(result.Ok);
    assertNotNull(result.Err);

    assertInstanceOf(TimeoutCheckingWinner.class, result.Err);

    generateAssertionsService.persistAssertionsOrErrors(result, checkingWinnersTimeoutRequest);
    Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(timeOutCheckingWinnersContest);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(timeOutCheckingWinnersContest, GenerateAssertionsSummary.UNKNOWN_WINNER,
        TIMEOUT_CHECKING_WINNER.toString(), "", "Time out checking winner"));
  }

  /**
   * Byron Mayoral times out generating assertions when given a very very short timeout.
   */
  @Test
  @Transactional
  void ByronWithShortTimeoutThrowsTimeoutGeneratingAssertionsError() throws RaireServiceException {
    testUtils.log(logger, "ByronWithShortTimeoutThrowsTimeoutGeneratingAssertionsError");
    RaireResultOrError result = generateAssertionsService.generateAssertions(ByronShortTimeoutRequest);

    assertNull(result.Ok);
    assertNotNull(result.Err);

    assertInstanceOf(TimeoutFindingAssertions.class, result.Err);

    generateAssertionsService.persistAssertionsOrErrors(result, ByronShortTimeoutRequest);
    Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(ByronMayoral);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(ByronMayoral, GenerateAssertionsSummary.UNKNOWN_WINNER,
        TIMEOUT_FINDING_ASSERTIONS.toString(), "", "Time out finding assertions"));
  }
}
