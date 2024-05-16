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

import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.controller.GetAssertionsInProgressValidAPIRequestTests;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.testUtils;
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

/**
 * Test cases for csv generation, including:
 * - a basic, simple test case with two assertions (NEN and NEB),
 * - a test case with lots of ties, to test that extremum-calculation is correct,
 * - a test case with difficult characters, such as " and ' and , in the candidate names.
 * TODO Note that there are assumptions about how these characters are represented in the database,
 * which need to be validated on real data.
 */
@ActiveProfiles("csv-challenges")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsCSVTests {

  private static final Logger logger = LoggerFactory.getLogger(GetAssertionsCSVTests.class);

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GetAssertionsCsvService getAssertionsCSVService;

  List<String> candidates = List.of("Alice", "Bob", "Chuan", "Diego");
  List<String> trickyCharacters = List.of("Annoying, Alice", "\"Breaking, Bob\"",
      "Challenging, Chuan", "O'Difficult, Diego");

  /**
   * Test proper csv file generation of assertions when those assertions have lots of ties. These
   * maxima and minima have been manually computed to make sure they're correct.
   * @throws RaireServiceException if assertion database retrieval fails.
   */
  @Test
  public void testCSVTies() throws RaireServiceException {
    testUtils.log(logger, "testCSVTies");
    GetAssertionsRequest request = new GetAssertionsRequest(
        "Lots of assertions with ties Contest", candidates, new BigDecimal("0.10"));
    String output = getAssertionsCSVService.generateCSV(request);
    assertTrue(output.contains("Contest name, Lots of assertions with ties Contest\n"));
    assertTrue(output.contains("Candidates, \"Alice, Bob, Chuan, Diego\"\n\n"));
    assertTrue(output.contains("Extreme item, Value, Assertion IDs"));
    assertTrue(output.contains("Margin, 220, \"2, 5, 6\""));
    assertTrue(output.contains("Diluted margin, 0.22, \"2, 5, 6\""));
    assertTrue(output.contains("Raire difficulty, 3.1, 3"));
    assertTrue(output.contains("Current risk, 0.23, \"2, 3\""));
    assertTrue(output.contains("Optimistic samples to audit, 910, 4"));
    assertTrue(output.contains("Estimated samples to audit, 430, \"2, 5\"\n\n"));
    assertTrue(output.contains(
        "ID, Type, Winner, Loser, Assumed continuing, Difficulty, Margin, Diluted margin, Risk, "
        + "Estimated samples to audit, Optimistic samples to audit, Two vote over count, "
        + "One vote over count, Other discrepancy count, One vote under count, "
        + "Two vote under count\n"
    ));
    assertTrue(output.contains("1, NEB, Alice, Bob, , 2.1, 320, 0.32, 0.04, 110, 100, 0, 0, 0, 0, 0\n"));
    assertTrue(output.contains("2, NEB, Chuan, Bob, , 1.1, 220, 0.22, 0.23, 430, 200, 0, 0, 0, 0, 0\n"));
    assertTrue(output.contains("3, NEB, Diego, Chuan, , 3.1, 320, 0.32, 0.23, 50, 110, 0, 0, 0, 0, 0\n"));
    assertTrue(output.contains(
        "4, NEN, Alice, Bob, \"Alice, Bob, Chuan\", 2.0, 420, 0.42, 0.04, 320, 910, 0, 0, 0, 0, 0\n"
    ));
    assertTrue(output.contains(
        "5, NEN, Alice, Diego, \"Alice, Diego\", 1.1, 220, 0.22, 0.07, 430, 210, 0, 0, 0, 0, 0\n"
    ));
    assertTrue(output.contains(
        "6, NEN, Alice, Bob, \"Alice, Bob, Diego\", 1.2, 220, 0.22, 0.04, 400, 110, 0, 0, 0, 0, 0\n"
    ));
  }

  /**
   * Test for difficult characters in candidate names, including ' and " and ,
   * @throws RaireServiceException if assertion database retrieval fails.
   */
  @Test
  public void testCharacterEscaping() throws RaireServiceException {
    testUtils.log(logger, "testCharacterEscaping");
    GetAssertionsRequest request = new GetAssertionsRequest("Lots of tricky characters Contest",
        trickyCharacters, new BigDecimal("0.10"));
    String output = getAssertionsCSVService.generateCSV(request);
    assertTrue(StringUtils.containsIgnoreCase(output, trickyCharacters.get(0)));
    assertTrue(StringUtils.containsIgnoreCase(output, trickyCharacters.get(1)));
    assertTrue(StringUtils.containsIgnoreCase(output, trickyCharacters.get(2)));
    assertTrue(StringUtils.containsIgnoreCase(output, trickyCharacters.get(3)));

  }

  /**
   * A simple test for correct generation on a simple test case with one assertion of each type.
   * @throws RaireServiceException if assertion database retrieval fails.
   */
  @Test
  public void testCsvDemoContest() throws RaireServiceException {
    testUtils.log(logger, "testCsvDemoContest");
    GetAssertionsRequest request = new GetAssertionsRequest(
        "CSV Demo Contest", candidates, new BigDecimal("0.10"));
    String output = getAssertionsCSVService.generateCSV(request);
    assertTrue(output.contains("Contest name, CSV Demo Contest\n"));
    assertTrue(output.contains("Candidates, \"Alice, Bob, Chuan, Diego\"\n\n"));
    assertTrue(output.contains("Extreme item, Value, Assertion IDs\n"));
    assertTrue(output.contains("Margin, 100, 2\n"));
    assertTrue(output.contains("Diluted margin, 0.1, 2\n"));
    assertTrue(output.contains("Raire difficulty, 6.1, 2\n"));
    assertTrue(output.contains("Current risk, 0.06, 1\n"));
    assertTrue(output.contains("Optimistic samples to audit, 45, 2\n"));
    assertTrue(output.contains("Estimated samples to audit, 55, 1\n"));
    assertTrue(output.contains(
        "ID, Type, Winner, Loser, Assumed continuing, Difficulty, Margin, Diluted margin, Risk, "
            + "Estimated samples to audit, Optimistic samples to audit, Two vote over count, "
            + "One vote over count, Other discrepancy count, One vote under count, "
            + "Two vote under count\n"));
    assertTrue(output.contains("1, NEB, Bob, Alice, , 5.1, 112, 0.112, 0.06, 55, 35, 0, 2, 0, 0, 0\n"));
    assertTrue(output.contains(
        "2, NEN, Diego, Chuan, \"Alice, Chuan, Diego\", 6.1, 100, 0.1, 0.05, 45, 45, 0, 0, 0, 0, 0\n"
    ));
  }
}
