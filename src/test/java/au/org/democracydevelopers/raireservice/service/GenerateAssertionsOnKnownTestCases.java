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
import static au.org.democracydevelopers.raireservice.testUtils.correctAssumedContinuing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCodes;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests to validate the behaviour of Assertion generation on a collection of simple contest with
 * human-computable assertions. Relevant data is preloaded into the test database from
 * src/test/resources/known_testcases_votes.sql.
 * This includes
 * - A contest with tied winners, to check that the correct error is produced.
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
 */
@ActiveProfiles("known-testcases")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsOnKnownTestCases {

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GenerateAssertionsService generateAssertionsService;

  // error allowed when comparing doubles.
  private static final double EPS = 0.0000000001;

  /**
   * Names of contests, to match pre-loaded data.
   */
  private static final String oneNEBAssertionContest = "Sanity Check NEB Assertion Contest";
  private static final String oneNENAssertionContest = "Sanity Check NEN Assertion Contest";
  private static final String guideToRaireExample1 = "Guide To Raire Example 1";
  private static final String guideToRaireExample2 = "Guide To Raire Example 2";
  private static final String tiedWinnersContest = "Tied Winners Contest";
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

  private final static GenerateAssertionsRequest tiedWinnersRequest
      = new GenerateAssertionsRequest(tiedWinnersContest, 2, 5,
      Arrays.stream(aliceChuanBob).toList());

  /**
   * Check that NEB assertion retrieval works. This is just a sanity check for the other tests.
   * TODO This can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void NEBassertionRetrievalWorks() {

    Assertion assertion = assertionRepository.findByContestName(oneNEBAssertionContest).getFirst();
    assertInstanceOf(NEBAssertion.class, assertion);
    assertTrue(correctDBAssertionData(320, 0.32, 1.1, "Alice",
        "Bob", assertion, EPS));
  }

  /**
   * Check that NEN assertion retrieval works. This is just a sanity check for the other tests.
   * TODO This can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void NENassertionRetrievalWorks() {

    Assertion assertion = assertionRepository.findByContestName(oneNENAssertionContest).getFirst();

    assertTrue(correctDBAssertionData(20, 0.4, 2.5, "Alice",
        "Bob", assertion, EPS));
    assertTrue(correctAssumedContinuing(Arrays.stream(aliceChuanBob).toList(), assertion));
    assertInstanceOf(NENAssertion.class, assertion);
  }


  /**
   * Trivial test to see whether the placeholder service throws the expected placeholder exception.
   * TODO This can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void dummyServiceThrowsException() {
    assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(tiedWinnersRequest)
    );
  }

  /**
   * Tied winners throws the right exception.
   * This is a super-simple election with two candidates with one vote each.
   */
  @Test
  @Transactional
  @Disabled
  void tiedWinnersThrowsTiedWinnersException() {
    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(tiedWinnersRequest)
    );
    String msg = ex.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg,"Tied winners"));
    assertTrue(StringUtils.containsIgnoreCase(msg,"Alice"));
    assertTrue(StringUtils.containsIgnoreCase(msg,"Bob"));
    assertSame(ex.errorCode, RaireErrorCodes.TIED_WINNERS);
  }

  /**
   * Some tests of the assertions described in the Guide to Raire Example 1.
   * The test data has 1/500 of the votes, so divide margins by 500.
   * The difficulties should be the same, because both numerator and denominator should be divided by 500.
   * We do not test the NEN assertions because the ones in the Guide have some redundancy.
   * Test assertion: Alice NEB Bob.
   * Margin is 4000, but data is divided by 500, so 8. Difficulty is 3.375 as in the Guide.
   * Diluted margin is 8/27 = 0.296...
   */
  @Test
  @Transactional
  @Disabled
  void testGuideToRaireExample1() throws RaireServiceException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(guideToRaireExample1,
        27, 5, Arrays.stream(aliceBobChuanDiego).toList());
    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner), "Chuan"));
    List<Assertion> assertions = assertionRepository.findByContestName(guideToRaireExample2);

    // There should be one NEB assertion: Chaun NEB Bob
    Optional<Assertion> nebMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();

    assertTrue(correctDBAssertionData(4000, 8 / 27.0, 27 / 8.0,
        "Chuan", "Bob", nebAssertion, EPS));
  }

  /**
   * Exact matching of the assertions described in the Guide to Raire Example 2.
   * The test data has 1/1000 of the votes, so divide margins by 1000.
   * The difficulties should be the same, because both numerator and denominator should be divided by 1000.
   */
  @Test
  @Transactional
  @Disabled
  void testGuideToRaireExample2() throws RaireServiceException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(guideToRaireExample2,
        41, 5, Arrays.stream(aliceChuanBob).toList());
    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner),"Chuan"));
    List<Assertion> assertions = assertionRepository.findByContestName(guideToRaireExample2);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Chaun NEB Alice
    // Margin is 10,000, but data is divided by 1000, so 10. Difficulty is 4.1 as in the Guide.
    // Diluted Margin is 10/41.
    Optional<Assertion> nebMaybeAssertion =
        assertions.stream().filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctDBAssertionData(10, 10 / 41.0, 4.1,
        "Chuan","Alice",nebAssertion, EPS));

    // There should be one NEN assertion: Chuan > Bob if only {Chuan,Bob} remain.
    // Margin is 9,000, but data is divided by 1000, so 9. Difficulty is 41/9 = 4.5555...,
    // rounded to 4.6 in the Guide.
    // Diluted margin is 9/41 = 0.219512195...
    Optional<Assertion> nenMaybeAssertion =
        assertions.stream().filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctDBAssertionData(9, 9/41.0, 41.0/9,
        "Chuan", "Bob", nenAssertion, EPS));
    assertTrue(correctAssumedContinuing(List.of("Chuan","Bob"), nenAssertion));
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
  @Disabled
  public void simpleContestSingleCounty() throws RaireServiceException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        5, 5, Arrays.stream(aliceChuanBob).toList());
    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner), "Alice"));
    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Alice NEB Chaun
    Optional<Assertion> nebMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.2, 5, "Alice",
        "Chuan", nebAssertion, EPS));

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.2, 5, "Alice",
        "Bob", nenAssertion, EPS));
    assertTrue(correctAssumedContinuing(List.of("Bob","Alice"), nenAssertion));
  }

  /**
   * The same simple contest, but across two counties. Nothing should change.
   */
  @Test
  @Transactional
  @Disabled
  public void simpleContestCrossCounty() throws RaireServiceException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(crossCountySimpleContest,
        5, 5, Arrays.stream(aliceChuanBob).toList());
    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner), "Alice"));
    List<Assertion> assertions = assertionRepository.findByContestName(crossCountySimpleContest);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Alice NEB Chaun
    Optional<Assertion> nebMaybeAssertion =
        assertions.stream().filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.2, 5, "Alice",
        "Chuan", nebAssertion, EPS));

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream().filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.2, 5, "Alice",
        "Bob", nenAssertion, EPS));
    assertTrue(correctAssumedContinuing(List.of("Bob","Alice"), nenAssertion));

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
  @Disabled
  public void simpleContestSingleCountyDoubleBallots() throws RaireServiceException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        10, 5, Arrays.stream(aliceChuanBob).toList());
    RaireResultOrError response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(response.Ok.winner), "Alice"));
    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Alice NEB Chaun
    Optional<Assertion> nebMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.1, 10, "Alice",
        "Chuan", nebAssertion, EPS));

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctDBAssertionData(1, 0.1, 10, "Alice",
        "Bob", nenAssertion, EPS));
    assertTrue(correctAssumedContinuing(List.of("Bob","Alice"), nenAssertion));
  }

  /**
   * Insufficient totalAuditableBallots causes the right exception.
   * This test case has 5 ballots, so 2 totalAuditableBallots is an error.
   */
  @Test
  @Transactional
  @Disabled
  public void simpleContestSingleCountyInsuffientBallotsThrowsException() {
    GenerateAssertionsRequest notEnoughBallotsRequest = new GenerateAssertionsRequest(simpleContest,
        2, 5, Arrays.stream(aliceChuanBob).toList());
    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(notEnoughBallotsRequest)
    );
    String msg = ex.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Error"));
    assertSame(ex.errorCode, RaireErrorCodes.INTERNAL_ERROR);
  }

  /**
   * Candidate names that don't match the database cause the right exception.
   * Note that it's OK if the _list_ is different, as long as none of the actual votes contain
   * unexpected names.
   * This test case has "Alice", "Bob", "Chuan".
   */
  @Test
  @Transactional
  @Disabled
  public void WrongCandidatesThrowsException() {
    String[] wrongCandidates = {"Alicia", "Chuan", "Boba"};
    GenerateAssertionsRequest wrongCandidatesRequest = new GenerateAssertionsRequest(simpleContest,
        5, 5, Arrays.stream(wrongCandidates).toList());
    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(wrongCandidatesRequest)
    );
    String msg = ex.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Candidate list"));
    assertSame(ex.errorCode, RaireErrorCodes.WRONG_CANDIDATE_NAMES);
  }


}
