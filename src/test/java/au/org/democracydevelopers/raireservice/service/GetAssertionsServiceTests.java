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
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCodes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests of assertion retrieval in GetAssertionsService.
 */
@ActiveProfiles("test-containers")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class GetAssertionsServiceTests {

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * To facilitate easier checking of retrieved/saved assertion content.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();


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

  /**
   * Retrieve assertions for a contest that has one NEB assertion.
   */
  @Test
  @Transactional
  @Sql(scripts = {"/simple_assertions.sql"}, executionPhase = BEFORE_TEST_METHOD)
  void retrieveAssertionsExistentContestOneNEBAssertion() throws RaireServiceException {
    GetAssertionsService service = new GetAssertionsService(assertionRepository);
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
  @Sql(scripts = {"/simple_assertions.sql"}, executionPhase = BEFORE_TEST_METHOD)
  void retrieveAssertionsExistentContestOneNENAssertion() throws RaireServiceException {
    GetAssertionsService service = new GetAssertionsService(assertionRepository);
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
   * Retrieve assertions for a contest that has one NEN and one NEB assertion (audit in progress).
   */
  @Test
  @Transactional
  @Sql(scripts = {"/simple_assertions_in_progress.sql"}, executionPhase = BEFORE_TEST_METHOD)
  void retrieveAssertionsOneNENOneNEBAssertionInProgress() throws RaireServiceException {
    GetAssertionsService service = new GetAssertionsService(assertionRepository);
    GetAssertionsRequest request = new GetAssertionsRequest("One NEN NEB Assertion Contest",
        List.of("Liesl", "Wendell", "Amanda", "Chuan"), new BigDecimal("0.05"));

    RaireSolution solution = service.getRaireSolution(request);

    // Check that the metadata has been constructed appropriately
    final String metadata = "{\"candidates\":[\"Liesl\",\"Wendell\",\"Amanda\",\"Chuan\"]," +
        "\"contest\":\"One NEN NEB Assertion Contest\",\"risk_limit\":0.05}";

    assertEquals(metadata, GSON.toJson(solution.metadata));

    // The RaireSolution contains a RaireResultOrError, but the error should be null.
    assertNull(solution.solution.Err);

    // Check the contents of the RaireResults within the RaireSolution.
    assertEquals(3.17, solution.solution.Ok.difficulty);
    assertEquals(112, solution.solution.Ok.margin);

    AssertionAndDifficulty[] assertions = solution.solution.Ok.assertions;
    assertEquals(2, assertions.length);

    AssertionAndDifficulty aad1 = assertions[0];
    assertEquals(0.1, aad1.difficulty);
    assertEquals(112, aad1.margin);
    assertEquals(new BigDecimal("0.08"), aad1.status.get(Metadata.STATUS_RISK));
    assertTrue(aad1.assertion.isNEB());
    assertEquals(2, ((NotEliminatedBefore)aad1.assertion).winner);
    assertEquals(0, ((NotEliminatedBefore)aad1.assertion).loser);

    AssertionAndDifficulty aad2 = assertions[1];
    assertEquals(3.17, aad2.difficulty);
    assertEquals(560, aad2.margin);
    assertFalse(aad2.assertion.isNEB());
    assertEquals(2, ((NotEliminatedNext)aad2.assertion).winner);
    assertEquals(1, ((NotEliminatedNext)aad2.assertion).loser);

    int[] continuing = {0, 1, 2};
    assertArrayEquals(continuing, ((NotEliminatedNext)aad2.assertion).continuing);

    assertEquals(new BigDecimal("0.70"), aad2.status.get(Metadata.STATUS_RISK));
  }

  /**
   * Retrieve assertions for a contest where the request has been setup with incorrect
   * candidate names for the given contest.
   */
  @Test
  @Transactional
  @Sql(scripts = {"/simple_assertions.sql"}, executionPhase = BEFORE_TEST_METHOD)
  void retrieveAssertionsInconsistentRequest1()  {
    GetAssertionsService service = new GetAssertionsService(assertionRepository);
    GetAssertionsRequest request = new GetAssertionsRequest("One NEN NEB Assertion Contest",
        List.of("Alice", "Charlie", "Diego", "Bob"), new BigDecimal("0.10"));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        service.getRaireSolution(request));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, ex.errorCode);
    assertTrue(ex.getMessage().toLowerCase().
        contains("Candidate list is inconsistent with assertion".toLowerCase()));
  }

  /**
   * Retrieve assertions for a contest where the request has been setup with incorrect
   * candidate names for the given contest.
   */
  @Test
  @Transactional
  @Sql(scripts = {"/simple_assertions.sql"}, executionPhase = BEFORE_TEST_METHOD)
  void retrieveAssertionsInconsistentRequest2()  {
    GetAssertionsService service = new GetAssertionsService(assertionRepository);
    GetAssertionsRequest request = new GetAssertionsRequest("One NEN NEB Assertion Contest",
        List.of(), new BigDecimal("0.10"));

    RaireServiceException ex = assertThrows(RaireServiceException.class, () ->
        service.getRaireSolution(request));
    assertEquals(RaireErrorCodes.INTERNAL_ERROR, ex.errorCode);
    assertTrue(ex.getMessage().toLowerCase().
        contains("Candidate list is inconsistent with assertion".toLowerCase()));
  }
}
