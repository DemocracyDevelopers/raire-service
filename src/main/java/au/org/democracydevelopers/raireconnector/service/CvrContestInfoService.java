package au.org.democracydevelopers.raireconnector.service;

import au.org.democracydevelopers.raire.RaireException;
import au.org.democracydevelopers.raire.RaireProblem;
import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.audittype.BallotComparisonOneOnDilutedMargin;
import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.irv.Votes;
import au.org.democracydevelopers.raire.pruning.HeuristicWorkOutWhichAssertionsAreUsed;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.time.TimeOut;
import au.org.democracydevelopers.raireconnector.client.RaireClient;
import au.org.democracydevelopers.raireconnector.domain.raire.Audit;
import au.org.democracydevelopers.raireconnector.domain.raire.ElectionData;
import au.org.democracydevelopers.raireconnector.domain.raire.ConnectorVote;
import au.org.democracydevelopers.raireconnector.domain.request.ContestRequest;
import au.org.democracydevelopers.raireconnector.domain.response.AuditResponse;
import au.org.democracydevelopers.raireconnector.repository.ContestRepository;
import au.org.democracydevelopers.raireconnector.repository.CvrContestInfoRepository;
import au.org.democracydevelopers.raireconnector.repository.entity.Contest;
import au.org.democracydevelopers.raireconnector.repository.entity.CvrContestInfo;
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CvrContestInfoService {
  // When getting contests by name, group contestIDs and assertions based on names as well not contestIds

  private final ObjectMapper objectMapper;
  private final CvrContestInfoRepository cvrContestInfoRepository;
  private final ContestRepository contestRepository;
  private final RaireClient raireClient;

  private final Vote testVote1 = new Vote(2, new int[]{0,1});
  private final Vote testVote2 = new Vote(3, new int[]{1,0});


  public Set<RaireSolution> findCvrContestInfo(List<ContestRequest> contests) {

    List<String> contestNames = contests.stream().map(ContestRequest::getContestName)
        .collect(Collectors.toList());

    //group contestIds by Name;
    List<Contest> contestDetails = contestRepository.findByNameIn(contestNames);

    Set<Integer> contestIds = contestDetails.stream().map(Contest::getId).map(Math::toIntExact)
        .collect(Collectors.toSet());

    //have a single db interaction
    List<CvrContestInfo> cvrContestInfos = cvrContestInfoRepository.findByContestIdIn(contestIds);

    log.info("Retrieved all cvrContestInfos records {}", cvrContestInfos.size());

    Map<Integer, String> contestIdToContestNameMapping = contestDetails.stream()
        .collect(Collectors.toMap(contestDetail -> Math.toIntExact(contestDetail.getId()),
            Contest::getName));

    Map<String, Set<CvrContestInfo>> cvrContestInfosGroupedByContestNames = new HashMap<>();
    cvrContestInfos.forEach(cvrContestInfo -> {
      Set<CvrContestInfo> cvrContestInfoSet = cvrContestInfosGroupedByContestNames.getOrDefault(contestIdToContestNameMapping.get(cvrContestInfo.getContestId()), new HashSet<>());
      cvrContestInfoSet.add(cvrContestInfo);
      cvrContestInfosGroupedByContestNames.put(contestIdToContestNameMapping.get(cvrContestInfo.getContestId()), cvrContestInfoSet);
    });

    Set<RaireSolution> auditResponsesGroupedByContestName = new HashSet<>();
    cvrContestInfosGroupedByContestNames.forEach((contestName, cvrContests) -> {
      var electionData = buildRaireRequest(cvrContests);
    });
      int ballotTotal = 5; // **use ballots from colorado-rla.
      var auditType = new BallotComparisonOneOnDilutedMargin(ballotTotal);
      RaireResult raireResult;

        var candidatesAndMetadata = new HashMap<String, Object>();
        candidatesAndMetadata.put("candidates", new String[]{"A","B","C","D"});
        candidatesAndMetadata.put("contest", "TestContestName");
        RaireProblem raireProblem = new RaireProblem(candidatesAndMetadata,
                new Vote[]{testVote1, testVote2},
                4, null, auditType , TrimAlgorithm.MinimizeAssertions, null,
                120.0);
      auditResponsesGroupedByContestName.add(raireProblem.solve());

    // public RaireResult(Votes votes, Integer claimed_winner, AuditType audit, TrimAlgorithm trim_algorithm, TimeOut timeout) throws

    return auditResponsesGroupedByContestName;
  }

  public ElectionData buildRaireRequest(Collection<CvrContestInfo> cvrContestInfos) {
    // List of votes
    List<String> choices = cvrContestInfos.stream().map(CvrContestInfo::getChoices).toList();

    List<String> sanitizedChoices = choices.stream().map(StringUtils::toRootUpperCase)
        .map(choice -> StringUtils.replace(choice, "[", ""))
        .map(choice -> StringUtils.replace(choice, "]", ""))
        .map(choice -> StringUtils.replace(choice, "\"", ""))
        .map(StringUtils::trim).toList();

    List<String[]> splitChoices = sanitizedChoices.stream().map(s -> s.split(",")).toList();

    // A map from candidate name to ID.
    Map<String, Integer> candidatesMap = buildCandidatesMap(splitChoices);

    // Iterate through the votes, converting them from a list of names to a list of IDs.
    // Store them in a map from preference orderings to the number of times that order was encountered.
    Map<List<Integer>, Integer> raireBallots = new HashMap<>();
    for (String[] preferences : splitChoices) {
      List<Integer> IDPreferenceList = nameListToIDList(candidatesMap, preferences);
      raireBallots.put(IDPreferenceList, raireBallots.getOrDefault(IDPreferenceList, 0) + 1);
    }

    Set<Integer> uniqueCandidates = new HashSet<>();
    // Not sure what this line is doing.
    raireBallots.keySet().forEach(uniqueCandidates::addAll);
    //Build ElectionData
    List<ConnectorVote> votes = getVotes(raireBallots);

    // Build candidate metadata, i.e. the list of candidates with corresponding IDs.
    Map<Integer, String> orderedCandidates = new TreeMap<>();
    candidatesMap.forEach((key, value) -> orderedCandidates.put(value, key));

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("candidates", orderedCandidates.values());
    return buildElectionDataModel(cvrContestInfos, uniqueCandidates, votes,
        metadata);
  }

  private ElectionData buildElectionDataModel(Collection<CvrContestInfo> cvrContestInfos,
                                              Set<Integer> uniqueCandidates, List<ConnectorVote> votes, Map<String, Object> metadata) {
    return ElectionData.builder()
        .metadata(objectMapper.valueToTree(metadata))
        .audit(Audit.builder().totalAuditableBallots(cvrContestInfos.size())
            .type("OneOnMargin")
            .build())
//        .winner(2)
        .totalVotes(cvrContestInfos.size())
        .numberOfCandidates(uniqueCandidates.size())
        .votes(votes)
        .build();
  }

  private List<ConnectorVote> getVotes(Map<List<Integer>, Integer> raireBallots) {
    return raireBallots.entrySet().stream()
        .map(entry -> ConnectorVote.builder()
            .count(entry.getValue())
            .preference(entry.getKey())
            .build())
        .collect(Collectors.toList());
  }

  // Input:
  // preferences - an ordered list of candidate names
  // candidatesMap - a map from candidate names to IDs
  // Convert a list of names (assumed to be in the order of preference) into the corresponding list of IDs
  // according to the candidate map.
  private List<Integer> nameListToIDList(Map <String, Integer> candidatesMap, String[] preferences) {
      return Arrays.stream(preferences).map(c -> candidatesMap.get(c)).toList();
  }

  // Generates a map from candidate name to (arbitrary) ID. This ID is used to express votes to RAIRE.
  // It simply ignores multiple copies of the same candidate name.
  // TODO This could probably be done more easily than deriving them from the complete set of CVRs.
  // Should be able to retrieve the list of choices from the ContestChoice table (name column) in the database.
  // If these are not identical for every contestID with the same contest name, that's an error.
  // Then this function would simply need to iterate through the single list/set of expected choice names.
  private Map<String, Integer> buildCandidatesMap(List<String[]> sanitizedChoices) {
    int count = 0;
    Map<String, Integer> candidatesMap = new HashMap<>();
    for (String[] choiceArray : sanitizedChoices) {
      for (String candidateName : choiceArray) {
        if (!candidatesMap.containsKey(candidateName)) {
          candidatesMap.put(candidateName, count++);
        }
      }
    }
    return candidatesMap;
  }

}
