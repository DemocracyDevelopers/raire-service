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
   * Test assertion: Alice NEB Bob in the contest "One NEB Assertion Contest".
   */
  private String aliceNEBBob = "{\"id\":0,\"version\":0,\"contestName\":" +
      "\"One NEB Assertion Contest\",\"winner\":\"Alice\",\"loser\":\"Bob\",\"margin\":100," +
      "\"difficulty\":1.1,\"assumedContinuing\":[],\"dilutedMargin\":0.32,\"cvrDiscrepancy\":{}," +
      "\"estimatedSamplesToAudit\":0,\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0," +
      "\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Alice NEN Charlie assuming Alice, Charlie, Diego and Bob are continuing,
   * for the contest "One NEN Assertion Contest".
   */
  private String aliceNENCharlie = "{\"id\":1,\"version\":0,\"contestName\":" +
      "\"One NEN Assertion Contest\",\"winner\":\"Alice\",\"loser\":\"Charlie\",\"margin\":240," +
      "\"difficulty\":3.01,\"assumedContinuing\":[\"Alice\",\"Charlie\",\"Diego\",\"Bob\"]," +
      "\"dilutedMargin\":0.12,\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0," +
      "\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0," +
      "\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

  /**
   * Test assertion: Amanda NEB Liesl in the contest "One NEN NEB Assertion Contest".
   */
  private String amandaNEBLiesl = "{\"id\":2,\"version\":0,\"contestName\":" +
      "\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\",\"loser\":\"Liesl\",\"margin\":112,"+
      "\"difficulty\":0.1,\"assumedContinuing\":[],\"dilutedMargin\":0.52," +
      "\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0,\"twoVoteUnderCount\":0," +
      "\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0,\"twoVoteOverCount\":0,\"otherCount\":0," +
      "\"currentRisk\":1.00}";

  /**
   * Test assertion: Amanda NEN Wendell assuming Liesl, Wendell and Amanda are continuing,
   * for the contest "One NEN NEB Assertion Contest".
   */
  private String amandaNENWendell = "{\"id\":3,\"version\":0,\"contestName\":" +
      "\"One NEN NEB Assertion Contest\",\"winner\":\"Amanda\",\"loser\":\"Wendell\"," +
      "\"margin\":250,\"difficulty\":3.17,\"assumedContinuing\":[\"Liesl\",\"Wendell\"," +
      "\"Amanda\"],\"dilutedMargin\":0.72,\"cvrDiscrepancy\":{},\"estimatedSamplesToAudit\":0," +
      "\"twoVoteUnderCount\":0,\"oneVoteUnderCount\":0,\"oneVoteOverCount\":0," +
      "\"twoVoteOverCount\":0,\"otherCount\":0,\"currentRisk\":1.00}";

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
