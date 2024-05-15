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
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.testUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * Tests of assertion retrieval in GetAssertionsService. Assertions and other relevant data is
 * preloaded into the test database from: src/test/resources/assertions_in_progress.sql.
 * Note that tests of GetAssertionsService have been spread across several test classes, each
 * defined with respect to a different test container.
 */
@ActiveProfiles("assertions-in-progress")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsServiceInProgressTests {

  private static final Logger logger = LoggerFactory.getLogger(
      GetAssertionsServiceInProgressTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  /**
   * To facilitate easier checking of retrieved/saved assertion content.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();


  /**
   * Retrieve assertions for a contest that has one NEN and one NEB assertion (audit in progress).
   */
  @Test
  @Transactional
  void retrieveAssertionsOneNENOneNEBAssertionInProgress() throws RaireServiceException {
    testUtils.log(logger, "retrieveAssertionsOneNENOneNEBAssertionInProgress");
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

}
