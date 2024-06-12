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


import static au.org.democracydevelopers.raireservice.NSWValues.expectedSolutionData;
import static au.org.democracydevelopers.raireservice.testUtils.difficultyMatchesMax;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raireservice.NSWValues.Expected;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.CVRContestInfoRepository;
import au.org.democracydevelopers.raireservice.request.ContestRequest;
import au.org.democracydevelopers.raireservice.testUtils;
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
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests to validate the behaviour of Assertion generation on NSW 2021 Mayoral election data.
 * Data is loaded in from src/test/resources/NSW2021Data/
 * These tests all pass, but are disabled because loading in all the NSW data takes a long time.
 */
@ActiveProfiles("nsw-testcases")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@EnabledIf(value = "${test-strategy.run-nsw-tests}", loadContext = true)
public class GenerateAssertionsServiceNSWTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GenerateAssertionsServiceNSWTests.class);

  @Autowired
  private CVRContestInfoRepository cvrContestInfoRepository;

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GenerateAssertionsService generateAssertionsService;

  private static final int DEFAULT_TIME_LIMIT = 5;

  /**
   * Sanity check to make sure that the first contest's first vote's first preference has one of the
   * candidate names we expect for that contest.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest1() {
    testUtils.log(logger, "firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest1");
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(1, 1);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(expectedSolutionData.getFirst().choices().contains(retrievedFirstChoice));
  }

  /**
   * Iterate through all the NSW example contests,
   * - request assertion generation from the service,
   * - check the winner,
   * - save the assertions in the database,
   * - retrieve the assertions from the database,
   * - verify the maximum expected difficulty.
   * @throws RaireServiceException if either assertion generation or assertion retrieval fail.
   */
  @Test
  @Transactional
  public void checkAllNSWByService() throws RaireServiceException {

    for (Expected expected : expectedSolutionData) {
      testUtils.log(logger, "checkAllNSWByService: contest " + expected.contestName());

      ContestRequest request = new ContestRequest(expected.contestName(),
          expected.ballotCount(), DEFAULT_TIME_LIMIT, expected.choices());

      // Generate assertions.
      RaireResultOrError response = generateAssertionsService.generateAssertions(request);
      // Check the winner.
      assertEquals(expected.winner(), request.candidates.get(response.Ok.winner));

      // Save the assertions
      generateAssertionsService.persistAssertions(response.Ok, request);

      // Check difficulty.
      List<Assertion> assertions = assertionRepository.findByContestName(expected.contestName());
      assertTrue(difficultyMatchesMax(expected.difficulty(), assertions));
    }
  }
}

