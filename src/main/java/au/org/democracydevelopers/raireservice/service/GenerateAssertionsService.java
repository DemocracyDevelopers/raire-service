package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raire.RaireProblem;
import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.audittype.BallotComparisonOneOnDilutedMargin;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.util.VoteConsolidator;
import au.org.democracydevelopers.raireservice.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.repository.CVRRepository;
import au.org.democracydevelopers.raireservice.repository.converters.StringArrayConverter;
import au.org.democracydevelopers.raireservice.request.ContestRequestByIDs;
import au.org.democracydevelopers.raireservice.request.DirectContestRequest;
import au.org.democracydevelopers.raireservice.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
@Service
@Slf4j
public class GenerateAssertionsService {

    private final AssertionRepository assertionRepository;
    private final CVRRepository cvrRepository;

    /**
     * Retrieve the votes from the database
     * @param request A ContestRequestByIDs, which includes a list of <countyID, contestID> pairs that identify
     *                the relevant CVRs.
     * @return        The vote data for the requested contest.
     */
    public DirectContestRequest getVotesFromDatabase(ContestRequestByIDs request) throws RaireServiceException {
        StringArrayConverter conv = new StringArrayConverter();

        validateContestRequestByIDs(request);

        List<String[]> votesByName = request.getCountyAndContestIDs()
                .stream().flatMap(
                        iDs -> cvrRepository.getCVRs(iDs.getCountyID(), iDs.getContestID()).stream()
                ).map(conv::convertToEntityAttribute).toList();

        return new DirectContestRequest(request, votesByName);
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
    public RaireSolution.RaireResultOrError generateAssertions(DirectContestRequest request) {

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

    /**
     *
     * @param solution The RaireSolution (possibly an error) to be dealt with. If it contains assertions, it is stored;
     *                 if it contains an error, nothing is stored.
     * @param request  The ContestRequest, used for metadata (including candidate names) used for storage.
     * @return A GenerateAssertionsResponse, summarizing either success (with a winner) or the error.
     * Database interactions: if assertions are successfully generated, it deletes prior assertions for this contest
     * and stores new ones in the database.
     */
    @Transactional
    public GenerateAssertionsResponse storeAssertions(RaireSolution.RaireResultOrError solution, DirectContestRequest request) {
            if( solution.Ok != null) {
                // Assertions are present.
                log.info("Assertions successfully generated for contest "+request.getContestName());

                // Delete any existing assertions for this contest.
                assertionRepository.deleteByContestName(request.getContestName());
                log.info("deleting old assertions for contest "+request.getContestName()+" and saving new ones.");

                // Save them and report the winner.
                assertionRepository.saveRaireAssertions(solution.Ok.assertions, request.getContestName(), request.getTotalAuditableBallots(), request.getCandidates());
                String winnerName = request.getCandidates()[solution.Ok.winner];
                return new GenerateAssertionsResponse(request.getContestName(),
                        new GenerateAssertionsResponse.GenerateAssertionsResultOrError(winnerName));
            } else {
                assert solution.Err != null;
                // Something went wrong. Return the error. Note this error may reflect something about
                // the contest, e.g. that it is tied or that assertion generation ran out of time.
                log.error(solution.Err.toString());
                return new GenerateAssertionsResponse(request.getContestName(), request.getCandidates(), solution.Err);
            }
        }


    /**
     * A prototype-placeholder for more careful input validation. The idea is that the production version of this
     * function should check for invalid requests, including
     * - if the County & Contest IDs don't all correspond to the same contest name
     * - if the contest is not IRV
     * - if the County & Contest IDs don't exist
     * - if the list of candidates is empty
     *
     * @param request
     * TODO Implement all necessary checks. This will require some database interaction.
     */
    private void validateContestRequestByIDs(ContestRequestByIDs request) throws RaireServiceException {
        if (request.getCountyAndContestIDs().isEmpty() || request.getCandidates().isEmpty()) {
            throw new RaireServiceException(new RaireServiceError.InvalidInput("No IDs"));
        } else if (request.getCandidates().isEmpty()) {
            throw new RaireServiceException(new RaireServiceError.InvalidInput("No candidates"));
        }
    }
}
