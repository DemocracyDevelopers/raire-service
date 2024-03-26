/*
  Copyright 2024 Democracy Developers
  The Raire Service is designed to connect colorado-rla and its associated database to
  the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).
  
  This file is part of raire-service.
  raire-service is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with raire-service.  If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.request;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import au.org.democracydevelopers.raireservice.persistence.repository.ContestRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class GenerateAssertionsRequestTests {

  @Autowired
  ContestRepository contestRepository;

  private List<CountyAndContestID> byronCountyAndContestIDs
      = List.of(
      new CountyAndContestID(8L, 999990L),
      new CountyAndContestID(9L, 999991L)
  );

  private List<CountyAndContestID> ballinaCountyAndContestIDs
      = List.of(
      new CountyAndContestID(8L, 999992L)
  );

  // One is 'Byron' and one is 'Ballina Mayoral'
  private List<CountyAndContestID> mixedIRVNamesCountyAndContestIDs
      = List.of(
      new CountyAndContestID(8L, 999992L),
      new CountyAndContestID(9L, 999991L)
  );

  private List<CountyAndContestID> invalidMixedDescriptionCountyAndContestIDs
      = List.of(
          new CountyAndContestID(9L, 999993L),
          new CountyAndContestID(10L, 999994L)
      );

  private List<CountyAndContestID> pluralityCountyAndContestIDs
      = List.of(
      new CountyAndContestID(10L, 999995L)
  );

  private List<String> candidates = List.of("Alice");
  private String ballina = "Ballina Mayoral";
  private String byron = "Byron";
  private String invalidMixed = "Invalid Mixed Contest";
  private String validPlurality = "Valid Plurality Contest";

  // A valid request for an IRV contest is valid.
  @Test
  public void validRequestForSingleIRVContestIsValid() {
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(ballina, 100, 100,
        candidates, ballinaCountyAndContestIDs);
    assertDoesNotThrow(() -> validRequest.Validate(contestRepository));
  }

  // A valid request for a cross-county IRV contest is valid.
  @Test
  public void validRequestForCrossCountyIRVContestIsValid() {
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(byron, 100, 100,
          candidates, byronCountyAndContestIDs);
    assertDoesNotThrow(() -> validRequest.Validate(contestRepository));
  }

  // A request with a contest name that doesn't match the database is invalid.
  // TODO. Note unnecessary if we get requests by contestname.

  // A request with a contest name that is inconsistent across (countyID, contestID) pairs is invalid.
  // TODO. Note unnecessary if we get requests by contestname.

  // A request for a contest that doesn't exist is invalid.
  @Test
  public void requestForNonexistentContestIsInvalid() {
    GenerateAssertionsRequest invalidRequest
        = new GenerateAssertionsRequest("NonExistentContest", 100,
          100, candidates, ballinaCountyAndContestIDs);
    Exception ex = assertThrows(RequestValidationException.class, () -> invalidRequest.Validate(contestRepository));
    assertTrue(ex.getMessage().toLowerCase().contains("No such contest".toLowerCase()));
  }

  // A request for an existent plurality contest is invalid.
  @Test
  public void validRequestForPluralityContestIsInvalid() {
    GenerateAssertionsRequest request
        = new GenerateAssertionsRequest(validPlurality, 100,
        100, candidates, pluralityCountyAndContestIDs);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().toLowerCase().contains("not all IRV".toLowerCase()));
  }

  /*
   * Test that a request for mixed IRV-plurality contests is invalid.
   * Note that this is a data problem - contests should have a consistent description.
   */
  @Test
  public void validRequestForMixedContestTypesIsInvalid() {
    GenerateAssertionsRequest request
        = new GenerateAssertionsRequest(invalidMixed, 100,
        100,  candidates, invalidMixedDescriptionCountyAndContestIDs);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().toLowerCase().contains("not all IRV".toLowerCase()));
  }

  // A request with null contest name is invalid.
  @Test
  public void requestWithNullNameIsInvalid() {
    GenerateAssertionsRequest request
        = new GenerateAssertionsRequest(null, 100,
          100, candidates, byronCountyAndContestIDs);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("No contest name"));
  }

  // A request with empty contest name is invalid.
  @Test
  public void requestWithEmptyNameIsInvalid() {
    GenerateAssertionsRequest request
        = new GenerateAssertionsRequest("", 100,
        100, candidates, byronCountyAndContestIDs);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("No contest name"));
  }

  // A request with all-whitespace contest name is invalid.
  @Test
  public void requestWithWhitespaceNameIsInvalid() {
    GenerateAssertionsRequest request
        = new GenerateAssertionsRequest("   ",  100,
        100, candidates, byronCountyAndContestIDs);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("No contest name"));
  }

  // A request with null candidate list is invalid.
  @Test
  public void requestWithNullCandidateListIsInvalid() {
    GenerateAssertionsRequest request
        = new GenerateAssertionsRequest(ballina, 100, 100,
          null, byronCountyAndContestIDs);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("Bad candidate list"));
  }

  // A request with empty candidate list is invalid.
  @Test
  public void requestWithEmptyCandidateListIsInvalid() {
    GenerateAssertionsRequest request
        = new GenerateAssertionsRequest(ballina, 100, 100,
          List.of(), byronCountyAndContestIDs);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("Bad candidate list"));
  }

   // A request with an all-whitespace candidate name is invalid.
  @Test
  public void requestWithWhitespaceCandidateNameIsInvalid() {
    GenerateAssertionsRequest request
        = new GenerateAssertionsRequest(ballina, 50, 50,
          List.of("Alice","    "), ballinaCountyAndContestIDs);
    Exception ex = assertThrows(RequestValidationException.class, () -> request.Validate(contestRepository));
    assertTrue(ex.getMessage().contains("Bad candidate list"));
  }

  // A zero totalAuditableBallots is invalid.
  @Test
  public void ZeroTotalAuditableBallotsIsInvalid() {
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(byron,
        0, 100, candidates, byronCountyAndContestIDs);
    assertThrows(RequestValidationException.class, () -> validRequest.Validate(contestRepository));
  }


  // A negative totalAuditableBallots is invalid.
  @Test
  public void negativeTotalAuditableBallotsIsInvalid() {
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(byron,
        -10, 100,
        candidates, byronCountyAndContestIDs);
    assertThrows(RequestValidationException.class, () -> validRequest.Validate(contestRepository));
  }

  // A zero timeProvisionForResult is invalid.
  @Test
  public void ZeroTimeProvisionForResultIsInvalid() {
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(byron,
          100, 0, candidates, byronCountyAndContestIDs);
    assertThrows(RequestValidationException.class, () -> validRequest.Validate(contestRepository));
  }


  // A negative timeProvisionForResult is invalid.
  @Test
  public void negativeTimeProvisionForResultIsInvalid() {
    GenerateAssertionsRequest validRequest = new GenerateAssertionsRequest(byron, 100, -100,
        candidates, byronCountyAndContestIDs);
    assertThrows(RequestValidationException.class, () -> validRequest.Validate(contestRepository));
  }
}
