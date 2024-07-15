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


import static au.org.democracydevelopers.raireservice.testUtils.correctDBAssertionData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.time.TimeTaken;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsSummary;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.GenerateAssertionsSummaryRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.testUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
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
 * Tests to validate the behaviour of Assertion generation on a collection of simple contests with
 * human-computable assertions. Relevant data is preloaded into the test database from
 * src/test/resources/known_testcases_votes.sql.
 * This includes
 * - The examples from the Guide To Raire Vol 2. Exact matching for Ex. 2 and some for Ex. 1.
 * - A very simple example test with two obvious assertions (an NEN and NEB), described below.
 * - A cross-county version of the simple example.
 * - A request for the simple example with twice the totalAuditableBallots as ballots in the database,
 *   to test that the diluted margin and difficulties change by a factor of 2, but absolute margin
 *   stays the same
 * - A request for the simple example with fewer totalAuditableBallots than there are in the database,
 *   to check that there's an appropriate error response.
 * - A request for the simple example with the wrong candidate names, to check that there's an
 *   appropriate error response.
 *   TODO add tests that replacement of summaries happens when expected, i.e. if save is called twice.
 */
@ActiveProfiles("known-testcases")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsServiceKnownTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsServiceKnownTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GenerateAssertionsSummaryRepository summaryRepository;

  @Autowired
  GenerateAssertionsService generateAssertionsService;

  /**
   * Names of contests, to match preloaded data.
   */
  private static final String oneNEBAssertionContest = "Sanity Check NEB Assertion Contest";
  private static final String oneNENAssertionContest = "Sanity Check NEN Assertion Contest";
  private static final String NEBNENAssertionContest = "Sanity Check NEB NEN Assertion Contest";
  private static final String ThreeAssertionContest = "Sanity Check 3 Assertion Contest";
  private static final String guideToRaireExample1 = "Guide To Raire Example 1";
  private static final String guideToRaireExample2 = "Guide To Raire Example 2";
  private static final String simpleContest = "Simple Contest";
  private static final String crossCountySimpleContest = "Cross-county Simple Contest";

  /**
   * Array of candidates: Alice, Bob, Chuan, Diego.
   */
  private static final String[] aliceBobChuanDiego = {"Alice", "Bob", "Chuan", "Diego"};

  /**
   * Array of candidates: Alice, Chuan, Bob.
   */
  private static final String[] aliceChuanBob = {"Alice", "Chuan", "Bob"};

  /**
   * Check that NEB assertion retrieval works. This is just a sanity check.
   */
  @Test
  @Transactional
  void NEBassertionRetrievalWorks() {
    testUtils.log(logger, "NEBassertionRetrievalWorks");
    Assertion assertion = assertionRepository.findByContestName(oneNEBAssertionContest).getFirst();
    assertInstanceOf(NEBAssertion.class, assertion);
    assertTrue(correctDBAssertionData(320, 0.32, 1.1, "Alice",
        "Bob", List.of(), assertion));
  }


  /**
   * Check that NEN assertion retrieval works. This is just a sanity check.
   */
  @Test
  @Transactional
  void NENassertionRetrievalWorks() {
    testUtils.log(logger, "NENassertionRetrievalWorks");
    Assertion assertion = assertionRepository.findByContestName(oneNENAssertionContest).getFirst();

    assertTrue(correctDBAssertionData(20, 0.4, 2.5, "Alice",
        "Bob", Arrays.stream(aliceChuanBob).toList(), assertion));
    assertInstanceOf(NENAssertion.class, assertion);
  }

  /**
   * Some tests of the assertions described in the Guide to Raire, Part 2, Example 1.
   * The test data has 1/500 of the votes, so divide margins by 500.
   * The difficulties should be the same, because both numerator and denominator should be divided by 500.
   * We do not test the NEN assertions because the ones in the Guide have some redundancy.
   * Test assertion: Chuan NEB Bob.
   * Margin is 4000, but data is divided by 500, so 8. Difficulty is 3.375 as in the Guide.
   * Diluted margin is 8/27 = 0.296...
   */
  @Test
  @Transactional
  void testGuideToRairePart2Example1() throws RaireServiceException {
    testUtils.log(logger, "testGuideToRairePart2Example1");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(guideToRaireExample1,
        27, 5, Arrays.stream(aliceBobChuanDiego).toList());

    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertNotNull(response.Ok);
    assertNull(response.Err);

    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner), "Chuan"));

    // Test persistence of assertions generated by raire-java.
    generateAssertionsService.persistAssertionsOrErrors(response, request);

    List<Assertion> assertions = assertionRepository.findByContestName(guideToRaireExample1);

    // There should be one NEB assertion: Chaun NEB Bob
    Optional<Assertion> nebMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();

    assertTrue(correctDBAssertionData(8, 8 / 27.0, 27 / 8.0,
        "Chuan", "Bob", List.of(), nebAssertion));

    // There should be a summary with winner Chuan and no error.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(guideToRaireExample1);
    assertTrue(optSummary.isPresent());
    GenerateAssertionsSummary summary = optSummary.get();
    summary.equalData(guideToRaireExample1, "Chuan","", "", "");
  }

  /**
   * Exact matching of the assertions described in the Guide to Raire Example 2.
   * The test data has 1/1000 of the votes, so divide margins by 1000.
   * The difficulties should be the same, because both numerator and denominator should be divided by 1000.
   */
  @Test
  @Transactional
  void testGuideToRaireExample2() throws RaireServiceException {
    testUtils.log(logger, "testGuideToRairePart2Example2");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(guideToRaireExample2,
        41, 5, Arrays.stream(aliceChuanBob).toList());
    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertNotNull(response.Ok);
    assertNull(response.Err);
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner),"Chuan"));

    // Test persistence of assertions generated by raire-java.
    generateAssertionsService.persistAssertionsOrErrors(response, request);

    List<Assertion> assertions = assertionRepository.findByContestName(guideToRaireExample2);
    checkGuideToRaireExample2Assertions(assertions);

    // Check persistence of summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(guideToRaireExample2);
    assertTrue(optSummary.isPresent());
    GenerateAssertionsSummary summary = optSummary.get();
    summary.equalData(guideToRaireExample2, "Chuan","", "", "");
  }

  /**
   * Simple contest. The votes are
   * 2 (A,B)
   * 2 (B)
   * 1 (C,A).
   * The assertions should be
   * A NEB C
   * - Margin 1, diluted margin 1/5 = 0.2, difficulty 5/1 = 5.
   * A NEN B | {A,B} continuing.
   * - Margin 1, diluted margin 1/5 = 0.2, difficulty 5/1 = 5.
   * Note that A NEB B is not true.
   * This is the single-county case.
   */
  @Test
  @Transactional
  public void simpleContestSingleCounty() throws RaireServiceException {
    testUtils.log(logger, "simpleContestSingleCounty");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        5, 5, Arrays.stream(aliceChuanBob).toList());
    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertNotNull(response.Ok);
    assertNull(response.Err);
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner),
        "Alice"));

    // Test persistence of assertions generated by raire-java.
    generateAssertionsService.persistAssertionsOrErrors(response, request);

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Alice NEB Chaun
    Optional<Assertion> nebMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.2, 5, "Alice",
        "Chuan", List.of(), nebAssertion));

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.2, 5, "Alice",
        "Bob", List.of("Bob","Alice"), nenAssertion));

    // Check persistence of summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertTrue(optSummary.isPresent());
    GenerateAssertionsSummary summary = optSummary.get();
    summary.equalData(simpleContest, "Alice","", "", "");
  }

  /**
   * The same simple contest, but across two counties. Nothing should change.
   */
  @Test
  @Transactional
  public void simpleContestCrossCounty() throws RaireServiceException {
    testUtils.log(logger, "simpleContestCrossCounty");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(crossCountySimpleContest,
        5, 5, Arrays.stream(aliceChuanBob).toList());
    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertNotNull(response.Ok);
    assertNull(response.Err);
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner),
        "Alice"));

    // Test persistence of assertions generated by raire-java.
    generateAssertionsService.persistAssertionsOrErrors(response, request);

    List<Assertion> assertions = assertionRepository.findByContestName(crossCountySimpleContest);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Alice NEB Chaun
    Optional<Assertion> nebMaybeAssertion =
        assertions.stream().filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.2, 5, "Alice",
        "Chuan", List.of(), nebAssertion));

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream().filter(
        a -> a instanceof NENAssertion).findFirst(); assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.2, 5, "Alice",
        "Bob", List.of("Bob","Alice"), nenAssertion));

    // Check persistence of summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(crossCountySimpleContest);
    assertTrue(optSummary.isPresent());
    GenerateAssertionsSummary summary = optSummary.get();
    summary.equalData(crossCountySimpleContest, "Alice","", "", "");
  }

  /**
   * Single-county simple contest again.
   * Doubling the totalAuditableBallots to 10 doubles the difficulty, and halves the diluted margin,
   * but does not change the absolute margins.
   * The actual test data is still the same, with 5 ballots - we just set totalAuditableBallots in
   * the request to 10.
   * We now have 10 totalAuditableBallots, so we expect:
   * A NEB B: Margin 1, diluted margin 1/10 = 0.1, difficulty 10/1 = 10.
   * A NEN B | {A,B} continuing: Margin 1, diluted margin 1/10 = 0.1, difficulty 10/1 = 10.
   */
  @Test
  @Transactional
  public void simpleContestSingleCountyDoubleBallots() throws RaireServiceException {
    testUtils.log(logger, "simpleContestSingleCountyDoubleBallots");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        10, 5, Arrays.stream(aliceChuanBob).toList());
    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertNotNull(response.Ok);
    assertNull(response.Err);
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner),
        "Alice"));

    // Test persistence of assertions generated by raire-java.
    generateAssertionsService.persistAssertionsOrErrors(response, request);
    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Alice NEB Chaun
    Optional<Assertion> nebMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.1, 10, "Alice",
        "Chuan", List.of(), nebAssertion));

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.1, 10, "Alice",
        "Bob", List.of("Bob","Alice"), nenAssertion));

    // Check persistence of summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertTrue(optSummary.isPresent());
    GenerateAssertionsSummary summary = optSummary.get();
    summary.equalData(simpleContest, "Alice","", "", "");
  }

  /**
   * Insufficient totalAuditableBallots causes the right raire error to be returned.
   * This test case has 5 ballots, so 2 totalAuditableBallots is an error.
   */
  @Test
  @Transactional
  public void simpleContestSingleCountyInsufficientBallotsError() {
    testUtils.log(logger, "simpleContestSingleCountyInsufficientBallotsError");
    GenerateAssertionsRequest notEnoughBallotsRequest = new GenerateAssertionsRequest(simpleContest,
        2, 5, Arrays.stream(aliceChuanBob).toList());
    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(notEnoughBallotsRequest)
    );
    assertSame(ex.errorCode, RaireErrorCode.INVALID_TOTAL_AUDITABLE_BALLOTS);

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

  /**
   * Candidate names that don't match the database cause the right exception.
   * Note that it's OK if the _list_ is different, as long as none of the actual votes contain
   * unexpected names.
   * This test case has "Alice", "Bob", "Chuan".
   */
  @Test
  @Transactional
  public void WrongCandidatesThrowsException() {
    testUtils.log(logger, "WrongCandidatesThrowsException");
    String[] wrongCandidates = {"Alicia", "Chuan", "Boba"};
    GenerateAssertionsRequest wrongCandidatesRequest = new GenerateAssertionsRequest(simpleContest,
        5, 5, Arrays.stream(wrongCandidates).toList());
    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(wrongCandidatesRequest)
    );
    assertSame(ex.errorCode, RaireErrorCode.WRONG_CANDIDATE_NAMES);

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

  /**
   * Test persistAssertions when passed invalid data: negative universe size for an
   * NEB assertion. A RaireServiceException should be thrown.
   */
  @Test
  @Transactional
  void testPersistWithNegativeUniverseSize(){
    testUtils.log(logger, "testPersistWithNegativeUniverseSize");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        -1, 5, Arrays.stream(aliceChuanBob).toList());

    RaireResult result = createTestRaireResultNEB(20, 0, 1);
    RaireResultOrError resultOrError = new RaireResultOrError(result);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(resultOrError, request));

    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "invalid arguments"));

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(0, assertions.size());

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

  /**
   * Test persistAssertions when passed invalid data: zero universe size for an
   * NEB assertion. A RaireServiceException should be thrown.
   */
  @Test
  @Transactional
  void testPersistWithZeroUniverseSize(){
    testUtils.log(logger, "testPersistWithZeroUniverseSize");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        0, 5, Arrays.stream(aliceChuanBob).toList());

    RaireResult result = createTestRaireResultNEB(21, 1, 0);
    RaireResultOrError resultOrError = new RaireResultOrError(result);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(resultOrError, request));

    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "invalid arguments"));

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(0, assertions.size());

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

  /**
   * Test persistAssertions when passed invalid data: an assertion with a negative margin.
   * A RaireServiceException should be thrown.
   */
  @Test
  @Transactional
  void testPersistWithNegativeMargin(){
    testUtils.log(logger, "testPersistWithNegativeMargin");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        100, 5, Arrays.stream(aliceChuanBob).toList());

    RaireResult result = createTestRaireResultNEB(-20, 1, 0);
    RaireResultOrError resultOrError = new RaireResultOrError(result);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(resultOrError, request));

    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "non-negative margin"));

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(0, assertions.size());

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

   /**
    * Test persistAssertions when passed invalid data: an assertion with a margin that is larger
    * than the total number of auditable ballots. A RaireServiceException should be thrown.
    */
  @Test
  @Transactional
  void testPersistWithTooHighMargin(){
    testUtils.log(logger, "testPersistWithTooHighMargin");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        100, 5, Arrays.stream(aliceChuanBob).toList());

    RaireResult result = createTestRaireResultNEB(120, 1, 0);
    RaireResultOrError resultOrError = new RaireResultOrError(result);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(resultOrError, request));

    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "less than universe size"));

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(0, assertions.size());

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

  /**
   * Test persistAssertions when passed invalid data: an assertion with the same winner and loser.
   * A RaireServiceException should be thrown.
   */
  @Test
  @Transactional
  void testPersistWithAssertionSameWinnerAndLoser(){
    testUtils.log(logger, "testPersistWithAssertionSameWinnerAndLoser");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        100, 5, Arrays.stream(aliceChuanBob).toList());

    RaireResult result = createTestRaireResultNEB(20, 1, 1);
    RaireResultOrError resultOrError = new RaireResultOrError(result);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(resultOrError, request));

    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "must not be the same candidate"));

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(0, assertions.size());

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

  /**
   * Test persistAssertions when passed invalid data: an NEN assertion with invalid combination
   * of winner, loser, and continuing candidate list (winner not a continuing candidate).
   * A RaireServiceException should be thrown.
   */
  @Test
  @Transactional
  void testPersistWithAssertionWinnerNotContinuing(){
    testUtils.log(logger, "testPersistWithAssertionWinnerNotContinuing");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        100, 5, Arrays.stream(aliceChuanBob).toList());

    RaireResult result = createTestRaireResultNEN(19, 1, 0, new int[]{0,2});
    RaireResultOrError resultOrError = new RaireResultOrError(result);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(resultOrError, request));

    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "must also be continuing"));

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(0, assertions.size());

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

  /**
   * Test persistAssertions when passed invalid data: an NEN assertion with invalid combination
   * of winner, loser, and continuing candidate list (loser not a continuing candidate).
   * A RaireServiceException should be thrown.
   */
  @Test
  @Transactional
  void testPersistWithAssertionLoserNotContinuing(){
    testUtils.log(logger, "testPersistWithAssertionLoserNotContinuing");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        100, 5, Arrays.stream(aliceChuanBob).toList());

    RaireResult result = createTestRaireResultNEN(20, 1, 0, new int[]{1,2});
    RaireResultOrError resultOrError = new RaireResultOrError(result);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(resultOrError, request));

    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "must also be continuing"));

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(0, assertions.size());

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

  /**
   * Test persistAssertions when passed invalid data: an NEN assertion with invalid combination
   * of winner, loser, and continuing candidate list (winner index out of bounds).
   * A RaireServiceException should be thrown.
   */
  @Test
  @Transactional
  void testPersistWithAssertionWinnerOutOfBounds(){
    testUtils.log(logger, "testPersistWithAssertionWinnerOutOfBounds");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        100, 5, Arrays.stream(aliceChuanBob).toList());

    RaireResult result = createTestRaireResultNEB(20, 3, 0);
    RaireResultOrError resultOrError = new RaireResultOrError(result);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(resultOrError, request));

    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "index out of bounds"));

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(0, assertions.size());

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }

  /**
   * Test persistAssertions when passed invalid data: an NEN assertion with invalid combination
   * of winner, loser, and continuing candidate list (loser index out of bounds).
   * A RaireServiceException should be thrown.
   */
  @Test
  @Transactional
  void testPersistWithAssertionLoserOutOfBounds(){
    testUtils.log(logger, "testPersistWithAssertionLoserOutOfBounds");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        100, 5, Arrays.stream(aliceChuanBob).toList());

    RaireResult result = createTestRaireResultNEB(19, 1, 3);
    RaireResultOrError resultOrError = new RaireResultOrError(result);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.persistAssertionsOrErrors(resultOrError, request));

    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "index out of bounds"));

    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(0, assertions.size());

    // Check there is no summary data.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(simpleContest);
    assertFalse(optSummary.isPresent());
  }


  /**
   * Create and return a test RaireResult for use in testing persistAssertions with varying
   * types of invalid assertion generation requests or results. This method returns a RaireResult
   * containing one NEB assertion.
   */
  RaireResult createTestRaireResultNEB(int margin, int winner, int loser){
    AssertionAndDifficulty aadAliceNEBBob = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedBefore(winner, loser),
        1.1, margin);

    AssertionAndDifficulty[] assertions = new AssertionAndDifficulty[]{aadAliceNEBBob};

    TimeTaken zeros = new TimeTaken(0,0);
    return new RaireResult(assertions, 1.1, margin, winner,
        4, zeros, zeros, zeros, false);
  }

  /**
   * Create and return a test RaireResult for use in testing persistAssertions with varying
   * types of invalid assertion generation requests or results. This method returns a RaireResult
   * containing one NEN assertion.
   */
  RaireResult createTestRaireResultNEN(int margin, int winner, int loser, int[] continuing){
    AssertionAndDifficulty aadAliceNENBob = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedNext(winner, loser, continuing),
        1.1, margin);

    AssertionAndDifficulty[] assertions = new AssertionAndDifficulty[]{aadAliceNENBob};

    TimeTaken zeros = new TimeTaken(0,0);
    return new RaireResult(assertions, 1.1, margin, winner,
        4, zeros, zeros, zeros, false);
  }


  /**
   * Test that if a contest already has one assertion in the database, it will be replaced
   * with newly generated assertions.
   */
  @Test
  @Transactional
  void replaceOneAssertion() throws RaireServiceException {
    testUtils.log(logger, "replaceOneAssertion");
    // Test replacement of assertions generated by raire-java.
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(oneNEBAssertionContest,
        41, 5, Arrays.stream(aliceChuanBob).toList());

    // Create a RaireResult containing assertions to persist.
    RaireResult result = createTestRaireResultRaireGuide2();
    RaireResultOrError resultOrError = new RaireResultOrError(result);
    generateAssertionsService.persistAssertionsOrErrors(resultOrError, request);

    List<Assertion> assertions = assertionRepository.findByContestName(oneNEBAssertionContest);
    checkGuideToRaireExample2Assertions(assertions);
  }


  /**
   * Test that if a contest already has two assertions in the database, they will be replaced
   * with newly generated assertions.
   */
  @Test
  @Transactional
  void replaceTwoAssertions() throws RaireServiceException {
    testUtils.log(logger, "replaceTwoAssertions");
    // Test replacement of assertions generated by raire-java.
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(NEBNENAssertionContest,
        41, 5, Arrays.stream(aliceChuanBob).toList());

    // Create a RaireResult containing assertions to persist.
    RaireResult result = createTestRaireResultRaireGuide2();
    RaireResultOrError resultOrError = new RaireResultOrError(result);
    generateAssertionsService.persistAssertionsOrErrors(resultOrError, request);

    List<Assertion> assertions = assertionRepository.findByContestName(NEBNENAssertionContest);
    checkGuideToRaireExample2Assertions(assertions);
  }


  /**
   * Test that if a contest already has some assertions in the database, they will be replaced
   * with newly generated assertions (where the new set is smaller).
   */
  @Test
  @Transactional
  void replaceLessAssertions() throws RaireServiceException {
    testUtils.log(logger, "replaceLessAssertions");
    // Test replacement of assertions generated by raire-java.
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(ThreeAssertionContest,
        41, 5, Arrays.stream(aliceChuanBob).toList());

    // Create a RaireResult containing assertions to persist.
    RaireResult result = createTestRaireResultRaireGuide2();
    RaireResultOrError resultOrError = new RaireResultOrError(result);
    generateAssertionsService.persistAssertionsOrErrors(resultOrError, request);

    List<Assertion> assertions = assertionRepository.findByContestName(ThreeAssertionContest);
    checkGuideToRaireExample2Assertions(assertions);
  }


  /**
   * Check that the given list of assertions contains all those that should be generated for
   * the second Guide to Raire example.
   */
  void checkGuideToRaireExample2Assertions(List<Assertion> assertions){
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Chaun NEB Alice
    // Margin is 10,000, but data is divided by 1000, so 10. Difficulty is 4.1 as in the Guide.
    // Diluted Margin is 10/41.
    Optional<Assertion> nebMaybeAssertion =
        assertions.stream().filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctDBAssertionData(10, 10 / 41.0, 4.1,
        "Chuan","Alice", List.of(), nebAssertion));

    // There should be one NEN assertion: Chuan > Bob if only {Chuan,Bob} remain.
    // Margin is 9,000, but data is divided by 1000, so 9. Difficulty is 41/9 = 4.5555...,
    // rounded to 4.6 in the Guide.
    // Diluted margin is 9/41 = 0.219512195...
    Optional<Assertion> nenMaybeAssertion =
        assertions.stream().filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctDBAssertionData(9, 9/41.0, 41.0/9,
        "Chuan", "Bob", List.of("Chuan","Bob"), nenAssertion));
  }



  /**
   * Create and return a RaireResult containing assertions that would be generated for second
   * Guide to Raire example.
   */
  RaireResult createTestRaireResultRaireGuide2(){
    AssertionAndDifficulty aadChuanNEBAlice = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedBefore(1, 0),
        4.1, 10);

    AssertionAndDifficulty aadChuanNENBob = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedNext(1, 2,
        new int[]{1,2}), 41/9.0, 9);

    AssertionAndDifficulty[] assertions = new AssertionAndDifficulty[]{aadChuanNEBAlice,
        aadChuanNENBob};

    TimeTaken zeros = new TimeTaken(0,0);
    return new RaireResult(assertions, 41/9.0, 9, 0,
        3, zeros, zeros, zeros, false);
  }

}
