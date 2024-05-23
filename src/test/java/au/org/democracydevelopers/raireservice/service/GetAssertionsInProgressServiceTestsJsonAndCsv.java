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

import static au.org.democracydevelopers.raireservice.testUtils.correctMetadata;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
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
 * Tests of assertion retrieval in GetAssertionsJsonService. Assertions and other relevant data is
 * preloaded into the test database from: src/test/resources/assertions_in_progress.sql.
 * Note that tests of GetAssertionsJsonService have been spread across several test classes, each
 * defined with respect to a different test container.
 * There is a json version and a csv version of the same test.
 */
@ActiveProfiles("assertions-in-progress")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsInProgressServiceTestsJsonAndCsv {

  private static final Logger logger = LoggerFactory.getLogger(
      GetAssertionsInProgressServiceTestsJsonAndCsv.class);

  @Autowired
  GetAssertionsJsonService getAssertionsJsonService;

  @Autowired
  GetAssertionsCsvService getAssertionsCsvService;

  /**
   * Retrieve assertions for a contest that has one NEN and one NEB assertion (audit in progress).
   * (JSON).
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNENOneNEBAssertionInProgressJSON() throws RaireServiceException {
    testUtils.log(logger, "retrieveAssertionsOneNENOneNEBAssertionInProgressJSON");
    GetAssertionsRequest request = new GetAssertionsRequest("One NEN NEB Assertion Contest",
        List.of("Liesl", "Wendell", "Amanda", "Chuan"), new BigDecimal("0.05"));

    RaireSolution solution = getAssertionsJsonService.getRaireSolution(request);

    // Check that the metadata has been constructed appropriately
    assertTrue(correctMetadata(List.of("Liesl","Wendell","Amanda","Chuan"),
        "One NEN NEB Assertion Contest",0.05, solution.metadata));

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
   * Retrieve assertions for a contest that has one NEN and one NEB assertion (audit in progress).
   * (CSV)
   * For the csv files, we _do_ expect consistent ordering of the assertions, so this test requests
   * exact matches with expected strings that give the assertions in a particular order and the
   * extrema with particular indices.
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNENOneNEBAssertionInProgressCSV() throws RaireServiceException {
    testUtils.log(logger, "retrieveAssertionsOneNENOneNEBAssertionInProgressCSV");
    GetAssertionsRequest request = new GetAssertionsRequest("One NEN NEB Assertion Contest",
        List.of("Liesl", "Wendell", "Amanda", "Chuan"), new BigDecimal("0.05"));

    String csv = getAssertionsCsvService.generateCSV(request);

    assertTrue(csv.contains("Contest name,One NEN NEB Assertion Contest\n"));
    assertTrue(csv.contains("Candidates,\"Liesl,Wendell,Amanda,Chuan\""));
    assertTrue(csv.contains("Extreme item,Value,Assertion IDs\n"));
    assertTrue(csv.contains("Margin,112,\"1\"\n"));
    assertTrue(csv.contains("Diluted margin,0.1,\"1\"\n"));
    assertTrue(csv.contains("Raire difficulty,3.17,\"2\"\n"));
    assertTrue(csv.contains("Current risk,0.70,\"2\"\n"));
    assertTrue(csv.contains("Optimistic samples to audit,200,\"2\"\n"));
    assertTrue(csv.contains("Estimated samples to audit,300,\"2\"\n"));
    assertTrue(csv.contains(
        "ID,Type,Winner,Loser,Assumed continuing,Difficulty,Margin,Diluted margin,Risk,"
            + "Estimated samples to audit,Optimistic samples to audit,Two vote over count,"
            + "One vote over count,Other discrepancy count,One vote under count,"
            + "Two vote under count\n"));
    assertTrue(csv.contains("1,NEB,Amanda,Liesl,,0.1,112,0.1,0.08,27,20,2,0,0,1,0\n"));
    assertTrue(csv.contains(
        "2,NEN,Amanda,Wendell,\"Liesl,Wendell,Amanda\",3.17,560,0.5,0.70,300,200,0,2,0,0,1\n"
    ));
  }

}
