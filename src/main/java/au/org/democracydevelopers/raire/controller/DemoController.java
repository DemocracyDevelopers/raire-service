package au.org.democracydevelopers.raire.controller;

import au.org.democracydevelopers.raire.domain.DemoPOJO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("demo")
public class DemoController {

  @GetMapping(path = "/hello/{firstName}/{lastName}", produces = MediaType.APPLICATION_JSON_VALUE) //this will automatically return a JSON serialized version of DemoPojo
  public DemoPOJO welcomeUser(@PathVariable final String firstName, @PathVariable final String lastName, @RequestParam final String salutation) {
    return new DemoPOJO(salutation, firstName, lastName);
  }

}
