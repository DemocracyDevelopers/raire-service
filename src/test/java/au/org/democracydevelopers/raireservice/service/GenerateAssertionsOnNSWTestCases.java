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


import static au.org.democracydevelopers.raireservice.testUtils.correctAssertionData;
import static au.org.democracydevelopers.raireservice.testUtils.correctAssumedContinuing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.service.GenerateAssertionsException.RaireErrorCodes;
import java.math.BigDecimal;
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
 * Tests to validate the behaviour of Assertion generation on NSW 2021 Mayoral election data.
 * src/test/resources/NSW2021Data/Bellingen_Mayoral.sql.
 */
@ActiveProfiles("nsw-testcases")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsOnNSWTestCases {

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GenerateAssertionsService generateAssertionsService;

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
      = new GenerateAssertionsRequest(tiedWinnersContest, 2, 5, Arrays.stream(aliceChuanBob).toList());

  /**
   * Trivial test to see whether the placeholder service throws the expected placeholder exception.
   * TODO This can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void dummyServiceThrowsException() {
    assertThrows(GenerateAssertionsException.class, () ->
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
    GenerateAssertionsException ex = assertThrows(GenerateAssertionsException.class, () ->
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
  void testGuideToRaireExample1() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(guideToRaireExample1,
        27, 5, Arrays.stream(aliceBobChuanDiego).toList());
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(response.winner, "Chuan"));
    List<Assertion> assertions = assertionRepository.findByContestName(guideToRaireExample2);

    // There should be one NEB assertion: Chaun NEB Bob
    Optional<Assertion> nebMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();

    assertTrue(correctAssertionData(4000, BigDecimal.valueOf(8 / 27.0), BigDecimal.valueOf(27 / 8.0),
        "Chuan", "Bob", nebAssertion));
  }

  /**
   * Exact matching of the assertions described in the Guide to Raire Example 2.
   * The test data has 1/1000 of the votes, so divide margins by 1000.
   * The difficulties should be the same, because both numerator and denominator should be divided by 1000.
   */
  @Test
  @Transactional
  @Disabled
  void testGuideToRaireExample2() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(guideToRaireExample2,
        41, 5, Arrays.stream(aliceChuanBob).toList());
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(response.winner,"Chuan"));
    List<Assertion> assertions = assertionRepository.findByContestName(guideToRaireExample2);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Chaun NEB Alice
    // Margin is 10,000, but data is divided by 1000, so 10. Difficulty is 4.1 as in the Guide.
    // Diluted Margin is 10/41.
    Optional<Assertion> nebMaybeAssertion = assertions.stream().filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctAssertionData(10, BigDecimal.valueOf(10 / 41.0), BigDecimal.valueOf(4.1),
        "Chuan","Alice",nebAssertion));

    // There should be one NEN assertion: Chuan > Bob if only {Chuan,Bob} remain.
    // Margin is 9,000, but data is divided by 1000, so 9. Difficulty is 41/9 = 4.5555...,
    // rounded to 4.6 in the Guide.
    // Diluted margin is 9/41 = 0.219512195...
    // TODO check how many dp are serialised and refine test accordingly.
    Optional<Assertion> nenMaybeAssertion = assertions.stream().filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctAssertionData(9, BigDecimal.valueOf(9/41.0), BigDecimal.valueOf(41.0/9),
        "Chuan", "Bob", nenAssertion));
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
  public void simpleContestSingleCounty() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        5, 5, Arrays.stream(aliceChuanBob).toList());
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(response.winner, "Alice"));
    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Alice NEB Chaun
    Optional<Assertion> nebMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctAssertionData(1, BigDecimal.valueOf(0.2), BigDecimal.valueOf(5),
        "Alice","Chuan", nebAssertion));

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctAssertionData(1, BigDecimal.valueOf(0.2), BigDecimal.valueOf(5),
        "Alice","Bob", nenAssertion));
    assertTrue(correctAssumedContinuing(List.of("Bob","Alice"), nenAssertion));
  }

  /**
   * The same simple contest, but across two counties. Nothing should change.
   */
  @Test
  @Transactional
  @Disabled
  public void simpleContestCrossCounty() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(crossCountySimpleContest,
        5, 5, Arrays.stream(aliceChuanBob).toList());
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(response.winner, "Alice"));
    List<Assertion> assertions = assertionRepository.findByContestName(crossCountySimpleContest);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Alice NEB Chaun
    Optional<Assertion> nebMaybeAssertion = assertions.stream().filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctAssertionData(1, BigDecimal.valueOf(0.2), BigDecimal.valueOf(5),
        "Alice","Chuan", nebAssertion));

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream().filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctAssertionData(1, BigDecimal.valueOf(0.2), BigDecimal.valueOf(5),
        "Alice","Bob", nenAssertion));
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
  public void simpleContestSingleCountyDoubleBallots() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(simpleContest,
        10, 5, Arrays.stream(aliceChuanBob).toList());
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);
    assertTrue(StringUtils.containsIgnoreCase(response.winner, "Alice"));
    List<Assertion> assertions = assertionRepository.findByContestName(simpleContest);
    assertEquals(2, assertions.size());

    // There should be one NEB assertion: Alice NEB Chaun
    Optional<Assertion> nebMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    assertTrue(correctAssertionData(1, BigDecimal.valueOf(0.1), BigDecimal.valueOf(10),
        "Alice","Chuan", nebAssertion));

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    assertTrue(correctAssertionData(1, BigDecimal.valueOf(0.1), BigDecimal.valueOf(10),
        "Alice","Bob", nenAssertion));
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
    GenerateAssertionsException ex = assertThrows(GenerateAssertionsException.class, () ->
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
    GenerateAssertionsException ex = assertThrows(GenerateAssertionsException.class, () ->
        generateAssertionsService.generateAssertions(wrongCandidatesRequest)
    );
    String msg = ex.getMessage();
    assertTrue(StringUtils.containsIgnoreCase(msg, "Candidate list"));
    assertSame(ex.errorCode, RaireErrorCodes.WRONG_CANDIDATE_NAMES);
  }


}
