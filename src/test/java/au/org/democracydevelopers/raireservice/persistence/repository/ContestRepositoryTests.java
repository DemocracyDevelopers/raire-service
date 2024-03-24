/*
Copyright 2024 Democracy Developers
The Raire Service is designed to connect colorado-rla and its associated database to
the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).

This file is part of raire-service.
raire-service is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License along with ConcreteSTV. If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.persistence.repository;

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Tests on contest retrieval.
 * Contests are pre-loaded into the database using data.sql.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ContestRepositoryTests {
  @Autowired
  ContestRepository contestRepository;

  private static final String ballinaMayoral = "Ballina Mayoral";

  // Retrieval of a non-existent contest name retrieves nothing
  @Test
  @Transactional
  void retrieveZeroContests() {
    Optional<Contest> retrieved = contestRepository.findFirstByName("nonExistentContest");
    assertTrue(retrieved.isEmpty());
  }

  // Retrieving Ballina Mayoral by name works as expected
  @Test
  @Transactional
  void retrieveBallinaMayoral() {
    Optional<Contest> ballina = contestRepository.findFirstByName(ballinaMayoral);

    assertTrue(ballina.isPresent());
    assertEquals(ballinaMayoral, ballina.get().name);
    assertEquals("IRV", ballina.get().description);
    assertEquals(8L, ballina.get().countyID);
    assertEquals(0L, ballina.get().version);
  }

  // Retrieving "Valid Plurality Contest" by name works as expected.
  @Test
  @Transactional
  void retrievePlurality() {
    Optional<Contest> plurality = contestRepository.findFirstByName("Valid Plurality Contest");
    assertTrue(plurality.isPresent());
    assertEquals("Valid Plurality Contest",plurality.get().name);
    assertEquals("Plurality", plurality.get().description);
    assertEquals(10L, plurality.get().countyID);
    assertEquals(0L, plurality.get().version);
  }

  // Retrieving Ballina Mayor by contestID and countyID works as expected.
  @Test
  @Transactional
  void retrieveByCountyAndContestID() {
    List<Contest> byIDs = contestRepository.findByContestAndCountyID(999991L, 8L);
    assertEquals(1, byIDs.size());
    Contest retrievedContest = byIDs.getFirst();
    assertEquals(ballinaMayoral, retrievedContest.name);
  }

  //
}
