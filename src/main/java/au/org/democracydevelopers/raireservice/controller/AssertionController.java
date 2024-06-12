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

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.RequestValidationException;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.service.GetAssertionsCsvService;
import au.org.democracydevelopers.raireservice.service.RaireServiceException;
import au.org.democracydevelopers.raireservice.service.GenerateAssertionsService;
import au.org.democracydevelopers.raireservice.service.GetAssertionsJsonService;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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

  private static final Logger logger = LoggerFactory.getLogger(AssertionController.class);

  private final ContestRepository contestRepository;

  private final GenerateAssertionsService generateAssertionsService;

  private final GetAssertionsJsonService getAssertionsService;
  private final GetAssertionsCsvService getAssertionsCSVService;

  /**
   * The API endpoint for generating assertions, by contest name, and returning the IRV winner as
   * part of a GenerateAssertionsResponse. The raire-java API will be accessed to generate
   * assertions for the contest. If this is successful, these assertions will be stored in the
   * database.
   * @param request a ContestRequest, specifying an IRV contest name for which to generate
   *                the assertions.
   * @return the winner (in the case of success) or an error. The winner, together with the contest,
   * is a GenerateAssertionsResponse.
   * @throws RequestValidationException which is handled by ControllerExceptionHandler.
   * This tests for invalid requests, such as non-existent, null, or non-IRV contest names.
   * @throws RaireServiceException for other errors that are specific to assertion generation, such
   * as tied winners or timeouts. These are caught by ControllerExceptionHandler and translated into the
   * appropriate http error.
   */
  @PostMapping(path = "/generate-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GenerateAssertionsResponse> serve(@RequestBody GenerateAssertionsRequest request)
      throws RequestValidationException, RaireServiceException
  {
    final String prefix = "[endpoint:generate-assertions]";
    logger.debug(String.format("%s Assertion generation request received for contest: %s.",
        prefix, request.contestName));

    // Validate request: validation errors will be thrown as RequestValidationExceptions to be
    // handled by the ControllerExceptionHandler.
    request.Validate(contestRepository);
    logger.debug(String.format("%s Assertion generation request successfully validated.",prefix));

    // Call raire-java to generate assertions, and check if it was able to do so successfully.
    logger.debug(String.format("%s Calling raire-java with assertion generation request.",prefix));
    RaireResultOrError solution = generateAssertionsService.generateAssertions(request);

    if (solution.Ok != null) {
      // Generation of assertions was successful, now save them to the database.
      logger.debug(String.format("%s Assertion generation successful: %d assertions " +
              "generated in %ss.", prefix, solution.Ok.assertions.length,
              solution.Ok.time_to_find_assertions.seconds));
      generateAssertionsService.persistAssertions(solution.Ok, request);

      logger.debug(String.format("%s Assertions stored in database for contest %s.",
          prefix, request.contestName));

      // Form and return request response.
      GenerateAssertionsResponse response = new GenerateAssertionsResponse(request.contestName,
          request.candidates.get(solution.Ok.winner));

      logger.debug(String.format("%s Assertion generation and storage complete.", prefix));
      return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // raire-java was not able to generate assertions successfully.
    if(solution.Err == null){
      final String msg = "An error occurred in raire-java, yet no error information was returned.";
      logger.error(String.format("%s %s", prefix, msg));
      throw new RaireServiceException(msg, RaireErrorCode.INTERNAL_ERROR);
    }

    // raire-java returned error information, form and throw an exception using that data. (Note:
    // we need to create the exception first to get a human readable message to log).
    RaireServiceException ex = new RaireServiceException(solution.Err, request.candidates);
    final String msg = "An error occurred in raire-java: " + ex.getMessage();
    logger.error(String.format("%s %s", prefix, msg));
    throw ex;
  }


  /**
   * The API endpoint for finding and returning assertions, by contest name. This endpoint returns
   * assertions in the form of a JSON Visualiser Report.
   * @param request a GetAssertionsRequest, specifying an IRV contest name for which to retrieve the
   *        assertions.
   * @return the assertions, as JSON (in the case of success) or an error.
   * @throws RequestValidationException for invalid requests, such as non-existent, null, or
   *         non-IRV contest names.
   * @throws RequestValidationException for invalid requests, such as non-existent, null, or non-IRV
   *         contest names.
   * @throws RaireServiceException if the request is valid but assertion retrieval fails, for example
   *         if there are no assertions for the contest.
   * These exceptions are handled by ControllerExceptionHandler.
   */
  @PostMapping(path = "/get-assertions-json", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<RaireSolution> serveJson(@RequestBody GetAssertionsRequest request)
      throws RequestValidationException, RaireServiceException {

    final String prefix = "[endpoint:get-assertions(json)]";
    logger.debug(String.format(
        "%s Get assertions request in JSON visualiser format for contest %s with candidates %s.",
        prefix, request.contestName, request.candidates));

    // Validate request: errors in the request will be thrown as RequestValidationExceptions that
    // are handled by the ControllerExceptionHandler.
    request.Validate(contestRepository);
    logger.debug(String.format("%s Get assertions request successfully validated.", prefix));

    // Extract a RaireSolution containing the assertions that we want to serialise into
    // a JSON Assertion Visualiser report.
    RaireSolution solution = getAssertionsService.getRaireSolution(request);
    logger.debug(String.format("%s Report generated for return.", prefix));

    return new ResponseEntity<>(solution, HttpStatus.OK);
  }

  /**
   * The API endpoint for finding and returning assertions, by contest name. This endpoint returns
   * assertions as a csv file.
   * @param request a GetAssertionsRequest, specifying an IRV contest name for which to retrieve the
   *                assertions.
   * @return the assertions, as a csv file (in the case of success) or an error.
   * @throws RequestValidationException for invalid requests, such as non-existent, null, or non-IRV
   *         contest names.
   * @throws RaireServiceException if the request is valid but assertion retrieval fails, for example
   *         if there are no assertions for the contest.
   * These exceptions are handled by ControllerExceptionHandler.
   */
  @PostMapping(path = "/get-assertions-csv")
  @ResponseBody
  public ResponseEntity<String> serveCSV(@RequestBody GetAssertionsRequest request)
      throws RequestValidationException, RaireServiceException {

    // Validate the request.
    request.Validate(contestRepository);

    // Extract the assertions as csv.
    String csv = getAssertionsCSVService.generateCSV(request);

    return new ResponseEntity<>(csv, HttpStatus.OK);
  }

  /**
   * All args constructor
   * @param contestRepository the contestRepository, used for validating requests.
   * @param generateAssertionsService the generateAssertions service.
   * @param getAssertionsService the getAssertions service.
   */
  public AssertionController(ContestRepository contestRepository,
      GenerateAssertionsService generateAssertionsService,
      GetAssertionsJsonService getAssertionsService, GetAssertionsCsvService getAssertionsCSVService) {
    this.contestRepository = contestRepository;
    this.generateAssertionsService = generateAssertionsService;
    this.getAssertionsService = getAssertionsService;
    this.getAssertionsCSVService = getAssertionsCSVService;
  }
}
