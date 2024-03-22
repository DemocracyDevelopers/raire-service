package au.org.democracydevelopers.raireservice.persistence.repository;

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import java.util.Optional;
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
import org.springframework.test.context.jdbc.Sql;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContestRepositoryTests {
  @Autowired
  ContestRepository contestRepository;

  private static Contest ballinaMayoralContest;
  private static Contest ballinaCouncillorContest;
  private static String ballinaMayoral = "Ballina Mayoral";

  @BeforeAll
  public static void setup() {
    ballinaMayoralContest = new Contest();
    ballinaMayoralContest.setName(ballinaMayoral);
    ballinaMayoralContest.setDescription("IRV");
    ballinaMayoralContest.setVersion(0L);
    ballinaMayoralContest.setCountyID(1L);

    ballinaCouncillorContest = new Contest();
    ballinaCouncillorContest.setName("Ballina Councillor");
    ballinaCouncillorContest.setDescription("IRV");
    ballinaCouncillorContest.setVersion(0L);
    ballinaCouncillorContest.setCountyID(2L);
  }

  @Test
  void retrieveZeroContests() {
    List<Contest> retrieved = contestRepository.findByName("nonExistentContest");
    assertEquals(0, retrieved.size());
  }

  @Test
  void retrieveBallinaMayoral() {
    contestRepository.save(ballinaMayoralContest);
    List<Contest> ballina = contestRepository.findByName(ballinaMayoral);

    assertEquals(1, ballina.size());
    assertEquals(ballinaMayoral, ballina.get(0).getName());
    assertEquals("IRV", ballina.get(0).getDescription());
    assertEquals(1L, ballina.get(0).getCountyID());
    assertEquals(0L, ballina.get(0).getVersion());
  }

  @Test
  void retrieveByCountyAndContestID() {
    contestRepository.save(ballinaCouncillorContest);
    List<Contest> byIDs = contestRepository.findByContestAndCountyID(1L, 2L);
    List<Contest> byIDs2 = contestRepository.findByContestAndCountyID(2L, 2L);

    // We don't know what order the contests will be inserted, so it might be either 0th or 1st ID.
    assertEquals(1, byIDs.size()+byIDs2.size());
  }
}
