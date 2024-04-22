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

import au.org.democracydevelopers.raireservice.response.GetAssertionsResponse;
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.time.TimeTaken;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.RequestValidationException;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.response.Metadata;
import au.org.democracydevelopers.raireservice.service.GenerateAssertionsException;
import au.org.democracydevelopers.raireservice.service.GenerateAssertionsService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

  private final GenerateAssertionsService generateAssertionsService;

  /**
   * The API endpoint for generating assertions, by contest name, and returning the IRV winner.
   * @param request a GenerateAssertionsRequest, specifying an IRV contest name for which to generate
   *                the assertions.
   * @return the winner (in the case of success) or an error. The winner, together with the contest,
   * is a GenerateAssertionsResponse. TODO the String is just a placeholder for now.
   * In the case of success, it also stores the assertions that were derived for the specified contest
   * in the database.
   * @throws RequestValidationException which is handled by ControllerExceptionHandler.
   * This tests for invalid requests, such as non-existent, null, or non-IRV contest names.
   * Other exceptions that are specific to assertion generation are caught and translated into the
   * appropriate http error. TODO add these when assertion generation is implemented.
   */
  @PostMapping(path = "/generate-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GenerateAssertionsResponse> serve(@RequestBody GenerateAssertionsRequest request)
      throws RequestValidationException, GenerateAssertionsException {
      request.Validate(contestRepository);
      GenerateAssertionsResponse response = generateAssertionsService.generateAssertions();
      return new ResponseEntity<>(response, HttpStatus.OK);
  }


  /**
   * The API endpoint for finding and return assertions, by contest name.
   * @param request a GetAssertionsRequest, specifying an IRV contest name for which to retrieve the
   *                assertions.
   * @return the assertions, as JSON (in the case of success) or an error. TODO the String is just a placeholder for now.
   * @throws RequestValidationException which is handled by ControllerExceptionHandler.
   * This tests for invalid requests, such as non-existent, null, or non-IRV contest names.
   * Other exceptions that are specific to assertion generation are caught and translated into the
   * appropriate http error. TODO add these when assertion retrieval is implemented.
   */
  @PostMapping(path = "/get-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GetAssertionsResponse> serve(@RequestBody GetAssertionsRequest request)
      throws RequestValidationException {

    request.Validate(contestRepository);

    try {
      // For the moment, this is just a dummy "OK" response. Later, it will contain the winner.
      GetAssertionsResponse dummyResponse = new GetAssertionsResponse(
          new Metadata(request, List.of()),
          new RaireResult(new AssertionAndDifficulty[0], 10.0, 100, 0,
              5, new TimeTaken(5L, 5), new TimeTaken(2L, 2),
              new TimeTaken(2L, 2), false)
      );
      return new ResponseEntity<>(dummyResponse, HttpStatus.OK);

      // TODO Catch all the exceptions that the GetAssertionsService can throw, for example if the
      // assertions are present but retrieval fails for some reason.
      // They can either be caught here or handled by the ControllerExceptionHandler.
      // This should be part of Issue https://github.com/DemocracyDevelopers/raire-service/issues/44
    } catch (IllegalArgumentException  | ArrayIndexOutOfBoundsException ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
  }

  /**
   * All args constructor
   * @param contestRepository the contestRepository, used for validating requests.
   * @param generateAssertionsService the generateAssertions service.
   */
  public AssertionController(ContestRepository contestRepository,
      GenerateAssertionsService generateAssertionsService) {
    this.contestRepository = contestRepository;
    this.generateAssertionsService = generateAssertionsService;
  }
}
