package au.org.democracydevelopers.raireconnector.service;

import au.org.democracydevelopers.raire.RaireProblem;
import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.audittype.BallotComparisonOneOnDilutedMargin;
import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
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

  private final ObjectMapper objectMapper;

  public RaireSolution findCvrContestInfo(ContestRequest contest) {

      var auditType = new BallotComparisonOneOnDilutedMargin(contest.getTotalAuditableBallots());
      Vote[] votes = contest.toRaireVotes();

        var candidatesAndMetadata = new HashMap<String, Object>();
        candidatesAndMetadata.put("candidates", contest.getCandidates());
        candidatesAndMetadata.put("contest", contest.getContestName());
        candidatesAndMetadata.put("totalAuditableBallots", contest.getTotalAuditableBallots());
        RaireProblem raireProblem = new RaireProblem(candidatesAndMetadata,
                votes,
                contest.getCandidates().size(), null, auditType , TrimAlgorithm.MinimizeAssertions, null,
                120.0);
    return raireProblem.solve();
  }



}
