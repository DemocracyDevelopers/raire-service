package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raireservice.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.repository.CVRRepository;
import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import au.org.democracydevelopers.raireservice.repository.entity.CVRContestInfo;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.CountyAndContestID;
import au.org.democracydevelopers.raireservice.request.DirectContestRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.response.RaireServiceError;
import au.org.democracydevelopers.raireservice.response.RaireServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.String;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class GenerateAssertionsServiceTests {

    @Autowired
    AssertionRepository assertionRepository;

    @Autowired
    CVRRepository cvrRepository;

    @Autowired
    GenerateAssertionsService generateAssertionsService;

    private String ALICE = "Alice";
    private String BOB = "Bob";
    private String CHUAN = "Chuan";
    private String DIEGO = "Diego";
    private String aliceOnly[] = new String[]{ALICE};
    private String aliceBeforeBob[] = new String[]{ALICE,BOB};
    private String bobBeforeAlice[] = new String[]{BOB, ALICE};

    private String testContestName = "testContestName";

    /*
     * The simplest possible assertion-generation test, with two candidates and a single vote for Alice.
     * We expect this to generate NEB(Alice,Bob).
     * Strictly speaking it would also be correct to generate NEN(Alice, Bob, {Alice, Bob}).
     * Either way, we check that there is one assertion with winner Alice and loser Bob and margin 1.
     */
    @Test
    void whenThereIsOnlyOneVote_ThereIsOneAssertionWithMarginOne() {
        List<String[]> oneVoteForAlice = new ArrayList<String[]>();
        oneVoteForAlice.add(aliceOnly);
        DirectContestRequest contest = new DirectContestRequest(testContestName, 1,
                100, aliceBeforeBob, oneVoteForAlice);

        RaireSolution.RaireResultOrError solution = generateAssertionsService.generateAssertions(contest);
        // There should be one assertion.
        assertEquals(1, solution.Ok.assertions.length);

        AssertionAndDifficulty a = solution.Ok.assertions[0];
        // It should have a margin of 1, because there was only 1 vote.
        assertEquals(1, a.margin);

        GenerateAssertionsResponse outcome = generateAssertionsService.storeAssertions(solution, contest);
        // The election winner should be Alice.
        assertEquals(ALICE, outcome.response.Ok);

        List<Assertion> retrieved = assertionRepository.findByContestName(testContestName);
        // After we store the assertions and retrieve them, there should still be only one of them.
        assertEquals(1, retrieved.size());

        // And Alice should still be the winner
        assertEquals(ALICE, retrieved.get(0).getWinner());

        // And Bob should still be the loser.
        assertEquals(BOB, retrieved.get(0).getLoser());
    }

    /*
     * Test for the edge case that there is only one candidate.
     * There are no assertions.
     * The winner is the sole candidate.
     * The purpose of this test is to ensure nothing weird happens in this edge case.
     */
    @Test
    void whenThereIsOnlyOneCandidate_ThereAreNoAssertions() {
        List<String[]> oneVoteForAlice = new ArrayList<String[]>();
        oneVoteForAlice.add(aliceOnly);
        DirectContestRequest contest = new DirectContestRequest("testContestName2", 1,
                100, aliceOnly, oneVoteForAlice);

        RaireSolution.RaireResultOrError solution = generateAssertionsService.generateAssertions(contest);
        // There should be an OK with an empty assertion list.
        assertNotNull(solution.Ok);
        assertEquals(0, solution.Ok.assertions.length);

        GenerateAssertionsResponse outcome = generateAssertionsService.storeAssertions(solution, contest);
        // The election winner should be Alice.
        assertNotNull(outcome.response.Ok);
        assertEquals(ALICE, outcome.response.Ok);

        List<Assertion> retrieved = assertionRepository.findByContestName("testContestName2");
        // After we store the assertions and retrieve them, there should still be only one of them.
        assertEquals(0, retrieved.size());
    }

    /*
     * Test for the edge case that there is only one candidate and no votes.
     * There are no assertions.
     * The winner is the sole candidate.
     * The purpose of this test is to ensure nothing weird happens in this edge case.
     */
    @Test
    void whenThereIsOnlyOneCandidateAndNoVotes_ThereAreNoAssertions() {
        List<String[]> oneVoteForAlice = new ArrayList<String[]>();
        DirectContestRequest contest = new DirectContestRequest("testContestName3", 1,
                100, aliceOnly, oneVoteForAlice);

        RaireSolution.RaireResultOrError solution = generateAssertionsService.generateAssertions(contest);
        // There should be an OK with an empty assertion list.
        assertNotNull(solution.Ok);
        assertEquals(0, solution.Ok.assertions.length);

        GenerateAssertionsResponse outcome = generateAssertionsService.storeAssertions(solution, contest);
        // The election winner should be Alice.
        assertNotNull(outcome.response.Ok);
        assertEquals(ALICE, outcome.response.Ok);

        List<Assertion> retrieved = assertionRepository.findByContestName("testContestName3");
        // After we store the assertions and retrieve them, there should still be only one of them.
        assertEquals(0, retrieved.size());
    }

    /*
     * Test for the edge case that there are no candidates and no votes.
     * This should cause an error because it is impossible to establish a winner.
     * Note - this one fails. TODO Think about the weird edge case with no candidates.
     */
    /*
    @Test
    void whenThereAreNoCandidates_ErrorNoWinner() {
        List<String[]> nothing = new ArrayList<String[]>();
        DirectContestRequest contest = new DirectContestRequest("testContestName4", 1,
                100, new String[0], nothing);

        RaireSolution.RaireResultOrError solution = generateAssertionsService.generateAssertions(contest);
        // There should be an OK with an empty assertion list.
        assertNotNull(solution.Err);

        GenerateAssertionsResponse outcome = generateAssertionsService.storeAssertions(solution, contest);
        // The election winner should be Alice.
        assertNotNull(outcome.response.Err);
        assertEquals(new GenerateAssertionsError.PlaceholderError(), outcome.response.Err);

        List<Assertion> retrieved = assertionRepository.findByContestName("testContestName4");
        // After we store the assertions and retrieve them, there should still be only one of them.
        assertEquals(0, retrieved.size());
    }
     */

    /*
     * The same data as whenThereIsOnlyOneVote_ThereIsOneAssertionWithMarginOne, except that we ask the assertionsService
     * to get the single vote out of the database first, then generate the (one) assertion.
     */
    @Test
    void generateTrivialAssertionFromDatabase() throws RaireServiceException {

        CVRContestInfo cvr1 = new CVRContestInfo(1L,1L,1L,aliceOnly);
        cvrRepository.save(cvr1);

        GenerateAssertionsRequest request = new GenerateAssertionsRequest("testContestName5", 1,
                100, List.of(aliceBeforeBob), List.of(new CountyAndContestID(1L,1L)) );

        DirectContestRequest contestRequest = generateAssertionsService.getVotesFromDatabase(request);
        RaireSolution.RaireResultOrError solution = generateAssertionsService.generateAssertions(contestRequest);

        // There should be one assertion.
        assertEquals(1, solution.Ok.assertions.length);

        AssertionAndDifficulty a = solution.Ok.assertions[0];
        // It should have a margin of 1, because there was only 1 vote.
        assertEquals(1, a.margin);

        GenerateAssertionsResponse outcome = generateAssertionsService.storeAssertions(solution, contestRequest);
        // The election winner should be Alice.
        assertEquals(ALICE, outcome.response.Ok);

        List<Assertion> retrieved = assertionRepository.findByContestName(testContestName);
        // After we store the assertions and retrieve them, there should still be only one of them.
        assertEquals(1, retrieved.size());

        // And Alice should still be the winner
        assertEquals(ALICE, retrieved.get(0).getWinner());

        // And Bob should still be the loser.
        assertEquals(BOB, retrieved.get(0).getLoser());
    }

    /**
     * Test that an exception is thrown when the contest request contains an empty candidate list.
     */
    @Test
    void emptyCandidatesInContestRequestThrowsException() {

        GenerateAssertionsRequest request = new GenerateAssertionsRequest("testContestName5", 1,
                100, new ArrayList<String>(), List.of(new CountyAndContestID(1L,1L)) );

        Exception e = assertThrows(RaireServiceException.class, () -> {
            generateAssertionsService.getVotesFromDatabase(request);
        });

        assertInstanceOf(RaireServiceException.class, e);
        RaireServiceError serviceError = ((RaireServiceException) e).error;
        assertInstanceOf(RaireServiceError.InvalidRequest.class, serviceError);
        String message = ((RaireServiceError.InvalidRequest) serviceError).message;
        assertTrue(message.contains("No candidates"));
    }

    @Test
    void emtpyIDsInContestRequestThrowsException() {

        List<CountyAndContestID> nothing = new ArrayList<>();

        GenerateAssertionsRequest request = new GenerateAssertionsRequest("testContestName5", 1,
                100, List.of(aliceBeforeBob), nothing);

        Exception e = assertThrows(RaireServiceException.class, () -> {
            generateAssertionsService.getVotesFromDatabase(request);
        });

        assertInstanceOf(RaireServiceException.class, e);
        RaireServiceError serviceError = ((RaireServiceException) e).error;
        assertInstanceOf(RaireServiceError.InvalidRequest.class, serviceError);
        String message = ((RaireServiceError.InvalidRequest) serviceError).message;
        assertTrue(message.contains("No IDs"));
    }

    /*
     * Test that the proper error is generated when the contest is tied.
     * This is the simplest possible tied example, with two candidates Alice and Bob, and preferential votes (A,B) and (B,A).
     */
    @Test
    void whenWinnersAreTied_Error() {
        List<String[]> tiedVotes = new ArrayList<String[]>();
        tiedVotes.add(aliceBeforeBob);
        tiedVotes.add(bobBeforeAlice);

        DirectContestRequest contest = new DirectContestRequest(testContestName, 1,
                100, aliceBeforeBob, tiedVotes);

        RaireSolution.RaireResultOrError solution = generateAssertionsService.generateAssertions(contest);
        // There should be an error.
        assertNotNull(solution.Err);

        // The error should be RAIRE's tied winners error.
        assertInstanceOf(RaireError.TiedWinners.class, solution.Err);

        // And it should be properly interpreted by the generateAssertionsService as a tied winner error.
        GenerateAssertionsResponse outcome = generateAssertionsService.storeAssertions(solution, contest);
        assertNotNull(outcome.response.Err);
        assertInstanceOf(RaireServiceError.TiedWinners.class, outcome.response.Err);
    }
}
