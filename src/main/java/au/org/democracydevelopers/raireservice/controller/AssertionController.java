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
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.request.RequestValidationException;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.service.GetAssertionsCSVService;
import au.org.democracydevelopers.raireservice.service.RaireServiceException;
import au.org.democracydevelopers.raireservice.service.GenerateAssertionsService;
import au.org.democracydevelopers.raireservice.service.GetAssertionsService;
import org.springframework.http.HttpHeaders;
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

  private final ContestRepository contestRepository;

  private final GenerateAssertionsService generateAssertionsService;

  private final GetAssertionsService getAssertionsService;
  private final GetAssertionsCSVService getAssertionsCSVService;

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
   * @throws RaireServiceException for other errors that are specific to assertion generation, such
   * as tied winners or timeouts. These are caught by ControllerExceptionHandler and translated into the
   * appropriate http error.
   */
  @PostMapping(path = "/generate-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<GenerateAssertionsResponse> serve(@RequestBody GenerateAssertionsRequest request)
      throws RequestValidationException, RaireServiceException {
      request.Validate(contestRepository);
      GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);
      return new ResponseEntity<>(response, HttpStatus.OK);
  }


  /**
   * The API endpoint for finding and returning assertions, by contest name. This endpoint returns
   * assertions in the form of a JSON Visualiser Report.
   * @param request a GetAssertionsRequest, specifying an IRV contest name for which to retrieve the
   *        assertions.
   * @return the assertions, as JSON (in the case of success) or an error.
   * @throws RequestValidationException for invalid requests, such as non-existent, null, or non-IRV
   *         contest names.
   * @throws RaireServiceException if the request is valid but assertion retrieval fails, for example
   *         if there are no assertions for the contest.
   * These exceptions are handled by ControllerExceptionHandler.
   */
  @PostMapping(path = "/get-assertions", produces = MediaType.APPLICATION_JSON_VALUE)
  @ResponseBody
  public ResponseEntity<RaireSolution> serveJson(@RequestBody GetAssertionsRequest request)
      throws RequestValidationException, RaireServiceException {

    request.Validate(contestRepository);

    // Extract a RaireSolution containing the assertions that we want to serialise into
    // a JSON Assertion Visualiser report.
    RaireSolution solution = getAssertionsService.getRaireSolution(request);

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
  public ResponseEntity<byte []> serveCSV(@RequestBody GetAssertionsRequest request)
      throws RequestValidationException, RaireServiceException {

    request.Validate(contestRepository);

    // Extract a RaireSolution containing the assertions that we want, then convert to csv.
    byte[] csv = getAssertionsCSVService.generateCSV(request).getBytes();

    // Set correct headers; return response.
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDispositionFormData("attachment",
        removeIllegalChars(request.contestName)+"_Assertions.csv");
    return new ResponseEntity<>(csv, headers, HttpStatus.OK);
  }

  /**
   * All args constructor
   * @param contestRepository the contestRepository, used for validating requests.
   * @param generateAssertionsService the generateAssertions service.
   * @param getAssertionsService the getAssertions service.
   */
  public AssertionController(ContestRepository contestRepository,
      GenerateAssertionsService generateAssertionsService,
      GetAssertionsService getAssertionsService, GetAssertionsCSVService getAssertionsCSVService) {
    this.contestRepository = contestRepository;
    this.generateAssertionsService = generateAssertionsService;
    this.getAssertionsService = getAssertionsService;
    this.getAssertionsCSVService = getAssertionsCSVService;
  }

  /**
   * Remove the characters that will not be accepted as a filename. Because this is platform-
   * dependent, we retain only a subset that will be accepted by Mac,Windows and Linux.
   * Anything else is dropped. This is an extra-restrictive test: there are valid filenames that
   * are not accepted here.
   * Based on the <a href="https://wiki.sei.cmu.edu/confluence/display/java/IDS50-J.+Use+conservative+file+naming+conventions">
   * Conservative file naming conventions</A> of David Svoboda, disallow whitespace and leading
   * hyphens. (Actually that reference advocates completely disallowing whitespace, whereas here we
   * allow spaces if they are not leading.)
   * @param contestName the name of the contest
   * @return the same name, with any problematic characters filtered out.
   */
  private String removeIllegalChars(String contestName) {
    // Remove any characters other than those on the allowed list.
    String cleanedName = contestName.replaceAll("[^-_ .A-Za-z0-9]","");
    // Remove leading hyphens and spaces.
    return cleanedName.replaceAll("^[- ]*","");
  }
}
