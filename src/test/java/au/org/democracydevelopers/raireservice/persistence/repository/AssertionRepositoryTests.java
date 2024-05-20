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
import au.org.democracydevelopers.raireservice.service.RaireServiceException;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.testUtils;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
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

import static au.org.democracydevelopers.raireservice.testUtils.correctDBAssertionData;
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

  private static final Logger logger = LoggerFactory.getLogger(AssertionRepositoryTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * Array of candidates: Alice, Charlie, Diego, Bob.
   */
  private static final String[] aliceCharlieDiegoBob =  {"Alice", "Charlie", "Diego", "Bob"};

  /**
   * Array of candidates: Alice, Charlie, Bob.
   */
  private static final String[] aliceCharlieBob =  {"Alice", "Charlie", "Bob"};

  /**
   * Verify that the given assertion Alice NEB Bob has all the right attributes.
   * @param r Assertion Alice NEB Bob.
   */
  public static void verifyAliceNEBBob(Assertion r){
    assertEquals(NEBAssertion.class, r.getClass());

    assertTrue(correctDBAssertionData(1, 320, 0.32, 1.1,
        "Alice", "Bob", List.of(), Collections.emptyMap(), 0,
        0, 0, 0, 0,
        0, 0, BigDecimal.valueOf(1),
        "One NEB Assertion Contest", r));
  }

  /**
   * Verify that the given assertion Alice NEN Charlie has all the right attributes.
   * @param r Assertion Alice NEN Charlie.
   */
  public static void verifyAliceNENCharlie(Assertion r){
    assertEquals(NENAssertion.class, r.getClass());
    assertTrue(correctDBAssertionData(2, 240, 0.12, 3.01,
        "Alice", "Charlie", List.of("Alice", "Charlie", "Diego", "Bob"),
        Collections.emptyMap(), 0, 0, 0,
        0, 0, 0, 0,
        BigDecimal.valueOf(1), "One NEN Assertion Contest", r));
  }

  /**
   * Verify that the given assertion Amanda NEB Liesl has all the right attributes.
   * @param r Assertion Amanda NEB Liesl.
   */
  public static void verifyAmandaNEBLiesl(Assertion r){
    assertEquals(NEBAssertion.class, r.getClass());
    assertTrue(correctDBAssertionData(3, 112, 0.1, 0.1,
        "Amanda", "Liesl", List.of(), Collections.emptyMap(), 0,
        0, 0, 0, 0,
        0, 0, BigDecimal.valueOf(1),
        "One NEN NEB Assertion Contest", r));
  }

  /**
   * Verify that the given assertion Amanda NEN Wendell has all the right attributes.
   * @param r Assertion Amanda NEN Wendell.
   */
  public static void verifyAmandaNENWendell(Assertion r){
    assertEquals(NENAssertion.class, r.getClass());
    assertTrue(correctDBAssertionData(4, 560, 0.5, 3.17,
        "Amanda", "Wendell", List.of("Liesl", "Wendell", "Amanda"),
        Collections.emptyMap(), 0, 0, 0,
        0, 0, 0, 0,
        BigDecimal.valueOf(1), "One NEN NEB Assertion Contest", r));
  }

  /**
   * Verify that the given assertion Charlie C. Chaplin NEB Alice P. Mangrove has all the right
   * attributes.
   * @param r Assertion Charlie C. Chaplin NEB Alice P. Mangrove.
   */
  public static void verifyCharlieCNEBAliceM(Assertion r){
    assertEquals(NEBAssertion.class, r.getClass());
    assertTrue(correctDBAssertionData(5, 310, 0.01, 2.1,
        "Charlie C. Chaplin", "Alice P. Mangrove", List.of(), Collections.emptyMap(),
        0, 0, 0, 0,
        0, 0, 0, BigDecimal.valueOf(1),
        "Multi-County Contest 1", r));
  }

  /**
   * Verify that the given assertion Alice P. Mangrove NEB Al (Bob) Jones has all the right
   * attributes.
   * @param r Assertion Alice P. Mangrove NEB Al (Bob) Jones.
   */
  public static void verifyAliceMNEBAlJones(Assertion r){
    assertEquals(NEBAssertion.class, r.getClass());
    assertTrue(correctDBAssertionData(6, 2170, 0.07, 0.9,
        "Alice P. Mangrove", "Al (Bob) Jones", List.of(), Collections.emptyMap(),
        0, 0, 0, 0,
        0, 0, 0, BigDecimal.valueOf(1),
        "Multi-County Contest 1", r));
  }

  /**
   * Verify that the given assertion Alice P. Mangrove NEN West W. Westerson has all the right
   * attributes.
   * @param r Assertion Alice P. Mangrove NEN West W. Westerson Jones.
   */
  public static void verifyAliceMNENWestW(Assertion r){
    assertEquals(NENAssertion.class, r.getClass());
    assertTrue(correctDBAssertionData(7, 31, 0.001, 5.0,
        "Alice P. Mangrove", "West W. Westerson", List.of("West W. Westerson",
            "Alice P. Mangrove"), Collections.emptyMap(), 0,
        0, 0, 0, 0,
        0, 0, BigDecimal.valueOf(1),
        "Multi-County Contest 1", r));
  }

  /**
   * Retrieval of assertions (via findByContestName) for an existing contest with no associated
   * assertions will return an empty list.
   */
  @Test
  @Transactional
  void existentContestNoAssertions(){
    testUtils.log(logger, "existentContestNoAssertions");
    List<Assertion> retrieved = assertionRepository.findByContestName("No CVR Mayoral");
    assertEquals(0, retrieved.size());
  }


  /**
   * Retrieval of assertions for a non-existent contest will return an empty list.
   */
  @Test
  @Transactional
  void nonExistentContestNoAssertions(){
    testUtils.log(logger, "nonExistentContestNoAssertions");
    List<Assertion> retrieved = assertionRepository.findByContestName("Non-Existent Contest Name");
    assertEquals(0, retrieved.size());
  }

  /**
   * Retrieval of assertions (via getAssertionsThrowError) for an existing contest with no
   * associated assertions will throw a RaireServiceException.
   */
  @Test
  @Transactional
  void existentContestNoAssertionsThrowError(){
    testUtils.log(logger, "existentContestNoAssertionsThrowError");
    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        assertionRepository.getAssertionsThrowError("No CVR Mayoral"));
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT, ex.errorCode);
  }


  /**
   * Retrieval of assertions (via getAssertionsThrowError) for a non-existent contest will
   * throw a RaireServiceException.
   */
  @Test
  @Transactional
  void nonExistentContestNoAssertionsThrowError(){
    testUtils.log(logger, "nonExistentContestNoAssertionsThrowError");
    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        assertionRepository.getAssertionsThrowError("Non-Existent Contest Name"));
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT, ex.errorCode);
  }

  /**
   * Deletion of assertions for a non-existent contest will remove no records.
   */
  @Test
  @Transactional
  void deleteAssertionsNonExistentContest(){
    testUtils.log(logger, "deleteAssertionsNonExistentContest");
    long records = assertionRepository.deleteByContestName("Non-Existent Contest Name");
    assertEquals(0, records);
  }

  /**
   * Deletion of assertions for an existent contest with no assertions will remove no records.
   */
  @Test
  @Transactional
  void deleteAssertionsExistentContestNoAssertions(){
    testUtils.log(logger, "deleteAssertionsExistentContestNoAssertions");
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
    testUtils.log(logger, "translateAndSaveNoAssertions");
    String[] candidates = {"Alice", "Bob", "Charlie"};
    AssertionAndDifficulty[] empty = {};
    assertionRepository.translateAndSaveAssertions("Ballina Mayoral", 11000,
        candidates, empty);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        assertionRepository.getAssertionsThrowError("Ballina Mayoral"));
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT, ex.errorCode);
  }

  /**
   * Test retrieval of assertions for a succession of contests:
   * - "One NEB Assertion Contest"
   * - "One NEN Assertion Contest"
   * - "One NEN NEB Assertion Contest"
   * - "Multi-County Contest 1"
   * Note that this test is not designed to pass on its own, it is designed to be run
   * after translateAndSaveAssertions.
   */
  @Test
  @Transactional
  void translateAndSaveAssertions() throws RaireServiceException {
    testUtils.log(logger, "translateAndSaveAssertions");
    saveAssertionsOneNEBContest();
    List<Assertion> retrieved = assertionRepository.getAssertionsThrowError(
        "One NEB Assertion Contest");
    assertEquals(1, retrieved.size());

    List<Assertion> retrieved1 = assertionRepository.findByContestName("One NEB Assertion Contest");
    assertEquals(1, retrieved1.size());

    // Alice NEB Bob
    verifyAliceNEBBob(retrieved.get(0));
    verifyAliceNEBBob(retrieved1.get(0));

    saveAssertionsOneNENContest();
    retrieved = assertionRepository.getAssertionsThrowError("One NEN Assertion Contest");
    assertEquals(1, retrieved.size());

    retrieved1 = assertionRepository.findByContestName("One NEN Assertion Contest");
    assertEquals(1, retrieved1.size());

    // Alice NEN Charlie given Alice, Charlie, Diego and Bob remain.
    verifyAliceNENCharlie(retrieved.get(0));
    verifyAliceNENCharlie(retrieved1.get(0));

    saveAssertionsOneNENOneNEBContest();
    retrieved = assertionRepository.getAssertionsThrowError("One NEN NEB Assertion Contest");
    assertEquals(2, retrieved.size());

    retrieved1 = assertionRepository.findByContestName("One NEN NEB Assertion Contest");
    assertEquals(2, retrieved1.size());

    // Amanda NEB Liesel
    Assertion r1 = retrieved.get(0);
    verifyAmandaNEBLiesl(r1);

    r1 = retrieved1.get(0);
    verifyAmandaNEBLiesl(r1);

    // Amanda NEN Wendell given Amanda, Wendell and Liesl are continuing.
    Assertion r2 = retrieved.get(1);
    verifyAmandaNENWendell(r2);

    r2 = retrieved1.get(1);
    verifyAmandaNENWendell(r2);

    saveAssertionsMultiCountyContest();
    retrieved = assertionRepository.getAssertionsThrowError("Multi-County Contest 1");
    assertEquals(3, retrieved.size());

    retrieved1 = assertionRepository.findByContestName("Multi-County Contest 1");
    assertEquals(3, retrieved.size());

    // Charlie C. Chaplin NEB Alice P. Mangrove
    r1 = retrieved.get(0);
    verifyCharlieCNEBAliceM(r1);

    r1 = retrieved1.get(0);
    verifyCharlieCNEBAliceM(r1);

    // Alice P. Mangrove NEB Al (Bob) Jones
    r2 = retrieved.get(1);
    verifyAliceMNEBAlJones(r2);

    r2 = retrieved1.get(1);
    verifyAliceMNEBAlJones(r2);

    // Alice P. Mangrove NEN West W. Westerson given only they remain.
    Assertion r3 = retrieved.get(2);
    verifyAliceMNENWestW(r3);

    r3 = retrieved1.get(2);
    verifyAliceMNENWestW(r3);
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
    testUtils.log(logger, "translateAndSaveNegativeUniverseSizeNEB");
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        0, 2);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
      assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
        -1000, aliceCharlieBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "assertion must have a positive universe size"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: zero universe size for an
   * NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveZeroUniverseSizeNEB(){
    testUtils.log(logger, "translateAndSaveZeroUniverseSizeNEB");
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        0, 2);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            0, aliceCharlieBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "assertion must have a positive universe size"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: negative universe size for an
   * NEN assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNegativeUniverseSizeNEN(){
    testUtils.log(logger, "translateAndSaveNegativeUniverseSizeNEN");
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            -2000, aliceCharlieDiegoBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "assertion must have a positive universe size"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: zero universe size for an
   * NEN assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveZeroUniverseSizeNEN(){
    testUtils.log(logger, "translateAndSaveZeroUniverseSizeNEN");
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            0, aliceCharlieDiegoBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "assertion must have a positive universe size"));
  }


  /**
   * Test translateAndSaveAssertions when passed invalid data: negative margin for an
   * NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNegativeMarginNEB(){
    testUtils.log(logger, "translateAndSaveNegativeMarginNEB");
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(-100,
        0, 2);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "non-negative margin"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: negative margin for an
   * NEN assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNegativeMarginNEN(){
    testUtils.log(logger, "translateAndSaveNegativeMarginNEN");
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(-100,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "negative margin"));
  }


  /**
   * Test translateAndSaveAssertions when passed invalid data: margin larger than universe size
   * for an NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNegativeMarginTooHighNEB(){
    testUtils.log(logger, "translateAndSaveNegativeMarginTooHighNEB");
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(2000,
        0, 2);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "less than universe size"));
  }


  /**
   * Test translateAndSaveAssertions when passed invalid data:  margin larger than universe size
   * for an NEN assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveMarginTooHighNEN(){
    testUtils.log(logger, "translateAndSaveMarginTooHighNEN");
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(3000,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "less than universe size"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: same winner and loser
   * for an NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveSameWinnerLoserNEB(){
    testUtils.log(logger, "translateAndSaveSameWinnerLoserNEB");
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        1, 1);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "must not be the same candidate"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: same winner and loser
   * for an NEB assertion. An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveSameWinnerLoserNEN(){
    testUtils.log(logger, "translateAndSaveSameWinnerLoserNEN");
    int[] continuing = {0, 1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 0, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "must not be the same candidate"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: winner of NEN is not continuing.
   * An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNENWinnerNotContinuing(){
    testUtils.log(logger, "translateAndSaveNENWinnerNotContinuing");
    int[] continuing = {1 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "must also be continuing"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: loser of NEN is not continuing.
   * An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveNENLoserNotContinuing(){
    testUtils.log(logger, "translateAndSaveNENLoserNotContinuing");
    int[] continuing = {0 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 1, continuing);

    Exception ex = assertThrows(IllegalArgumentException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));

    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "must also be continuing"));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: winner index is outside the bounds
   * of the candidate array (NEB). An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveWinnerOutOfBoundsNEB(){
    testUtils.log(logger, "translateAndSaveWinnerOutOfBoundsNEB");
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        5, 2);

    assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: winner index is outside the bounds
   * of the candidate array (NEN). An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveWinnerOutOfBoundsNEN(){
    testUtils.log(logger, "translateAndSaveWinnerOutOfBoundsNEN");
    int[] continuing = {0 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        5, 1, continuing);

    assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: loser index is outside the bounds
   * of the candidate array (NEB). An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveLoserOutOfBoundsNEB(){
    testUtils.log(logger, "translateAndSaveLoserOutOfBoundsNEB");
    AssertionAndDifficulty[] assertions = formAssertionsOneNEBAssertionContest(320,
        2, 7);

    assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEB Assertion Contest",
            1000, aliceCharlieBob, assertions));
  }

  /**
   * Test translateAndSaveAssertions when passed invalid data: loser index is outside the bounds
   * of the candidate array (NEN). An IllegalArgumentException should be thrown.
   */
  @Test
  @Transactional
  void translateAndSaveLoserOutOfBoundsNEN(){
    testUtils.log(logger, "translateAndSaveLoserOutOfBoundsNEN");
    int[] continuing = {0 ,2, 3};
    AssertionAndDifficulty[] assertions = formAssertionsOneNENAssertionContest(240,
        0, 7, continuing);

    assertThrows(ArrayIndexOutOfBoundsException.class, () ->
        assertionRepository.translateAndSaveAssertions("One NEN Assertion Contest",
            2000, aliceCharlieDiegoBob, assertions));
  }
}
