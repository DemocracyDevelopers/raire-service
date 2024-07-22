/*
Copyright 2024 Democracy Developers

The Raire Service is designed to connect colorado-rla and its associated database to
the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).

This file is part of raire-service.

raire-service is free software: you can redistribute it and/or modify it under the terms
of the GNU Affero General Public License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with
raire-service. If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raire.RaireProblem;
import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raire.audittype.BallotComparisonOneOnDilutedMargin;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.util.VoteConsolidator;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.CVRContestInfoRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.GenerateAssertionsSummaryRepository;
import au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsSummary;
import au.org.democracydevelopers.raireservice.request.ContestRequest;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.INTERNAL_ERROR;

/**
 * This class contains functionality for generating assertions for a given contest by calling
 * raire-java, and persisting those assertions to the colorado-rla database.
 */
@Service
public class GenerateAssertionsService {

  private final static Logger logger = LoggerFactory.getLogger(GenerateAssertionsService.class);

  private final CVRContestInfoRepository cvrContestInfoRepository;

  private final ContestRepository contestRepository;

  private final AssertionRepository assertionRepository;

  private final GenerateAssertionsSummaryRepository summaryRepository;

  /**
   * All args constructor.
   * @param cvrContestInfoRepository for extracting CVR vote data from the database.
   * @param contestRepository for extraction county level contest details (IDs) from the database.
   * @param assertionRepository for saving assertions to the database.
   */
  public GenerateAssertionsService(CVRContestInfoRepository cvrContestInfoRepository,
      ContestRepository contestRepository, AssertionRepository assertionRepository,
      GenerateAssertionsSummaryRepository summaryRepository) {
    this.cvrContestInfoRepository = cvrContestInfoRepository;
    this.contestRepository = contestRepository;
    this.assertionRepository = assertionRepository;
    this.summaryRepository = summaryRepository;
  }

  /**
   * Given a request to generate assertions for a contest, this method collects all CVR vote
   * data for that contest, consolidates it into raire-java Votes, and accesses the raire-java
   * API to form assertions. A RaireResultOrError is returned containing either the successfully
   * generated assertions or error information detailing reasons why they were not generated.
   * @param request Assertions generation request specifying the contest name and candidates.
   * @return A RaireResultOrError containing either the generated assertions or error details
   * indicating why assertion generation was not successful.
   * @throws RaireServiceException if any vote data for a contest was found to be invalid (i.e.,
   * it referred to candidates that were not in the expected list) or an error arose in database
   * access.
   */
  public RaireResultOrError generateAssertions(GenerateAssertionsRequest request)
      throws RaireServiceException {
    final String prefix = "[generateAssertions]";
    try{
      logger.debug(String.format("%s Preparing to generate assertions for contest %s. Request " +
          "parameters: candidate list (%s); total auditable ballots (%d); and time limit (%f)",
          prefix, request.contestName, request.candidates, request.totalAuditableBallots,
          request.timeLimitSeconds));

      // Check that the contest exists and is all IRV. Otherwise this is an internal error because
      // it should be caught before here.
      if(contestRepository.findFirstByName(request.contestName).isEmpty()
          || !contestRepository.isAllIRV(request.contestName)) {
        final String msg = String.format("%s Contest %s does not exist or is not all IRV", prefix,
            request.contestName);
        logger.error(msg + "Throwing a RaireServiceException.");
        throw new RaireServiceException(msg, RaireErrorCode.INTERNAL_ERROR);
      }

      // Use raire-java to consolidate the votes, collecting all votes with the same ranking
      // together and representing that collection as a single ranking with an associated number
      // denoting how many votes with that ranking exist.
      VoteConsolidator consolidator = new VoteConsolidator(request.candidates.toArray(String[]::new));

      // First extract all county level contests matching the contest name in the request. For
      // these contests, extract CVR vote data from the database, and add those votes to the
      // vote consolidator.
      logger.debug(String.format("%s (Database access) Collecting all vote rankings for contest " +
          "%s from CVRs in database.", prefix, request.contestName));
      final List<String[]> votes = contestRepository.findByName(request.contestName).stream().map(
          c -> cvrContestInfoRepository.getCVRs(c.getContestID(), c.getCountyID())).
          flatMap(List::stream).toList();

      if(votes.size() > request.totalAuditableBallots) {
        final String msg = String.format("%s %d votes present for contest %s but a universe size of "
            + "%d specified in the assertion generation request. Throwing a RaireServiceException.",
            prefix, votes.size(), request.contestName, request.totalAuditableBallots);
        logger.error(msg);
        throw new RaireServiceException(msg, RaireErrorCode.INVALID_TOTAL_AUDITABLE_BALLOTS);
      }

      if(votes.isEmpty()) {
        final String msg = String.format("%s No votes present for contest %s.", prefix,
            request.contestName);
        logger.error(msg + " Throwing a RaireServiceException.");
        throw new RaireServiceException(msg, RaireErrorCode.NO_VOTES_PRESENT);
      }

      logger.debug(String.format("%s Adding all extracted rankings to a consolidator to identify " +
          "unique rankings and their number.", prefix));
      votes.forEach(consolidator::addVoteNames);

      logger.debug(String.format("%s Votes consolidated.", prefix));

      // If the extracted votes are valid, get raire-java to generate assertions.
      // First, form a metadata map containing contest details.
      Map<String,Object> metadata = new HashMap<>();
      metadata.put(Metadata.CANDIDATES, request.candidates);
      metadata.put(Metadata.CONTEST, request.contestName);

      // Create the RaireProblem containing all information raire-java needs.
      logger.debug(String.format("%s Creating the RaireProblem to provide to raire-java with " +
          "parameters: candidates (%s); contest name (%s); number of candidates (%d); " +
          "total auditable ballots (%d); minimize assertions trimming algorithm; and time limit %f.",
          prefix, request.candidates, request.contestName, request.candidates.size(),
              request.totalAuditableBallots, request.timeLimitSeconds));
      RaireProblem raireProblem = new RaireProblem(
          metadata, consolidator.getVotes(), request.candidates.size(), null,
          new BallotComparisonOneOnDilutedMargin(request.totalAuditableBallots),
          TrimAlgorithm.MinimizeAssertions, null, request.timeLimitSeconds
      );

      // Tell raire-java to generate assertions, returning a RaireSolutionOrError.
      logger.debug(String.format("%s Calling raire-java.", prefix));
      RaireResultOrError result = raireProblem.solve().solution;

      // Log fact that raire-java returned; more details about result will be logged in the caller.
      logger.debug(String.format("%s raire-java returned result; passing to controller.", prefix));
      return result;
    }
    catch (VoteConsolidator.InvalidCandidateName ex) {
      final String msg = String.format("%s Invalid vote sent to RAIRE for contest %s. %s",
          prefix, request.contestName, ex.getMessage());
      logger.error(msg);
      throw new RaireServiceException(msg, RaireErrorCode.WRONG_CANDIDATE_NAMES);
    }
    catch(RaireServiceException ex){
      final String msg = String.format("%s A RaireServiceException was caught; passing to caller. %s",
          prefix, ex.getMessage());
      logger.error(msg);
      throw ex;
    }
    catch(DataAccessException ex){
      final String msg = String.format("%s A data access exception arose when extracting " +
              "CVR/Contest data for contest %s. %s", prefix, request.contestName, ex.getMessage());
      logger.error(msg);
      throw new RaireServiceException(msg, RaireErrorCode.INTERNAL_ERROR);
    }
    catch(Exception ex){
      final String msg = String.format("%s An exception arose when generating assertions. %s",
          prefix, ex.getMessage());
      logger.error(msg);
      throw new RaireServiceException(msg, RaireErrorCode.INTERNAL_ERROR);
    }
  }

