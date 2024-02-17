package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raireservice.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import au.org.democracydevelopers.raireservice.request.ContestRequestByIDs;
import au.org.democracydevelopers.raireservice.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateAssertionsService {

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
    public GenerateAssertionsResponse generateAssertions(ContestRequestByIDs request) {

        // Get assertions from database
        List<Assertion> assertions = assertionRepository.findByContestName(request.getContestName());
        List<String> candidates = request.getCandidates();
        Metadata metadata = new Metadata(request);


    }
}
