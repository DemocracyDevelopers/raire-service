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
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.audittype.BallotComparisonOneOnDilutedMargin;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.util.VoteConsolidator;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.CVRContestInfoRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCodes;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

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

  /**
   * All args constructor.
   * @param cvrContestInfoRepository for extracting CVR vote data from the database.
   * @param contestRepository for extraction county level contest details (IDs) from the database.
   * @param assertionRepository for saving assertions to the database.
   */
  public GenerateAssertionsService(CVRContestInfoRepository cvrContestInfoRepository,
      ContestRepository contestRepository, AssertionRepository assertionRepository){
    this.cvrContestInfoRepository = cvrContestInfoRepository;
    this.contestRepository = contestRepository;
    this.assertionRepository = assertionRepository;
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
    try{
      // Use raire-java to consolidate the votes, collecting all votes with the same ranking
      // together and representing that collection as a single ranking with an associated number
      // denoting how many votes with that ranking exist.
      VoteConsolidator consolidator = new VoteConsolidator(request.candidates.toArray(String[]::new));

      // First extract all county level contests matching the contest name in the request. For
      // these contests, extract CVR vote data from the database, and add those votes to the
      // vote consolidator.
      contestRepository.findByName(request.contestName).stream().map(c -> cvrContestInfoRepository.getCVRs(
          c.getContestID(), c.getCountyID())).flatMap(List::stream).forEach(consolidator::addVoteNames);

      // If the extracted votes are valid, get raire-java to generate assertions.
      // First, form a metadata map containing contest details.
      Map<String,Object> metadata = new HashMap<>();
      metadata.put(Metadata.CANDIDATES, request.candidates);
      metadata.put(Metadata.CONTEST, request.contestName);

      // Create the RaireProblem containing all information raire-java needs.
      RaireProblem raireProblem = new RaireProblem(
          metadata, consolidator.getVotes(), request.candidates.size(), null,
          new BallotComparisonOneOnDilutedMargin(request.totalAuditableBallots),
          TrimAlgorithm.MinimizeAssertions, null,
          (double) request.timeLimitSeconds
      );

      // Tell raire-java to generate assertions, returning a RaireSolutionOrError.
      return raireProblem.solve().solution;
    }
    catch (VoteConsolidator.InvalidCandidateName ex) {
      final String msg = "Invalid vote sent to RAIRE for contest: " + request.contestName + ". " +
          ex.getMessage();
      logger.error("GenerateAssertionsService::generateAssertions " + msg);
      throw new RaireServiceException(msg, RaireErrorCodes.WRONG_CANDIDATE_NAMES);
    }
    catch(DataAccessException ex){
      final String msg = "A data access exception arose when extracting CVR/Contest data. " +
          ex.getMessage();
      logger.error("GenerateAssertionsService::generateAssertions " + msg);
      throw new RaireServiceException(msg, RaireErrorCodes.INTERNAL_ERROR);
    }
    catch(Exception ex){
      final String msg = "An exception arose when generating assertions. " + ex.getMessage();
      logger.error("GenerateAssertionsService::generateAssertions " + msg);
      throw new RaireServiceException(msg, RaireErrorCodes.INTERNAL_ERROR);
    }
  }

  /**
   * Given successfully generated assertions stored within a RaireResult, persist these
   * assertions to the database.
   * @param solution RaireResult containing assertions to persist for a given contest.
   * @param request Assertions generation request containing contest information.
   * @throws RaireServiceException when an error arises in either the translation of
   * raire-java assertions into a form suitable for saving to the database, or in persisting these
   * translated assertions to the database.
   */
  @Transactional
  public void persistAssertions(RaireResult solution, GenerateAssertionsRequest request)
      throws RaireServiceException
  {
    try{
      // Delete any existing assertions for this contest.
      assertionRepository.deleteByContestName(request.contestName);

      // Persist assertions formed by raire-java.
      assertionRepository.translateAndSaveAssertions(request.contestName,
          request.totalAuditableBallots, request.candidates.toArray(String[]::new),
          solution.assertions);
    }
    catch(IllegalArgumentException ex){
      final String msg = "Invalid arguments were supplied to " +
          "AssertionRepository::translateAndSaveAssertions. This is likely either a non-positive " +
          "universe size, invalid margin, or invalid combination of winner, loser and list of " +
          "assumed continuing candidates. " + ex.getMessage();
      logger.error("GenerateAssertionsService::persistAssertions " + msg);
      throw new RaireServiceException(msg, RaireErrorCodes.INTERNAL_ERROR);
    }
    catch(ArrayIndexOutOfBoundsException ex){
      final String msg = "Array index out of bounds access in " +
          "AssertionRepository::translateAndSaveAssertions. This was likely due to a winner " +
          "or loser index in a raire-java assertion being invalid with respect to the " +
          "candidates list for the contest. " + ex.getMessage();
      logger.error("GenerateAssertionsService::persistAssertions " + msg);
      throw new RaireServiceException(msg, RaireErrorCodes.INTERNAL_ERROR);
    }
    catch(DataAccessException ex){
      final String msg = "A data access exception arose when persisting assertions. " + ex.getMessage();
      logger.error("GenerateAssertionsService::persistAssertions " + msg);
      throw new RaireServiceException(msg, RaireErrorCodes.INTERNAL_ERROR);
    }
    catch(Exception ex){
      final String msg = "An exception arose when persisting assertions. " + ex.getMessage();
      logger.error("GenerateAssertionsService::persistAssertions " + msg);
      throw new RaireServiceException(msg, RaireErrorCodes.INTERNAL_ERROR);
    }
  }
}
