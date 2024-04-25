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


import static org.apache.commons.lang3.StringUtils.substring;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
 * Tests to validate the behaviour of Assertion retrieval and storage. Assertions and other
 * relevant data is preloaded into the test database from src/test/resources/data.sql.
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

  /**
   * To facilitate easier checking of retrieved/saved assertion content.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  /**
   * Names of contests, to match pre-loaded data.
   */
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

  /**
   * All the initial data for an assertion, which is the same for them all.
   */
  private final static String genericInitialAssertionState
      = "\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0," +
      "\"optimisticSamplesToAudit\":0,\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0," +
      "\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Alice NEB Bob, for "Guide To Raire Example 1".
   * Margin is 4000, but data is divided by 500, so 8. Difficulty is 3.375 as in the Guide.
   * Diluted margin is 8/27 = 0.296...
   * TODO check how many dp are serialised and refine test string accordingly.
   */
  private final static String chuanNEBBob = "{\"id\":4,\"version\":0,\"contestName\":" +
      "\""+guideToRaireExample1+"\",\"winner\":\"Chuan\",\"loser\":\"Bob\"," +
      "\"margin\":8,\"difficulty\":3.375,\"assumedContinuing\":[],\"dilutedMargin\":0.2962962962962963, +"
      + genericInitialAssertionState;

  /**
   * Test assertion: Chuan NEN Bob assuming Bob and Chuan are continuing, for "Guide To Raire Example 2".
   * Margin is 9,000, but data is divided by 1000, so 9. Difficulty is 41/9 = 4.5555...,
   * rounded to 4.6 in the Guide.
   * Diluted margin is 9/41 = 0.219512195...
   * TODO check how many dp are serialised and refine test string accordingly.
   * We need two different orders for the 'assumed continuing' array because we're not sure what order
   * will be generated.
   */
  private final static String chuanNENBob = "{\"id\":4,\"version\":0,\"contestName\":" +
      "\""+guideToRaireExample2+"\",\"winner\":\"Chuan\",\"loser\":\"Bob\"," +
      "\"margin\":9,\"difficulty\":4.5555555555555555,\"assumedContinuing\":";

  private final static String chuanNENBobOrder1 = chuanNENBob + "[\"Chuan\",\"Bob\"],"+
      "\"dilutedMargin\":0.219512195121951,"+genericInitialAssertionState;
  private final static String chuanNENBobOrder2 = chuanNENBob + "[\"Bob\",\"Chuan\"],"+
      "\"dilutedMargin\":0.219512195121951,"+genericInitialAssertionState;

  /**
   * Test assertion: Chuan NEB Alice in contest "Guide To Raire Example 2".
   * Margin is 10,000, but data is divided by 1000, so 10. Difficulty is 4.1 as in the Guide.
   */
  private final static String chuanNEBAlice = "{\"id\":5,\"version\":0,\"contestName\":" +
      "\""+guideToRaireExample2+"\",\"winner\":\"Chuan\",\"loser\":\"Alice\"," +
      "\"margin\":10,\"difficulty\":4.1,\"assumedContinuing\":[],\"dilutedMargin\":0.01," +
      "\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0,\"optimisticSamplesToAudit\":0," +
      "\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0," +
      "\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Alice NEB Chuan, in 'Simple Contest'.
   * Margin is 1. Diluted Margin 1/5 = 0.2, difficulty 5/1 = 5.
   *
   */
  private final static String aliceNEBChuanSimpleContest = "{\"id\":4,\"version\":0,\"contestName\":" +
      "\""+simpleContest+"\",\"winner\":\"Alice\",\"loser\":\"Chuan\"," +
      "\"margin\":1,\"difficulty\":5,\"assumedContinuing\":[],\"dilutedMargin\":0.2, +"
      + genericInitialAssertionState;

  /**
   * Test assertion: Alice NEB Chuan, in 'Cross County Simple Contest'.
   * Margin is 1. Diluted Margin 1/5 = 0.2, difficulty 5/1 = 5.
   *
   */
  private final static String aliceNEBChuanCrossCountySimpleContest =
      "{\"id\":4,\"version\":0,\"contestName\":" +
      "\""+crossCountySimpleContest+"\",\"winner\":\"Alice\",\"loser\":\"Chuan\"," +
      "\"margin\":1,\"difficulty\":5,\"assumedContinuing\":[],\"dilutedMargin\":0.2, +"
      + genericInitialAssertionState;


  /**
   * Test assertion: Alice NEN Bob assuming Bob and Alice are continuing, for "Simple Contest".
   * Margin is 1, diluted margin is 1/5 = 0.2, difficulty = 5/1 = 5.
   * We need two different orders for the 'assumed continuing' array because we're not sure what order
   * will be generated.
   */
  private final static String aliceNENBob = "{\"id\":4,\"version\":0,\"contestName\":" +
      "\""+simpleContest+"\",\"winner\":\"Alice\",\"loser\":\"Bob\"," +
      "\"margin\":1,\"difficulty\":5,\"assumedContinuing\":";

  private final static String aliceNENBobOrder1 = aliceNENBob + "[\"Alice\",\"Bob\"],"+
      "\"dilutedMargin\":0.2,"+genericInitialAssertionState;
  private final static String aliceNENBobOrder2 = aliceNENBob + "[\"Bob\",\"Alice\"],"+
      "\"dilutedMargin\":0.2,"+genericInitialAssertionState;

  /**
   * Test assertion - the same, but for cross-county simple contest.
   * Alice NEN Bob assuming Bob and Alice are continuing, for "Simple Contest".
   * Margin is 1, diluted margin is 1/5 = 0.2, difficulty = 5/1 = 5.
   */
  private final static String aliceNENBobCrossCounty = "{\"id\":4,\"version\":0,\"contestName\":" +
      "\""+crossCountySimpleContest+"\",\"winner\":\"Alice\",\"loser\":\"Bob\"," +
      "\"margin\":1,\"difficulty\":5,\"assumedContinuing\":";

  private final static String aliceNENBobOrder1CrossCounty = aliceNENBobCrossCounty + "[\"Alice\",\"Bob\"],"+
      "\"dilutedMargin\":0.2,"+genericInitialAssertionState;
  private final static String aliceNENBobOrder2CrossCounty = aliceNENBobCrossCounty + "[\"Bob\",\"Alice\"],"+
      "\"dilutedMargin\":0.2,"+genericInitialAssertionState;

  /**
   * Test assertion - the same, but for cross-county simple contest.
   * Alice NEN Bob assuming Bob and Alice are continuing, for "Simple Contest".
   * Margin is 1, diluted margin is 1/5 = 0.2, difficulty = 5/1 = 5.
   */



  private final static GenerateAssertionsRequest tiedWinnersRequest
      = new GenerateAssertionsRequest(tiedWinnersContest, 2, 5, Arrays.stream(aliceChuanBob).toList());

  /**
   * Trivial test to see whether the placeholder service throws the expected placeholder exception.
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
    // Remove the id prefix, because we don't know what the assertion id will be.
    String expectedNEBStringWithoutID = chuanNEBBob.substring(chuanNEBBob.indexOf(','));
    String retrievedString = GSON.toJson(nebAssertion);
    String retrievedStringWithoutID = retrievedString.substring(retrievedString.indexOf(','));
    assertEquals(expectedNEBStringWithoutID, retrievedStringWithoutID);
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
    Optional<Assertion> nebMaybeAssertion = assertions.stream().filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();
    // Remove the id prefix, because we don't know what the assertion id will be.
    String expectedNEBStringWithoutID = chuanNEBAlice.substring(chuanNEBAlice.indexOf(','));
    String retrievedString =   GSON.toJson(nebAssertion);
    String retrievedStringWithoutID =  retrievedString.substring(retrievedString.indexOf(','));
    assertEquals(expectedNEBStringWithoutID, retrievedStringWithoutID);

    // There should be one NEN assertion: Chuan > Bob if only {Chuan,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream().filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    // Remove the id prefix, because we don't know what the assertion id will be.
    String expectedString1WithoutID = chuanNENBobOrder1.substring(chuanNENBobOrder1.indexOf(','));
    String expectedString2WithoutID = chuanNENBobOrder2.substring(chuanNENBobOrder2.indexOf(','));
    String retrievedNENString =   GSON.toJson(nenAssertion);
    String retrievedNENStringWithoutID =  retrievedNENString.substring(retrievedNENString.indexOf(','));
    // We're not sure what order the 'assumed equals' list is in, but it should match one of them.
    assertTrue(expectedString1WithoutID.equals(retrievedNENStringWithoutID)
        || expectedString2WithoutID.equals(retrievedNENStringWithoutID));




  }

  /**
   * Simple contest. The votes are
   * 2 (A,B)
   * 2 (B)
   * 1 (C,A).
   * The assertions should be
   * A NEB C
   * A NEN B | {A,B} continuing.
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
    // Remove the id prefix, because we don't know what the assertion id will be.
    String expectedNEBStringWithoutID
        = aliceNEBChuanSimpleContest.substring(aliceNEBChuanSimpleContest.indexOf(','));
    String retrievedString = GSON.toJson(nebAssertion);
    String retrievedStringWithoutID = retrievedString.substring(retrievedString.indexOf(','));
    assertEquals(expectedNEBStringWithoutID, retrievedStringWithoutID);

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    // Remove the id prefix, because we don't know what the assertion id will be.
    String expectedString1WithoutID = aliceNENBobOrder1.substring(aliceNENBobOrder1.indexOf(','));
    String expectedString2WithoutID = aliceNENBobOrder2.substring(aliceNENBobOrder2.indexOf(','));
    String retrievedNENString = GSON.toJson(nenAssertion);
    String retrievedNENStringWithoutID = retrievedNENString.substring(
        retrievedNENString.indexOf(','));
    // We're not sure what order the 'assumed equals' list is in, but it should match one of them.
    assertTrue(expectedString1WithoutID.equals(retrievedNENStringWithoutID)
        || expectedString2WithoutID.equals(retrievedNENStringWithoutID));
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
    // Remove the id prefix, because we don't know what the assertion id will be.
    String expectedNEBStringWithoutID
        = aliceNEBChuanCrossCountySimpleContest.substring(aliceNEBChuanCrossCountySimpleContest.indexOf(','));
    String retrievedString =   GSON.toJson(nebAssertion);
    String retrievedStringWithoutID =  retrievedString.substring(retrievedString.indexOf(','));
    assertEquals(expectedNEBStringWithoutID, retrievedStringWithoutID);

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream().filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    // Remove the id prefix, because we don't know what the assertion id will be.
    String expectedString1WithoutID = aliceNENBobOrder1CrossCounty.substring(aliceNENBobOrder1CrossCounty.indexOf(','));
    String expectedString2WithoutID = aliceNENBobOrder2CrossCounty.substring(aliceNENBobOrder2CrossCounty.indexOf(','));
    String retrievedNENString =   GSON.toJson(nenAssertion);
    String retrievedNENStringWithoutID =  retrievedNENString.substring(retrievedNENString.indexOf(','));
    // We're not sure what order the 'assumed equals' list is in, but it should match one of them.
    assertTrue(expectedString1WithoutID.equals(retrievedNENStringWithoutID)
        || expectedString2WithoutID.equals(retrievedNENStringWithoutID));
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
    String retrievedString = GSON.toJson(nebAssertion);
    JsonObject data = GSON.fromJson(retrievedString, JsonObject.class);
    // margin should be 1.
    JsonElement marginElement = data.get("margin");
    int margin = GSON.fromJson(marginElement, Integer.class);
    assertEquals(1, margin);

    // difficulty should be 10.
    JsonElement difficultyElement = data.get("difficulty");
    BigDecimal difficulty = GSON.fromJson(difficultyElement, BigDecimal.class);
    assertEquals(BigDecimal.valueOf(10), difficulty);

    // diluted margin should be 0.1
    JsonElement dilutedMarginElement = data.get("diluted_margin");
    BigDecimal dilutedMargin = GSON.fromJson(dilutedMarginElement, BigDecimal.class);
    assertEquals(BigDecimal.valueOf(0.1), dilutedMargin);

    // There should be one NEN assertion: Alice > Bob if only {Alice,Bob} remain.
    Optional<Assertion> nenMaybeAssertion = assertions.stream()
        .filter(a -> a instanceof NENAssertion).findFirst();
    assertTrue(nenMaybeAssertion.isPresent());
    NENAssertion nenAssertion = (NENAssertion) nenMaybeAssertion.get();
    String retrievedNENString = GSON.toJson(nenAssertion);
    JsonObject NENdata = GSON.fromJson(retrievedNENString, JsonObject.class);
    // margin should be 1.
    JsonElement NENmarginElement = NENdata.get("margin");
    int NENmargin = GSON.fromJson(NENmarginElement, Integer.class);
    assertEquals(1, NENmargin);

    // difficulty should be 10.
    JsonElement NENdifficultyElement = data.get("difficulty");
    BigDecimal NENdifficulty = GSON.fromJson(NENdifficultyElement, BigDecimal.class);
    assertEquals(BigDecimal.valueOf(10), NENdifficulty);

    // diluted margin should be 0.1
    JsonElement NENdilutedMarginElement = data.get("diluted_margin");
    BigDecimal NENdilutedMargin = GSON.fromJson(NENdilutedMarginElement, BigDecimal.class);
    assertEquals(BigDecimal.valueOf(0.1), NENdilutedMargin);
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
}
