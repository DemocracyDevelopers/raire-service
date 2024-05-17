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

import static au.org.democracydevelopers.raireservice.testUtils.correctDBAssertionData;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.service.Metadata;
import au.org.democracydevelopers.raireservice.testUtils;
import java.math.BigDecimal;
import java.util.Collections;
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
 * src/test/resources/simple_assertions.sql.
 */
@ActiveProfiles("simple-assertions")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class AssertionRepositorySimpleAssertionsTests {

  private static final Logger logger = LoggerFactory.getLogger(
      AssertionRepositorySimpleAssertionsTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * Retrieve assertions for a contest that has one NEB assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNEBAssertion(){
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNEBAssertion");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEB Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    assertEquals(NEBAssertion.class, r.getClass());

    assertTrue(correctDBAssertionData(1, 320, 0.32, 1.1,
        "Alice", "Bob", List.of(), Collections.emptyMap(), 0,
        0, 0, 0, 0,
        0, 0, BigDecimal.valueOf(1),
        "One NEB Assertion Contest", r));
  }

  /**
   * Retrieve assertions for a contest that has one NEN assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENAssertion(){
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNENAssertion");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    assertEquals(NENAssertion.class, r.getClass());

    assertTrue(correctDBAssertionData(2, 240, 0.12, 3.01,
        "Alice", "Charlie", List.of("Alice", "Charlie", "Diego", "Bob"),
        Collections.emptyMap(), 0, 0, 0,
        0, 0, 0, 0,
        BigDecimal.valueOf(1), "One NEN Assertion Contest", r));
  }

  /**
   * Test NENAssertion::convert().
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENAssertionConvert(){
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNENAssertionConvert");
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

    // Check that current risk is 1
    assertEquals(0, BigDecimal.valueOf(1).compareTo(
        ((BigDecimal)aad.status.get(Metadata.STATUS_RISK))));
  }


  /**
   * Retrieve assertions for a contest that has one NEN and one NEB assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENOneNEBAssertion(){
    testUtils.log(logger, "retrieveAssertionsExistentContestOneNENOneNEBAssertion");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN NEB Assertion Contest");
    assertEquals(2, retrieved.size());

    final Assertion r1 = retrieved.get(0);
    assertEquals(NEBAssertion.class, r1.getClass());
    assertTrue(correctDBAssertionData(3, 112, 0.1, 0.1,
        "Amanda", "Liesl", List.of(), Collections.emptyMap(), 0,
        0, 0, 0, 0,
        0, 0, BigDecimal.valueOf(1),
        "One NEN NEB Assertion Contest", r1));

    final Assertion r2 = retrieved.get(1);
    assertEquals(NENAssertion.class, r2.getClass());
    assertTrue(correctDBAssertionData(4, 560, 0.5, 3.17,
        "Amanda", "Wendell", List.of("Liesl", "Wendell", "Amanda"),
        Collections.emptyMap(), 0, 0, 0,
        0, 0, 0, 0,
        BigDecimal.valueOf(1), "One NEN NEB Assertion Contest", r2));
  }


  /**
   * Retrieve assertions for a multi-county contest.
   */
  @Test
  @Transactional
  void retrieveAssertionsMultiCountyContest(){
    testUtils.log(logger, "retrieveAssertionsMultiCountyContest");
    List<Assertion> retrieved = assertionRepository.findByContestName("Multi-County Contest 1");
    assertEquals(3, retrieved.size());

    final Assertion r1 = retrieved.get(0);
    assertEquals(NEBAssertion.class, r1.getClass());
    assertTrue(correctDBAssertionData(5, 310, 0.01, 2.1,
        "Charlie C. Chaplin", "Alice P. Mangrove", List.of(), Collections.emptyMap(),
        0, 0, 0, 0,
        0, 0, 0, BigDecimal.valueOf(1),
        "Multi-County Contest 1", r1));

    final Assertion r2 = retrieved.get(1);
    assertEquals(NEBAssertion.class, r2.getClass());
    assertTrue(correctDBAssertionData(6, 2170, 0.07, 0.9,
        "Alice P. Mangrove", "Al (Bob) Jones", List.of(), Collections.emptyMap(),
        0, 0, 0, 0,
        0, 0, 0, BigDecimal.valueOf(1),
        "Multi-County Contest 1", r2));

    final Assertion r3 = retrieved.get(2);
    assertEquals(NENAssertion.class, r3.getClass());
    assertTrue(correctDBAssertionData(7, 31, 0.001, 5.0,
        "Alice P. Mangrove", "West W. Westerson", List.of("West W. Westerson",
            "Alice P. Mangrove"), Collections.emptyMap(), 0,
        0, 0, 0, 0,
        0, 0, BigDecimal.valueOf(1),
        "Multi-County Contest 1", r3));
  }

  /**
   * Deletion of assertions for an existent contest with one NEB assertion will remove one record.
   * The contest will then have no associated assertions in the database.
   */
  @Test
  @Transactional
  void deleteAssertionsExistentContestOneNEBAssertion(){
    testUtils.log(logger, "deleteAssertionsExistentContestOneNEBAssertion");
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
    testUtils.log(logger, "deleteAssertionsExistentContestOneNENAssertion");
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
    testUtils.log(logger, "deleteAssertionsExistentContestOneNENOneNEBAssertion");
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
    testUtils.log(logger, "deleteAssertionsMultiCountyContest");
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
    testUtils.log(logger, "testAutoIncrementOfIDs");
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
    assertTrue(correctDBAssertionData(8, 25, 0.0125, 5.0,
        "CC", "A", List.of(), Collections.emptyMap(),
        0, 0, 0, 0,
        0, 0, 0, BigDecimal.valueOf(1),
        "Larger Contest", r1));

    Assertion r2 = retrieved.get(1);
    assertEquals(NENAssertion.class, r2.getClass());
    assertTrue(correctDBAssertionData(9, 100, 0.05, 2.0,
        "CC", "B", List.of("A", "B", "CC"), Collections.emptyMap(),
        0, 0, 0, 0,
        0, 0, 0, BigDecimal.valueOf(1),
        "Larger Contest", r2));
  }

}
