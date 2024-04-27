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


import static au.org.democracydevelopers.raireservice.testUtils.correctAssertionData;
import static au.org.democracydevelopers.raireservice.testUtils.correctAssumedContinuing;
import static au.org.democracydevelopers.raireservice.testUtils.difficultyMatchesMax;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.persistence.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.CVRContestInfoRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import au.org.democracydevelopers.raireservice.service.GenerateAssertionsException.RaireErrorCodes;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests to validate the behaviour of Assertion generation on NSW 2021 Mayoral election data.
 * Data is loaded in from src/test/resources/NSW2021Data/
 */
@ActiveProfiles("nsw-testcases")
@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class GenerateAssertionsOnNSWTestCases {

  @Autowired
  private CVRContestInfoRepository cvrContestInfoRepository;

  @Autowired
  AssertionRepository assertionRepository;

  @Autowired
  GenerateAssertionsService generateAssertionsService;

  private static final int DEFAULT_TIME_LIMIT=5;

  // error allowed when comparing doubles.
  private static final double EPS = 0.0000000001;

  /**
   * Names of contests, to match pre-loaded data.
   */
  // Contest Eurobodalla Mayoral
  private static final String nameContest_1 = "Eurobodalla Mayoral";
  private static final List<String> choicesContest_1 = List.of("WORTHINGTON Alison","GRACE David","SMITH Gary","HATCHER Mat","HARRISON N (Tubby)","POLLOCK Rob","STARMER Karyn");
  private static final int ballotCountContest_1 = 25526;
  private static final double difficultyContest_1 = 0; // TODO - get correct value.
  // Contest City of Lake Macquarie Mayoral
  private static final String nameContest_2 = "City of Lake Macquarie Mayoral";
  private static final List<String> choicesContest_2 = List.of("FRASER Kay","DAWSON Rosmairi","CUBIS Luke","PAULING Jason");
  private static final int ballotCountContest_2 = 130336;
  private static final double difficultyContest_2 = 0; // TODO - get correct value.
  // Contest City of Coffs Harbour Mayoral
  private static final String nameContest_3 = "City of Coffs Harbour Mayoral";
  private static final List<String> choicesContest_3 = List.of("SWAN Tegan","CECATO George","ADENDORFF Michael","JUDGE Tony","PRYCE Rodger","PIKE Donna","AMOS Paul","TOWNLEY Sally","ARKAN John","CASSELL Jonathan");
  private static final int ballotCountContest_3 = 45155;
  private static final double difficultyContest_3 = 0; // TODO - get correct value.
  // Contest Singleton Mayoral
  private static final String nameContest_4 = "Singleton Mayoral";
  private static final List<String> choicesContest_4 = List.of("MOORE Sue","THOMPSON Danny","JARRETT Tony","CHARLTON Belinda");
  private static final int ballotCountContest_4 = 13755;
  private static final double difficultyContest_4 = 0; // TODO - get correct value.
  // Contest City of Newcastle Mayoral
  private static final String nameContest_5 = "City of Newcastle Mayoral";
  private static final List<String> choicesContest_5 = List.of("CHURCH John","NELMES Nuatali","HOLDING Rod","MACKENZIE John","O'BRIEN Steve","BARRIE Jenny");
  private static final int ballotCountContest_5 = 100275;
  private static final double difficultyContest_5 = 0; // TODO - get correct value.
  // Contest Nambucca Valley Mayoral
  private static final String nameContest_6 = "Nambucca Valley Mayoral";
  private static final List<String> choicesContest_6 = List.of("JENVEY Susan","HOBAN Rhonda");
  private static final int ballotCountContest_6 = 12482;
  private static final double difficultyContest_6 = 0; // TODO - get correct value.
  // Contest City of Maitland Mayoral
  private static final String nameContest_7 = "City of Maitland Mayoral";
  private static final List<String> choicesContest_7 = List.of("BROWN John","MITCHELL Ben","BAKER Loretta","PENFOLD Philip","SAFFARI Shahriar (Sean)","COOPER Michael","BURKE Brian");
  private static final int ballotCountContest_7 = 54181;
  private static final double difficultyContest_7 = 0; // TODO - get correct value.
  // Contest Kempsey Mayoral
  private static final String nameContest_8 = "Kempsey Mayoral";
  private static final List<String> choicesContest_8 = List.of("HAUVILLE Leo","EVANS Andrew","BAIN Arthur","CAMPBELL Liz","SAUL Dean","IRWIN Troy","RAEBURN Bruce");
  private static final int ballotCountContest_8 = 17585;
  private static final double difficultyContest_8 = 0; // TODO - get correct value.
  // Contest Canada Bay Mayoral
  private static final String nameContest_9 = "Canada Bay Mayoral";
  private static final List<String> choicesContest_9 = List.of("TSIREKAS Angelo","LITTLE Julia","MEGNA Michael","JAGO Charles","RAMONDINO Daniela");
  private static final int ballotCountContest_9 = 48542;
  private static final double difficultyContest_9 = 0; // TODO - get correct value.
  // Contest Richmond Valley Mayoral
  private static final String nameContest_10 = "Richmond Valley Mayoral";
  private static final List<String> choicesContest_10 = List.of("MUSTOW Robert","HAYES Robert");
  private static final int ballotCountContest_10 = 13405;
  private static final double difficultyContest_10 = 0; // TODO - get correct value.
  // Contest City of Sydney Mayoral
  private static final String nameContest_11 = "City of Sydney Mayoral";
  private static final List<String> choicesContest_11 = List.of("VITHOULKAS Angela","WELDON Yvonne","SCOTT Linda","JARRETT Shauna","ELLSMORE Sylvie","MOORE Clover");
  private static final int ballotCountContest_11 = 118511;
  private static final double difficultyContest_11 = 0; // TODO - get correct value.
  // Contest Byron Mayoral
  private static final String nameContest_12 = "Byron Mayoral";
  private static final List<String> choicesContest_12 = List.of("HUNTER Alan","CLARKE Bruce","COOREY Cate","ANDERSON John","MCILRATH Christopher","LYON Michael","DEY Duncan","PUGH Asren","SWIVEL Mark");
  private static final int ballotCountContest_12 = 18165;
  private static final double difficultyContest_12 = 0; // TODO - get correct value.
  // Contest City of Broken Hill Mayoral
  private static final String nameContest_13 = "City of Broken Hill Mayoral";
  private static final List<String> choicesContest_13 = List.of("TURLEY Darriea","KENNEDY Tom","GALLAGHER Dave");
  private static final int ballotCountContest_13 = 10812;
  private static final double difficultyContest_13 = 0; // TODO - get correct value.
  // Contest City of Shellharbour Mayoral
  private static final String nameContest_14 = "City of Shellharbour Mayoral";
  private static final List<String> choicesContest_14 = List.of("HOMER Chris","SALIBA Marianne");
  private static final int ballotCountContest_14 = 46273;
  private static final double difficultyContest_14 = 0; // TODO - get correct value.
  // Contest City of Shoalhaven Mayoral
  private static final String nameContest_15 = "City of Shoalhaven Mayoral";
  private static final List<String> choicesContest_15 = List.of("GREEN Paul","KITCHENER Mark","WHITE Patricia","WATSON Greg","DIGIGLIO Nina","FINDLEY Amanda");
  private static final int ballotCountContest_15 = 67030;
  private static final double difficultyContest_15 = 0; // TODO - get correct value.
  // Contest Mosman Mayoral
  private static final String nameContest_16 = "Mosman Mayoral";
  private static final List<String> choicesContest_16 = List.of("MOLINE Libby","BENDALL Roy","HARDING Sarah","CORRIGAN Carolyn","MENZIES Simon");
  private static final int ballotCountContest_16 = 16425;
  private static final double difficultyContest_16 = 0; // TODO - get correct value.
  // Contest City of Orange Mayoral
  private static final String nameContest_17 = "City of Orange Mayoral";
  private static final List<String> choicesContest_17 = List.of("HAMLING Jason","SPALDING Amanda","JONES Neil","WHITTON Jeffery","DUFFY Kevin","SMITH Lesley","MILETO Tony");
  private static final int ballotCountContest_17 = 24355;
  private static final double difficultyContest_17 = 0; // TODO - get correct value.
  // Contest City of Wollongong Mayoral
  private static final String nameContest_18 = "City of Wollongong Mayoral";
  private static final List<String> choicesContest_18 = List.of("GLYKIS Marie","DORAHY John","BROWN Tania","BRADBERY Gordon","ANTHONY Andrew","COX Mithra");
  private static final int ballotCountContest_18 = 127240;
  private static final double difficultyContest_18 = 0; // TODO - get correct value.
  // Contest Port Stephens Mayoral
  private static final String nameContest_19 = "Port Stephens Mayoral";
  private static final List<String> choicesContest_19 = List.of("ANDERSON Leah","PALMER Ryan");
  private static final int ballotCountContest_19 = 47807;
  private static final double difficultyContest_19 = 0; // TODO - get correct value.
  // Contest Wollondilly Mayoral
  private static final String nameContest_20 = "Wollondilly Mayoral";
  private static final List<String> choicesContest_20 = List.of("KHAN Robert","BANASIK Michael","DEETH Matthew","LAW Ray","GOULD Matt","HANNAN Judy");
  private static final int ballotCountContest_20 = 31355;
  private static final double difficultyContest_20 = 0; // TODO - get correct value.
  // Contest Hornsby Mayoral
  private static final String nameContest_21 = "Hornsby Mayoral";
  private static final List<String> choicesContest_21 = List.of("HEYDE Emma","RUDDOCK Philip");
  private static final int ballotCountContest_21 = 85656;
  private static final double difficultyContest_21 = 0; // TODO - get correct value.
  // Contest Ballina Mayoral
  private static final String nameContest_22 = "Ballina Mayoral";
  private static final List<String> choicesContest_22 = List.of("WILLIAMS Keith","JOHNSON Jeff","MCCARTHY Steve","JOHNSTON Eoin","CADWALLADER Sharon");
  private static final int ballotCountContest_22 = 26913;
  private static final double difficultyContest_22 = 0; // TODO - get correct value.
  // Contest Bellingen Mayoral
  private static final String nameContest_23 = "Bellingen Mayoral";
  private static final List<String> choicesContest_23 = List.of("ALLAN Steve","WOODWARD Andrew","KING Dominic");
  private static final int ballotCountContest_23 = 8374;
  private static final double difficultyContest_23 = 0; // TODO - get correct value.
  // Contest City of Lismore Mayoral
  private static final String nameContest_24 = "City of Lismore Mayoral";
  private static final List<String> choicesContest_24 = List.of("KRIEG Steve","COOK Darlene","HEALEY Patrick","GRINDON-EKINS Vanessa","ROB Big","BIRD Elly");
  private static final int ballotCountContest_24 = 26474;
  private static final double difficultyContest_24 = 0; // TODO - get correct value.
  // Contest City of Willoughby Mayoral
  private static final String nameContest_25 = "City of Willoughby Mayoral";
  private static final List<String> choicesContest_25 = List.of("ROZOS Angelo","CAMPBELL Craig","TAYLOR Tanya");
  private static final int ballotCountContest_25 = 37942;
  private static final double difficultyContest_25 = 0; // TODO - get correct value.
  // Contest The Hills Shire Mayoral
  private static final String nameContest_26 = "The Hills Shire Mayoral";
  private static final List<String> choicesContest_26 = List.of("SHAHAMAT Vida","GANGEMI Peter","ROZYCKI Jerzy (George)","TRACEY Ryan","YAZDANI Ereboni (Alexia)");
  private static final int ballotCountContest_26 = 105384;
  private static final double difficultyContest_26 = 0; // TODO - get correct value.
  // Contest City of Cessnock Mayoral
  private static final String nameContest_27 = "City of Cessnock Mayoral";
  private static final List<String> choicesContest_27 = List.of("MURRAY Janet","SUVAAL Jay","MOORES John","OLSEN Ian");
  private static final int ballotCountContest_27 = 36497;
  private static final double difficultyContest_27 = 0; // TODO - get correct value.
  // Contest City of Griffith Mayoral
  private static final String nameContest_28 = "City of Griffith Mayoral";
  private static final List<String> choicesContest_28 = List.of("MERCURI Rina","NAPOLI Anne","ZAPPACOSTA Dino","LA ROCCA Mariacarmina (Carmel)","CURRAN Doug");
  private static final int ballotCountContest_28 = 14179;
  private static final double difficultyContest_28 = 0; // TODO - get correct value.
  // Contest Port Macquarie-Hastings Mayoral
  private static final String nameContest_29 = "Port Macquarie-Hastings Mayoral";
  private static final List<String> choicesContest_29 = List.of("PINSON Peta","GATES Steven","SHEPPARD Rachel","INTEMANN Lisa","LIPOVAC Nik");
  private static final int ballotCountContest_29 = 54499;
  private static final double difficultyContest_29 = 0; // TODO - get correct value.
  // Contest City of Liverpool Mayoral
  private static final String nameContest_30 = "City of Liverpool Mayoral";
  private static final List<String> choicesContest_30 = List.of("HAGARTY Nathan","MORSHED Asm","ANDJELKOVIC Milomir (Michael)","HARLE Peter","MANNOUN Ned");
  private static final int ballotCountContest_30 = 115177;
  private static final double difficultyContest_30 = 0; // TODO - get correct value.
  // Contest Uralla Mayoral
  private static final String nameContest_31 = "Uralla Mayoral";
  private static final List<String> choicesContest_31 = List.of("BELL Robert","LEDGER Natasha","STRUTT Isabel");
  private static final int ballotCountContest_31 = 3781;
  private static final double difficultyContest_31 = 0; // TODO - get correct value.
  // Contest Hunter's Hill Mayoral
  private static final String nameContest_32 = "Hunter's Hill Mayoral";
  private static final List<String> choicesContest_32 = List.of("GUAZZAROTTO David","MILES Zac","QUINN Richard","WILLIAMS Ross");
  private static final int ballotCountContest_32 = 8356;
  private static final double difficultyContest_32 = 0; // TODO - get correct value.
  // Contest Burwood Mayoral
  private static final String nameContest_33 = "Burwood Mayoral";
  private static final List<String> choicesContest_33 = List.of("HULL David","MURRAY Alan","CUTCHER Ned","FAKER John");
  private static final int ballotCountContest_33 = 17797;
  private static final double difficultyContest_33 = 0; // TODO - get correct value.

  /**
   * Trivial test to see whether the placeholder service throws the expected placeholder exception.
   * TODO This can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void dummyServiceThrowsException() {
    GenerateAssertionsRequest firstRequest = new GenerateAssertionsRequest(nameContest_1, ballotCountContest_1,
        DEFAULT_TIME_LIMIT, choicesContest_1);
    assertThrows(GenerateAssertionsException.class, () ->
        generateAssertionsService.generateAssertions(firstRequest)
    );
  }

  /**
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest. Bellingen_Mayoral
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest1() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(1, 1);
    List<String> expectedChoices = choicesContest_1;
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(expectedChoices.contains(retrievedFirstChoice));
  }

  /**
   * Test the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * Contest 1.
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest1() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_1,
        ballotCountContest_1, DEFAULT_TIME_LIMIT, choicesContest_1);
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);
    // TODO: Add winners.
    // assertEquals(winnerContest_1, response.winner);
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_1);
    assertTrue(difficultyMatchesMax(difficultyContest_1, assertions, EPS));
  }
}