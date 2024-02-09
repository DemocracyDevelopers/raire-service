package au.org.democracydevelopers.raireservice.controller;

import au.org.democracydevelopers.raireservice.request.ContestRequestByName;
import au.org.democracydevelopers.raireservice.response.GetAssertionResponse;
import au.org.democracydevelopers.raireservice.service.GetAssertionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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


  /*
  @PostMapping(path = "generate-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  TODO
   */
  public AssertionController(GetAssertionsService getAssertionsService) {this.getAssertionsService = getAssertionsService;}

  @PostMapping(path = "/get-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public GetAssertionResponse serve(@RequestBody ContestRequestByName contest) {
    log.info("Received request to get assertions for contest:  {}", contest.getContestName());
    // return new ResponseEntity<String>("TestThisWorks");
    GetAssertionResponse response = getAssertionsService.getAssertions(contest);
    return response;
  }
}
