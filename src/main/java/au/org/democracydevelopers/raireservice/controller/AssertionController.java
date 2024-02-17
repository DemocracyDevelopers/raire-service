package au.org.democracydevelopers.raireservice.controller;

import au.org.democracydevelopers.raireservice.request.ContestRequestByIDs;
import au.org.democracydevelopers.raireservice.request.RequestByContestName;
import au.org.democracydevelopers.raireservice.response.GetAssertionsResponse;
import au.org.democracydevelopers.raireservice.service.GetAssertionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/raire")
// @RequiredArgsConstructor
public class AssertionController {

  private final GetAssertionsService getAssertionsService;


  @PostMapping(path = "/generate-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public GenerateAssertionResponse serve(@RequestBody ContestRequestByIDs contests) {
    log.info("Received request to get assertions for contest:  {}", contests.getContestName());
    return generateAssertionsService.generateAssertions(contest);
  }

  public AssertionController(GetAssertionsService getAssertionsService) {this.getAssertionsService = getAssertionsService;}

  @PostMapping(path = "/get-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public GetAssertionsResponse serve(@RequestBody RequestByContestName contest) {
    log.info("Received request to get assertions for contest:  {}", contest.getContestName());
    return getAssertionsService.getAssertions(contest);
  }
}
