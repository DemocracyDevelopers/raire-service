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


import static au.org.democracydevelopers.raireservice.testUtils.correctAssumedContinuing;
import static au.org.democracydevelopers.raireservice.testUtils.correctDBAssertionData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireError.TiedWinners;
import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.time.TimeTaken;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCodes;
import au.org.democracydevelopers.raireservice.testUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests to validate the behavior of Assertion generation on a collection of particularly nasty
 * test cases designed to elicit errors. Relevant data is preloaded into the test database from
 * src/test/resources/known_testcases_votes.sql.
 * These are generally derived from the Wicked test cases in raire-java.
 * This includes
 * FIXME
 */
@ActiveProfiles("known-testcases")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsOnWickedTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GenerateAssertionsOnWickedTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GenerateAssertionsService generateAssertionsService;

  /**
   * Names of contests, to match preloaded data.
   */
  private static final String tiedWinnersContest = "Tied Winners Contest";

  private static final String oneNEBAssertionContest = "Sanity Check NEB Assertion Contest";
  private static final String oneNENAssertionContest = "Sanity Check NEN Assertion Contest";

  /**
   * Array of candidates: Alice, Chuan, Bob.
   */
  private static final String[] aliceChuanBob = {"Alice", "Chuan", "Bob"};

  private final static GenerateAssertionsRequest tiedWinnersRequest
      = new GenerateAssertionsRequest(tiedWinnersContest, 2, 5,
      Arrays.stream(aliceChuanBob).toList());

  /**
   * Tied winners results in raire-java returning a TiedWinners RaireError. This is a super-simple
   * election with two candidates with one vote each.
   * FIXME - decide whether tied winners goes here or in 'Known '
   */
  @Test
  @Transactional
  void tiedWinnersThrowsTiedWinnersError() throws RaireServiceException {
    testUtils.log(logger, "tiedWinnersThrowsTiedWinnersError");
    RaireResultOrError result = generateAssertionsService.generateAssertions(tiedWinnersRequest);

    assertNull(result.Ok);
    assertNotNull(result.Err);

    assertEquals(TiedWinners.class, result.Err.getClass());
  }
}
