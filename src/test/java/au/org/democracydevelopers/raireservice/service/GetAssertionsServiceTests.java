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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCodes;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests of assertion retrieval in GetAssertionsService. Data is preloaded into the database
 * using src/test/resources/data.sql. Note that tests of GetAssertionsService have been
 * spread across several test classes, each defined with respect to a different test container.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsServiceTests {

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * Retrieval of assertions for an existing contest with no associated assertions will throw
   * a RaireServiceException with error code NO_ASSERTIONS_PRESENT.
   */
  @Test
  @Transactional
  void existentContestNoAssertions(){
    GetAssertionsService service = new GetAssertionsService(assertionRepository);
    GetAssertionsRequest request = new GetAssertionsRequest("No CVR Mayoral",
        List.of(), new BigDecimal("0.10"));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        service.getRaireSolution(request));
    assertEquals(RaireErrorCodes.NO_ASSERTIONS_PRESENT, ex.errorCode);
  }

  /**
   * Retrieval of assertions for a non-existent contest will throw a RaireServiceException
   * with error code NO_ASSERTIONS_PRESENT.
   * Note that this should not happen because it should be caught by request validation.
   */
  @Test
  @Transactional
  void nonExistentContestNoAssertions(){
    GetAssertionsService service = new GetAssertionsService(assertionRepository);
    GetAssertionsRequest request = new GetAssertionsRequest("Non-Existent Contest Name",
        List.of(), new BigDecimal("0.10"));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        service.getRaireSolution(request));
    assertEquals(RaireErrorCodes.NO_ASSERTIONS_PRESENT, ex.errorCode);
  }

}
