package au.org.democracydevelopers.raireservice.controller;

import au.org.democracydevelopers.raireservice.request.ContestRequestByIDs;
import au.org.democracydevelopers.raireservice.request.OldContestRequest;
import au.org.democracydevelopers.raireservice.request.RequestByContestName;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.response.GetAssertionsResponse;
import au.org.democracydevelopers.raireservice.service.GenerateAssertionsService;
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
  private final GenerateAssertionsService generateAssertionsService;



  public AssertionController(GetAssertionsService getAssertionsService, GenerateAssertionsService generateAssertionsService) {this.getAssertionsService = getAssertionsService;
      this.generateAssertionsService = generateAssertionsService;
  }

  @PostMapping(path = "/generate-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public GenerateAssertionsResponse serve(@RequestBody OldContestRequest contest) {
    log.info("Received request to get assertions for contest:  {}", contest.getContestName());
    return generateAssertionsService.generateAssertions(contest);
  }


  @PostMapping(path = "/get-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public GetAssertionsResponse serve(@RequestBody RequestByContestName contest) {
    log.info("Received request to get assertions for contest:  {}", contest.getContestName());
    return getAssertionsService.getAssertions(contest);
  }
}
