package au.org.democracydevelopers.raireservice;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import au.org.democracydevelopers.raireservice.repository.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.repository.entity.NENAssertion;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Tests for Springboot functioning. This class automatically fires up the RAIRE Microservice on a random port,
 * and an independent version of the database, then runs
 * a series of (at this stage) very basic tests.
 * Note that you have to run the *whole class*. Individual tests do not work separately because they don't initiate the
 * microservice on their own.
 *
 * Based on instructions from https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class GetAssertionsTestsWithDatabase {
    private final static String host = "http://localhost:";
    private final static String auditEndpoint = "/raire/get-assertions";

    @LocalServerPort
    private int port;


    @Autowired
    private TestRestTemplate restTemplate;
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("integration-tests-db")
            .withUsername("testUser")
            .withPassword("testPwd");

      @BeforeAll
  static void beforeAll() {
    postgres.start();
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @Autowired
  AssertionRepository assertionRepository;

  @BeforeEach
  void setUp() {
      assertionRepository.deleteAll();
  }

    /*
     * This is really just a test that the testing is working.
     * There's no mapping for the plain localhost response, so when the microservice is running it just returns
     * a default error. We check for 404 because that appears in the default error text.
     */
    @Test
    public void testErrorForNonFunctioningEndpoint() {
        assertTrue((restTemplate.getForObject(host + port + "/",
                String.class)).contains("404"));
    }

  @Test
    void saveAndRetrieveSomeAssertions() {
      List<String> continuing = List.of("Alice","Bob");
      String testContestName = "testContest";
      List<Assertion> exampleAssertions = List.of(
        new NENAssertion(testContestName, "Alice", "Bob", 10, 10000, 200, continuing),
        new NEBAssertion(testContestName, "Alice", "Chuan", 15, 10000, 150)
      );

      assertionRepository.saveAll(exampleAssertions);

             HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = host +port + auditEndpoint;

        HttpEntity<String> request = new HttpEntity<>("{\"contestName\": \""+testContestName+"\", " +
                " \"candidates\": [\"Alice\",\"Bob\",\"Chuan\"], " +
                " \"riskLimit\": 0.03 " +
                "}", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class );

        assertTrue(Objects.requireNonNull(response.getBody()).contains("solution"));

  }
}