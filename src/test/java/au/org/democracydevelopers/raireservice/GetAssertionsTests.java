package au.org.democracydevelopers.raireservice;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raireservice.response.GetAssertionError;
import au.org.democracydevelopers.raireservice.response.GetAssertionResponse;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Tests for Springboot functioning. This class automatically fires up the RAIRE Microservice on a random port, then runs
 * a series of (at this stage) very basic tests.
 * Note that you have to run the *whole class*. Individual tests do not work separately because they don't initiate the
 * microservice on their own.
 */

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GetAssertionsTests {

    private final static String auditEndpoint = "/raire/get-assertions";

    @LocalServerPort
    private int port;


    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    /*
     * This is really just a test that the testing is working.
     * There's no mapping for the plain localhost response, so when the microservice is running it just returns
     * a default error. We check for 404 because that appears in the default error text.
     */
    @Test
    public void testErrorForNonFunctioningEndpoint() {
        assertTrue((restTemplate.getForObject("http://localhost:" + port + "/",
                String.class)).contains("404"));
    }

    /*
     * Check that calling the right endpoint with no header and no data produces a sensible error message.
     */
    @Test
    public void testMethodNotAllowed() {
        assertTrue((restTemplate.getForObject("http://localhost:" + port + auditEndpoint,
                String.class)).contains("405"));
        assertTrue((restTemplate.getForObject("http://localhost:" + port + auditEndpoint,
                String.class)).contains("Method Not Allowed"));
    }

    /*
     * The right endpoint, with correct headers but no data, should produce "Bad Request".
     */
    @Test
    public void testBadRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://localhost:" + port + auditEndpoint;

        HttpEntity<String> request = new HttpEntity<>("", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        assertTrue(Objects.requireNonNull(response.getBody()).contains("400"));
        assertTrue(response.getBody().contains("Bad Request"));
    }

    /*
     * At this stage, we are only testing that we get a solution.
     */
    @Test
    public void testTrivialExample() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://localhost:" +port + auditEndpoint;

        HttpEntity<String> request = new HttpEntity<>("{\"contestName\": \"TrivialExample1\", " +
                "\"totalAuditableBallots\": 15, " +
                " \"candidates\": [\"Alice\",\"Bob\"], " +
                " \"winner\": \"Alice\" " +
                "}", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class );

        assertTrue(Objects.requireNonNull(response.getBody()).contains("solution"));
    }

    /*
     * Test that the assertion list for a nonexistent contest is empty.
     * Note: FIXME This test is deliberately buggy for now, because I want to test the new testing automation.
     */
    @Test
    public void testExampleWithNoAssertions() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://localhost:" +port + auditEndpoint;

        HttpEntity<String> request = new HttpEntity<>("{\"contestName\": \"ImpossibleCantBeRealContestName982389273428\", " +
                "\"totalAuditableBallots\": 15, " +
                " \"candidates\": [\"Alice\",\"Bob\"], " +
                " \"winner\": \"Alice\" " +
                "}", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class );

        Gson gson = new Gson();
        String jsonString = response.getBody();
        GetAssertionResponse result = gson.fromJson(jsonString, GetAssertionResponse.class);

        // Actually this is the opposite of what should be true, just for testing the testing.
        assertTrue(result.solutionOrError.Err instanceof GetAssertionError.NoAssertions);
    }

    /*
     * Test that the assertion list for a nonexistent contest is empty.
     * Note: FIXME This test is deliberately buggy for now, because I want to test the new testing automation.
     */
    @Test
    public void testNonexistentExample() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://localhost:" +port + auditEndpoint;

        HttpEntity<String> request = new HttpEntity<>("{\"contestName\": \"ImpossibleCantBeRealContestName982389273428\", " +
                "\"totalAuditableBallots\": 15, " +
                " \"candidates\": [\"Alice\",\"Bob\"], " +
                " \"winner\": \"Alice\" " +
                "}", headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class );

        Gson gson = new Gson();
        String jsonString = response.getBody();
        RaireSolution result = gson.fromJson(jsonString, RaireSolution.class);

        // Actually this is the opposite of what should be true, just for testing the testing.
        assertTrue(result.solution.Ok.assertions.length != 0);
    }
}
