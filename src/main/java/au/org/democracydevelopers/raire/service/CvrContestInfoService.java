package au.org.democracydevelopers.raire.service;

import au.org.democracydevelopers.raire.domain.Audit;
import au.org.democracydevelopers.raire.domain.CvrContestInfo;
import au.org.democracydevelopers.raire.domain.ElectionData;
import au.org.democracydevelopers.raire.domain.Vote;
import au.org.democracydevelopers.raire.repository.CvrContestInfoRepository;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        .map(choice -> StringUtils.replace(choice, "CANDIDATE", ""))
        .map(choice -> StringUtils.replace(choice, "WRITE-IN", "99"))
        .map(StringUtils::trim).toList();

    Map<List<Integer>, Integer> raireBallots = new HashMap<>();
    sanitizedChoices.stream()
        .map(sanitizedChoice -> sanitizedChoice.trim().split(","))
        .map(strings -> Arrays.stream(strings)
            .map(preference -> preference.split("\\(")[0].trim()).map(Integer::parseInt).toList())
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



    return ElectionData.builder()
        .audit(Audit.builder().totalAuditableBallots(String.valueOf(cvrContestInfos.size()))
            .type("dummy") //TODO discuss how to build audit object
            .build())
        .totalVotes(uniqueCandidates.size())
        .numberOfCandidates(uniqueCandidates.size())
        .votes(votes)
        .build();
  }
}
