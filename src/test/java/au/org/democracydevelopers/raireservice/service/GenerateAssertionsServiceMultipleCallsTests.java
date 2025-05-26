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
import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.GenerateAssertionsSummary;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.GenerateAssertionsSummaryRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.testUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static au.org.democracydevelopers.raireservice.NSWValues.BallotCount_12;
import static au.org.democracydevelopers.raireservice.NSWValues.winnerContest_12;
import static au.org.democracydevelopers.raireservice.service.GenerateAssertionsServiceKnownTests.guideToRaireExample1;
import static au.org.democracydevelopers.raireservice.service.GenerateAssertionsServiceKnownTests.aliceBobChuanDiego;
import static au.org.democracydevelopers.raireservice.service.GenerateAssertionsServiceWickedTests.*;
import static au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode.TIMEOUT_FINDING_ASSERTIONS;
import static au.org.democracydevelopers.raireservice.testUtils.correctDBAssertionData;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to validate the behaviour of Assertion generation when called successively.
 * These are the same as the tests in GenerateAssertionsAPIMultipleCallsTests.java, but call the
 * service directly instead of (just) the API.
 * Relevant data is preloaded into the test database from
 * src/test/resources/known_testcases_votes.sql.
 * These tests include
 * 1. Calling the endpoint twice with the same contest name. The state at the end of the second call
 * should be the same as at the end of the first.
 * 2. Calling the endpoint first with an extremely small time limit that causes assertion generation
 * to fail, then calling it again with a longer time limit that succeeds. This should store
 * assertions and replace the summary.
 * 3. The same as (2), but in the opposite order.
 * In each case, the test
 * - makes a request for assertion generation in the service,
 * - checks for the right state (OK or error),
 * - checks for the right generateAssertionsSummary,
 * - checks whether the assertion list is empty.
 */
