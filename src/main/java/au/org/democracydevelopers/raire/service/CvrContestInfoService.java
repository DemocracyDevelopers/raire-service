package au.org.democracydevelopers.raire.service;

import au.org.democracydevelopers.raire.domain.Audit;
import au.org.democracydevelopers.raire.domain.CvrContestInfo;
import au.org.democracydevelopers.raire.domain.ElectionData;
import au.org.democracydevelopers.raire.domain.Vote;
import au.org.democracydevelopers.raire.repository.CvrContestInfoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

  private final CvrContestInfoRepository cvrContestInfoRepository;
//  private final RaireClient raireClient;
  private static final ObjectMapper objectMapper = new ObjectMapper();
  public ElectionData findCvrContestInfo() {
    List<CvrContestInfo> cvrContestInfos = cvrContestInfoRepository.findAll();
    log.info("retrieved cvrContestInfos records {}", cvrContestInfos.size());
    return parse(cvrContestInfos);
  }

  public ElectionData parse(List<CvrContestInfo> cvrContestInfos) {
    List<String> choices = cvrContestInfos.stream().map(CvrContestInfo::getChoices).toList();
    List<String> sanitizedChoices = choices.stream().map(StringUtils::toRootUpperCase)
        .map(choice -> StringUtils.replace(choice, "[", ""))
        .map(choice -> StringUtils.replace(choice, "", ""))
        .map(choice -> StringUtils.replace(choice, "\"", ""))
        .map(StringUtils::trim).toList();

    Map<String, Integer> candidatesMap = buildCandidatesMap(sanitizedChoices);
    Map<List<Integer>, Integer> raireBallots = new HashMap<>();
    sanitizedChoices.stream()
        .map(sanitizedChoice -> sanitizedChoice.trim().split(","))
        .map(strings -> Arrays.stream(strings)
            .map(preference -> {
              String[] vote = preference.split("\\(");
              String rank = vote[1].split("\\)")[0];

              return candidatesMap.get(vote[0].trim());
            }).toList())
        .forEach(ballot -> raireBallots.put(ballot, raireBallots.getOrDefault(ballot, 0) + 1));

    Set<Integer> uniqueCandidates = new HashSet<>();
    raireBallots.keySet().forEach(uniqueCandidates::addAll);
    //Build ElectionData
    List<Vote> votes = raireBallots.entrySet().stream()
        .map(entry -> Vote.builder()
            .count(entry.getValue())
            .preference(entry.getKey())
            .build())
        .collect(Collectors.toList());
    Map<Integer, String> orderedCandidates = new TreeMap<>();
    candidatesMap.forEach((key, value) -> orderedCandidates.put(value, key));
    Map<String, Object> metadata = new HashMap<>() ;
    metadata.put("candidates", orderedCandidates.values());
    ElectionData electionData = ElectionData.builder()
        .metadata(objectMapper.valueToTree(metadata))
        .audit(Audit.builder().totalAuditableBallots(cvrContestInfos.size())
            .type("OneOnMargin")
            .build())
        .totalVotes(cvrContestInfos.size())
        .numberOfCandidates(uniqueCandidates.size())
        .votes(votes)
        .build();
    //call raire service
    getRaireResponse(electionData);
    return electionData;
  }

  private void getRaireResponse(ElectionData electionData) {
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<Map> mapResponseEntity = restTemplate.postForEntity(
        "http://localhost:3000/raire", electionData, Map.class);
//    JsonNode raireAuditResult = raireClient.getRaireAuditResult(electionData);
    log.info("Received Raire Audit Result {}", mapResponseEntity.getBody());

  }

  private Map<String, Integer> buildCandidatesMap(List<String> sanitizedChoices) {
    int count = 1;
    Map<String, Integer> candidatesMap = new HashMap<>();
    for (String sanitizedChoice : sanitizedChoices) {
      String[] choices = sanitizedChoice.split(",");
      for (String choice : choices) {
        String candidateName = choice.split("\\(")[0].trim();
        if(!candidatesMap.containsKey(candidateName)) {
          candidatesMap.put(candidateName, count++);
        }
      }
    }
    return candidatesMap;
  }

}
