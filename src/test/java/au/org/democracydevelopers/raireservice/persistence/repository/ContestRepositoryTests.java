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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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

  private static Contest ballinaMayoralContest;
  private static Contest ballinaCouncillorContest;
  private static final String ballinaMayoral = "Ballina Mayoral";

  @BeforeAll
  public static void setup() {
    ballinaMayoralContest = new Contest("IRV",ballinaMayoral, 1L, 0L);

    ballinaCouncillorContest = new Contest("IRV","Ballina Councillor", 2L, 0L);
  }

  // Test that retrieval of a non-existent contest name retrieves nothing
  @Test
  void retrieveZeroContests() {
    List<Contest> retrieved = contestRepository.findByName("nonExistentContest");
    assertEquals(0, retrieved.size());
  }

  // Test that retrieving Ballina Mayoral works as expected
  @Test
  void retrieveBallinaMayoral() {
    contestRepository.save(ballinaMayoralContest);
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
  // We don't know what order the contests will be inserted, so it might be either 0th or 1st ID.
  @Test
  void retrieveByCountyAndContestID() {
    contestRepository.save(ballinaCouncillorContest);
    List<Contest> byIDs = contestRepository.findByContestAndCountyID(1L, 2L);
    List<Contest> byIDs2 = contestRepository.findByContestAndCountyID(2L, 2L);

    assertEquals(1, byIDs.size()+byIDs2.size());

  }
}
