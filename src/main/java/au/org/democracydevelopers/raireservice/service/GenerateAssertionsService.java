package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raire.RaireProblem;
import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.audittype.BallotComparisonOneOnDilutedMargin;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.util.VoteConsolidator;
import au.org.democracydevelopers.raireservice.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.repository.CVRRepository;
import au.org.democracydevelopers.raireservice.repository.converters.StringListConverter;
import au.org.democracydevelopers.raireservice.repository.entity.CVRContestInfo;
import au.org.democracydevelopers.raireservice.request.ContestRequestByIDs;
import au.org.democracydevelopers.raireservice.request.OldContestRequest;
import au.org.democracydevelopers.raireservice.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Arrays.stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenerateAssertionsService {

    private final AssertionRepository assertionRepository;
    private final CVRRepository cvrRepository;

    public OldContestRequest getVotesFromDatabase(ContestRequestByIDs request) {
        StringListConverter conv = new StringListConverter();

        List<String[]> votesByName = request.getCountyAndContestIDs()
                .stream().flatMap(
                        iDs -> cvrRepository.getCVRs(iDs.getCountyID(), iDs.getContestID()).stream()
                ).map(l -> conv.convertToEntityAttribute(l).toArray(new String[0])).toList();

        return new OldContestRequest(request.getContestName(), request.getTotalAuditableBallots(),
                request.getTimeProvisionForResult(), request.getCandidates().toArray(new String[0]), votesByName);
    }

    /**
     * The main method that actually does the work of this service. It
     * - inputs an election description in colorado-rla format, that is, with votes as lists of candidate names,
     * - converts it into the format that raire expects, that is, with votes as lists of candidate indices and
     * slightly differently-structured metadata,
     * - calls raire
     * - converts raire's response into the format that the database expects, that is, raire-service::Assertion,
     * - returns either a summary result (the winner), or an error.
     *
     * @param request a ContestRequest - a collection of IRV votes for a single contest, with metadata
     * @return a GenerateAssertionsResponse - either OK with a winner, or an error.
     */
    public RaireSolution.RaireResultOrError generateAssertions(OldContestRequest request) {

        List<String[]> votesByName = request.getVotes();
        VoteConsolidator consolidator = new VoteConsolidator(request.getCandidates());

        // Check that the request is valid
        if (request.votesAreValid()) {
            try {
                // Try converting it into RAIRE format. RAIRE will throw an exception if unexpected candidate names appear.
                votesByName.forEach(consolidator::addVoteNames);
            } catch (VoteConsolidator.InvalidCandidateName e) {
                log.error("Invalid vote sent to raire-service: {}", request.getContestName());
                throw new RuntimeException("Error: invalid votes sent to raire-service.");
            }
        } else {
            log.error("Vote with repeated names sent to raire-service: {}", request.getContestName());
            throw new RuntimeException("Error: vote with repeated names sent to raire-service.");
        }

        // If it's valid, get RAIRE to solve it.
        // BallotComparisonOneOnDilutedMargin is the appropriate audit type for Colorado, which uses ballot-level
        // Comparison audits.
        RaireProblem raireProblem = new RaireProblem(
                request.getMetadata(),
                consolidator.getVotes(),
                request.getCandidates().length,
                null,
                new BallotComparisonOneOnDilutedMargin(request.getTotalAuditableBallots()),
                TrimAlgorithm.MinimizeAssertions,
                null,
                Double.valueOf(request.getTimeProvisionForResult()));

        return raireProblem.solve().solution;
    }

    public GenerateAssertionsResponse storeAssertions(RaireSolution.RaireResultOrError solution, OldContestRequest request) {
            if( solution.Ok != null) {
                // Assertions successfully generated. Save them and report the winner.
                log.info("Assertions successfully generated for contest "+request.getContestName());
                assertionRepository.saveRaireAssertions(solution.Ok.assertions, request.getContestName(), request.getTotalAuditableBallots(), request.getCandidates());
                String winnerName = request.getCandidates()[solution.Ok.winner];
                return new GenerateAssertionsResponse(request.getContestName(),
                        new GenerateAssertionsResponse.GenerateAssertionsResultOrError(winnerName));
            } else {
                assert solution.Err != null;
                // Something went wrong. Return the error. Note this error may reflect something about
                // the contest, e.g. that it is tied or that assertion generation ran out of time.
                log.error(solution.Err.toString());
                return new GenerateAssertionsResponse(request.getContestName(),
                        new GenerateAssertionsResponse.GenerateAssertionsResultOrError(
                                new GenerateAssertionsError.PlaceholderError()
                        ));
            }
        }



}
