package au.org.democracydevelopers.raireconnector.service;

import au.org.democracydevelopers.raireconnector.client.RaireClient;
import au.org.democracydevelopers.raireconnector.domain.raire.Audit;
import au.org.democracydevelopers.raireconnector.domain.raire.ElectionData;
import au.org.democracydevelopers.raireconnector.domain.raire.Vote;
import au.org.democracydevelopers.raireconnector.domain.request.Contest;
import au.org.democracydevelopers.raireconnector.domain.response.AuditResponse;
import au.org.democracydevelopers.raireconnector.domain.response.RaireResponse;
import au.org.democracydevelopers.raireconnector.repository.CvrContestInfoRepository;
import au.org.democracydevelopers.raireconnector.repository.entity.CvrContestInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class CvrContestInfoService {
  // When gettiong contests by name, group contestIDs and assertions based on names as well not contestIds

  private final ObjectMapper objectMapper;
  private final CvrContestInfoRepository cvrContestInfoRepository;
  private final RaireClient raireClient;

  public List<AuditResponse> findCvrContestInfo(List<Contest> contests) {
    List<Integer> contestIds = contests.stream().map(Contest::getContestId)
        .collect(Collectors.toList());
    List<CvrContestInfo> cvrContestInfos = cvrContestInfoRepository.findByContestIdIn(contestIds);
    log.info("Retrieved all cvrContestInfos records {}", cvrContestInfos.size());
    Map<Integer, List<CvrContestInfo>> contestAudiRequests = cvrContestInfos.stream()
        .collect(Collectors.groupingBy(CvrContestInfo::getContestId));
    //iterate over all contestAuditRequests and collect results
    List<AuditResponse> auditResponses = new ArrayList<>();
    contestAudiRequests.forEach((contestId, cvrContests) -> {
      var electionData = buildRaireRequest(cvrContests);
      auditResponses.add(
          AuditResponse.builder().contestId(contestId)
              .result(raireClient.getRaireResponse(electionData))
              .build());
    });
    return auditResponses;
  }

  public ElectionData buildRaireRequest(List<CvrContestInfo> cvrContestInfos) {
    List<String> choices = cvrContestInfos.stream().map(CvrContestInfo::getChoices).toList();
    List<String> sanitizedChoices = choices.stream().map(StringUtils::toRootUpperCase)
        .map(choice -> StringUtils.replace(choice, "[", ""))
        .map(choice -> StringUtils.replace(choice, "", ""))
        .map(choice -> StringUtils.replace(choice, "\"", ""))
        .map(StringUtils::trim).toList();

    Map<String, Integer> candidatesMap = buildCandidatesMap(sanitizedChoices);
    Map<List<Integer>, Integer> raireBallots = new HashMap<>();
    for (String choice : sanitizedChoices) {
      buildChoices(candidatesMap, raireBallots, choice);
    }
    Set<Integer> uniqueCandidates = new HashSet<>();
    raireBallots.keySet().forEach(uniqueCandidates::addAll);
    //Build ElectionData
    List<Vote> votes = getVotes(raireBallots);

    Map<Integer, String> orderedCandidates = new TreeMap<>();
    candidatesMap.forEach((key, value) -> orderedCandidates.put(value, key));

    Map<String, Object> metadata = new HashMap<>();
    metadata.put("candidates", orderedCandidates.values());
    return buildElectionDataModel(cvrContestInfos, uniqueCandidates, votes,
        metadata);
  }

  private ElectionData buildElectionDataModel(List<CvrContestInfo> cvrContestInfos,
      Set<Integer> uniqueCandidates, List<Vote> votes, Map<String, Object> metadata) {
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

  private List<Vote> getVotes(Map<List<Integer>, Integer> raireBallots) {
    return raireBallots.entrySet().stream()
        .map(entry -> Vote.builder()
            .count(entry.getValue())
            .preference(entry.getKey())
            .build())
        .collect(Collectors.toList());
  }

  private void buildChoices(Map<String, Integer> candidatesMap,
      Map<List<Integer>, Integer> raireBallots, String choice) {
    String[] preferences = choice.trim().split(",");
    List<Integer> preferenceOrder = new ArrayList<>();
    if (preferences.length == 0) {
      return;
    }
    for (String preference : preferences) {
      String[] vote = preference.split("\\(");
      String rank = vote[1].split("\\)")[0];
      if (!StringUtils.isNumeric(rank)) {
        break; //Invalid rank. Ballot is not usable. IF ballot is invalid, this safeguards us against number format exception
      }
      try {
        preferenceOrder.add(Integer.parseInt(rank) - 1, candidatesMap.get(vote[0].trim()));

        // FIXME At the moment, this will simply skip any preference that is out of order.
        // This currently has the effect of accepting only the shortest valid prefix, which may
        // be empty.
        // Later, when this code is part of CVR upload parsing, we need to send an error back to the
        // user and get them to correct their CVR.
      } catch (IndexOutOfBoundsException e) {
        log.error("Invalid preferences: "+choice);
        break;
      }
    }
    raireBallots.put(preferenceOrder, raireBallots.getOrDefault(preferenceOrder, 0) + 1);
  }

  private Map<String, Integer> buildCandidatesMap(List<String> sanitizedChoices) {
    int count = 1;
    Map<String, Integer> candidatesMap = new HashMap<>();
    for (String sanitizedChoice : sanitizedChoices) {
      String[] choices = sanitizedChoice.split(",");
      for (String choice : choices) {
        String candidateName = choice.split("\\(")[0].trim();
        if (!candidatesMap.containsKey(candidateName)) {
          candidatesMap.put(candidateName, count++);
        }
      }
    }
    return candidatesMap;
  }

}