@ActiveProfiles("known-testcases")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsServiceMultipleCallsTests {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsServiceMultipleCallsTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GenerateAssertionsSummaryRepository summaryRepository;

  @Autowired
  GenerateAssertionsService generateAssertionsService;

  @Autowired
  private DataSource dataSource;

  /**
   * Second request for Byron, with a normal time limit.
   */
  public final static GenerateAssertionsRequest ByronNormalTimeoutRequest
      = new GenerateAssertionsRequest(ByronMayoral, BallotCount_12, 5,
      choicesByron);

  /**
   * Run assertion generation on Byron Mayoral twice with the default timeout. Verify that the
   * results are the same both times.
   * This test doesn't do exhaustive search of the data - it just sanity-checks that the
   * assertion generation summaries are the same, and the assertion lists are the same size.
   */
  @Test
  @Transactional
  void ByronRunsTwiceSameResults() throws RaireServiceException {
    {
      testUtils.log(logger, "ByronRunsTwiceSameResults");

      // Make the request with a very short timeout - expected to fail.
      RaireResultOrError result = generateAssertionsService.generateAssertions(ByronNormalTimeoutRequest);

      assertNotNull(result.Ok);
      assertNull(result.Err);

      assertInstanceOf(RaireResult.class, result.Ok);
      int resultLength = result.Ok.assertions.length;

      generateAssertionsService.persistAssertionsOrErrors(result, ByronNormalTimeoutRequest);
      Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(ByronMayoral);
      assertTrue(optSummary.isPresent());
      GenerateAssertionsSummary firstSummary = optSummary.get();

      // Check there are assertions in the database, of the same size as the result.
      List<Assertion> assertions = assertionRepository.findByContestName(ByronMayoral);
      assertEquals(resultLength, assertions.size());

      // Make a second identical request - expected to succeed.
      result = generateAssertionsService.generateAssertions(ByronNormalTimeoutRequest);

      assertNotNull(result.Ok);
      assertNull(result.Err);

      // Check the assertions have the same length as they did in the first request.
      assertInstanceOf(RaireResult.class, result.Ok);
      assertEquals(resultLength, result.Ok.assertions.length);

      generateAssertionsService.persistAssertionsOrErrors(result, ByronNormalTimeoutRequest);
      optSummary = summaryRepository.findByContestName(ByronMayoral);
      assertTrue(optSummary.isPresent());
      assertTrue(optSummary.get().equalData(ByronMayoral, firstSummary.getWinner(),
          firstSummary.getError(), firstSummary.getWarning(), firstSummary.getMessage()));

      // Check there are the same number of assertions in the database.
      assertions = assertionRepository.findByContestName(ByronMayoral);
      assertEquals(resultLength, assertions.size());
    }
  }

  /**
   * Run assertion generation on Byron Mayoral, first with a too-short timeout, then again with a
   * workable timeout. Verify that the error state is stored first, then replaced with success.
   * This test doesn't do exhaustive search of the data - it just sanity-checks that first the
   * failure result with no assertions, then the success result with some assertions, are stored.
   */
  @Test
  @Transactional
  void ByronTimeoutsSuccessReplacesFailure() throws RaireServiceException {
    testUtils.log(logger, "ByronTimeoutsSuccessReplacesFailure");

    // Make the request with a very short timeout - expected to fail.
    RaireResultOrError result = generateAssertionsService.generateAssertions(ByronShortTimeoutRequest);

    assertNull(result.Ok);
    assertNotNull(result.Err);

    assertInstanceOf(RaireError.TimeoutFindingAssertions.class, result.Err);

    generateAssertionsService.persistAssertionsOrErrors(result, ByronShortTimeoutRequest);
    Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(ByronMayoral);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(ByronMayoral, "",
        TIMEOUT_FINDING_ASSERTIONS.toString(), "", "Time out finding assertions"));

    // Check there are no assertions in the database.
    List<Assertion> assertions = assertionRepository.findByContestName(ByronMayoral);
    assertEquals(0, assertions.size());

    // Make the request with a normal timeout - expected to succeed. Check that the successful
    // values replace those from the failure.
    result = generateAssertionsService.generateAssertions(ByronNormalTimeoutRequest);

    assertNotNull(result.Ok);
    assertNull(result.Err);

    generateAssertionsService.persistAssertionsOrErrors(result, ByronNormalTimeoutRequest);
    optSummary = summaryRepository.findByContestName(ByronMayoral);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(ByronMayoral, winnerContest_12, "", "", ""));

    // Check there are some assertions in the database.
    assertions = assertionRepository.findByContestName(ByronMayoral);
    assertFalse(assertions.isEmpty());

  }

  /**
   * The same as ByronTimeoutsSuccessReplacesFailure, but in the opposite order.
   * @throws RaireServiceException if something goes wrong with database storage.
   */
  @Test
  @Transactional
  void ByronTimeoutsFailureReplacesSuccess() throws RaireServiceException {
    testUtils.log(logger, "ByronTimeoutsFailureReplacesSuccess");

    // Make the request with a normal timeout - expected to succeed. Check that the successful
    // values replace those from the failure.
    RaireResultOrError result = generateAssertionsService.generateAssertions(ByronNormalTimeoutRequest);

    assertNotNull(result.Ok);
    assertNull(result.Err);

    generateAssertionsService.persistAssertionsOrErrors(result, ByronNormalTimeoutRequest);
    Optional<GenerateAssertionsSummary> optSummary = summaryRepository.findByContestName(ByronMayoral);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(ByronMayoral, winnerContest_12, "", "", ""));

    // Check there are some assertions in the database.
    List<Assertion> assertions = assertionRepository.findByContestName(ByronMayoral);
    assertFalse(assertions.isEmpty());

    // Make the request with a very short timeout - expected to fail.
    result = generateAssertionsService.generateAssertions(ByronShortTimeoutRequest);

    assertNull(result.Ok);
    assertNotNull(result.Err);

    assertInstanceOf(RaireError.TimeoutFindingAssertions.class, result.Err);

    generateAssertionsService.persistAssertionsOrErrors(result, ByronShortTimeoutRequest);
    optSummary = summaryRepository.findByContestName(ByronMayoral);
    assertTrue(optSummary.isPresent());
    assertTrue(optSummary.get().equalData(ByronMayoral, "",
        TIMEOUT_FINDING_ASSERTIONS.toString(), "", "Time out finding assertions"));

    // Check there are no assertions in the database.
    assertions = assertionRepository.findByContestName(ByronMayoral);
    assertEquals(0, assertions.size());

  }


  /**
   * Run assertion generation on The Guide To Raire Example 1, with the default timeout.
   * - The first run is the usual version contained in The Guide and in known_testcases_votes.sql.
   * - The second run has Alice and Chuan flipped.
   * Verify that the assertions and winner summary for the flipped version replace the assertions
   * from the first run. The first part of this test is identical to
   * GenerateAssertionsServiceKnownTests::ttestGuideToRairePart2Example1; the second part is the
   * same again but with Alice and Chuan flipped.
   */
  @Test
  @Transactional
  void Example1RunsTwiceFlippedResults() throws RaireServiceException {
    testUtils.log(logger, "Example1RunsTwiceFlippedResults");

    GenerateAssertionsRequest request = new GenerateAssertionsRequest(guideToRaireExample1,
        27, 5, Arrays.stream(aliceBobChuanDiego).toList());

    RaireResultOrError result = generateAssertionsService.generateAssertions(request);
    assertNotNull(result.Ok);
    assertNull(result.Err);

    // Sanity check; check winner.
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(result.Ok.winner), "Chuan"));
    assertInstanceOf(RaireResult.class, result.Ok);

    // Count the assertions for the first run.
    int assertionCount = result.Ok.assertions.length;

    // Test persistence of assertions.
    generateAssertionsService.persistAssertionsOrErrors(result, request);
    List<Assertion> storedAssertions = assertionRepository.findByContestName(guideToRaireExample1);
    assertEquals(assertionCount, storedAssertions.size());

    // There should be one NEB assertion: Chaun NEB Bob
    Optional<Assertion> nebMaybeAssertion = storedAssertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    NEBAssertion nebAssertion = (NEBAssertion) nebMaybeAssertion.get();

    assertTrue(correctDBAssertionData(8, 8 / 27.0, 27 / 8.0,
        "Chuan", "Bob", List.of(), nebAssertion));

    // There should be a summary with winner Chuan and no error.
    Optional<GenerateAssertionsSummary> optSummary
        = summaryRepository.findByContestName(guideToRaireExample1);
    assertTrue(optSummary.isPresent());
    GenerateAssertionsSummary firstSummary = optSummary.get();
    firstSummary.equalData(guideToRaireExample1, "Chuan", "", "", "");

    // Execute the script to delete the CVRs and replace them with new ones with Alice and Chuan flipped.
    ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
    populator.addScript(new ClassPathResource("flipped_example_1.sql"));
    populator.execute(this.dataSource);


    // Run the whole test again, expecting the same thing but with Alice and Chuan flipped.
    result = generateAssertionsService.generateAssertions(request);
    assertNotNull(result.Ok);
    assertNull(result.Err);

    // Sanity check; check winner.
    assertTrue(StringUtils.containsIgnoreCase(request.candidates.get(result.Ok.winner), "Alice"));
    assertInstanceOf(RaireResult.class, result.Ok);

    // Should have the same number of assertions as the first run.
    assertEquals(assertionCount, result.Ok.assertions.length);

    // Test persistence of the new assertions - they should replace the old ones.
    generateAssertionsService.persistAssertionsOrErrors(result, request);
    storedAssertions = assertionRepository.findByContestName(guideToRaireExample1);
    assertEquals(assertionCount, storedAssertions.size());

    // There should be one NEB assertion: Alice NEB Bob
    nebMaybeAssertion = storedAssertions.stream()
        .filter(a -> a instanceof NEBAssertion).findFirst();
    assertTrue(nebMaybeAssertion.isPresent());
    nebAssertion = (NEBAssertion) nebMaybeAssertion.get();

    // Should have Alice instead of Chuan
    assertTrue(correctDBAssertionData(8, 8 / 27.0, 27 / 8.0,
        "Alice", "Bob", List.of(), nebAssertion));

    // There should be a summary with winner Alice and no error.
    optSummary = summaryRepository.findByContestName(guideToRaireExample1);
    assertTrue(optSummary.isPresent());
    GenerateAssertionsSummary secondSummary = optSummary.get();
    secondSummary.equalData(guideToRaireExample1, "Alice", "", "", "");
  }
}
