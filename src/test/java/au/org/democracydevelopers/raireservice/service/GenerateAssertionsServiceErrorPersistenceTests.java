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

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsSummary;
import au.org.democracydevelopers.raireservice.persistence.repository.GenerateAssertionsSummaryRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.testUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

import java.util.Optional;
import java.util.stream.Stream;

import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.INTERNAL_ERROR;
import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.TIMEOUT_FINDING_ASSERTIONS;
import static au.org.democracydevelopers.raireservice.testUtils.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for appropriate persistence of failed assertion generation
 * (except for TIED_WINNERS, which is in GenerateAssertionsServiceKnownTests).
 * Tests include examples of every error that raire-java can produce, plus tests that
 * successive calls to GenerateAssertions correctly replace results.
 *   TODO add tests for persisting error summaries (except tied winners, which is already done).
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsServiceErrorPersistenceTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsServiceErrorPersistenceTests.class);

  @Autowired
  private GenerateAssertionsSummaryRepository summaryRepository;

  @Autowired
  private GenerateAssertionsService generateAssertionsService;

  private static final GenerateAssertionsRequest ballinaMayoralRequest = new GenerateAssertionsRequest(ballinaMayoral, 100,
      10, aliceAndBob);

  @ParameterizedTest
  @Transactional
  @MethodSource("expectedRaireErrorSummaries")
  public void testErrorSummaryStorage(RaireError raireError, RaireServiceException.RaireErrorCode savedError, String savedWarning, String savedMessage)
      throws RaireServiceException {
    testUtils.log(logger, "testErrorSummaryStorage");

    RaireSolution.RaireResultOrError solution =
        new RaireSolution.RaireResultOrError(raireError);

    generateAssertionsService.persistAssertionsOrErrors(solution, ballinaMayoralRequest);
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(ballinaMayoral);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(ballinaMayoral, GenerateAssertionsSummary.UNKNOWN_WINNER,
        savedError.toString(), savedWarning, savedMessage));
  }

  /**
   * The data for testing the expected errors. These just consist of a RaireError (matching what would be output by
   * raire-java) and the corresponding strings we expect to be stored in the database (the same information as strings).
   */
   private static Stream<Arguments> expectedRaireErrorSummaries() {
     return Stream.of(
        Arguments.of(new RaireError.InternalErrorTrimming(), INTERNAL_ERROR, "", "Internal error"),
        Arguments.of(new RaireError.TimeoutFindingAssertions(43.4), TIMEOUT_FINDING_ASSERTIONS, "", "Time out finding assertions")
        );
   }
}
