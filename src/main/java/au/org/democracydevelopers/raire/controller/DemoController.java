package au.org.democracydevelopers.raire.controller;

import static net.logstash.logback.argument.StructuredArguments.kv;

import au.org.democracydevelopers.raire.domain.DemoPojo;
import au.org.democracydevelopers.raire.service.DemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("demo")
@RequiredArgsConstructor // this will automatically create a parameterised constructor with demoService as one of the parameters. This annotation will include parameters marked "final"
public class DemoController {
  private static final String GET_ENTRY_LOG = "Received a GET request with parameters {} {} {}";
  private static final String POST_ENTRY_LOG = "Received a POST request with request body {}"; // only printing body for demo. in production, we wouldn't print this
  private final DemoService demoService;

  @GetMapping(path = "/hello/{firstName}/{lastName}", produces = MediaType.APPLICATION_JSON_VALUE) //this will automatically return a JSON serialized version of DemoPojo
  public DemoPojo welcomeUser(@PathVariable final String firstName, @PathVariable final String lastName, @RequestParam final String salutation) {
    log.info(GET_ENTRY_LOG, kv("firstName", firstName), kv("lastname", lastName), kv("salutation", salutation));
    return new DemoPojo(salutation, firstName, lastName);
  }

  @PostMapping(path = "/hello", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) //this will automatically return a JSON serialized version of DemoPojo
  public DemoPojo welcomeUser(@RequestBody DemoPojo demoPojo) {
    log.info(POST_ENTRY_LOG, kv("requestBody", demoPojo));
    return demoService.buildDefaultPojo(demoPojo);
  }
}
