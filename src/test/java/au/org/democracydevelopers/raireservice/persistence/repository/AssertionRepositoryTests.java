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


import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests to validate the behaviour of Assertion retrieval and storage. Assertions and other
 * relevant data is preloaded into the test database from src/test/resources/data.sql.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class AssertionRepositoryTests {

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * To facilitate easier checking of retrieved/saved assertion content.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  /**
   * Array of candidates: Alice, Charlie, Diego, Bob.
   */
  private static final String[] aliceCharlieDiegoBob =  {"Alice", "Charlie", "Diego", "Bob"};

  /**
   * Array of candidates: Alice, Charlie, Bob.
   */
  private static final String[] aliceCharlieBob =  {"Alice", "Charlie", "Bob"};

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
   * Retrieval of assertions for an existing contest with no associated assertions will return an
   * empty list.
   */
  @Test
  @Transactional
  void existentContestNoAssertions(){
    List<Assertion> retrieved = assertionRepository.findByContestName("No CVR Mayoral");
    assertEquals(0, retrieved.size());
  }

  /**
   * Retrieval of assertions for a non-existent contest will return an empty list.
   */
  @Test
  @Transactional
  void nonExistentContestNoAssertions(){
    List<Assertion> retrieved = assertionRepository.findByContestName("Non-Existent Contest Name");
    assertEquals(0, retrieved.size());
  }

  /**
   * Deletion of assertions for a non-existent contest will remove no records.
   */
  @Test
  @Transactional
  void deleteAssertionsNonExistentContest(){
    long records = assertionRepository.deleteByContestName("Non-Existent Contest Name");
    assertEquals(0, records);
  }

  /**
   * Deletion of assertions for an existent contest with no assertions will remove no records.
   */
  @Test
  @Transactional
  void deleteAssertionsExistentContestNoAssertions(){
    long records = assertionRepository.deleteByContestName("No CVR Mayoral");
    assertEquals(0, records);
  }



  /**
   * Test translateAndSaveAssertions when passed with an empty array of assertions. The method
   * should store no assertions in the database.
   */
  @Test
  @Transactional
  void translateAndSaveNoAssertions(){
    String[] candidates = {"Alice", "Bob", "Charlie"};
    AssertionAndDifficulty[] empty = {};
    assertionRepository.translateAndSaveAssertions("Ballina Mayoral", 11000,
        candidates, empty);

    List<Assertion> retrieved = assertionRepository.findByContestName("Ballina Mayoral");
    assertEquals(0, retrieved.size());
  }

  /**
   * Test translation and saving of assertions for a succession of contests:
   * - "One NEB Assertion Contest"
   * - "One NEN Assertion Contest"
   * - "One NEN NEB Assertion Contest"
   * - "Multi-County Contest 1"
   */
  @Test
  @Transactional
  void translateAndSaveAssertions(){
    saveAssertionsOneNEBContest();

    List<Assertion> retrieved = assertionRepository.findByContestName("One NEB Assertion Contest");
    assertEquals(1, retrieved.size());

    Assertion r = retrieved.get(0);
    assertEquals(NEBAssertion.class, r.getClass());
    assertEquals(aliceNEBBob, GSON.toJson(r));

    saveAssertionsOneNENContest();

    retrieved = assertionRepository.findByContestName("One NEN Assertion Contest");
    assertEquals(1, retrieved.size());

    r = retrieved.get(0);
    assertEquals(NENAssertion.class, r.getClass());
    assertEquals(aliceNENCharlie, GSON.toJson(r));

    saveAssertionsOneNENOneNEBContest();

    retrieved = assertionRepository.findByContestName("One NEN NEB Assertion Contest");
    assertEquals(2, retrieved.size());

    Assertion r1 = retrieved.get(0);
    assertEquals(NEBAssertion.class, r1.getClass());
    assertEquals(amandaNEBLiesl, GSON.toJson(r1));

    Assertion r2 = retrieved.get(1);
    assertEquals(NENAssertion.class, r2.getClass());
    assertEquals(amandaNENWendell, GSON.toJson(r2));

    saveAssertionsMultiCountyContest();

    retrieved = assertionRepository.findByContestName("Multi-County Contest 1");
    assertEquals(3, retrieved.size());

    r1 = retrieved.get(0);
    assertEquals(NEBAssertion.class, r1.getClass());
    assertEquals(charlieNEBAlice, GSON.toJson(r1));

    r2 = retrieved.get(1);
    assertEquals(NEBAssertion.class, r2.getClass());
    assertEquals(aliceNEBAl, GSON.toJson(r2));

    Assertion r3 = retrieved.get(2);
    assertEquals(NENAssertion.class, r3.getClass());
    assertEquals(aliceNENwest, GSON.toJson(r3));
  }


  /**
   * Translate and save Alice NEB Bob in contest "One NEB Assertion Contest".
   */
  void saveAssertionsOneNEBContest(){
    String[] candidates = {"Alice", "Charlie", "Bob"};
    AssertionAndDifficulty aadAliceNEBBob = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedBefore(0, 2),
        1.1, 320);

    AssertionAndDifficulty[] assertions = {aadAliceNEBBob};

    assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
        1000, candidates, assertions);
  }

  /**
   * Translate and save Alice NEN Charlie in contest "One NEN Assertion Contest".
   */
  void saveAssertionsOneNENContest(){
    String[] candidates = {"Alice", "Charlie", "Diego", "Bob"};
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty aadAliceNENCharlie = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedNext(0, 1,
        continuing),3.01, 240);

    AssertionAndDifficulty[] assertions = {aadAliceNENCharlie};

    assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
        2000, candidates, assertions);
  }

  /**
   * Translate and save Amanda NEB Liesl and Amanda NEN Wendell for the contest
   * "One NEN NEB Assertion Contest".
   */
  void saveAssertionsOneNENOneNEBContest(){
    String[] candidates = {"Liesl", "Wendell", "Amanda"};
    int[] continuing = {0, 1 ,2};
    AssertionAndDifficulty aadAmandaNEBLiesl = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedBefore(2, 0),
        0.1, 112);

    AssertionAndDifficulty aadAmandaNENWendell = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedNext(2, 1,
        continuing),3.17, 560);

    AssertionAndDifficulty[] assertions = {aadAmandaNEBLiesl, aadAmandaNENWendell};

    assertionRepository.translateAndSaveAssertions("One NEN NEB Assertion Contest",
        1120, candidates, assertions);
  }

  /**
   * Translate and save Charlie C. Chaplin NEB Alice P. Mangrove, Alice P. Mangrove NEB
   * Al (Bob) Jones and Alice P. Mangrove NEN West W. Westerson for the contest
   * "Multi-County Contest 1".
   */
  void saveAssertionsMultiCountyContest(){
    String[] candidates = {"West W. Westerson", "Alice P. Mangrove", "Charlie C. Chaplin",
        "Al (Bob) Jones"};

    AssertionAndDifficulty aadCharlieNEBAlice = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedBefore(2, 1),
        2.1, 310);

    AssertionAndDifficulty aadAliceNEBAl = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedBefore(1, 3),
        0.9, 2170);

    int[] continuing = {0, 1};
    AssertionAndDifficulty aadAliceNENWest = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedNext(1, 0,
            continuing),5, 31);

    AssertionAndDifficulty[] assertions = {aadCharlieNEBAlice, aadAliceNEBAl, aadAliceNENWest};

    assertionRepository.translateAndSaveAssertions("Multi-County Contest 1",
        31000, candidates, assertions);
  }


  /**
   * Create and return a raire-java assertion for the contest "One NEN Assertion Contest".
   * @param margin Margin of the assertion to be created.
   * @param winner Winner of the assertion to be created.
   * @param loser Loser of the assertion to be created.
   * @param continuing Array of candidates assumed to be continuing.
   */
  AssertionAndDifficulty[] formAssertionsOneNENAssertionContest(int margin, int winner, int loser,
      int[] continuing){
    AssertionAndDifficulty aadAliceNENCharlie = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedNext(winner, loser,
            continuing),3.01, margin);

    return new AssertionAndDifficulty[]{aadAliceNENCharlie};
  }

  /**
   * Create and return a raire-java assertion for the contest "One NEB Assertion Contest".
   * @param margin Margin of the assertion to be created.
   * @param winner Winner of the assertion to be created.
   * @param loser Loser of the assertion to be created.
   */
  AssertionAndDifficulty[] formAssertionsOneNEBAssertionContest(int margin, int winner, int loser){
    AssertionAndDifficulty aadAliceNEBBob = new AssertionAndDifficulty(
        new au.org.democracydevelopers.raire.assertions.NotEliminatedBefore(winner, loser),
        1.1, margin);

    return new AssertionAndDifficulty[]{aadAliceNEBBob};
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: negative universe size for an
   * NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNegativeUniverseSizeNEB(){
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        0, 2);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
      assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
        -1000, aliceCharlieBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("assertion must have a positive universe size"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: zero universe size for an
   * NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveZeroUniverseSizeNEB(){
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        0, 2);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            0, aliceCharlieBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("assertion must have a positive universe size"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: negative universe size for an
   * NEN assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNegativeUniverseSizeNEN(){
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            -2000, aliceCharlieDiegoBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("assertion must have a positive universe size"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: zero universe size for an
   * NEN assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveZeroUniverseSizeNEN(){
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            0, aliceCharlieDiegoBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("assertion must have a positive universe size"));
  }


  /**
   * Test translateAndSaveAssertions when passed invalid data: negative margin for an
   * NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNegativeMarginNEB(){
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(-100,
        0, 2);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("non-negative margin"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: negative margin for an
   * NEN assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNegativeMarginNEN(){
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(-100,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("negative margin"));
  }


  /**
   * Test translateAndSaveAssertions when passed invalid data: margin larger than universe size
   * for an NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNegativeMarginTooHighNEB(){
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(2000,
        0, 2);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("less than universe size"));
  }


  /**
   * Test translateAndSaveAssertions when passed invalid data:  margin larger than universe size
   * for an NEN assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveMarginTooHighNEN(){
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(3000,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("less than universe size"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: same winner and loser
   * for an NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveSameWinnerLoserNEB(){
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        1, 1);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("must not be the same candidate"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: same winner and loser
   * for an NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveSameWinnerLoserNEN(){
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 0, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains("must not be the same candidate"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: winner of NEN is not continuing.
   * An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNENWinnerNotContinuing(){
    int[] continuing = {1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains(
        "the winner and loser of an assertion must also be continuing candidates"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: loser of NEN is not continuing.
   * An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNENLoserNotContinuing(){
    int[] continuing = {0 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(ex.getMessage().toLowerCase().contains(
        "the winner and loser of an assertion must also be continuing candidates"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: winner index is outside of the bounds
   * of the candidate array (NEB). An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveWinnerOutOfBoundsNEB(){
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        5, 2);

    assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: winner index is outside of the bounds
   * of the candidate array (NEN). An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveWinnerOutOfBoundsNEN(){
    int[] continuing = {0 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        5, 1, continuing);

    assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: loser index is outside of the bounds
   * of the candidate array (NEB). An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveLoserOutOfBoundsNEB(){
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        2, 7);

    assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: loser index is outside of the bounds
   * of the candidate array (NEN). An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveLoserOutOfBoundsNEN(){
    int[] continuing = {0 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 7, continuing);

    assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));
  }
}
