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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests to validate the behaviour of CVRContestInfo retrieval.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class CVRContestInfoRepositoryTests {

  @Autowired
  CVRContestInfoRepository cvrContestInfoRepository;

  /**
   * Test that an empty list of vote data is returned when we try to retrieve vote information
   * for a non-existent contest (non existent county AND contest ID).
   */
  @Test
  @Transactional
  void retrieveCVRsNonExistentContestCounty() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(0, 0);
    assertTrue(retrieved.isEmpty());
  }

  /**
   * Test that an empty list of vote data is returned when we try to retrieve vote information
   * for a contest that exists but has no associated CVRs.
   */
  @Test
  @Transactional
  void retrieveCVRsExistentContestNoCVRs() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(999996, 10);
    assertTrue(retrieved.isEmpty());
  }
}
