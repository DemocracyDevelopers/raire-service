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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCodes;
import au.org.democracydevelopers.raireservice.testUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests of assertion retrieval in GetAssertionsJsonService. Assertions and other relevant data is
 * preloaded into the test database from: src/test/resources/simple_assertions.sql. Note that tests
 * of GetAssertionsJsonService have been spread across several test classes, each defined with respect
 * to a different test container.
 */
@ActiveProfiles("simple-assertions")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsJsonServiceSimpleAssertionsTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GetAssertionsJsonServiceSimpleAssertionsTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * To facilitate easier checking of retrieved/saved assertion content.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();


  /**
   * Retrieve assertions for a contest that has one NEB assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNEBAssertion() throws RaireServiceException {
    testUtils.log(logger, "");
    GetAssertionsJsonService service = new GetAssertionsJsonService(assertionRepository);
    GetAssertionsRequest request = new GetAssertionsRequest("One NEB Assertion Contest",
        List.of("Alice", "Bob"), new BigDecimal("0.10"));

    RaireSolution solution = service.getRaireSolution(request);

    // Check that the metadata has been constructed appropriately
    final String metadata = "{\"candidates\":[\"Alice\",\"Bob\"]," +
        "\"contest\":\"One NEB Assertion Contest\",\"risk_limit\":0.10}";

    assertEquals(metadata, GSON.toJson(solution.metadata));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertNull(solution.solution.Err);

    // Check the contents of the RaireResults within the RaireSolution.
    assertEquals(1.1, solution.solution.Ok.difficulty);
    assertEquals(320, solution.solution.Ok.margin);

    AssertionAndDifficulty[] assertions = solution.solution.Ok.assertions;
    assertEquals(1, assertions.length);

    AssertionAndDifficulty aad = assertions[0];
    assertEquals(1.1, aad.difficulty);
    assertEquals(320, aad.margin);
    assertTrue(aad.assertion.isNEB());
    assertEquals(0, ((NotEliminatedBefore)aad.assertion).winner);
    assertEquals(1, ((NotEliminatedBefore)aad.assertion).loser);

    // Check that current risk is 1.00
    assertEquals(new BigDecimal("1.00"), aad.status.get(Metadata.STATUS_RISK));
  }

  /**
   * Retrieve assertions for a contest that has one NEN assertion.
   */
  @Test
  @Transactional
  void retrieveAssertionsExistentContestOneNENAssertion() throws RaireServiceException {
    testUtils.log(logger, "");
    GetAssertionsJsonService service = new GetAssertionsJsonService(assertionRepository);
    GetAssertionsRequest request = new GetAssertionsRequest("One NEN Assertion Contest",
        List.of("Alice", "Charlie", "Diego", "Bob"), new BigDecimal("0.10"));

    RaireSolution solution = service.getRaireSolution(request);

    // Check that the metadata has been constructed appropriately
    final String metadata = "{\"candidates\":[\"Alice\",\"Charlie\",\"Diego\",\"Bob\"]," +
        "\"contest\":\"One NEN Assertion Contest\",\"risk_limit\":0.10}";

    assertEquals(metadata, GSON.toJson(solution.metadata));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertNull(solution.solution.Err);

    // Check the contents of the RaireResults within the RaireSolution.
    assertEquals(3.01, solution.solution.Ok.difficulty);
    assertEquals(240, solution.solution.Ok.margin);

    AssertionAndDifficulty[] assertions = solution.solution.Ok.assertions;
    assertEquals(1, assertions.length);

    AssertionAndDifficulty aad = assertions[0];
    assertEquals(3.01, aad.difficulty);
    assertEquals(240, aad.margin);
    assertFalse(aad.assertion.isNEB());
    assertEquals(0, ((NotEliminatedNext)aad.assertion).winner);
    assertEquals(1, ((NotEliminatedNext)aad.assertion).loser);

    int[] continuing = {0, 1, 2, 3};
    assertArrayEquals(continuing, ((NotEliminatedNext)aad.assertion).continuing);

    // Check that current risk is 1.00
    assertEquals(new BigDecimal("1.00"), aad.status.get(Metadata.STATUS_RISK));
  }

  /**
   * Retrieve assertions for a contest where the request has been setup with incorrect
   * candidate names for the given contest.
   */
  @Test
  @Transactional
  void retrieveAssertionsInconsistentRequest1()  {
    testUtils.log(logger, "retrieveAssertionsInconsistentRequest1");
    GetAssertionsJsonService service = new GetAssertionsJsonService(assertionRepository);
    GetAssertionsRequest request = new GetAssertionsRequest("One NEN NEB Assertion Contest",
        List.of("Alice", "Charlie", "Diego", "Bob"), new BigDecimal("0.10"));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        service.getRaireSolution(request));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "Candidate list provided as parameter is inconsistent"));
  }

  /**
   * Retrieve assertions for a contest where the request has been setup with incorrect
   * candidate names for the given contest.
   */
  @Test
  @Transactional
  void retrieveAssertionsInconsistentRequest2()  {
    testUtils.log(logger, "retrieveAssertionsInconsistentRequest2");
    GetAssertionsJsonService service = new GetAssertionsJsonService(assertionRepository);
    GetAssertionsRequest request = new GetAssertionsRequest("One NEN NEB Assertion Contest",
        List.of(), new BigDecimal("0.10"));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        service.getRaireSolution(request));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, ex.errorCode);
    assertTrue(StringUtils.containsIgnoreCase(ex.getMessage(),
        "Candidate list provided as parameter is inconsistent"));
  }
}
