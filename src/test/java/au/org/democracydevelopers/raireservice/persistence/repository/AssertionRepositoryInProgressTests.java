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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.service.Metadata;
import au.org.democracydevelopers.raireservice.testUtils;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

  private static final Logger logger = LoggerFactory.getLogger(
      AssertionRepositoryInProgressTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * Retrieve assertions for a contest that has one NEB assertion (audit in progress with no
   * discrepancies observed).
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNEBAssertionInProgress(){
    testUtils.log(logger, "retrieveAssertionsOneNEBAssertionInProgress");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEB Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    assertEquals(NEBAssertion.class, r.getClass());

    assertTrue(correctDBAssertionData(1, 320, 0.32, 1.1,
        "Alice", "Bob", List.of(), Collections.emptyMap(), 111,
        111, 0, 0, 0,
        0, 0, BigDecimal.valueOf(0.5),
        "One NEB Assertion Contest", r));
  }

  /**
   * Test NEBAssertion::convert().
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNEBAssertionConvert(){
    testUtils.log(logger, "retrieveAssertionsOneNEBAssertionConvert");
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
    assertEquals(0, BigDecimal.valueOf(0.50).compareTo(
        ((BigDecimal)aad.status.get(Metadata.STATUS_RISK))));
  }


  /**
   * Retrieve assertions for a contest that has one NEN assertion (audit in progress with some
   * discrepancies observed).
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNENAssertionInProgress(){
    testUtils.log(logger, "retrieveAssertionsOneNENAssertionInProgress");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    assertEquals(NENAssertion.class, r.getClass());

    assertTrue(correctDBAssertionData(2, 240, 0.12, 3.01,
        "Alice", "Charlie", List.of("Alice", "Charlie", "Diego", "Bob"),
        Map.of(13L, 1, 14L, 0, 15L, 0), 245,
        201, 0, 0, 1,
        0, 2, BigDecimal.valueOf(0.20),
        "One NEN Assertion Contest", r));
  }


  /**
   * Retrieve assertions for a contest that has one NEN and one NEB assertion (audit in progress
   * with some discrepancies observed).
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNENOneNEBAssertionInProgress(){
    testUtils.log(logger, "retrieveAssertionsOneNENOneNEBAssertionInProgress");
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEN NEB Assertion Contest");
    assertEquals(2, retrieved.size());

    final Assertion r1 = retrieved.get(0);
    assertTrue(correctDBAssertionData(3, 112, 0.1, 0.1,
        "Amanda", "Liesl", List.of(), Map.of(13L, -1, 14L, 2, 15L, 2),
        27, 20, 0, 1,
        0, 2, 0, BigDecimal.valueOf(0.08),
        "One NEN NEB Assertion Contest", r1));

    final Assertion r2 = retrieved.get(1);
    assertEquals(NENAssertion.class, r2.getClass());
    assertTrue(correctDBAssertionData(4, 560, 0.5, 3.17,
        "Amanda", "Wendell", List.of("Liesl", "Wendell", "Amanda"),
        Map.of(13L, 1, 14L, 1, 15L, -2), 300,
        200, 1, 0, 2,
        0, 0, BigDecimal.valueOf(0.70),
        "One NEN NEB Assertion Contest", r2));
  }
}
