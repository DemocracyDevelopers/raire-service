package au.org.democracydevelopers.raire.controller;

import au.org.democracydevelopers.raire.domain.ElectionData;
import au.org.democracydevelopers.raire.service.CvrContestInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/cvr")
@RequiredArgsConstructor
public class CvrContestInfoController {

  private final CvrContestInfoService cvrContestInfoService;

  @GetMapping(path = "parser", produces = MediaType.APPLICATION_JSON_VALUE)
  public ElectionData serve() {
    return cvrContestInfoService.findCvrContestInfo();
  }
}
