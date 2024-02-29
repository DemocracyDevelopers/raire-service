package au.org.democracydevelopers.raireservice.repository.entity;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.response.RaireServiceException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing for the Assertion class, mostly to test conversions between Assertions retrieved from the database and
 * the raire-style assertions that the conversion function produces.
 */
public class AssertionTests {

    private final List<String> everyone = List.of("Alice", "Bob", "Chuan", "Diego");
    private final List<String> aliceAndBob = List.of("Alice", "Bob");
    @Test
    public void testNENValidConversion() throws RaireServiceException {

        Assertion a = new NENAssertion("TestContest","Alice", "Bob", 100,
                10000, 15.0, aliceAndBob);
        au.org.democracydevelopers.raire.assertions.Assertion raireAssertion = a.makeRaireAssertion(everyone);

        assert(raireAssertion instanceof NotEliminatedNext);
        assertEquals(a.winner, everyone.get( ((NotEliminatedNext) raireAssertion).winner));
        assertEquals(a.loser, everyone.get( ((NotEliminatedNext) raireAssertion).loser));

        List<String> continuingStrings = Arrays.stream(((NotEliminatedNext) raireAssertion).continuing)
                .mapToObj(everyone::get).toList();
        assert(continuingStrings.size() == 2);
    }

    /*
     * Test that when a winner that's not one of the candidates is included, it throws an exception
     */
    @Test
    public void testNENInvalidWinnerConversion() throws RaireServiceException {
        Exception e = assertThrows(RaireServiceException.class, () -> {
            Assertion a = new NENAssertion("TestContest","NotARealCandidate", "Bob", 100,
                    10000, 15.0, aliceAndBob);
            au.org.democracydevelopers.raire.assertions.Assertion raireAssertion = a.makeRaireAssertion(everyone);

        });

        assertTrue(e instanceof RaireServiceException);
    }
    /*
     * Test that when a loser that's not one of the candidates is included, it throws an exception
     */
    @Test
    public void testNENInvalidLoserConversion() throws RaireServiceException {
        Exception e = assertThrows(RaireServiceException.class, () -> {
            Assertion a = new NENAssertion("TestContest","Alice", "NotARealCandidate", 100,
                    10000, 15.0, aliceAndBob);
            au.org.democracydevelopers.raire.assertions.Assertion raireAssertion = a.makeRaireAssertion(everyone);

        });

        assertTrue(e instanceof RaireServiceException);
    }

    /*
     * Test that when invalid continuing candidates are included, it throws an exception
     */
    @Test
    public void testNENInvalidContinuingCandidates() throws RaireServiceException {

        final List<String> invalidCandidates = List.of("NotACandidate", "AnotherNonCandidate");

        Exception e = assertThrows(RaireServiceException.class, () -> {
            Assertion a = new NENAssertion("TestContest","Alice", "Bob", 100,
                    10000, 15.0, invalidCandidates);
            au.org.democracydevelopers.raire.assertions.Assertion raireAssertion = a.makeRaireAssertion(everyone);

        });

        assertTrue(e instanceof RaireServiceException);
    }

    /*
     * Test that when a mix of valid and invalid continuing candidates are included, it throws an exception
     */
    @Test
    public void testNENInvalidAndValidContinuingCandidates() throws RaireServiceException {

        final List<String> invalidAndValidCandidates = List.of("Alice", "AnotherNonCandidate");

        Exception e = assertThrows(RaireServiceException.class, () -> {
            Assertion a = new NENAssertion("TestContest","Alice", "Bob", 100,
                    10000, 15.0, invalidAndValidCandidates);
            au.org.democracydevelopers.raire.assertions.Assertion raireAssertion = a.makeRaireAssertion(everyone);

        });

        assertTrue(e instanceof RaireServiceException);
    }
}
