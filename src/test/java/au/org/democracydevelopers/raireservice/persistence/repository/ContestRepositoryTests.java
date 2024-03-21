package au.org.democracydevelopers.raireservice.persistence.repository;

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestExecution;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContestRepositoryTests {
  @Autowired
  ContestRepository contestRepository;

  private static Contest ballinaToSave;
  @BeforeAll
  public static void setup() {
    ballinaToSave = new Contest();
    ballinaToSave.setName("Ballina Mayoral");
    ballinaToSave.setDescription("IRV");
    ballinaToSave.setVersion(0L);
    ballinaToSave.setCountyID(1L);
  }

  @Test
  void retrieveZeroContests() {
    List<Contest> retrieved = contestRepository.findByName("nonExistentContest");
    assertEquals(0, retrieved.size());
  }

  @Test
  void retrieveBallinaMayoral() {
    contestRepository.save(ballinaToSave);

    List<Contest> ballina = contestRepository.findByName("Ballina Mayoral");
    assertEquals(1, ballina.size());
    assertEquals("Ballina Mayoral", ballina.get(0).getName());
    assertEquals("IRV", ballina.get(0).getDescription());
    assertEquals(1L, ballina.get(0).getCountyID());
    assertEquals(0L, ballina.get(0).getVersion());
  }

  @Test
  void retrieveByCountyAndContestID() {
    List<Contest> bellingen = contestRepository.findByContestAndCountyID(1L, 1L);
    assertEquals(1, bellingen.size());
  }
}
