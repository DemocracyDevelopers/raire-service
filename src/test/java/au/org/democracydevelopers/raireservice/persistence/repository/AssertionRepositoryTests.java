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


import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
   * Retrieve assertions for a contest that has one NEB assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNEBAssertion(){
    List<Assertion> retrieved = assertionRepository.findByContestName("One NEB Assertion Contest");
    assertEquals(1, retrieved.size());

    final Assertion r = retrieved.get(0);
    assertEquals(NEBAssertion.class, r.getClass());
    assertEquals(0, r.getId());
    assertEquals("Alice", r.getWinner());
    assertEquals("Bob", r.getLoser());
    assertEquals("One NEB Assertion Contest", r.getContestName());
    assertEquals(100, r.getMargin());
    assertEquals(0, r.getCurrentRisk().compareTo(BigDecimal.valueOf(1)));
    assertEquals(1.1, r.getDifficulty());
    assertEquals(0.32, r.getDilutedMargin());
    assertEquals(0, r.getOneVoteOverCount());
    assertEquals(0, r.getOneVoteUnderCount());
    assertEquals(0, r.getTwoVoteOverCount());
    assertEquals(0, r.getTwoVoteUnderCount());
    assertEquals(0, r.getOtherDiscrepancies());
    assertEquals(0, r.getEstimatedSamplesToAudit());
    assertEquals(0, r.getCvrDiscrepancy().size());
    assertEquals(0, r.getAssumedContinuing().size());
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
    assertEquals(1, r.getId());
    assertEquals("Alice", r.getWinner());
    assertEquals("Charlie", r.getLoser());
    assertEquals("One NEN Assertion Contest", r.getContestName());
    assertEquals(240, r.getMargin());
    assertEquals(0, r.getCurrentRisk().compareTo(BigDecimal.valueOf(1)));
    assertEquals(3.01, r.getDifficulty());
    assertEquals(0.12, r.getDilutedMargin());
    assertEquals(0, r.getOneVoteOverCount());
    assertEquals(0, r.getOneVoteUnderCount());
    assertEquals(0, r.getTwoVoteOverCount());
    assertEquals(0, r.getTwoVoteUnderCount());
    assertEquals(0, r.getOtherDiscrepancies());
    assertEquals(0, r.getEstimatedSamplesToAudit());
    assertEquals(0, r.getCvrDiscrepancy().size());
    assertEquals(List.of("Diego", "Bob"), r.getAssumedContinuing());
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
    assertEquals(2, r1.getId());
    assertEquals("Amanda", r1.getWinner());
    assertEquals("Liesl", r1.getLoser());
    assertEquals("One NEN NEB Assertion Contest", r1.getContestName());
    assertEquals(112, r1.getMargin());
    assertEquals(0, r1.getCurrentRisk().compareTo(BigDecimal.valueOf(1)));
    assertEquals(0.1, r1.getDifficulty());
    assertEquals(0.52, r1.getDilutedMargin());
    assertEquals(0, r1.getOneVoteOverCount());
    assertEquals(0, r1.getOneVoteUnderCount());
    assertEquals(0, r1.getTwoVoteOverCount());
    assertEquals(0, r1.getTwoVoteUnderCount());
    assertEquals(0, r1.getOtherDiscrepancies());
    assertEquals(0, r1.getEstimatedSamplesToAudit());
    assertEquals(0, r1.getCvrDiscrepancy().size());
    assertEquals(0, r1.getAssumedContinuing().size());

    final Assertion r2 = retrieved.get(1);
    assertEquals(NENAssertion.class, r2.getClass());
    assertEquals(3, r2.getId());
    assertEquals("Amanda", r2.getWinner());
    assertEquals("Wendell", r2.getLoser());
    assertEquals("One NEN NEB Assertion Contest", r2.getContestName());
    assertEquals(250, r2.getMargin());
    assertEquals(0, r2.getCurrentRisk().compareTo(BigDecimal.valueOf(1)));
    assertEquals(3.17, r2.getDifficulty());
    assertEquals(0.72, r2.getDilutedMargin());
    assertEquals(0, r2.getOneVoteOverCount());
    assertEquals(0, r2.getOneVoteUnderCount());
    assertEquals(0, r2.getTwoVoteOverCount());
    assertEquals(0, r2.getTwoVoteUnderCount());
    assertEquals(0, r2.getOtherDiscrepancies());
    assertEquals(0, r2.getEstimatedSamplesToAudit());
    assertEquals(0, r2.getCvrDiscrepancy().size());
    assertEquals(List.of("Liesl"), r2.getAssumedContinuing());
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
}
