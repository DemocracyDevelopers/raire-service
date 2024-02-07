package au.org.democracydevelopers.raireservice.controller;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raireservice.request.OldContestRequest;
import au.org.democracydevelopers.raireservice.service.CvrContestInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/cvr")
@RequiredArgsConstructor
public class CvrContestInfoController {

  private final CvrContestInfoService cvrContestInfoService;

  @PostMapping(path = "audit", produces = MediaType.APPLICATION_JSON_VALUE)
  public RaireSolution serve(@RequestBody OldContestRequest contest) {
    log.info("Received request to audit contests with name: {}", contest.getContestName());
    return cvrContestInfoService.findCvrContestInfo(contest);
  }
}
