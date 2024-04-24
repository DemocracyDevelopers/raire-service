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
@Disabled // TODO Re-enable when GenerateAssertionsService is implemented.
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
   * Test assertion: Alice P. Mangrove NEB Al (Bob) Jones in "Multi-County Contest 1".
   */
  private final static String aliceNEBAl = "{\"id\":6,\"version\":0,\"contestName\":" +
      "\"Multi-County Contest 1\",\"winner\":\"Alice P. Mangrove\",\"loser\":\"Al (Bob) Jones\"," +
      "\"margin\":2170,\"difficulty\":0.9,\"assumedContinuing\":[],\"dilutedMargin\":0.07," +
      "\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0,\"optimisticSamplesToAudit\":0," +
      "\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0," +
      "\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Alice P. Mangrove NEN West W. Westerson in "Multi-County Contest 1" assuming
   * only these two candidates remain standing.
   */
  private final static String aliceNENwest = "{\"id\":7,\"version\":0,\"contestName\":" +
      "\"Multi-County Contest 1\",\"winner\":\"Alice P. Mangrove\",\"loser\":\"West W. Westerson\","
      +
      "\"margin\":31,\"difficulty\":5.0,\"assumedContinuing\":[\"West W. Westerson\"," +
      "\"Alice P. Mangrove\"],\"dilutedMargin\":0.001,\"cvrDiscrepancy\":{}," +
      "\"estimatedSamplesToAudit\":0,\"optimisticSamplesToAudit\":0,\"twoVoteUnderCount\":0," +
      "\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0," +
      "\"currentRisk\":1.00}";

  /**
   * Test assertion: CC NEB A in "Larger Contest".
   */
  private final static String CCNEBA = "{\"id\":8,\"version\":0,\"contestName\":" +
      "\"Larger Contest\",\"winner\":\"CC\",\"loser\":\"A\"," +
      "\"margin\":25,\"difficulty\":5.0,\"assumedContinuing\":[],\"dilutedMargin\":0.0125," +
      "\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0,\"optimisticSamplesToAudit\":0," +
      "\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0," +
      "\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: CC NEN B in "Larger Contest".
   */
  private final static String CCNENB = "{\"id\":9,\"version\":0,\"contestName\":" +
      "\"Larger Contest\",\"winner\":\"CC\",\"loser\":\"B\"," +
      "\"margin\":100,\"difficulty\":2.0,\"assumedContinuing\":[\"A\",\"B\",\"CC\"]," +
      "\"dilutedMargin\":0.05,\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0," +
      "\"optimisticSamplesToAudit\":0,\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0," +
      "\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Alice NEB Bob in the contest "One NEB Assertion Contest" when the audit is in
   * progress and some discrepancies have been found.
   */
  private final static String aliceNEBBobInProgress = "{\"id\":1,\"version\":0,\"contestName\":" +
      "\"One NEB Assertion Contest\",\"winner\":\"Alice\",\"loser\":\"Bob\",\"margin\":320," +
      "\"difficulty\":1.1,\"assumedContinuing\":[],\"dilutedMargin\":0.32,\"cvrDiscrepancy\":{}," +
      "\"estimatedSamplesToAudit\":111,\"optimisticSamplesToAudit\":111,\"twoVoteUnderCount\":0," +
      "\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0," +
      "\"currentRisk\":0.50}";

  /**
   * Test assertion: Alice NEN Chuan assuming Alice, Chuan, Diego and Bob are continuing, for
   * the contest "One NEN Assertion Contest" when the audit is in progress and some discrepancies
   * have been found.
   */
  private final static String aliceNENChuanInProgress =
      "{\"id\":2,\"version\":0,\"contestName\":" +
          "\"One NEN Assertion Contest\",\"winner\":\"Alice\",\"loser\":\"Chuan\",\"margin\":240,"
          +
          "\"difficulty\":3.01,\"assumedContinuing\":[\"Alice\",\"Chuan\",\"Diego\",\"Bob\"]," +
          "\"dilutedMargin\":0.12,\"cvrDiscrepancy\":{\"13\":1,\"14\":0,\"15\":0}," +
          "\"estimatedSamplesToAudit\":245,\"optimisticSamplesToAudit\":201,\"twoVoteUnderCount\":0,"
          +
          "\"oneVoteUnderCount\":0,\"oneVoteOverCount\":1,\"twoVoteOverCount\":0,\"otherCount\":2,"
          +
          "\"currentRisk\":0.20}";

  /**
   * Test assertion: Amanda NEB Liesl in the contest "One NEN NEB Assertion Contest" when the audit
   * is in progress and some discrepancies have been found.
   */
  private final static String amandaNEBLieslInProgress =
      "{\"id\":3,\"version\":0,\"contestName\":" +
          "\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\",\"loser\":\"Liesl\",\"margin\":112,"
          +
          "\"difficulty\":0.1,\"assumedContinuing\":[],\"dilutedMargin\":0.1," +
          "\"cvrDiscrepancy\":{\"13\":-1,\"14\":2,\"15\":2},\"estimatedSamplesToAudit\":27," +
          "\"optimisticSamplesToAudit\":20,\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":1," +
          "\"oneVoteOverCount\":0,\"twoVoteOverCount\":2,\"otherCount\":0,\"currentRisk\":0.08}";

  /**
   * Test assertion: Amanda NEN Wendell assuming Liesl, Wendell and Amanda are continuing, for the
   * contest "One NEN NEB Assertion Contest" when the audit is in progress and some discrepancies
   * have been found.
   */
  private final static String amandaNENWendellInProgress = "{\"id\":4,\"version\":0," +
      "\"contestName\":\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\"," +
      "\"loser\":\"Wendell\",\"margin\":560,\"difficulty\":3.17," +
      "\"assumedContinuing\":[\"Liesl\",\"Wendell\",\"Amanda\"],\"dilutedMargin\":0.5," +
      "\"cvrDiscrepancy\":{\"13\":1,\"14\":1,\"15\":-2},\"estimatedSamplesToAudit\":300," +
      "\"optimisticSamplesToAudit\":200,\"twoVoteUnderCount\":1,\"oneVoteUnderCount\":0," +
      "\"oneVoteOverCount\":2,\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":0.70}";

  private final static GenerateAssertionsRequest request
      = new GenerateAssertionsRequest(tiedWinnersContest, 2, 5, Arrays.stream(aliceChuanBob).toList());

  /**
   * Trivial test to see whether the placeholder service throws the expected placeholder exception.
   */
  @Test
  @Transactional
  void dummyServiceThrowsException() {
    Exception ex = assertThrows(GenerateAssertionsException.class, () ->
        generateAssertionsService.generateAssertions(request)
    );
  }

  @Test
  void testThatTheTestsAreDisabled() {
    assertTrue(false);
  }

  /**
   * Tied winners throws the right exception.
   * This is a super-simple election with two candidates with one vote each.
   */
  @Test
  void tiedWinnersThrowsTiedWinnersException() {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(tiedWinnersContest,
        2, 5, Arrays.stream(aliceChuanBob).toList());
    GenerateAssertionsException ex = assertThrows(GenerateAssertionsException.class, () ->
        generateAssertionsService.generateAssertions(request)
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
   * Doubling the totalAuditableBallots doubles the difficulty but not the margins.
   */

  /**
   * Insufficient totalAuditableBallots causes the right exception.
   */

}
