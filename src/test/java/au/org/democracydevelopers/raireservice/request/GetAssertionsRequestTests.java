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

import static au.org.democracydevelopers.raireservice.testUtils.defaultCount;
import static au.org.democracydevelopers.raireservice.testUtils.defaultWinner;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import au.org.democracydevelopers.raireservice.testUtils;
import java.math.BigDecimal;
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
 * Tests on GetAssertionsRequests, particularly focusing on the validation step.
 * Contests which will be used to test the validity of the GenerateAssertionsRequest are
 * preloaded into the database using src/test/resources/data.sql.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GetAssertionsRequestTests {

  private static final Logger logger = LoggerFactory.getLogger(GetAssertionsRequestTests.class);

  @Autowired
  ContestRepository contestRepository;

  /**
   * A valid request for an IRV contest is valid.
   */
  @Test
  public void validRequestForIRVContestIsValid() {
    testUtils.log(logger, "validRequestForIRVContestIsValid");
    GetAssertionsRequest validRequest = new GetAssertionsRequest("Ballina Mayoral",
        defaultCount, List.of("Alice"), defaultWinner, BigDecimal.valueOf(0.03));
    assertDoesNotThrow(() -> validRequest.Validate(contestRepository));
  }

  /**
   * A request for a contest that doesn't exist is invalid.
   */
  @Test
  public void requestForNonexistentContestIsInvalid() {
    testUtils.log(logger, "requestForNonexistentContestIsInvalid");
    GetAssertionsRequest invalidRequest = new GetAssertionsRequest("NonExistentContest",
        defaultCount, List.of("Alice"), defaultWinner,  BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class,
        () -> invalidRequest.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "No such contest"));
  }

  /**
   * A request for an existent plurality contest is invalid, because we have assertions only for
   * IRV contests.
   */
  @Test
  public void validRequestForPluralityContestIsInvalid() {
    testUtils.log(logger, "validRequestForPluralityContestIsInvalid");
    String pluralityContestName = "Valid Plurality Contest";
    GetAssertionsRequest request = new GetAssertionsRequest(pluralityContestName, defaultCount,
        List.of("Alice"), defaultWinner, BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "not comprised of all IRV"));
  }

  /**
   * A request for mixed IRV-plurality contests is invalid.
   * Note that this is a data problem - contests should have a consistent description.
   */
  @Test
  public void validRequestForMixedContestTypesIsInvalid() {
    testUtils.log(logger, "validRequestForMixedContestTypesIsInvalid");
    String mixedContestName = "Invalid Mixed Contest";
    GetAssertionsRequest request = new GetAssertionsRequest(mixedContestName, defaultCount,
        List.of("Alice"), defaultWinner, BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "not comprised of all IRV"));
  }

  /**
   * A request with null contest name is invalid.
   */
  @Test
  public void requestWithNullNameIsInvalid() {
    testUtils.log(logger, "requestWithNullNameIsInvalid");
    GetAssertionsRequest request = new GetAssertionsRequest(null, defaultCount,
        List.of("Alice"), defaultWinner, BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "No contest name"));
  }

  /**
   * A request with empty contest name is invalid.
   */
  @Test
  public void requestWithEmptyNameIsInvalid() {
    testUtils.log(logger, "requestWithEmptyNameIsInvalid");
    GetAssertionsRequest request = new GetAssertionsRequest("", defaultCount,
        List.of("Alice"), defaultWinner, BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "No contest name"));
  }

  /**
   * A request with all-whitespace contest name is invalid.
   */
  @Test
  public void requestWithWhitespaceNameIsInvalid() {
    testUtils.log(logger, "requestWithWhitespaceNameIsInvalid");
    GetAssertionsRequest request = new GetAssertionsRequest("   ", defaultCount,
        List.of("Alice"), defaultWinner, BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "No contest name"));
  }

  /**
   * A request with null candidate list is invalid.
   */
  @Test
  public void requestWithNullCandidateListIsInvalid() {
    testUtils.log(logger, "requestWithNullCandidateListIsInvalid");
    GetAssertionsRequest request = new GetAssertionsRequest("IRVContestName",
        defaultCount,  null, defaultWinner, BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "bad candidate list"));
  }

  /**
   * A request with empty candidate list is invalid.
   */
  @Test
  public void requestWithEmptyCandidateListIsInvalid() {
    testUtils.log(logger, "requestWithEmptyCandidateListIsInvalid");
    GetAssertionsRequest request = new GetAssertionsRequest("IRVContestName", defaultCount,
        List.of(), defaultWinner, BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "bad candidate list"));
  }

  /**
   * A request with an all-whitespace candidate name is invalid.
   */
  @Test
  public void requestWithWhitespaceCandidateNameIsInvalid() {
    testUtils.log(logger, "requestWithWhitespaceCandidateNameIsInvalid");
    GetAssertionsRequest request = new GetAssertionsRequest("IRVContest", defaultCount,
        List.of("Alice","    "), defaultWinner, BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "bad candidate list"));
  }

  /**
   * A request with a null risk limit is invalid.
   */
  @Test
  public void requestWithNullRiskLimitIsInvalid() {
    testUtils.log(logger, "requestWithNullRiskLimitIsInvalid");
    GetAssertionsRequest request = new GetAssertionsRequest("Ballina Mayoral", defaultCount,
        List.of("Alice"), defaultWinner, null);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "risk limit"));
  }

  /**
   * A request with a negative risk limit is invalid.
   */
  @Test
  public void requestWithNegativeRiskLimitIsInvalid() {
    testUtils.log(logger, "requestWithNegativeRiskLimitIsInvalid");
    GetAssertionsRequest request = new GetAssertionsRequest("Ballina Mayoral", defaultCount,
        List.of("Alice"), defaultWinner, BigDecimal.valueOf(-0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(), "risk limit"));
  }
}
