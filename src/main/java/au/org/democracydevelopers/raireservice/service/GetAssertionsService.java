package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import au.org.democracydevelopers.raireservice.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.RequestByContestName;
import au.org.democracydevelopers.raireservice.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetAssertionsService {

    private final AssertionRepository assertionRepository;

    /**
     * The main method that actually does the work of this service. It
     * - inputs a request, with a contest identified by name,
     * - reads the relevant assertions from the database,
     * - converts it into the format that an assertion visualiser expects, that is, a RaireSolution,
     * - returns the result (or error).
     *
     * @param request a ContestRequestByName - name of a single contest, with metadata
     * @return a RaireSolution - the resulting collection of assertions, with metadata, or an error.
     */
    public GetAssertionsResponse getAssertions(RequestByContestName request) {

        // Get assertions from database
        List<Assertion> assertions = assertionRepository.findByContestName(request.getContestName());
        List<String> candidates = request.getCandidates();
        Metadata metadata = new Metadata(request);

        try {

            // Turn the assertions from the database into the right format for returning.
            AssertionAndDifficulty[] assertionsWithDifficulty = convertToRaireAssertionsInOrder(assertions, candidates);

            // Update metadata with risks
            metadata.AddRisks(assertions);

            // Find overall data: difficulty, margin, winner.
            Optional<Assertion> maxDifficultyAssertion = assertions.stream().max(Comparator.comparingDouble(Assertion::getDifficulty));
            Optional<Assertion> minMarginAssertion = assertions.stream().min(Comparator.comparingInt(Assertion::getMargin));

            // Assertions present. Everything as expected. Build the RAIRE result to return.
            if (assertionsWithDifficulty.length != 0) {
                log.debug(String.format("Assertions successfully retrieved from database for contest %s.", request.getContestName()));

                RetrievedRaireResult result = new RetrievedRaireResult(
                        assertionsWithDifficulty,
                        maxDifficultyAssertion.get().getDifficulty(),
                        minMarginAssertion.get().getMargin(),
                        candidates.size());

                return new GetAssertionsResponse(metadata.getMetadata(), new GetAssertionsResponse.GetAssertionResultOrError(result));

            } else {
                // If there are no assertions in the database, return an error. Note that this doesn't necessarily indicate
                // a serious problem - it might just be that no assertions have (yet) been generated for this contest.
                log.debug(String.format("No assertions present for contest %s.", request.getContestName()));
                log.info(String.format("No assertions present for contest %s.", request.getContestName()));
                return new GetAssertionsResponse(metadata.getMetadata(),
                        new GetAssertionsResponse.GetAssertionResultOrError(new GetAssertionsError.NoAssertions()));
            }

        } catch (Exception e) {
            // Something wrong with format/consistency of retrieved assertions.
            log.debug(String.format("Error retrieving assertions for contest %s.", request.getContestName()));
            log.error(String.format("Error retrieving assertions for contest %s.", request.getContestName()));
            return new GetAssertionsResponse(metadata.getMetadata(),
                    new GetAssertionsResponse.GetAssertionResultOrError(new GetAssertionsError.ErrorRetrievingAssertions()));
        }
    }

    /**
     * Convert from database-retrieved assertions to raire-assertions.
     * It's very important that this MAINTAINS ORDER because we'll include the risk assessments separately, from the
     * original assertion list.
     *
     * @param assertions the assertions that were retrieved from the database
     * @param candidates the list of candidate names, as strings
     * @return an equivalent array of assertions, as raire-java structures.
     * @throws GetAssertionsException if any of the assertions retrieved from the database have inconsistent data, e.g.
     *                               winners or losers who are not listed as candidates.
     */
    private AssertionAndDifficulty[] convertToRaireAssertionsInOrder(List<Assertion> assertions, List<String> candidates) throws GetAssertionsException {
        ArrayList<AssertionAndDifficulty> assertionsWithDifficulty = new ArrayList<AssertionAndDifficulty>();
        for (Assertion a : assertions) {
            au.org.democracydevelopers.raire.assertions.Assertion raireAssertion = a.makeRaireAssertion(candidates);
            assertionsWithDifficulty.add(new AssertionAndDifficulty(raireAssertion, a.getDifficulty(), a.getMargin()));
        }
        return assertionsWithDifficulty.toArray(new AssertionAndDifficulty[0]);
    }
}
