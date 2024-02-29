package au.org.democracydevelopers.raireservice.controller;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raireservice.request.ContestRequestByIDs;
import au.org.democracydevelopers.raireservice.request.DirectContestRequest;
import au.org.democracydevelopers.raireservice.request.RequestByContestName;
import au.org.democracydevelopers.raireservice.response.*;
import au.org.democracydevelopers.raireservice.service.GenerateAssertionsService;
import au.org.democracydevelopers.raireservice.service.GetAssertionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/raire")
@RequiredArgsConstructor
public class AssertionController {

  private final GetAssertionsService getAssertionsService;
  private final GenerateAssertionsService generateAssertionsService;

  @PostMapping(path = "/generate-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public GenerateAssertionsResponse serve(@RequestBody ContestRequestByIDs request) {
    log.info("Received request to get assertions for contest:  {}", request.getContestName());
    try {
      DirectContestRequest contest = generateAssertionsService.getVotesFromDatabase(request);
      RaireSolution.RaireResultOrError solution = generateAssertionsService.generateAssertions(contest);
      return generateAssertionsService.storeAssertions(solution, contest);
    } catch (RaireServiceException e) {
      return new GenerateAssertionsResponse(request.getContestName(), new GenerateAssertionsResponse.GenerateAssertionsResultOrError(e.error));
    }
  }


  @PostMapping(path = "/get-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public GetAssertionsResponse serve(@RequestBody RequestByContestName contest) {
    log.info("Received request to get assertions for contest:  {}", contest.getContestName());
    return getAssertionsService.getAssertions(contest);
  }

  // A stateless version which is not used in the current design but may be useful for testing.
  @PostMapping(path = "/generate-and-get-assertions", produces = MediaType.APPLICATION_JSON_VALUE)

  public GetAssertionsResponse serve(@RequestBody DirectContestRequest contest) {
    log.info("Received request to generate and get assertions for contest:  {}", contest.getContestName());
    RaireSolution.RaireResultOrError solution = generateAssertionsService.generateAssertions(contest);
    return new GetAssertionsResponse(new Metadata(contest).getMetadata(), new GetAssertionsResponse.GetAssertionResultOrError(solution));
  }
}
