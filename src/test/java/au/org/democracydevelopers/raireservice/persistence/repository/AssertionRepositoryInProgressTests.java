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

package au.org.democracydevelopers.raireservice.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raireservice.controller.GetAssertionsValidAPIRequestTests;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.service.Metadata;
import au.org.democracydevelopers.raireservice.testUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.math.BigDecimal;
import java.util.List;
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
 * Tests to validate the behaviour of Assertion retrieval and storage. Assertions and other
 * relevant data is preloaded into the test database from:
 * src/test/resources/simple_assertions_in_progress.sql.
 */
@ActiveProfiles("assertions-in-progress")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class AssertionRepositoryInProgressTests {

  private final static Logger logger = LoggerFactory.getLogger(AssertionRepositoryInProgressTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * To facilitate easier checking of retrieved/saved assertion content.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();


  /**
   * Test assertion: Alice NEB Bob in the contest "One NEB Assertion Contest" when the audit is
   * in progress and some discrepancies have been found.
   */
  private final static String aliceNEBBobInProgress = "{\"id\":1,\"version\":0,\"contestName\":" +
      "\"One NEB Assertion Contest\",\"winner\":\"Alice\",\"loser\":\"Bob\",\"margin\":320," +
      "\"difficulty\":1.1,\"assumedContinuing\":[],\"dilutedMargin\":0.32,\"cvrDiscrepancy\":{}," +
      "\"estimatedSamplesToAudit\":111,\"optimisticSamplesToAudit\":111,\"twoVoteUnderCount\":0," +
      "\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0," +
      "\"currentRisk\":0.50}";

  /**
   * Test assertion: Alice NEN Charlie assuming Alice, Charlie, Diego and Bob are continuing,
   * for the contest "One NEN Assertion Contest" when the audit is in progress and some
   * discrepancies have been found.
   */
  private final static String aliceNENCharlieInProgress = "{\"id\":2,\"version\":0,\"contestName\":" +
      "\"One NEN Assertion Contest\",\"winner\":\"Alice\",\"loser\":\"Charlie\",\"margin\":240," +
      "\"difficulty\":3.01,\"assumedContinuing\":[\"Alice\",\"Charlie\",\"Diego\",\"Bob\"]," +
      "\"dilutedMargin\":0.12,\"cvrDiscrepancy\":{\"13\":1,\"14\":0,\"15\":0}," +
      "\"estimatedSamplesToAudit\":245,\"optimisticSamplesToAudit\":201,\"twoVoteUnderCount\":0," +
      "\"oneVoteUnderCount\":0,\"oneVoteOverCount\":1,\"twoVoteOverCount\":0,\"otherCount\":2," +
      "\"currentRisk\":0.20}";

  /**
   * Test assertion: Amanda NEB Liesl in the contest "One NEN NEB Assertion Contest" when the audit
   * is in progress and some discrepancies have been found.
   */
  private final static String amandaNEBLieslInProgress = "{\"id\":3,\"version\":0,\"contestName\":" +
      "\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\",\"loser\":\"Liesl\",\"margin\":112,"+
      "\"difficulty\":0.1,\"assumedContinuing\":[],\"dilutedMargin\":0.1," +
      "\"cvrDiscrepancy\":{\"13\":-1,\"14\":2,\"15\":2},\"estimatedSamplesToAudit\":27," +
      "\"optimisticSamplesToAudit\":20,\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":1," +
      "\"oneVoteOverCount\":0,\"twoVoteOverCount\":2,\"otherCount\":0,\"currentRisk\":0.08}";

  /**
   * Test assertion: Amanda NEN Wendell assuming Liesl, Wendell and Amanda are continuing,
   * for the contest "One NEN NEB Assertion Contest" when the audit is in progress and some
   * discrepancies have been found.
   */
  private final static String amandaNENWendellInProgress = "{\"id\":4,\"version\":0," +
      "\"contestName\":\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\"," +
      "\"loser\":\"Wendell\",\"margin\":560,\"difficulty\":3.17," +
      "\"assumedContinuing\":[\"Liesl\",\"Wendell\",\"Amanda\"],\"dilutedMargin\":0.5," +
      "\"cvrDiscrepancy\":{\"13\":1,\"14\":1,\"15\":-2},\"estimatedSamplesToAudit\":300," +
      "\"optimisticSamplesToAudit\":200,\"twoVoteUnderCount\":1,\"oneVoteUnderCount\":0," +
      "\"oneVoteOverCount\":2,\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":0.70}";

  /**
   * Retrieve assertions for a contest that has one NEB assertion (audit in progress with no
   * discrepancies observed).
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNEBAssertionInProgress(){
    testUtils.log(logger,"retrieveAssertionsOneNEBAssertionInProgress");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEB Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    assertEquals(NEBAssertion.class, r.getClass());
    assertEquals(aliceNEBBobInProgress, GSON.toJson(r));
  }

  /**
   * Test NEBAssertion::convert().
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNEBAssertionConvert(){
    testUtils.log(logger,"retrieveAssertionsOneNEBAssertionConvert");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEB Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    AssertionAndDifficulty aad = r.convert(List.of("Alice", "Bob"));
    assertEquals(1.1, aad.difficulty);
    assertEquals(320, aad.margin);
    assertTrue(aad.assertion.isNEB());
    assertEquals(0, ((NotEliminatedBefore)aad.assertion).winner);
    assertEquals(1, ((NotEliminatedBefore)aad.assertion).loser);

    // Check that current risk is 0.5
    assertEquals(new BigDecimal("0.50"), aad.status.get(Metadata.STATUS_RISK));
  }


  /**
   * Retrieve assertions for a contest that has one NEN assertion (audit in progress with some
   * discrepancies observed).
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNENAssertionInProgress(){
    testUtils.log(logger,"retrieveAssertionsOneNENAssertionInProgress");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    assertEquals(NENAssertion.class, r.getClass());
    assertEquals(aliceNENCharlieInProgress, GSON.toJson(r));
  }


  /**
   * Retrieve assertions for a contest that has one NEN and one NEB assertion (audit in progress
   * with some discrepancies observed).
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNENOneNEBAssertionInProgress(){
    testUtils.log(logger,"retrieveAssertionsOneNENOneNEBAssertionInProgress");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN NEB Assertion Contest");
    assertEquals(2, retrieved.size());

    final Assertion r1 = retrieved.get(0);
    assertEquals(NEBAssertion.class, r1.getClass());
    assertEquals(amandaNEBLieslInProgress, GSON.toJson(r1));

    final Assertion r2 = retrieved.get(1);
    assertEquals(NENAssertion.class, r2.getClass());
    assertEquals(amandaNENWendellInProgress, GSON.toJson(r2));
  }
}
