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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests to validate the behaviour of CVRContestInfo retrieval. Contest, CVR and CVRContestInfo's
 * are preloaded into the test database from src/test/resources/data.sql.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class CVRContestInfoRepositoryTests {

  @Autowired
  CVRContestInfoRepository cvrContestInfoRepository;

  /**
   * Test that an empty list of vote data is returned when we try to retrieve vote information
   * for a non-existent contest (non-existent county AND contest ID).
   */
  @Test
  @Transactional
  void retrieveCVRsNonExistentContestCounty() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(0, 0);
    assertTrue(retrieved.isEmpty());
  }

  /**
   * Test that an empty list of vote data is returned when we try to retrieve vote information
   * for a non-existent contest (existent county ID and non-existent contest ID).
   */
  @Test
  @Transactional
  void retrieveCVRsNonExistentContestExistentCounty() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(4, 8);
    assertTrue(retrieved.isEmpty());
  }

  /**
   * Test that an empty list of vote data is returned when we try to retrieve vote information
   * for a non-existent contest (non existent county ID and existent contest ID).
   */
  @Test
  @Transactional
  void retrieveCVRsNonExistentCountyExistentContest() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(999990, 0);
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

  /**
   * Test retrieval of CVRContestInfo's for a single county contest with just one CVR.
   */
  @Test
  @Transactional
  void retrieveCVRsOneCVRSingleCountyContest() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(999998, 8);
    assertEquals(1, retrieved.size());
    String[] choices = {"Alice", "Bob", "Charlie"};
    assertArrayEquals(choices, retrieved.get(0));
  }

  /**
   * Test retrieval of CVRContestInfo's for a multi-county contest (one expected result).
   */
  @Test
  @Transactional
  void retrieveCVRsOneCVRMultiCountyContest() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(999988, 10);
    assertEquals(1, retrieved.size());
    String[] choices = {"Harold Holt","Wendy Squires","(B)(C)(D)"};
    assertArrayEquals(choices, retrieved.get(0));
  }

  /**
   * Test retrieval of CVRContestInfo's for a contest where the CVRs specify votes for multiple
   * contests (one expected result).
   */
  @Test
  @Transactional
  void retrieveCVRsMultiVoteCVR() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(999997, 8);
    assertEquals(1, retrieved.size());
    String[] choices = {"Laurie M.", "Bonny Smith", "Thomas D'Angelo"};
    assertArrayEquals(choices, retrieved.get(0));
  }


  /**
   * Test retrieval of CVRContestInfo's for a contest with multiple expected results (multi-county
   * contest).
   */
  @Test
  @Transactional
  void retrieveMultipleCVRContestInfo1() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(999990, 8);
    assertEquals(3, retrieved.size());

    String[] choice1 = {"Charlie C. Chaplin", "West W. Westerson"};
    String[] choice2 = {"West W. Westerson"};
    String[] choice3 = {"Al (Bob) Jones","West W. Westerson","Charlie C. Chaplin"};

    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice1)));
    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice2)));
    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice3)));
  }


  /**
   * Test retrieval of CVRContestInfo's for a contest with multiple expected results (multi-county
   * contest).
   */
  @Test
  @Transactional
  void retrieveMultipleCVRContestInfo2() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(999991, 9);
    assertEquals(3, retrieved.size());

    String[] choice1 = {"Alice P. Mangrove"};
    String[] choice2 = {"Charlie C. Chaplin"};
    String[] choice3 = {"West W. Westerson","Al (Bob) Jones"};

    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice1)));
    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice2)));
    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice3)));
  }


  /**
   * Test retrieval of CVRContestInfo's for a contest with multiple expected results (single-county
   * contest).
   */
  @Test
  @Transactional
  void retrieveMultipleCVRContestInfoSingleCountyContest() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(999999, 10);
    assertEquals(4, retrieved.size());

    String[] choice1 = {"A","B","CC"};
    String[] choice2 = {"B","CC"};
    String[] choice3 = {"CC"};
    String[] choice4 = {"CC","A","B"};

    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice1)));
    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice2)));
    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice3)));
    assertTrue(retrieved.stream().anyMatch(c -> Arrays.equals(c, choice4)));
  }

  /**
   * Test retrieval of CVRContestInfo's for a contest where at least one of the matching
   * cvr_contest_info table records has malformed data for its choice string. For this test,
   * the single matching cvr_contest_info row has null as its choice string.
   */
  @Test
  @Transactional
  void malformedChoiceStringIsNull1() {
    Exception ex = assertThrows(JpaSystemException.class, () ->
        cvrContestInfoRepository.getCVRs(999987, 11));
    assertTrue(ex.getMessage().toLowerCase().
        contains("Error attempting to apply AttributeConverter".toLowerCase()));
  }

  /**
   * Test retrieval of CVRContestInfo's for a contest where at least one of the matching
   * cvr_contest_info table records has malformed data for its choice string. For this test,
   * the single matching cvr_contest_info row has NULL as its choice string.
   */
  @Test
  @Transactional
  void malformedChoiceStringIsNull2() {
    Exception ex = assertThrows(JpaSystemException.class, () ->
        cvrContestInfoRepository.getCVRs(999986, 11));
    assertTrue(ex.getMessage().toLowerCase().
        contains("Error attempting to apply AttributeConverter".toLowerCase()));
  }

  /**
   * Test retrieval of CVRContestInfo's for a contest where at least one of the matching
   * cvr_contest_info table records has a blank vote for its choice string. This choice string
   * is still a valid JSON list (ie. '[]').
   */
  @Test
  @Transactional
  void blankVoteChoiceString() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(999985, 11);
    assertEquals(1, retrieved.size());
    assertEquals(0, retrieved.get(0).length);
  }

  /**
   * Test retrieval of CVRContestInfo's for a contest where at least one of the matching
   * cvr_contest_info table records has some text that isn't a list for its choice string.
   */
  @Test
  @Transactional
  void nonChoiceListVote1() {
    Exception ex = assertThrows(JpaSystemException.class, () ->
        cvrContestInfoRepository.getCVRs(999984, 11));
    assertTrue(ex.getMessage().toLowerCase().
        contains("Error attempting to apply AttributeConverter".toLowerCase()));
  }

  /**
   * Test retrieval of CVRContestInfo's for a contest where at least one of the matching
   * cvr_contest_info table records has some text that isn't a list for its choice string.
   * In this case the string is empty ('').
   */
  @Test
  @Transactional
  void nonChoiceListVote2() {
    Exception ex = assertThrows(JpaSystemException.class, () ->
        cvrContestInfoRepository.getCVRs(999983, 11));
    assertTrue(ex.getMessage().toLowerCase().
        contains("Error attempting to apply AttributeConverter".toLowerCase()));
  }
}
