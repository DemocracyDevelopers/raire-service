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

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import au.org.democracydevelopers.raireservice.testUtils;
import java.util.Optional;
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

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for contest retrieval.
 * Contests are preloaded into the database using src/test/resources/data.sql.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ContestRepositoryTests {

  private static final Logger logger = LoggerFactory.getLogger(ContestRepositoryTests.class);

  @Autowired
  ContestRepository contestRepository;

  private static final String ballinaMayoral = "Ballina Mayoral";

  /**
   * Retrieval of a non-existent contest name retrieves nothing.
   */
  @Test
  @Transactional
  void retrieveZeroContests() {
    testUtils.log(logger, "retrieveZeroContests");
    Optional<Contest> retrieved = contestRepository.findFirstByName("nonExistentContest");
    assertTrue(retrieved.isEmpty());
  }

  /**
   * Retrieval of all of a non-existent contest name retrieves nothing.
   */
  @Test
  @Transactional
  void retrieveAllZeroContests() {
    testUtils.log(logger, "retrieveAllZeroContests");
    List<Contest> retrieved = contestRepository.findByName("nonExistentContest");
    assertTrue(retrieved.isEmpty());
  }

  /**
   * Retrieving Ballina Mayoral by name works as expected.
   */
  @Test
  @Transactional
  void retrieveBallinaMayoral() {
    testUtils.log(logger, "retrieveBallinaMayoral");
    Optional<Contest> ballina = contestRepository.findFirstByName(ballinaMayoral);

    assertTrue(ballina.isPresent());
    assertEquals(ballinaMayoral, ballina.get().getName());
    assertEquals("IRV", ballina.get().getDescription());
    assertEquals(8L, ballina.get().getCountyID());
  }

  /**
   * Retrieving all matching Ballina Mayoral by name returns one item.
   */
  @Test
  @Transactional
  void retrieveAllBallinaMayoral() {
    testUtils.log(logger, "retrieveAllBallinaMayoral");
    List<Contest> ballina = contestRepository.findByName(ballinaMayoral);
    assertEquals(1, ballina.size());
  }

  /**
   * Retrieving all matching "Invalid Mixed Contest" by name returns both items.
   */
  @Test
  @Transactional
  void retrieveAllInvalidMixed() {
    testUtils.log(logger, "retrieveAllInvalidMixed");
    List<Contest> mixed = contestRepository.findByName("Invalid Mixed Contest");
    assertEquals(2, mixed.size());
  }

  /**
   * Retrieving "Valid Plurality Contest" by name works as expected.
   */
  @Test
  @Transactional
  void retrievePlurality() {
    testUtils.log(logger, "retrievePlurality");
    Optional<Contest> plurality = contestRepository.findFirstByName("Valid Plurality Contest");
    assertTrue(plurality.isPresent());
    assertEquals("Valid Plurality Contest",plurality.get().getName());
    assertEquals("Plurality", plurality.get().getDescription());
    assertEquals(10L, plurality.get().getCountyID());
  }

  /**
   * Retrieving all of "Valid Plurality Contest" by name works as expected.
   */
  @Test
  @Transactional
  void retrieveAllPlurality() {
    testUtils.log(logger, "retrieveAllPlurality");
    List<Contest> plurality = contestRepository.findByName("Valid Plurality Contest");
    assertEquals(1, plurality.size());
  }

  /**
   * Retrieving Ballina Mayor by contestID and countyID works as expected.
   */
  @Test
  @Transactional
  void retrieveByCountyAndContestID() {
    testUtils.log(logger, "retrieveByCountyAndContestID");
    Optional<Contest> byIDs = contestRepository.findByContestAndCountyID(999992L, 8L);
    assertTrue(byIDs.isPresent());
    Contest retrievedContest = byIDs.get();
    assertEquals(ballinaMayoral, retrievedContest.getName());
  }

  /**
   * Retrieving Byron by the right contestID but wrong countyID returns nothing.
   */
  @Test
  @Transactional
  void retrieveWronglyByCountyAndContestID() {
    testUtils.log(logger, "retrieveWronglyByCountyAndContestID");
    Optional<Contest> byIDs = contestRepository.findByContestAndCountyID(999992L, 1L);
    assertTrue(byIDs.isEmpty());
  }

  /**
   * Retrieving Byron by the wrong contestID but right countyID returns nothing.
   */
  @Test
  @Transactional
  void retrieveWronglyByCountyAndContestID2() {
    testUtils.log(logger, "retrieveWronglyByCountyAndContestID2");
    Optional<Contest> byIDs = contestRepository.findByContestAndCountyID(888881L, 8L);
    assertTrue(byIDs.isEmpty());
  }

  /**
   * A single IRV contest is correctly identified as all IRV.
   */
  @Test
  @Transactional
  void singleIRVIsAllIRV() {
    testUtils.log(logger, "singleIRVIsAllIRV");
    assertTrue(contestRepository.isAllIRV(ballinaMayoral));
  }

  /**
   * A non-existent contest is all IRV.
   */
  @Test
  @Transactional
  void multiCountyIRVIsAllIRV() {
    testUtils.log(logger, "multiCountyIRVIsAllIRV");
    assertTrue(contestRepository.isAllIRV("Non-existent Contest"));
  }

  /**
   * A single Plurality contest is not all IRV.
   */
  @Test
  @Transactional
  void SinglePluralityIsNotAllIRV() {
    testUtils.log(logger, "SinglePluralityIsNotAllIRV");
    assertFalse(contestRepository.isAllIRV("Valid Plurality Contest"));
  }


  /**
   * A (invalid) mixed contest is not all IRV.
   */
  @Test
  @Transactional
  void MixedDescriptionIsNotAllIRV() {
    testUtils.log(logger, "MixedDescriptionIsNotAllIRV");
    assertFalse(contestRepository.isAllIRV("Invalid Mixed Contest"));
  }
}
