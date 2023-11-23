package au.org.democracydevelopers.raireconnector.service;

import au.org.democracydevelopers.raire.RaireProblem;
import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.audittype.BallotComparisonOneOnDilutedMargin;
import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.util.VoteConsolidator;
import au.org.democracydevelopers.raireconnector.request.ContestRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CvrContestInfoService {

  public RaireSolution findCvrContestInfo(ContestRequest request) {

      List<String[]> votesByName = request.getVotes();
      VoteConsolidator consolidator = new VoteConsolidator(request.getCandidates());

      // Check that the request is valid
      if (request.votesAreValid()) {
        try {
            // Try converting it into RAIRE format. RAIRE will throw an exception if unexpected candidate names appear.
            votesByName.forEach(consolidator::addVoteNames);
        } catch ( VoteConsolidator.InvalidCandidateName e) {
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

    return raireProblem.solve();
  }



}
