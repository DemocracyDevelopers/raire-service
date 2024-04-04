/*
Copyright 2024 Democracy Developers

The Raire Service is designed to connect colorado-rla and its associated database to
the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).

This file is part of raire-service.

raire-service is free software: you can redistribute it and/or modify it under the terms
of the GNU Affero General Public License as published by the Free Software Foundation, either
version 3 of the License, or (at your option) any later version.

raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with
raire-service. If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.controller;

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.RequestValidationException;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.response.GetAssertionsResponse;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This class controls the post request mappings for all requests related
 * to assertions.
 * /generate-assertions takes a generate assertions request (contest by name) and generates the
 * assertions for that contest. In the case of success, it returns the winner
 * and stores the assertions in the database. Otherwise, it returns an error.
 * /get-assertions takes a get assertions request (contest by name) and tries to retrieve
 * the assertions for that contest from the database. In the case of success, it returns
 * the assertions as json, in a form appropriate for the assertion explainer. Otherwise, it
 * returns an error.
 */
@RestController
@RequestMapping("/raire")
public class AssertionController {

  private final ContestRepository contestRepository;


  @PostMapping(path = "/generate-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> serve(@RequestBody GenerateAssertionsRequest request)
      throws RequestValidationException {
    // try {
      request.Validate(contestRepository);
      // For the moment, this is just a dummy "OK" response. Later, it will contain the winner.
      return new ResponseEntity<String>("Placeholder winner", HttpStatus.OK);
      // return new GenerateAssertionsResponse(request.getContestName(), "OK");
    // } catch (RequestValidationException e) {
    //   return new GenerateAssertionsResponse(request.getContestName(), e);
    //}
  }


  /**
   * Find and return assertions, by contest name.
   * @param request
   * @return the assertions, as JSON. TODO String is just a placeholder for now.
   */
  @PostMapping(path = "/get-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> serve(@RequestBody GetAssertionsRequest request)
      throws RequestValidationException {
    // try {
      request.Validate(contestRepository);
      // For the moment, this is just a dummy "OK" response. Later, it will contain the winner.
      return new ResponseEntity<String>("Placeholder assertions", HttpStatus.OK);
      // return new GetAssertionsResponse(request.getContestName(), "OK");
    // } catch (RequestValidationException e) {
    //  return new GetAssertionsResponse(request.getContestName(), e);
    //}
  }

  /**
   * Constructor
   * @param contestRepository the contestRespository, used for validating requests.
   */
  public AssertionController(ContestRepository contestRepository) {
    this.contestRepository = contestRepository;
  }
}
