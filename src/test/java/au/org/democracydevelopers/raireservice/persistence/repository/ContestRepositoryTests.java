package au.org.democracydevelopers.raireservice.persistence.repository;

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContestRepositoryTests {
  @Autowired
  ContestRepository contestRepository;

  @Test
  void retrieveZeroContests() {
    List<Contest> retrieved = contestRepository.findByName("nonExistentContest");
    assertEquals(0, retrieved.size());
  }

  // Scratch test which, obviously, only works when this contest is already in the database.
  @Test
  void retrieveBallinaMayoral() {
    List<Contest> ballina = contestRepository.findByName("Ballina Mayoral");
    assertEquals(1, ballina.size());
  }

  @Test
  void retrieveByCountyAndContestID() {
    List<Contest> bellingen = contestRepository.findByContestAndCountyID(3856102L, 2L);
    assertEquals(1, bellingen.size());
  }
}
