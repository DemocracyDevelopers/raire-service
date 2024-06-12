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

import static au.org.democracydevelopers.raireservice.testUtils.defaultCount;
import static au.org.democracydevelopers.raireservice.testUtils.defaultWinner;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import au.org.democracydevelopers.raireservice.testUtils;
import java.math.BigDecimal;
import java.util.List;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests of assertion retrieval causing errors in GetAssertionsJsonService and GetAssertionsCsvService.
 * Data is preloaded into the database using src/test/resources/data.sql. Note that tests of
 * GetAssertionsJsonService and GetAssertionsCsvService have been
 * spread across several test classes, each defined with respect to a different test container.
 * The tests for WRONG_CANDIDATE_NAMES are in the respective format-specific class.
 * There is a json version and a csv version of each test.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsServiceErrorJsonAndCsvTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GetAssertionsServiceErrorJsonAndCsvTests.class);

  @Autowired
  GetAssertionsJsonService getAssertionsJsonService;
  @Autowired
  GetAssertionsCsvService getAssertionsCsvService;

  /**
   * Retrieval of assertions for an existing contest with no associated assertions will throw
   * a RaireServiceException with error code NO_ASSERTIONS_PRESENT. (JSON)
   */
  @Test
  @Transactional
  void existentContestNoAssertionsJSON(){
    testUtils.log(logger, "existentContestNoAssertionsJSON");
    GetAssertionsRequest request = new GetAssertionsRequest("No CVR Mayoral", defaultCount,
        List.of(), defaultWinner, BigDecimal.valueOf(0.1));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        getAssertionsJsonService.getRaireSolution(request));
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT, ex.errorCode);
  }

  /**
   * Retrieval of assertions for an existing contest with no associated assertions will throw
   * a RaireServiceException with error code NO_ASSERTIONS_PRESENT. (CSV)
   */
  @Test
  @Transactional
  void existentContestNoAssertionsCSV(){
    testUtils.log(logger, "existentContestNoAssertionsCSV");
    GetAssertionsRequest request = new GetAssertionsRequest("No CVR Mayoral", defaultCount,
        List.of(), defaultWinner, BigDecimal.valueOf(0.1));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        getAssertionsCsvService.generateCSV(request));
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT, ex.errorCode);
  }

  /**
   * Retrieval of assertions for a non-existent contest will throw a RaireServiceException
   * with error code NO_ASSERTIONS_PRESENT. (JSON)
   * Note that this should not happen because it should be caught by request validation.
   */
  @Test
  @Transactional
  void nonExistentContestNoAssertionsJSON(){
    testUtils.log(logger, "nonExistentContestNoAssertionsJSON");
    GetAssertionsRequest request = new GetAssertionsRequest("Non-Existent Contest Name",
        defaultCount, List.of(), defaultWinner, BigDecimal.valueOf(0.1));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        getAssertionsJsonService.getRaireSolution(request));
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT, ex.errorCode);
  }

  /**
   * Retrieval of assertions for a non-existent contest will throw a RaireServiceException
   * with error code NO_ASSERTIONS_PRESENT. (CSV)
   * Note that this should not happen because it should be caught by request validation.
   */
  @Test
  @Transactional
  void nonExistentContestNoAssertionsCSV(){
    testUtils.log(logger, "nonExistentContestNoAssertionsCSV");
    GetAssertionsRequest request = new GetAssertionsRequest("Non-Existent Contest Name",
        defaultCount, List.of(), defaultWinner, BigDecimal.valueOf(0.1));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        getAssertionsCsvService.generateCSV(request));
    assertEquals(RaireErrorCode.NO_ASSERTIONS_PRESENT, ex.errorCode);
  }
}
