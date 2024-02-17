package au.org.democracydevelopers.raireservice.response;

/** Exceptions the RAIRE algorithm may produce. The real detail is in the RaireError class. */
public class GetAssertionsException extends Exception {
    public final GetAssertionsError error;
    public GetAssertionsException(GetAssertionsError error) {
        this.error = error;
    }
}