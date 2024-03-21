package au.org.democracydevelopers.raireservice.persistence.repository;

import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test-containers")
// @DataJpaTest
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AssertionsTestsWithDatabase {
  @Autowired
  ContestRepository contestRepository;

  @Test
  void retrieveZeroContests() {
    List<Contest> retrieved = contestRepository.findByContestName("nonExistentContest");
    assertEquals(0, retrieved.size());
  }

  /*
  @Test
  void WhenAssertionsAreSavedTheRightNumberAreRetrieved() {
    List<String> continuing = List.of("Alice", "Bob");
    String testContestName = "testContest";
    List<Assertion> exampleAssertions = List.of(
        new NENAssertion(testContestName, "Alice", "Bob", 10, 10000, 200, continuing),
        new NEBAssertion(testContestName, "Alice", "Chuan", 15, 10000, 150)
    );

    assertionRepository.saveAll(exampleAssertions);

    List<Assertion> retrieved = assertionRepository.findByContestName(testContestName);
    assertEquals(2, retrieved.size());
  }
  */
}
