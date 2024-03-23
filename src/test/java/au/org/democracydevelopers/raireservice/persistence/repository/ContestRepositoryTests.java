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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * tests on contest retrieval - currently very basic.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class ContestRepositoryTests {
  @Autowired
  ContestRepository contestRepository;

  private static final String ballinaMayoral = "Ballina Mayoral";
  private static final Contest ballinaMayoralContest = new Contest("IRV",ballinaMayoral, 1L, 0L);

  // Test that retrieval of a non-existent contest name retrieves nothing
  @Test
  @Transactional
  void retrieveZeroContests() {
    List<Contest> retrieved = contestRepository.findByName("nonExistentContest");
    assertEquals(0, retrieved.size());
  }

  // Test that retrieving Ballina Mayoral works as expected
  @Test
  @Transactional
  void retrieveBallinaMayoral() {
    contestRepository.saveAndFlush(ballinaMayoralContest);
    List<Contest> ballina = contestRepository.findByName(ballinaMayoral);

    assertEquals(1, ballina.size());
    assertEquals(ballinaMayoral, ballina.getFirst().name);
    assertEquals("IRV", ballina.getFirst().description);
    assertEquals(1L, ballina.getFirst().countyID);
    assertEquals(0L, ballina.getFirst().version);
  }

  /*
  ** FIXME This ought to work, because the relevant record has been loaded in via data.sql. However,
  *   it does not seem able to find it.
  @Test
  void retrieveByron() {
    List<Contest> byron = contestRepository.findByName("Byron");
    assertEquals(1,byron.size());
  }
  */

  // Test that retrieving Ballina Councillor by county and contestID works as expected.
  // We don't know what order the contests will be inserted, so this test retrieves it by name and
  // then checks that the same thing is retrieved by IDs.
  @Test
  @Transactional
  void retrieveByCountyAndContestID() {
    contestRepository.deleteAll();
    contestRepository.saveAndFlush(ballinaMayoralContest);
    List<Contest> byName = contestRepository.findByName(ballinaMayoral);
    assertEquals(1, byName.size());
    Contest contest = byName.getFirst();

    List<Contest> byIDs = contestRepository.findByContestAndCountyID(contest.id, contest.countyID);

    assertEquals(1, byIDs.size());
    Contest retrievedContest = byIDs.getFirst();
    assertEquals(retrievedContest.name, contest.name);
  }
}
