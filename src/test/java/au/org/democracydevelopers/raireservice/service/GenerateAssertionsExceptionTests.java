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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raire.RaireError.CouldNotRuleOut;
import au.org.democracydevelopers.raire.RaireError.InternalErrorDidntRuleOutLoser;
import au.org.democracydevelopers.raire.RaireError.InternalErrorRuledOutWinner;
import au.org.democracydevelopers.raire.RaireError.InternalErrorTrimming;
import au.org.democracydevelopers.raire.RaireError.InvalidCandidateNumber;
import au.org.democracydevelopers.raire.RaireError.InvalidNumberOfCandidates;
import au.org.democracydevelopers.raire.RaireError.InvalidTimeout;
import au.org.democracydevelopers.raire.RaireError.TiedWinners;
import au.org.democracydevelopers.raire.RaireError.TimeoutCheckingWinner;
import au.org.democracydevelopers.raire.RaireError.TimeoutFindingAssertions;
import au.org.democracydevelopers.raire.RaireError.TimeoutTrimmingAssertions;
import au.org.democracydevelopers.raire.RaireError.WrongWinner;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCodes;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/*
 * Simple tests on RaireServiceExceptions, to ensure that the exceptions from raire-java
 * are being translated correctly.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GenerateAssertionsExceptionTests {

  @Autowired
  ContestRepository contestRepository;

  private final List<String> candidates = List.of("Alice","Bob","Chuan","Diego");

  /**
   * The tied winners are correctly transcribed for human readability.
   * The correct error code is returned.
   */
  @Test
  public void TiedWinnersIsTiedWinners() {
    int[] winners  = {0,1};
    RaireError raireError = new TiedWinners(winners);
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Alice"));
    assertTrue(StringUtils.containsIgnoreCase(msg,"Bob"));
    assertFalse(StringUtils.containsIgnoreCase(msg,"Chuan"));
    assertFalse(StringUtils.containsIgnoreCase(msg,"Diego"));
    assertEquals(RaireErrorCodes.TIED_WINNERS, e.errorCode);
  }

  /**
   * Reasonable error message and correct error code for timeOutFindingAssertions.
   */
  @Test
  public void timeoutFindingAssertionsGetsCorrectMessage() {
    RaireError raireError = new TimeoutFindingAssertions(5.0);
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Time out finding assertions"));
    assertEquals(RaireErrorCodes.TIMEOUT_FINDING_ASSERTIONS, e.errorCode);
  }

  /**
   * Reasonable error message and correct error code for timeOutTrimmingAssertions.
   */
  @Test
  public void timeoutTrimmingAssertionsGetsCorrectMessage() {
    RaireError raireError = new TimeoutTrimmingAssertions();
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Time out trimming assertions"));
    assertEquals(RaireErrorCodes.TIMEOUT_TRIMMING_ASSERTIONS, e.errorCode);
  }

  /**
   * Reasonable error message and correct error code for timeOutCheckingWinner.
   */
  @Test
  public void timeoutCheckingWinnerGetsCorrectMessage() {
    RaireError raireError = new TimeoutCheckingWinner();
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Time out checking winner"));
    assertEquals(RaireErrorCodes.TIMEOUT_CHECKING_WINNER, e.errorCode);
  }

  /**
   * Reasonable error message and correct error code for invalidCandidateNumber.
   * TODO check that this is what happens when the candidate list doesn't match the database.
   * See Issue https://github.com/DemocracyDevelopers/raire-service/issues/66
   */
  @Test
  public void invalidCandidateNumberGetsCorrectMessage() {
    RaireError raireError = new InvalidCandidateNumber();
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Candidate list does not match database"));
    assertEquals(RaireErrorCodes.WRONG_CANDIDATE_NAMES, e.errorCode);
  }

  /**
   * Reasonable error message and correct error code for CouldNotRuleOut.
   * Raire returns this error when it fails to rule out a certain elimination sequence, even though
   * it didn't detect tied winners.  It's possible that this never actually happens, because
   * the tied-winner detection should prevent it.
   */
  @Test
  public void CouldNotRuleOutEliminationSequenceIsProperlyTranscribed() {
    int[] sequence = {1,3,0,2};
    RaireError raireError = new CouldNotRuleOut(sequence);
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Bob, Diego, Alice, Chuan"));
    assertEquals(RaireErrorCodes.COULD_NOT_RULE_OUT_ALTERNATIVE, e.errorCode);
  }

  /**
   * Invalid timeout is an internal error, because an invalid timeout request should be detected
   * at input time and not passed to raire-java.
   */
  @Test
  public void invalidTimeoutIsAnInternalError() {
    RaireError raireError = new InvalidTimeout();
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Internal error"));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, e.errorCode);
  }

  /**
   * Didn't rule out loser is an internal error, because raire-java computes its own winners and
   * losers based on the database, so it shouldn't be inconsistent later with what it initially
   * computed.
   */
  @Test
  public void didntRuleOutLoserIsAnInternalError() {
    RaireError raireError = new InternalErrorDidntRuleOutLoser();
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Internal error"));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, e.errorCode);
  }

  /**
   * Ruled out winner is an internal error, because raire-java computes its own winners and
   * losers based on the database, so it shouldn't be inconsistent later with what it initially
   * computed.
   */
  @Test
  public void ruledOutWinnerIsAnInternalError() {
    RaireError raireError = new InternalErrorRuledOutWinner();
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Internal error"));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, e.errorCode);
  }

  /**
   * Trimming errors are internal raire-java errors.
   */
  @Test
  public void internalErrorTrimmingIsAnInternalError() {
    RaireError raireError = new InternalErrorTrimming();
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Internal error"));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, e.errorCode);
  }

  /**
   * Invalid number of candidates is an internal error, because an invalid candidate list should be detected
   * at input time and not passed to raire-java.
   */
  @Test
  public void invalidNumberOfCandidatesIsAnInternalError() {
    RaireError raireError = new InvalidNumberOfCandidates();
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Internal error"));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, e.errorCode);
  }

  /**
   * Wrong winner is an internal error, because raire-java computes its own winners and
   * losers based on the database, so it shouldn't be inconsistent later with what it initially
   * computed.
   */
  @Test
  public void wrongWinnerIsAnInternalError() {
    RaireError raireError = new WrongWinner(new int[]{});
    RaireServiceException e = new RaireServiceException(raireError, candidates);
    String msg = e.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Internal error"));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, e.errorCode);
  }
}