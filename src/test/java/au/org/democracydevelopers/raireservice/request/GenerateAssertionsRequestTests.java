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

package au.org.democracydevelopers.raireservice.request;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
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
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests on GenerateAssertionsRequests, particularly focusing on the validation step.
 * Contests which will be used to test the validity of the GenerateAssertionsRequest are
 * preloaded into the database using src/test/resources/data.sql.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GenerateAssertionsRequestTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsRequestTests.class);

  @Autowired
  ContestRepository contestRepository;

  /**
   * Example candidate and contest names.
   */
  private final List<String> candidates = List.of("Alice");
  private final String ballina = "Ballina Mayoral";
  private final String multiCounty = "Multi-County Contest 1";

  /**
   * A valid request for an IRV contest is valid.
   */
  @Test
  public void validRequestForSingleIRVContestIsValid() {
    testUtils.log(logger, "validRequestForSingleIRVContestIsValid");
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(ballina,
        100, 100, candidates);
    assertDoesNotThrow(() -> validRequest.Validate(contestRepository));
  }

  /**
   * A valid request for a cross-county IRV contest is valid.
   */
  @Test
  public void validRequestForCrossCountyIRVContestIsValid() {
    testUtils.log(logger, "validRequestForCrossCountyIRVContestIsValid");
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(multiCounty,
        100, 100, candidates);
    assertDoesNotThrow(() -> validRequest.Validate(contestRepository));
  }

  /**
   * A request for a contest that doesn't exist is invalid.
   */
  @Test
  public void requestForNonexistentContestIsInvalid() {
    testUtils.log(logger, "requestForNonexistentContestIsInvalid");
    GenerateAssertionsRequest invalidRequest = new GenerateAssertionsRequest(
        "NonExistentContest", 100, 100, candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> invalidRequest.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "No such contest"));
  }

  /**
   * A request for an existent plurality contest is invalid, because we generate assertions only
   * for IRV contests.
   */
  @Test
  public void validRequestForPluralityContestIsInvalid() {
    testUtils.log(logger, "validRequestForPluralityContestIsInvalid");
    String validPlurality = "Valid Plurality Contest";
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(validPlurality,
        100, 100, candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "not comprised of all IRV"));
  }

  /**
   * A request for mixed IRV-plurality contests is invalid.
   * Note that this is a data problem - contests should have a consistent description.
   */
  @Test
  public void validRequestForMixedContestTypesIsInvalid() {
    testUtils.log(logger, "validRequestForMixedContestTypesIsInvalid");
    String invalidMixed = "Invalid Mixed Contest";
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(invalidMixed,
        100, 100,  candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "not comprised of all IRV"));
  }

  /**
   * A request with null contest name is invalid.
   */
  @Test
  public void requestWithNullNameIsInvalid() {
    testUtils.log(logger, "requestWithNullNameIsInvalid");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(null,
        100, 100, candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "No contest name specified"));
  }

  /**
   * A request with empty contest name is invalid.
   */
  @Test
  public void requestWithEmptyNameIsInvalid() {
    testUtils.log(logger, "requestWithEmptyNameIsInvalid");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest("",
        100, 100, candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "No contest name specified"));
  }

  /**
   * A request with all-whitespace contest name is invalid.
   */
  @Test
  public void requestWithWhitespaceNameIsInvalid() {
    testUtils.log(logger, "requestWithWhitespaceNameIsInvalid");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest("   ",
        100, 100, candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "No contest name specified"));
  }

  /**
   * A request with null candidate list is invalid.
   */
  @Test
  public void requestWithNullCandidateListIsInvalid() {
    testUtils.log(logger, "requestWithNullCandidateListIsInvalid");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(ballina,
        100, 100, null);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "bad candidate list"));
  }

  /**
   * A request with empty candidate list is invalid.
   */
  @Test
  public void requestWithEmptyCandidateListIsInvalid() {
    testUtils.log(logger, "requestWithEmptyCandidateListIsInvalid");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(ballina,
        100, 100, List.of());
    Exception ex = assertThrows(RequestValidationException.class,
        () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "bad candidate list"));
  }

  /**
   * A request with an all-whitespace candidate name is invalid.
   */
  @Test
  public void requestWithWhitespaceCandidateNameIsInvalid() {
    testUtils.log(logger, "requestWithWhitespaceCandidateNameIsInvalid");
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(ballina,
        50, 50, List.of("Alice","    "));
    Exception ex = assertThrows(RequestValidationException.class,
        () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "bad candidate list"));
  }

  /**
   * A zero totalAuditableBallots is invalid.
   */
  @Test
  public void ZeroTotalAuditableBallotsIsInvalid() {
    testUtils.log(logger, "ZeroTotalAuditableBallotsIsInvalid");
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(multiCounty,
        0, 100, candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> validRequest.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "Non-positive total auditable ballots"));
  }


  /**
   * A negative totalAuditableBallots is invalid.
   */
  @Test
  public void negativeTotalAuditableBallotsIsInvalid() {
    testUtils.log(logger, "negativeTotalAuditableBallotsIsInvalid");
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(ballina,
        -10, 100, candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> validRequest.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "Non-positive total auditable ballots"));
  }

  /**
   * A zero timeProvisionForResult is invalid.
   */
  @Test
  public void ZeroTimeProvisionForResultIsInvalid() {
    testUtils.log(logger, "ZeroTimeProvisionForResultIsInvalid");
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(ballina,
          100, 0, candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> validRequest.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "Non-positive time limit"));
  }


  /**
   * A negative timeProvisionForResult is invalid.
   */
  @Test
  public void negativeTimeProvisionForResultIsInvalid() {
    testUtils.log(logger, "negativeTimeProvisionForResultIsInvalid");
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(ballina,
        100, -100, candidates);
    Exception ex = assertThrows(RequestValidationException.class,
        () -> validRequest.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "Non-positive time limit"));
  }
}
