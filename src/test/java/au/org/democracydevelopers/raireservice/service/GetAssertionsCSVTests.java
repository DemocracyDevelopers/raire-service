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

import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.RaireResultMixIn;
import au.org.democracydevelopers.raireservice.service.GetAssertionsCSVService;
import au.org.democracydevelopers.raireservice.service.RaireServiceException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("simple-assertions")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class GetAssertionsCSVTests {

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GetAssertionsCSVService getAssertionsCSVService;
  String[] candidates = {"Liesl", "Wendell", "Amanda"};
  private final Map<String,Object> metadata = Map.of("candidates", candidates, "contest","TestContest");
  int[] continuing = {0, 1 ,2};
  AssertionAndDifficulty aadAmandaNEBLiesl = new AssertionAndDifficulty(
      new au.org.democracydevelopers.raire.assertions.NotEliminatedBefore(2, 0),
      0.1, 112);

  AssertionAndDifficulty aadAmandaNENWendell = new AssertionAndDifficulty(
      new au.org.democracydevelopers.raire.assertions.NotEliminatedNext(2, 1,
          continuing),3.17, 560);
  AssertionAndDifficulty[] assertions = new AssertionAndDifficulty[]{aadAmandaNEBLiesl, aadAmandaNENWendell};
  private final RaireResultMixIn result = new RaireResultMixIn(assertions, 43.0,
      234, 4);
  private final RaireSolution solution = new RaireSolution(metadata, new RaireResultOrError(result));
  @Test
  public void testCSVOutput() throws RaireServiceException {
    GetAssertionsRequest request = new GetAssertionsRequest("One NEB Assertion Contest",
        List.of("Alice", "Bob"), new BigDecimal("0.10"));
    String output = getAssertionsCSVService.generateCSV(request);
    assertTrue(StringUtils.containsIgnoreCase(output, "Bob"));
  }

  /*
  Tests with tricky characters to escape.
      String test = preface + '\n' + headers + ',' + StringEscapeUtils.escapeCsv("Smith, Bob") + ','
          + StringEscapeUtils.escapeCsv("Alice \"The fixer\"") + ','
          + StringEscapeUtils.escapeCsv("Alice's friend");
   */
}