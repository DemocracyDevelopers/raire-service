package au.org.democracydevelopers.raireservice.response;

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raireservice.request.DirectContestRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * These tests match the transcription rules written in the Implementation Plan v1.1, Sec 4.1.2.
 */
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
     * TiedWinners: Check that tied winners are correctly transcribed by name.
     */
    void twoTiedWinnersAreCorrectlyTranscribed() {
        RaireError err = new RaireError.TiedWinners(twoTiedWinners);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.TiedWinners);
        assertTrue(((RaireServiceError.TiedWinners) response.response.Err).expected.containsAll(List.of("Alice", "Diego")));
    }

    @Test
    /**
     * TiedWinners: Check that tied winners are correctly transcribed by name.
     */
    void threeTiedWinnersAreCorrectlyTranscribed() {
        RaireError err = new RaireError.TiedWinners(threeTiedWinners);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.TiedWinners);
        assertTrue(((RaireServiceError.TiedWinners) response.response.Err).expected.containsAll(List.of("Alice", "Bob", "Chuan")));
    }

    @Test
    /**
     * TimeoutFindingAssertions: Check that difficultyAtTimeOfStopping is correctly transferred.
     */
    void findingAssertionsDifficultyAtTimeOfStoppingProperlyCopied() {
        double difficulty = 57.2;
        RaireError err = new RaireError.TimeoutFindingAssertions(difficulty);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.TimeoutFindingAssertions);
        assertEquals(difficulty, ((RaireServiceError.TimeoutFindingAssertions) response.response.Err).difficultyAtTimeOfStopping);
    }

    /**
     * TimeoutTrimmingAssertions: has no data and should just be passed on.
     */
    void timeOutTrimmingAssertionsIsCorrectlyTranscribed() {
        RaireError err = new RaireError.TimeoutTrimmingAssertions();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.TimeoutTrimmingAssertions);
    }


    /*
     * The next two errors relate to situations in which raire cannot understand the election.
     * TimeoutCheckingWinners and CouldNotRuleOut are both transcribed as CouldNotAnalyzeElection.
     */
    @Test
    void timeoutCheckingWinnerIsCannotAnalyze() {
        RaireError err = new RaireError.TimeoutCheckingWinner();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.CouldNotAnalyzeElection);
    }
    @Test
    void couldNotRuleOutIsCannotAnalyze() {
        RaireError err = new RaireError.CouldNotRuleOut(twoTiedWinners);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.CouldNotAnalyzeElection);
    }

    /**
     * The rest of these tests just make sure that we return an 'internal error' when various things that aren't
     * supposed to happen, happen.
     */


    // InternalErrorDidntRuleOutLoser
    @Test
    void didntRuleOutLoserIsAnInternalError() {
        RaireError err = new RaireError.InternalErrorDidntRuleOutLoser();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }

    // InternalErrorRuledOutWinner
    @Test
    void ruledOutWinnerIsAnInternalError() {
        RaireError err = new RaireError.InternalErrorRuledOutWinner();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }

    // InternalErrorTrimming
    @Test
    void internalErrorTrimmingIsAnInternalError() {
        RaireError err = new RaireError.InternalErrorTrimming();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }

    /**
     * Invalid timeout: the initial request may have a valid timeout, but this should be caught before being sent to raire.
     */
    @Test
    void invalidTimeoutIsAnInternalError() {
        RaireError err = new RaireError.InvalidTimeout();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }

    // InvalidCandidateNumber
    @Test
    void invalidCandidateNumberIsAnInternalError() {
        RaireError err = new RaireError.InvalidCandidateNumber();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }
    /*
     * InvalidNumberOfCandidates: A request to the raire-service can have an invalid number of candidates (i.e. empty)
     * but this should be checked before being sent to raire.
     */
    @Test
    void invalidNumberOfCandidatesIsAnInternalError() {
        RaireError err = new RaireError.InvalidNumberOfCandidates();
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }

    // WrongWinner
    @Test
    void wrongWinnerIsAnInternalError() {
        RaireError err = new RaireError.WrongWinner(twoTiedWinners);
        GenerateAssertionsResponse response = new GenerateAssertionsResponse("testContest", testCandidates, err);
        assertTrue(response.response.Err instanceof RaireServiceError.InternalError);
    }
}
