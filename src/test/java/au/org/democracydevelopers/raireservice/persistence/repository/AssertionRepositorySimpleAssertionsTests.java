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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.service.Metadata;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.math.BigDecimal;
import java.util.List;
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
 * relevant data is preloaded into the test database from:
 * src/test/resources/simple_assertions.sql.
 */
@ActiveProfiles("simple-assertions")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class AssertionRepositorySimpleAssertionsTests {

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * To facilitate easier checking of retrieved/saved assertion content.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

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
   * Test assertion: Alice NEN Charlie assuming Alice, Charlie, Diego and Bob are continuing,
   * for the contest "One NEN Assertion Contest".
   */
  private final static String aliceNENCharlie = "{\"id\":2,\"version\":0,\"contestName\":" +
      "\"One NEN Assertion Contest\",\"winner\":\"Alice\",\"loser\":\"Charlie\",\"margin\":240," +
      "\"difficulty\":3.01,\"assumedContinuing\":[\"Alice\",\"Charlie\",\"Diego\",\"Bob\"]," +
      "\"dilutedMargin\":0.12,\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0," +
      "\"optimisticSamplesToAudit\":0,\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0," +
      "\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Amanda NEB Liesl in the contest "One NEN NEB Assertion Contest".
   */
  private final static String amandaNEBLiesl = "{\"id\":3,\"version\":0,\"contestName\":" +
      "\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\",\"loser\":\"Liesl\",\"margin\":112,"+
      "\"difficulty\":0.1,\"assumedContinuing\":[],\"dilutedMargin\":0.1," +
      "\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0,\"optimisticSamplesToAudit\":0," +
      "\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0," +
      "\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Amanda NEN Wendell assuming Liesl, Wendell and Amanda are continuing,
   * for the contest "One NEN NEB Assertion Contest".
   */
  private final static String amandaNENWendell = "{\"id\":4,\"version\":0,\"contestName\":" +
      "\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\",\"loser\":\"Wendell\"," +
      "\"margin\":560,\"difficulty\":3.17,\"assumedContinuing\":[\"Liesl\",\"Wendell\"," +
      "\"Amanda\"],\"dilutedMargin\":0.5,\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0," +
      "\"optimisticSamplesToAudit\":0,\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0," +
      "\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Charlie C. Chaplin NEB Alice P. Mangrove in "Multi-County Contest 1".
   */
  private final static String charlieNEBAlice = "{\"id\":5,\"version\":0,\"contestName\":" +
      "\"Multi-County Contest 1\",\"winner\":\"Charlie C. Chaplin\",\"loser\":\"Alice P. Mangrove\"," +
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
   * Test assertion: Alice P. Mangrove NEN West W. Westerson in "Multi-County Contest 1"
   * assuming only these two candidates remain standing.
   */
  private final static String aliceNENwest = "{\"id\":7,\"version\":0,\"contestName\":" +
      "\"Multi-County Contest 1\",\"winner\":\"Alice P. Mangrove\",\"loser\":\"West W. Westerson\"," +
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
   * Retrieve assertions for a contest that has one NEB assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNEBAssertion(){
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEB Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    assertEquals(NEBAssertion.class, r.getClass());
    assertEquals(aliceNEBBob, GSON.toJson(r));
  }

  /**
   * Retrieve assertions for a contest that has one NEN assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENAssertion(){
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    assertEquals(NENAssertion.class, r.getClass());
    assertEquals(aliceNENCharlie, GSON.toJson(r));
  }

  /**
   * Test NENAssertion::convert().
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENAssertionConvert(){
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    AssertionAndDifficulty aad = r.convert(List.of("Alice", "Charlie", "Diego", "Bob"));
    assertEquals(3.01, aad.difficulty);
    assertEquals(240, aad.margin);
    assertFalse(aad.assertion.isNEB());
    assertEquals(0, ((NotEliminatedNext)aad.assertion).winner);
    assertEquals(1, ((NotEliminatedNext)aad.assertion).loser);

    int[] continuing = {0, 1, 2, 3};
    assertArrayEquals(continuing, ((NotEliminatedNext)aad.assertion).continuing);

    // Check that current risk is 1.00
    assertEquals(new BigDecimal("1.00"), aad.status.get(Metadata.STATUS_RISK));
  }


  /**
   * Retrieve assertions for a contest that has one NEN and one NEB assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENOneNEBAssertion(){
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN NEB Assertion Contest");
    assertEquals(2, retrieved.size());

    final Assertion r1 = retrieved.get(0);
    assertEquals(NEBAssertion.class, r1.getClass());
    assertEquals(amandaNEBLiesl, GSON.toJson(r1));

    final Assertion r2 = retrieved.get(1);
    assertEquals(NENAssertion.class, r2.getClass());
    assertEquals(amandaNENWendell, GSON.toJson(r2));
  }


  /**
   * Retrieve assertions for a multi-county contest.
   */
  @Test
  @Transactional
  void retrieveAssertionsMultiCountyContest(){
    List<Assertion> retrieved = assertionRepository.findByContestName("Multi-County Contest 1");
    assertEquals(3, retrieved.size());

    final Assertion r1 = retrieved.get(0);
    assertEquals(NEBAssertion.class, r1.getClass());
    assertEquals(charlieNEBAlice, GSON.toJson(r1));

    final Assertion r2 = retrieved.get(1);
    assertEquals(NEBAssertion.class, r2.getClass());
    assertEquals(aliceNEBAl, GSON.toJson(r2));

    final Assertion r3 = retrieved.get(2);
    assertEquals(NENAssertion.class, r3.getClass());
    assertEquals(aliceNENwest, GSON.toJson(r3));
  }

  /**
   * Deletion of assertions for an existent contest with one NEB assertion will remove one record.
   * The contest will then have no associated assertions in the database.
   */
  @Test
  @Transactional
  void deleteAssertionsExistentContestOneNEBAssertion(){
    long records = assertionRepository.deleteByContestName("One NEB Assertion Contest");
    assertEquals(1, records);

    List<Assertion> retrieved = assertionRepository.findByContestName("One NEB Assertion Contest");
    assertEquals(0, retrieved.size());
  }

  /**
   * Deletion of assertions for an existent contest with one NEN assertion will remove one record.
   * The contest will then have no associated assertions in the database.
   */
  @Test
  @Transactional
  void deleteAssertionsExistentContestOneNENAssertion(){
    long records = assertionRepository.deleteByContestName("One NEN Assertion Contest");
    assertEquals(1, records);

    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN Assertion Contest");
    assertEquals(0, retrieved.size());
  }

  /**
   * Deletion of assertions for an existent contest with one NEN and one NEB assertion will
   * remove two record. The contest will then have no associated assertions in the database.
   */
  @Test
  @Transactional
  void deleteAssertionsExistentContestOneNENOneNEBAssertion(){
    long records = assertionRepository.deleteByContestName("One NEN NEB Assertion Contest");
    assertEquals(2, records);

    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN NEB Assertion Contest");
    assertEquals(0, retrieved.size());
  }

  /**
   * Deletion of assertions for an existent multi-county with three assertions.
   * The contest will then have no associated assertions in the database.
   */
  @Test
  @Transactional
  void deleteAssertionsMultiCountyContest(){
    long records = assertionRepository.deleteByContestName("Multi-County Contest 1");
    assertEquals(3, records);

    List<Assertion> retrieved = assertionRepository.findByContestName("Multi-County Contest 1");
    assertEquals(0, retrieved.size());
  }

  /**
   * Translation and saving of assertions when the database already contains some
   * assertions. This test is designed to test the incrementing of IDs. The database
   * will have 7 assertions pre-populated from simple_assertions.sql.
   */
  @Test
  @Transactional
  void testAutoIncrementOfIDs(){
    String[] candidates = {"A", "B", "CC"};
    int[] continuing = {0,1,2};
    AssertionAndDifficulty aadCCNEBA = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedBefore(2, 0),
        5, 25);

    AssertionAndDifficulty aadCCNENB = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedNext(2, 1,
            continuing),2, 100);

    AssertionAndDifficulty[] assertions = {aadCCNEBA, aadCCNENB};

    assertionRepository.translateAndSaveAssertions("Larger Contest",
        2000, candidates, assertions);

    List<Assertion> retrieved = assertionRepository.findByContestName("Larger Contest");
    assertEquals(2, retrieved.size());

    Assertion r1 = retrieved.get(0);
    assertEquals(NEBAssertion.class, r1.getClass());
    assertEquals(CCNEBA, GSON.toJson(r1));

    Assertion r2 = retrieved.get(1);
    assertEquals(NENAssertion.class, r2.getClass());
    assertEquals(CCNENB, GSON.toJson(r2));
  }

}
