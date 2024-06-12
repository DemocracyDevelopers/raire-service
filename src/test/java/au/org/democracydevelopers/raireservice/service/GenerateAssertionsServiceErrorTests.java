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

package au.org.democracydevelopers.raireservice.service;

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.WRONG_CANDIDATE_NAMES;
import static au.org.democracydevelopers.raireservice.testUtils.aliceAndBob;
import static au.org.democracydevelopers.raireservice.testUtils.aliceAndBobAndCharlie;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.testUtils;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests for appropriate responses to bad input to the generate assertions service.
 * These sorts of errors are _not_ supposed to happen - they indicate programming errors or problems
 * with databases etc. Currently, we check for invalid and inconsistent input.
 * The list of tests is similar to GenerateAssertionsAPIErrorTests.java, when the same test is
 * relevant to both services.
 * Contests which will be used for validity testing are
 * preloaded into the database using src/test/resources/data.sql.
 * Tests include:
 * - null, missing or whitespace contest name,
 * - non-IRV contests, mixed IRV-plurality contests or contests not in the database,
 * - null, missing or whitespace candidate names,
 * - candidate names that are valid but do not include all the candidates mentioned in votes in the
 *   database,
 * - missing, negative or zero values for numerical inputs (totalAuditableBallots and
 *   timeLimitSeconds).
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsServiceErrorTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsServiceErrorTests.class);

  @Autowired
  private GenerateAssertionsService generateAssertionsService;
  private final static String BallinaOneVote = "Ballina One Vote Contest";

  /**
   * The generateAssertions service, called with a nonexistent contest, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNonExistentContestIsAnError() {
    testUtils.log(logger, "generateAssertionsWithNonExistentContestIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest("NonExistentContest", 100,
        10, aliceAndBob);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "does not exist or is not all IRV"));
  }

  /**
   * The generateAssertions service, called with a valid IRV contest for which no votes are present,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsFromNoVotesIsAnError() {
    testUtils.log(logger, "generateAssertionsFromNoVotesIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest("No CVR Mayoral", 100,
        10, aliceAndBob);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(RaireErrorCode.NO_VOTES_PRESENT, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "No votes present for contest"));
  }

  /**
   * The generateAssertions service, called with a valid plurality contest, returns a meaningful
   * error.
   */
  @Test
  public void generateAssertionsWithPluralityContestIsAnError() {
    testUtils.log(logger, "generateAssertionsWithPluralityContestIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest(
        "Valid Plurality Contest", 100, 10, aliceAndBob);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "does not exist or is not all IRV"));
  }

  /**
   * The generateAssertions service, called with a mixed IRV and non-IRV contest,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithMixedIRVPluralityContestIsAnError() {
    testUtils.log(logger, "generateAssertionsWithMixedIRVPluralityContestIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest("Invalid Mixed Contest",
        100,10,aliceAndBob);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "does not exist or is not all IRV"));
  }

  /**
   * The generateAssertions service, called with an empty contest name, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithEmptyContestNameIsAnError() {
    testUtils.log(logger, "generateAssertionsWithEmptyContestNameIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest("",
        100,10,aliceAndBob);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "does not exist or is not all IRV"));
  }

  /**
   * The generateAssertions service, called with an all-whitespace contest name, returns a
   * meaningful error.
   */
  @Test
  public void generateAssertionsWithWhitespaceContestNameIsAnError() {
    testUtils.log(logger, "generateAssertionsWithWhitespaceContestNameIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest("   ",
        100, 10, aliceAndBob);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(RaireErrorCode.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "does not exist or is not all IRV"));
  }

  /**
   * The generateAssertions service, called with an empty candidate list, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithEmptyCandidateListIsAnError() {
    testUtils.log(logger, "generateAssertionsWithEmptyCandidateListIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest("Ballina One Vote Contest",
        100, 10, List.of());

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(WRONG_CANDIDATE_NAMES, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "not on the list of candidates"));
  }

  /**
   * The generateAssertions service, called with a whitespace candidate name, returns a meaningful
   * error.
   */
  @Test
  public void generateAssertionsWithWhiteSpaceCandidateNameIsAnError() {
    testUtils.log(logger, "generateAssertionsWithWhiteSpaceCandidateNameIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest(BallinaOneVote,
        100, 10, List.of("Alice", "   "));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(WRONG_CANDIDATE_NAMES, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "not on the list of candidates"));
  }

  /**
   * The generateAssertions service, called with zero total auditable ballots, returns a meaningful
   * error.
   */
  @Test
  public void generateAssertionsWithZeroAuditableBallotsIsAnError() {
    testUtils.log(logger, "generateAssertionsWithZeroAuditableBallotsIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest(BallinaOneVote,
        0, 10, aliceAndBobAndCharlie);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(RaireErrorCode.INVALID_TOTAL_AUDITABLE_BALLOTS, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "universe size"));
  }

  /**
   * The generateAssertions service, called with negative total auditable ballots,
   * returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNegativeAuditableBallotsIsAnError() {
    testUtils.log(logger, "generateAssertionsWithNegativeAuditableBallotsIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest(BallinaOneVote,
        -10, 10, aliceAndBobAndCharlie);

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(RaireErrorCode.INVALID_TOTAL_AUDITABLE_BALLOTS, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "universe size"));
  }

  /**
   * The generateAssertions service, called with zero time limit, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithZeroTimeLimitIsAnError() throws RaireServiceException {
    testUtils.log(logger, "generateAssertionsWithZeroTimeLimitIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest(BallinaOneVote,
        100, 0, aliceAndBobAndCharlie);

    var response = generateAssertionsService.generateAssertions(request);
    assertNull(response.Ok);
    assertNotNull(response.Err);
    assertInstanceOf(RaireError.InvalidTimeout.class, response.Err);
  }

  /**
   * The generateAssertions service, called with negative time limit, returns a meaningful error.
   */
  @Test
  public void generateAssertionsWithNegativeTimeLimitIsAnError() throws RaireServiceException {
    testUtils.log(logger, "generateAssertionsWithNegativeTimeLimitIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest(BallinaOneVote,
        100, -50, aliceAndBobAndCharlie);

    var response = generateAssertionsService.generateAssertions(request);
    assertNull(response.Ok);
    assertNotNull(response.Err);
    assertInstanceOf(RaireError.InvalidTimeout.class, response.Err);
  }

  /**
   * A GenerateAssertions request with a candidate list that is valid, but the votes in the database
   * contain at least one candidate who is not in the expected candidate list. This is an error.
   */
  @Test
  public void wrongCandidatesIsAnError() {
    testUtils.log(logger, "wrongCandidatesIsAnError");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest("Ballina One Vote Contest",
        100, 10, List.of("Alice","Bob","Chuan"));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        generateAssertionsService.generateAssertions(request));
    assertEquals(WRONG_CANDIDATE_NAMES, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "not on the list of candidates"));
  }
}