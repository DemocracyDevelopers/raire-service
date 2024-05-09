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

import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test cases for csv generation, including
 * - a basic, simple test case with two assertions (NEN and NEB),
 * - a test case with lots of ties, to test that extremum-calculationn is correct,
 * - a test case with difficult characters, such as " and ' and , in the candidate names.
 * TODO Note that there are assumptions about how these characters are represented in the database,
 * which need to be validated on real data.
 */
@ActiveProfiles("csv-challenges")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GetAssertionsCSVTests {

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GetAssertionsCSVService getAssertionsCSVService;
  List<String> candidates = List.of("Alice", "Bob", "Chuan", "Diego");
  List<String> trickyCharacters
      = List.of("\"Annoying,\" Alice", "\"Breaking\", Bob", "Challenging, Chuan", "O'Difficult, Diego");

  @Test
  public void testCSVTies() throws RaireServiceException {
    GetAssertionsRequest request = new GetAssertionsRequest(
        "Lots of assertions with ties Contest", candidates, new BigDecimal("0.10"));
    String output = getAssertionsCSVService.generateCSV(request);
    assertTrue(StringUtils.containsIgnoreCase(output, "Bob"));
  }

  @Test
  public void testCharacterEscaping() throws RaireServiceException {
    GetAssertionsRequest request = new GetAssertionsRequest("Lots of tricky characters Contest",
        trickyCharacters, new BigDecimal("0.10"));
    String output = getAssertionsCSVService.generateCSV(request);
    assertTrue(StringUtils.containsIgnoreCase(output, "Difficult, Diego"));
  }

  @Test
  public void testCSVDemoContest() throws RaireServiceException {
    GetAssertionsRequest request = new GetAssertionsRequest(
        "CSV Demo Contest", candidates, new BigDecimal("0.10"));
    String output = getAssertionsCSVService.generateCSV(request);
    assertTrue(StringUtils.containsIgnoreCase(output, "Alice"));
  }
}
