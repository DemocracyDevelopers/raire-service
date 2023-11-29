package au.org.democracydevelopers.raireservice;

import au.org.democracydevelopers.raireservice.request.ContestRequest;
import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

// @SpringBootTest
class ContestRequestTests {

    private String[] testCandidates = {"Alice", "Bob", "Chuan", "Diego"};
    private String[] v1 ={"Alice","Bob"};
    private String[] v2 ={"Alice","Chuan"};
    private String[] v3 = {"Alice", "Alice"};
    private String[] v4 = {"Eli", "Alice"};
    @Test
    /**
     * Check that a vote we expect to be valid is valid.
     */
    void validVoteListIsValid () {
        List<String[]> testVotes = List.of(v1,v2);
        ContestRequest testValidRequest
                = new ContestRequest("TestContest", 10, 10, testCandidates, testVotes );
        assert testValidRequest.votesAreValid();
    }

    @Test
    /**
     * Check that the repeated choice 'Alice' is identified as invalid.
     */
    void invalidVoteListIsNotValid () {
        List<String[]> testVotesInvalid = List.of(v1,v2,v3);
        ContestRequest testValidRequest
                = new ContestRequest("TestContest", 10, 10, testCandidates, testVotesInvalid );
        assert !testValidRequest.votesAreValid();
    }

    @Test
    /**
     * Observe that an apparently-valid (i.e. without repeats) list of candidate choices passes validity checking,
     * even if it does not contain valid candidates.
     *
     * This DOES NOT detect the presence of an unexpected candidate name - that's left for raire-java.
     */
    void invalidCandidateNameIsNotNoticed () {
        List<String[]> testVotesInvalid = List.of(v1,v2,v4);
        ContestRequest testValidRequest
                = new ContestRequest("TestContest", 10, 10, testCandidates, testVotesInvalid );
        assert testValidRequest.votesAreValid();
    }






}
