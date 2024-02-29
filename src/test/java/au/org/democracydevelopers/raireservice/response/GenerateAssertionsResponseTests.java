package au.org.democracydevelopers.raireservice.response;

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raireservice.request.DirectContestRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerateAssertionsResponseTests {

    private String[] testCandidates = {"Alice", "Bob", "Chuan", "Diego"};
    private String[] v1 ={"Alice","Bob"};
    private String[] v2 ={"Alice","Chuan"};
    private String[] v3 = {"Alice", "Alice"};
    private String[] v4 = {"Eli", "Alice"};
    private int[] threeTiedWinners = {0,1,2};
    private int[] twoTiedWinners = {0,3};

    @Test
    /**
     * Check that tied winners are correctly transcribed by name.
     */
    void twoTiedWinnersAreCorrectlyTranscribed() {
        RaireError err = new RaireError.TiedWinners(twoTiedWinners);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.TiedWinners);
        assertTrue(((RaireServiceError.TiedWinners) response.response.Err).expected.containsAll(List.of("Alice", "Diego")));
    }

    @Test
    /**
     * Check that tied winners are correctly transcribed by name.
     */
    void threeTiedWinnersAreCorrectlyTranscribed() {
        RaireError err = new RaireError.TiedWinners(threeTiedWinners);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.TiedWinners);
        assertTrue(((RaireServiceError.TiedWinners) response.response.Err).expected.containsAll(List.of("Alice", "Bob", "Chuan")));
    }

    @Test
    /**
     * Check that difficultyAtTimeOfStopping is correctly transferred.
     */
    void difficultyAtTimeOfStoppingProperlyCopied() {
        double difficulty = 57.2;
        RaireError err = new RaireError.TimeoutFindingAssertions(difficulty);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.TimeoutFindingAssertions);
        assertEquals(difficulty, ((RaireServiceError.TimeoutFindingAssertions) response.response.Err).difficultyAtTimeOfStopping);
    }

    /**
     * The rest of these tests just make sure that we return an 'internal error' when various things that aren't
     * supposed to happen, happen.
     */
    @Test
    void wrongWinnerIsAnInternalError() {
        RaireError err = new RaireError.WrongWinner(twoTiedWinners);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }

    @Test
    void couldNotRuleOutIsAnInternalError() {
        RaireError err = new RaireError.CouldNotRuleOut(twoTiedWinners);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }
    @Test
    void didntRuleOutLoserIsAnInternalError() {
        RaireError err = new RaireError.InternalErrorDidntRuleOutLoser();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }
    @Test
    void ruledOutWinnerIsAnInternalError() {
        RaireError err = new RaireError.InternalErrorRuledOutWinner();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }
    @Test
    void internalErrorTrimmingIsAnInternalError() {
        RaireError err = new RaireError.InternalErrorTrimming();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }
    @Test
    void invalidCandidateNumberIsAnInternalError() {
        RaireError err = new RaireError.InvalidCandidateNumber();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }
    @Test
    void invalidTimeoutIsAnInternalError() {
        RaireError err = new RaireError.InvalidTimeout();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }
    @Test
    void timeoutCheckingWinnerIsAnInternalError() {
        RaireError err = new RaireError.TimeoutCheckingWinner();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }

    @Test
    void genericRaireErrorIsAnInternalError() {
        RaireError err = new RaireError.TimeoutCheckingWinner();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }

}
