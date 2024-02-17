package au.org.democracydevelopers.raireservice.response;

/** Exceptions the RAIRE algorithm may produce. The real detail is in the GenerateAssertionsError class. */
public class GenerateAssertionsException extends Exception {
    public final GenerateAssertionsError error;
    public GenerateAssertionsException(GenerateAssertionsError error) {
        this.error = error;
    }
}