  /**
   * Given a raire result or error, persist it.
   * If the result contains successfully generated assertions stored within a RaireResult, persist
   * these assertions to the database and the winner in the GenerateAssertionsResponse table.
   * If the result contains an error or warning, persist it and its message in the
   * GenerateAssertionsResponse table.
   * Previously-stored assertions are deleted, regardless of whether assertion generation
   * was successful this time. Previously-stored summaries are updated.
   * @param solution RaireResultOrError containing either assertions to persist for a given contest,
   *                 with a winner, or an error. Note that there may be both a warning and successful
   *                 assertion generation.
   * @param request Assertions generation request containing contest information.
   * @throws RaireServiceException when an error arises in either the translation of
   * raire-java assertions into a form suitable for saving to the database, in persisting these
   * translated assertions and associated data to the database, or in retrieving and saving or updating
   * the summaries. These cause the whole transaction to roll back.
   * Note default behaviour of @Transactional is:
   * - to commit and flush at the end of the transaction
   * - to roll back if any RuntimeException is thrown,
   * and we explicitly tell it to roll back for RaireServiceException too.
   *
   */
  @Transactional(rollbackOn = {RuntimeException.class, DataAccessException.class, RaireServiceException.class})
  public void persistAssertionsOrErrors(RaireResultOrError solution, ContestRequest request)
      throws RaireServiceException {
    final String prefix = "[persistAssertionsOrErrors]";

    // Retrieve an existing summary or start a new one.
    GenerateAssertionsSummary summary;
    final Optional<GenerateAssertionsSummary> OptSummary
        = summaryRepository.findByContestName(request.contestName);
    if (OptSummary.isPresent()) {
      // A summary is already present in the database - we will update this.
      summary = OptSummary.get();
    } else {
      // There is no summary for ths contest - make a new blank summary.
      summary = new GenerateAssertionsSummary(request.contestName);
    }

    // Delete any existing assertions for this contest.
    logger.debug(String.format("%s (Database access) Proceeding to delete any assertions " +
        "stored for contest %s (if present).", prefix, request.contestName));
    assertionRepository.deleteByContestName(request.contestName);

    if (solution.Ok != null) {
      // The solution is OK. Persist assertions formed by raire-java and save the winner and warning.
      logger.debug(String.format("%s Proceeding to translate and save %d assertions to the " +
          "database for contest %s.", prefix, solution.Ok.assertions.length, request.contestName));
      assertionRepository.translateAndSaveAssertions(request.contestName,
          request.totalAuditableBallots, request.candidates.toArray(String[]::new), solution.Ok.assertions);

      logger.debug(String.format("%s Assertions persisted.", prefix));

      // Update summary data, success.
      summary.update(request.candidates, solution.Ok.winner, solution.Ok.warning_trim_timed_out);

    } else if (solution.Err != null) {
      // The solution indicates a failed assertion generation. Persist the error and message.

      // Creating a RaireServiceException gives us a human-readable error message, though the
      // exception is not thrown.
      RaireServiceException ex = new RaireServiceException(solution.Err, request.candidates);

      // Update summary data.
      summary.update(ex.errorCode.toString(), ex.getMessage());
    } else {
      // This is not supposed to happen - we got neither assertions nor an error.
      summary.update(INTERNAL_ERROR.toString(), "Internal error");
    }

    // This is redundant for an object we retrieved from the database, but necessary if we made a
    // new one.
    summaryRepository.save(summary);
  }
}
