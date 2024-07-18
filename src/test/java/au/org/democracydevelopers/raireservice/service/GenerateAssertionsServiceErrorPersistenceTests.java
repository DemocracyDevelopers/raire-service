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

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.time.TimeTaken;
import au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsSummary;
import au.org.democracydevelopers.raireservice.persistence.repository.GenerateAssertionsSummaryRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.testUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.*;
import static au.org.democracydevelopers.raireservice.testUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for appropriate persistence of failed assertion generation
 * (except for TIED_WINNERS, which is in GenerateAssertionsServiceKnownTests).
 * Tests include examples of every error that raire-java can produce, plus tests that
 * successive calls to GenerateAssertions correctly replace results.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsServiceErrorPersistenceTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsServiceErrorPersistenceTests.class);

  @Autowired
  private GenerateAssertionsSummaryRepository summaryRepository;

  @Autowired
  private GenerateAssertionsService generateAssertionsService;

  private static final List<String> aliceBobAndChuan = List.of("Alice", "Bob", "Chuan");
  private static final GenerateAssertionsRequest ballinaMayoralRequest = new GenerateAssertionsRequest(ballinaMayoral, 100,
      10, aliceBobAndChuan);

  /**
   * Data for a contest and corresponding request that never work, i.e. only for testing their absence from the database.
   */
  private static final String neverWorksContest = "neverWorksContest";
  private static final GenerateAssertionsRequest neverWorksRequest = new GenerateAssertionsRequest(neverWorksContest, 100,
      10, aliceBobAndChuan);

  /**
   * Test that error data is correctly stored, for all possible raire errors.
   * @param raireError   the error returned by raire-java.
   * @param savedError   the error expected to be saved, as a string.
   * @param savedWarning the warning, if any - the only current warning it TIME_OUT_TRIMMING.
   * @param savedMessage the message associated with the error, if any.
   * @throws RaireServiceException never.
   */
  @ParameterizedTest
  @Transactional
  @MethodSource("expectedRaireErrorSummaries")
  public void testErrorSummaryStorage(RaireError raireError, RaireServiceException.RaireErrorCode savedError, String savedWarning, String savedMessage)
      throws RaireServiceException {
    testUtils.log(logger, "testErrorSummaryStorage");

    RaireSolution.RaireResultOrError solution =
        new RaireSolution.RaireResultOrError(raireError);

    generateAssertionsService.persistAssertionsOrErrors(solution, ballinaMayoralRequest);
    Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(ballinaMayoral);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(ballinaMayoral, GenerateAssertionsSummary.UNKNOWN_WINNER,
        savedError.toString(), savedWarning, savedMessage));
  }

  /**
   * The data for testing the expected errors. These just consist of a RaireError (matching what would be output by
   * raire-java) and the corresponding strings we expect to be stored in the database (the same information as strings).
   */
   private static Stream<Arguments> expectedRaireErrorSummaries() {
     return Stream.of(
         // The elimination order "Chuan, Bob, Alice" apparently could not be ruled out.
         Arguments.of(new RaireError.CouldNotRuleOut(new int[]{2,1,0}), COULD_NOT_RULE_OUT_ALTERNATIVE, "", "Chuan, Bob, Alice"),
         // This happens if the assertion generation request had the wrong candidate names.
         Arguments.of(new RaireError.InvalidCandidateNumber(), WRONG_CANDIDATE_NAMES, "", "Candidate list does not match database"),
         Arguments.of(new RaireError.InternalErrorDidntRuleOutLoser(), INTERNAL_ERROR, "", "Internal error"),
         Arguments.of(new RaireError.InternalErrorRuledOutWinner(), INTERNAL_ERROR, "", "Internal error"),
         Arguments.of(new RaireError.InternalErrorTrimming(), INTERNAL_ERROR, "", "Internal error"),
         Arguments.of(new RaireError.InvalidNumberOfCandidates(), INTERNAL_ERROR, "", "Internal error"),
         Arguments.of(new RaireError.InvalidTimeout(), INTERNAL_ERROR, "", "Internal error"),
         // Bob and Chuan are apparently tied winners.
         Arguments.of(new RaireError.TiedWinners(new int[]{1,2}), TIED_WINNERS, "", "Tied winners: Bob, Chuan"),
         Arguments.of(new RaireError.TimeoutCheckingWinner(), TIMEOUT_CHECKING_WINNER, "", "Time out checking winner"),
         Arguments.of(new RaireError.TimeoutFindingAssertions(43.4), TIMEOUT_FINDING_ASSERTIONS, "", "Time out finding assertions"),
         // Alice is candidate 0, apparently the winner.
         Arguments.of(new RaireError.WrongWinner(new int[]{0}), INTERNAL_ERROR, "", "")
        );
   }

  /**
   * Timeout trimming assertions warning is correctly persisted.
   */
  @Test
  @Transactional
  public void testTimeoutTrimming() throws RaireServiceException {
    testUtils.log(logger, "testTimeoutTrimming");
    RaireSolution.RaireResultOrError solution = new RaireSolution.RaireResultOrError(new RaireResult(
        new AssertionAndDifficulty[]{new AssertionAndDifficulty(new NotEliminatedBefore(0,1), 42.5, 2)}, 42.5, 2, 0, 3,
        new TimeTaken(12L, 4), new TimeTaken(3L, 3), new TimeTaken(2L, 4), true));
    generateAssertionsService.persistAssertionsOrErrors(solution, ballinaMayoralRequest);
    Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(ballinaMayoral);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(ballinaMayoral, "Alice", "", TIMEOUT_TRIMMING_ASSERTIONS.toString(), ""));
  }

  /**
   * Trying to save a summary of a solution with an invalid winner index (-1) throws an exception with code INTERNAL_ERROR.
   */
  @Test
  @Transactional
  public void testInvalidWinnerThrowsException() {
    testUtils.log(logger, "testInvalidWinnerThrowsException");
    RaireSolution.RaireResultOrError solution = new RaireSolution.RaireResultOrError(new RaireResult(
        new AssertionAndDifficulty[]{}, 42.5, 200, -1, 4,
        new TimeTaken(12L, 4), new TimeTaken(3L, 3), new TimeTaken(2L, 4), false));
    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(solution, neverWorksRequest)
    );
    assertEquals(INTERNAL_ERROR.toString(), ex.errorCode.toString());
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "Invalid winner"));

    // Test that there is no record in the database.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(neverWorksContest);
    assertTrue(optSummary.isEmpty());
  }

  /**
   * Trying to save a summary of a solution with an invalid winner index throws an exception with code INTERNAL_ERROR.
   * This has a 3-candidate winner list and a claimed winner index of 3, which isn't a valid index.
   */
  @Test
  @Transactional
  public void testInvalidWinnerTooLargeThrowsException() {
    testUtils.log(logger, "testInvalidWinnerTooLargeThrowsException");
    RaireSolution.RaireResultOrError solution = new RaireSolution.RaireResultOrError(new RaireResult(
        new AssertionAndDifficulty[]{}, 42.5, 200, 3, 4,
        new TimeTaken(12L, 4), new TimeTaken(3L, 3), new TimeTaken(2L, 4), false));
    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
      generateAssertionsService.persistAssertionsOrErrors(solution, neverWorksRequest)
    );
    assertEquals(INTERNAL_ERROR.toString(), ex.errorCode.toString());
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "Invalid winner"));

    // Test that there is no record in the database.
    Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(neverWorksContest);
    assertTrue(optSummary.isEmpty());
  }

  /**
   * Repeated saves for the same contest are properly overwritten.
   */
  @Test
  @Transactional
  public void testErrorSummaryStoragereplacement() throws RaireServiceException {
    testUtils.log(logger, "testErrorSummaryStorageReplacement");

    RaireSolution.RaireResultOrError solution =
        new RaireSolution.RaireResultOrError(new RaireError.CouldNotRuleOut(new int[]{2,1,0}));

    // Persist the first one, check that the database contains it.
    generateAssertionsService.persistAssertionsOrErrors(solution, ballinaMayoralRequest);
    Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(ballinaMayoral);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(ballinaMayoral, GenerateAssertionsSummary.UNKNOWN_WINNER,
        COULD_NOT_RULE_OUT_ALTERNATIVE.toString(), "", "Chuan, Bob, Alice"));

    // Make an update, check that the new values have been stored.
    // The elimination order "Chuan, Bob, Alice" apparently could not be ruled out.
    RaireSolution.RaireResultOrError solution2 =
        new RaireSolution.RaireResultOrError(new RaireError.InternalErrorTrimming());

    // Persist the first one, check that the database contains it.
    generateAssertionsService.persistAssertionsOrErrors(solution2, ballinaMayoralRequest);
    Optional<GenerateAssertionsSummary> optSummary2 = summaryRepository.findByContestName(ballinaMayoral);
    assertTrue(optSummary2.isPresent());
    assertTrue(optSummary2.get().equalData(ballinaMayoral, GenerateAssertionsSummary.UNKNOWN_WINNER,
        INTERNAL_ERROR.toString(), "", "Internal error"));
  }

}
