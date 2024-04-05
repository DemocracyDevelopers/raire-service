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
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/*
 * Tests on GetAssertionsRequests, particularly focusing on the validation step.
 * Contests which will be used to test the validity of the GenerateAssertionsRequest are
 * pre-loaded into the database using src/test/resources/data.sql.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class GetAssertionsRequestTests {

  @Autowired
  ContestRepository contestRepository;

  /**
   * A valid request for an IRV contest is valid.
   */
  @Test
  public void validRequestForIRVContestIsValid() {
    GetAssertionsRequest validRequest = new GetAssertionsRequest("Ballina Mayoral", List.of("Alice"), BigDecimal.valueOf(0.03));
    assertDoesNotThrow(() -> validRequest.Validate(contestRepository));
  }

  /**
   * A request for a contest that doesn't exist is invalid.
   */
  @Test
  public void requestForNonexistentContestIsInvalid() {
    GetAssertionsRequest invalidRequest
        = new GetAssertionsRequest("NonExistentContest", List.of("Alice"), BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> invalidRequest.Validate(contestRepository));
    assertTrue(ex.getMessage().toLowerCase().contains("No such contest".toLowerCase()));
  }

  /**
   * A request for an existent plurality contest is invalid, because we have assertions only for
   * IRV contests.
   */
  @Test
  public void validRequestForPluralityContestIsInvalid() {
    String pluralityContestName = "Valid Plurality Contest";
    GetAssertionsRequest request
        = new GetAssertionsRequest(pluralityContestName, List.of("Alice"), BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().toLowerCase().contains("not all IRV".toLowerCase()));
  }

  /**
   * A request for mixed IRV-plurality contests is invalid.
   * Note that this is a data problem - contests should have a consistent description.
   */
  @Test
  public void validRequestForMixedContestTypesIsInvalid() {
    String mixedContestName = "Invalid Mixed Contest";
    GetAssertionsRequest request
        = new GetAssertionsRequest(mixedContestName, List.of("Alice"), BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().toLowerCase().contains("not all IRV".toLowerCase()));
  }

  /**
   * A request with null contest name is invalid.
   */
  @Test
  public void requestWithNullNameIsInvalid() {
    GetAssertionsRequest request
        = new GetAssertionsRequest(null, List.of("Alice"), BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("No contest name"));
  }

  /**
   * A request with empty contest name is invalid.
   */
  @Test
  public void requestWithEmptyNameIsInvalid() {
    GetAssertionsRequest request
        = new GetAssertionsRequest("", List.of("Alice"), BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("No contest name"));
  }

  /**
   * A request with all-whitespace contest name is invalid.
   */
  @Test
  public void requestWithWhitespaceNameIsInvalid() {
    GetAssertionsRequest request
        = new GetAssertionsRequest("   ", List.of("Alice"), BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("No contest name"));
  }

  /**
   * A request with null candidate list is invalid.
   */
  @Test
  public void requestWithNullCandidateListIsInvalid() {
    GetAssertionsRequest request
        = new GetAssertionsRequest("IRVContestName", null, BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("Bad candidate list"));
  }

  /**
   * A request with empty candidate list is invalid.
   */
  @Test
  public void requestWithEmptyCandidateListIsInvalid() {
    GetAssertionsRequest request
        = new GetAssertionsRequest("IRVContestName",  List.of(), BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("Bad candidate list"));
  }

  /**
   * A request with an all-whitespace candidate name is invalid.
   */
  @Test
  public void requestWithWhitespaceCandidateNameIsInvalid() {
    GetAssertionsRequest request
        = new GetAssertionsRequest("IRVContest", List.of("Alice","    "), BigDecimal.valueOf(0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("Bad candidate list"));
  }

  /**
   * A request with a null risk limit is invalid.
   */
  @Test
  public void requestWithNullRiskLimitIsInvalid() {
    GetAssertionsRequest request
        = new GetAssertionsRequest("Ballina Mayoral", List.of("Alice"), null);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("risk limit"));
  }

  /**
   * A request with a negative risk limit is invalid.
   */
  @Test
  public void requestWithNegativeRiskLimitIsInvalid() {
    GetAssertionsRequest request
        = new GetAssertionsRequest("Ballina Mayoral", List.of("Alice"), BigDecimal.valueOf(-0.03));
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("risk limit"));
  }
}
