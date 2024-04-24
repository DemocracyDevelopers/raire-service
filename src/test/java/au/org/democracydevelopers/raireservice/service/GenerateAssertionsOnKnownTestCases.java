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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.response.GetAssertionsResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Arrays;
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
   * Array of candidates: Alice, Chuan, Diego, Bob.
   */
  private static final String[] aliceChuanDiegoBob = {"Alice", "Chuan", "Diego", "Bob"};

  /**
   * Array of candidates: Alice, Chuan, Bob.
   */
  private static final String[] aliceChuanBob = {"Alice", "Chuan", "Bob"};

  /**
   * Test assertion: Alice NEB Bob in the contest "One NEB Assertion Contest".
   */
  private final static String aliceNEBBob = "{\"id\":1,\"version\":0,\"contestName\":" +
      "\"One NEB Assertion Contest\",\"winner\":\"Alice\",\"loser\":\"Bob\",\"margin\":320," +
      "\"difficulty\":1.1,\"assumedContinuing\":[],\"dilutedMargin\":0.32,\"cvrDiscrepancy\":{}," +
      "\"estimatedSamplesToAudit\":0,\"optimisticSamplesToAudit\":0,\"twoVoteUnderCount\":0," +
      "\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0," +
      "\"currentRisk\":1.00}";

  /**
   * Test assertion: Alice NEN Chuan assuming Alice, Chuan, Diego and Bob are continuing, for
   * the contest "One NEN Assertion Contest".
   */
  private final static String aliceNENChuan = "{\"id\":2,\"version\":0,\"contestName\":" +
      "\"One NEN Assertion Contest\",\"winner\":\"Alice\",\"loser\":\"Chuan\",\"margin\":240," +
      "\"difficulty\":3.01,\"assumedContinuing\":[\"Alice\",\"Chuan\",\"Diego\",\"Bob\"]," +
      "\"dilutedMargin\":0.12,\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0," +
      "\"optimisticSamplesToAudit\":0,\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0," +
      "\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Amanda NEB Liesl in the contest "One NEN NEB Assertion Contest".
   */
  private final static String amandaNEBLiesl = "{\"id\":3,\"version\":0,\"contestName\":" +
      "\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\",\"loser\":\"Liesl\",\"margin\":112,"
      +
      "\"difficulty\":0.1,\"assumedContinuing\":[],\"dilutedMargin\":0.1," +
      "\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0,\"optimisticSamplesToAudit\":0," +
      "\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0," +
      "\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Amanda NEN Wendell assuming Liesl, Wendell and Amanda are continuing, for the
   * contest "One NEN NEB Assertion Contest".
   */
  private final static String amandaNENWendell = "{\"id\":4,\"version\":0,\"contestName\":" +
      "\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\",\"loser\":\"Wendell\"," +
      "\"margin\":560,\"difficulty\":3.17,\"assumedContinuing\":[\"Liesl\",\"Wendell\"," +
      "\"Amanda\"],\"dilutedMargin\":0.5,\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0," +
      "\"optimisticSamplesToAudit\":0,\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0," +
      "\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Chuan C. Chaplin NEB Alice P. Mangrove in "Multi-County Contest 1".
   */
  private final static String chuanNEBAlice = "{\"id\":5,\"version\":0,\"contestName\":" +
      "\"Multi-County Contest 1\",\"winner\":\"Chuan C. Chaplin\",\"loser\":\"Alice P. Mangrove\","
      +
      "\"margin\":310,\"difficulty\":2.1,\"assumedContinuing\":[],\"dilutedMargin\":0.01," +
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
      = new GenerateAssertionsRequest("Tied Winners Contest", 2, 5, Arrays.stream(aliceChuanBob).toList());

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
   * Exact matching of the assertions described in the Guide to Raire Example 2.
   * The test data has 1/1000 of the votes, so divide margins by 1000.
   * The difficulties should be the same, because both numerator and denominator should be divided by 1000.
   */
  @Test
  void testGuideToRaireExample2() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest("Guide To Raire Example 2",
        41, 5, Arrays.stream(aliceChuanBob).toList());
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

  }

  /**
   * Doubling the totalAuditableBallots doubles the difficulty but not the margins.
   */

  /**
   * Insufficient totalAuditableBallots causes the right exception.
   */

  /**
   * Tied winners causes the right exception.
   */


}
