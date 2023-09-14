package au.org.democracydevelopers.raire.service;

import au.org.democracydevelopers.raire.domain.Audit;
import au.org.democracydevelopers.raire.domain.CvrContestInfo;
import au.org.democracydevelopers.raire.domain.ElectionData;
import au.org.democracydevelopers.raire.domain.Vote;
import au.org.democracydevelopers.raire.repository.CvrContestInfoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CvrContestInfoService {

  private final CvrContestInfoRepository cvrContestInfoRepository;
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
            .map(preference -> candidatesMap.get(preference.split("\\(")[0].trim())).toList())
        .toList()
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

    return ElectionData.builder()
        .metadata(objectMapper.valueToTree(metadata))
        .audit(Audit.builder().totalAuditableBallots(String.valueOf(cvrContestInfos.size()))
            .type("dummy") //TODO discuss how to build audit object
            .build())
        .totalVotes(cvrContestInfos.size())
        .numberOfCandidates(uniqueCandidates.size())
        .votes(votes)
        .build();
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
