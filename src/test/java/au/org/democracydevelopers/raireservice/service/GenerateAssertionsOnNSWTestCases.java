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


import static au.org.democracydevelopers.raireservice.testUtils.difficultyMatchesMax;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.persistence.repository.CVRContestInfoRepository;
import au.org.democracydevelopers.raireservice.request.GenerateAssertionsRequest;
import au.org.democracydevelopers.raireservice.response.GenerateAssertionsResponse;
import java.util.List;
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
   * Expected data for each NSW contest.
   * Difficulties are taken from raire-java::src/test/java/au/org/democracydevelopers/raire/TestNSW
   * which in turn tests against raire-rs.
   * Winners are taken from the New South Wales official election results at
   * https://pastvtr.elections.nsw.gov.au/LG2101/status/mayoral
   * The ballotCounts are derived from the data, but double-checked for exact match with the
   * NSWEC website.
   */
  // Contest Eurobodalla Mayoral
  private static final String nameContest_1 = "Eurobodalla Mayoral";
  private static final List<String> choicesContest_1 = List.of("WORTHINGTON Alison","GRACE David","SMITH Gary","HATCHER Mat","HARRISON N (Tubby)","POLLOCK Rob","STARMER Karyn");
  private static final int ballotCountContest_1 = 25526;
  private static final double difficultyContest_1 = 23.079566003616637;
  private static final String winnerContest_1 = "HATCHER Mat";

  // Contest City of Lake Macquarie Mayoral
  private static final String nameContest_2 = "City of Lake Macquarie Mayoral";
  private static final List<String> choicesContest_2 = List.of("FRASER Kay","DAWSON Rosmairi","CUBIS Luke","PAULING Jason");
  private static final int ballotCountContest_2 = 130336;
  private static final double difficultyContest_2 = 3.1113869658629745;
  private static final String winnerContest_2 = "FRASER Kay";

  // Contest City of Coffs Harbour Mayoral
  private static final String nameContest_3 = "City of Coffs Harbour Mayoral";
  private static final List<String> choicesContest_3 = List.of("SWAN Tegan","CECATO George","ADENDORFF Michael","JUDGE Tony","PRYCE Rodger","PIKE Donna","AMOS Paul","TOWNLEY Sally","ARKAN John","CASSELL Jonathan");
  private static final int ballotCountContest_3 = 45155;
  private static final double difficultyContest_3 = 8.571564160971906;
  private static final String winnerContest_3 = "AMOS Paul";

  // Contest Singleton Mayoral
  private static final String nameContest_4 = "Singleton Mayoral";
  private static final List<String> choicesContest_4 = List.of("MOORE Sue","THOMPSON Danny","JARRETT Tony","CHARLTON Belinda");
  private static final int ballotCountContest_4 = 13755;
  private static final double difficultyContest_4 = 12.118942731277533;
  private static final String winnerContest_4 = "MOORE Sue";

  // Contest City of Newcastle Mayoral
  private static final String nameContest_5 = "City of Newcastle Mayoral";
  private static final List<String> choicesContest_5 = List.of("CHURCH John","NELMES Nuatali","HOLDING Rod","MACKENZIE John","O'BRIEN Steve","BARRIE Jenny");
  private static final int ballotCountContest_5 = 100275;
  private static final double difficultyContest_5 = 5.913487055493307;
  private static final String winnerContest_5 = "NELMES Nuatali";

  // Contest Nambucca Valley Mayoral
  private static final String nameContest_6 = "Nambucca Valley Mayoral";
  private static final List<String> choicesContest_6 = List.of("JENVEY Susan","HOBAN Rhonda");
  private static final int ballotCountContest_6 = 12482;
  private static final double difficultyContest_6 = 2.7360806663743973;
  private static final String winnerContest_6 = "HOBAN Rhonda";

  // Contest City of Maitland Mayoral
  private static final String nameContest_7 = "City of Maitland Mayoral";
  private static final List<String> choicesContest_7 = List.of("BROWN John","MITCHELL Ben","BAKER Loretta","PENFOLD Philip","SAFFARI Shahriar (Sean)","COOPER Michael","BURKE Brian");
  private static final int ballotCountContest_7 = 54181;
  private static final double difficultyContest_7 = 47.072980017376196;
  private static final String winnerContest_7 = "PENFOLD Philip";

  // Contest Kempsey Mayoral
  private static final String nameContest_8 = "Kempsey Mayoral";
  private static final List<String> choicesContest_8 = List.of("HAUVILLE Leo","EVANS Andrew","BAIN Arthur","CAMPBELL Liz","SAUL Dean","IRWIN Troy","RAEBURN Bruce");
  private static final int ballotCountContest_8 = 17585;
  private static final double difficultyContest_8 = 45.43927648578811;
  private static final String winnerContest_8 = "HAUVILLE Leo";

  // Contest Canada Bay Mayoral
  private static final String nameContest_9 = "Canada Bay Mayoral";
  private static final List<String> choicesContest_9 = List.of("TSIREKAS Angelo","LITTLE Julia","MEGNA Michael","JAGO Charles","RAMONDINO Daniela");
  private static final int ballotCountContest_9 = 48542;
  private static final double difficultyContest_9 = 8.140533288613113;
  private static final String winnerContest_9 = "TSIREKAS Angelo";

  // Contest Richmond Valley Mayoral
  private static final String nameContest_10 = "Richmond Valley Mayoral";
  private static final List<String> choicesContest_10 = List.of("MUSTOW Robert","HAYES Robert");
  private static final int ballotCountContest_10 = 13405;
  private static final double difficultyContest_10 = 2.302868922865487;
  private static final String winnerContest_10 = "MUSTOW Robert";

  // Contest City of Sydney Mayoral
  private static final String nameContest_11 = "City of Sydney Mayoral";
  private static final List<String> choicesContest_11 = List.of("VITHOULKAS Angela","WELDON Yvonne","SCOTT Linda","JARRETT Shauna","ELLSMORE Sylvie","MOORE Clover");
  private static final int ballotCountContest_11 = 118511;
  private static final double difficultyContest_11 = 3.6873366521468576;
  private static final String winnerContest_11 = "MOORE Clover";

  // Contest Byron Mayoral
  private static final String nameContest_12 = "Byron Mayoral";
  private static final List<String> choicesContest_12 = List.of("HUNTER Alan","CLARKE Bruce","COOREY Cate","ANDERSON John","MCILRATH Christopher","LYON Michael","DEY Duncan","PUGH Asren","SWIVEL Mark");
  private static final int ballotCountContest_12 = 18165;
  private static final double difficultyContest_12 = 17.13679245283019;
  private static final String winnerContest_12 = "LYON Michael";

  // Contest City of Broken Hill Mayoral
  private static final String nameContest_13 = "City of Broken Hill Mayoral";
  private static final List<String> choicesContest_13 = List.of("TURLEY Darriea","KENNEDY Tom","GALLAGHER Dave");
  private static final int ballotCountContest_13 = 10812;
  private static final double difficultyContest_13 = 3.2773567747802366;
  private static final String winnerContest_13 = "KENNEDY Tom";

  // Contest City of Shellharbour Mayoral
  private static final String nameContest_14 = "City of Shellharbour Mayoral";
  private static final List<String> choicesContest_14 = List.of("HOMER Chris","SALIBA Marianne");
  private static final int ballotCountContest_14 = 46273;
  private static final double difficultyContest_14 = 17.83159922928709;
  private static final String winnerContest_14 = "HOMER Chris";

  // Contest City of Shoalhaven Mayoral
  private static final String nameContest_15 = "City of Shoalhaven Mayoral";
  private static final List<String> choicesContest_15 = List.of("GREEN Paul","KITCHENER Mark","WHITE Patricia","WATSON Greg","DIGIGLIO Nina","FINDLEY Amanda");
  private static final int ballotCountContest_15 = 67030;
  private static final double difficultyContest_15 = 41.53035935563817;
  private static final String winnerContest_15 = "FINDLEY Amanda";

  // Contest Mosman Mayoral
  private static final String nameContest_16 = "Mosman Mayoral";
  private static final List<String> choicesContest_16 = List.of("MOLINE Libby","BENDALL Roy","HARDING Sarah","CORRIGAN Carolyn","MENZIES Simon");
  private static final int ballotCountContest_16 = 16425;
  private static final double difficultyContest_16 = 4.498767460969598;
  private static final String winnerContest_16 = "CORRIGAN Carolyn";

  // Contest City of Orange Mayoral
  private static final String nameContest_17 = "City of Orange Mayoral";
  private static final List<String> choicesContest_17 = List.of("HAMLING Jason","SPALDING Amanda","JONES Neil","WHITTON Jeffery","DUFFY Kevin","SMITH Lesley","MILETO Tony");
  private static final int ballotCountContest_17 = 24355;
  private static final double difficultyContest_17 = 50.01026694045174;
  private static final String winnerContest_17 = "HAMLING Jason";

  // Contest City of Wollongong Mayoral
  private static final String nameContest_18 = "City of Wollongong Mayoral";
  private static final List<String> choicesContest_18 = List.of("GLYKIS Marie","DORAHY John","BROWN Tania","BRADBERY Gordon","ANTHONY Andrew","COX Mithra");
  private static final int ballotCountContest_18 = 127240;
  private static final double difficultyContest_18 = 47.72693173293323;
  private static final String winnerContest_18 = "BRADBERY Gordon";

  // Contest Port Stephens Mayoral
  private static final String nameContest_19 = "Port Stephens Mayoral";
  private static final List<String> choicesContest_19 = List.of("ANDERSON Leah","PALMER Ryan");
  private static final int ballotCountContest_19 = 47807;
  private static final double difficultyContest_19 = 84.31569664902999;
  private static final String winnerContest_19 = "PALMER Ryan";

  // Contest Wollondilly Mayoral
  private static final String nameContest_20 = "Wollondilly Mayoral";
  private static final List<String> choicesContest_20 = List.of("KHAN Robert","BANASIK Michael","DEETH Matthew","LAW Ray","GOULD Matt","HANNAN Judy");
  private static final int ballotCountContest_20 = 31355;
  private static final double difficultyContest_20 = 24.40077821011673;
  private static final String winnerContest_20 = "GOULD Matt";

  // Contest Hornsby Mayoral
  private static final String nameContest_21 = "Hornsby Mayoral";
  private static final List<String> choicesContest_21 = List.of("HEYDE Emma","RUDDOCK Philip");
  private static final int ballotCountContest_21 = 85656;
  private static final double difficultyContest_21 = 6.866762866762866;
  private static final String winnerContest_21 = "RUDDOCK Philip";

  // Contest Ballina Mayoral
  private static final String nameContest_22 = "Ballina Mayoral";
  private static final List<String> choicesContest_22 = List.of("WILLIAMS Keith","JOHNSON Jeff","MCCARTHY Steve","JOHNSTON Eoin","CADWALLADER Sharon");
  private static final int ballotCountContest_22 = 26913;
  private static final double difficultyContest_22 = 7.285598267460747;
  private static final String winnerContest_22 = "CADWALLADER Sharon";

  // Contest Bellingen Mayoral
  private static final String nameContest_23 = "Bellingen Mayoral";
  private static final List<String> choicesContest_23 = List.of("ALLAN Steve","WOODWARD Andrew","KING Dominic");
  private static final int ballotCountContest_23 = 8374;
  private static final double difficultyContest_23 = 3.3335987261146496;
  private static final String winnerContest_23 = "ALLAN Steve";

  // Contest City of Lismore Mayoral
  private static final String nameContest_24 = "City of Lismore Mayoral";
  private static final List<String> choicesContest_24 = List.of("KRIEG Steve","COOK Darlene","HEALEY Patrick","GRINDON-EKINS Vanessa","ROB Big","BIRD Elly");
  private static final int ballotCountContest_24 = 26474;
  private static final double difficultyContest_24 = 2.929836210712705;
  private static final String winnerContest_24 = "KRIEG Steve";

  // Contest City of Willoughby Mayoral
  private static final String nameContest_25 = "City of Willoughby Mayoral";
  private static final List<String> choicesContest_25 = List.of("ROZOS Angelo","CAMPBELL Craig","TAYLOR Tanya");
  private static final int ballotCountContest_25 = 37942;
  private static final double difficultyContest_25 = 14.990912682734097;
  private static final String winnerContest_25 = "TAYLOR Tanya";

  // Contest The Hills Shire Mayoral
  private static final String nameContest_26 = "The Hills Shire Mayoral";
  private static final List<String> choicesContest_26 = List.of("SHAHAMAT Vida","GANGEMI Peter","ROZYCKI Jerzy (George)","TRACEY Ryan","YAZDANI Ereboni (Alexia)");
  private static final int ballotCountContest_26 = 105384;
  private static final double difficultyContest_26 = 3.6801229221958374;
  private static final String winnerContest_26 = "GANGEMI Peter";

  // Contest City of Cessnock Mayoral
  private static final String nameContest_27 = "City of Cessnock Mayoral";
  private static final List<String> choicesContest_27 = List.of("MURRAY Janet","SUVAAL Jay","MOORES John","OLSEN Ian");
  private static final int ballotCountContest_27 = 36497;
  private static final double difficultyContest_27 = 6.466513111268604;
  private static final String winnerContest_27 = "SUVAAL Jay";

  // Contest City of Griffith Mayoral
  private static final String nameContest_28 = "City of Griffith Mayoral";
  private static final List<String> choicesContest_28 = List.of("MERCURI Rina","NAPOLI Anne","ZAPPACOSTA Dino","LA ROCCA Mariacarmina (Carmel)","CURRAN Doug");
  private static final int ballotCountContest_28 = 14179;
  private static final double difficultyContest_28 = 3.9320576816417083;
  private static final String winnerContest_28 = "CURRAN Doug";

  // Contest Port Macquarie-Hastings Mayoral
  private static final String nameContest_29 = "Port Macquarie-Hastings Mayoral";
  private static final List<String> choicesContest_29 = List.of("PINSON Peta","GATES Steven","SHEPPARD Rachel","INTEMANN Lisa","LIPOVAC Nik");
  private static final int ballotCountContest_29 = 54499;
  private static final double difficultyContest_29 = 2.8524547262640008;
  private static final String winnerContest_29 = "PINSON Peta";

  // Contest City of Liverpool Mayoral
  private static final String nameContest_30 = "City of Liverpool Mayoral";
  private static final List<String> choicesContest_30 = List.of("HAGARTY Nathan","MORSHED Asm","ANDJELKOVIC Milomir (Michael)","HARLE Peter","MANNOUN Ned");
  private static final int ballotCountContest_30 = 115177;
  private static final double difficultyContest_30 = 45.416798107255524;
  private static final String winnerContest_30 = "MANNOUN Ned";

  // Contest Uralla Mayoral
  private static final String nameContest_31 = "Uralla Mayoral";
  private static final List<String> choicesContest_31 = List.of("BELL Robert","LEDGER Natasha","STRUTT Isabel");
  private static final int ballotCountContest_31 = 3781;
  private static final double difficultyContest_31 = 1.6297413793103448;
  private static final String winnerContest_31 = "BELL Robert";

  // Contest Hunter's Hill Mayoral
  private static final String nameContest_32 = "Hunter's Hill Mayoral";
  private static final List<String> choicesContest_32 = List.of("GUAZZAROTTO David","MILES Zac","QUINN Richard","WILLIAMS Ross");
  private static final int ballotCountContest_32 = 8356;
  private static final double difficultyContest_32 = 38.330275229357795;
  private static final String winnerContest_32 = "MILES Zac";

  // Contest Burwood Mayoral
  private static final String nameContest_33 = "Burwood Mayoral";
  private static final List<String> choicesContest_33 = List.of("HULL David","MURRAY Alan","CUTCHER Ned","FAKER John");
  private static final int ballotCountContest_33 = 17797;
  private static final double difficultyContest_33 = 2.5269061479483175;
  private static final String winnerContest_33 = "FAKER John";

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
   * Contest 1.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest1() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(1, 1);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_1.contains(retrievedFirstChoice));
  }

  /**
   * Contest 1.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest1() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_1,
        ballotCountContest_1, DEFAULT_TIME_LIMIT, choicesContest_1);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_1, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_1);
    assertTrue(difficultyMatchesMax(difficultyContest_1, assertions, EPS));
  }

  /**
   * Contest 2.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest2() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(2, 2);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_2.contains(retrievedFirstChoice));
  }

  /**
   * Contest 2.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest2() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_2,
        ballotCountContest_2, DEFAULT_TIME_LIMIT, choicesContest_2);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_2, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_2);
    assertTrue(difficultyMatchesMax(difficultyContest_2, assertions, EPS));
  }

  /**
   * Contest 3.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest3() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(3, 3);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_3.contains(retrievedFirstChoice));
  }

  /**
   * Contest 3.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest3() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_3,
        ballotCountContest_3, DEFAULT_TIME_LIMIT, choicesContest_3);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_3, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_3);
    assertTrue(difficultyMatchesMax(difficultyContest_3, assertions, EPS));
  }

  /**
   * Contest 4.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest4() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(4, 4);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_4.contains(retrievedFirstChoice));
  }

  /**
   * Contest 4.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest4() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_4,
        ballotCountContest_4, DEFAULT_TIME_LIMIT, choicesContest_4);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_4, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_4);
    assertTrue(difficultyMatchesMax(difficultyContest_4, assertions, EPS));
  }

  /**
   * Contest 5.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest5() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(5, 5);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_5.contains(retrievedFirstChoice));
  }

  /**
   * Contest 5.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest5() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_5,
        ballotCountContest_5, DEFAULT_TIME_LIMIT, choicesContest_5);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_5, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_5);
    assertTrue(difficultyMatchesMax(difficultyContest_5, assertions, EPS));
  }

  /**
   * Contest 6.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest6() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(6, 6);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_6.contains(retrievedFirstChoice));
  }

  /**
   * Contest 6.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest6() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_6,
        ballotCountContest_6, DEFAULT_TIME_LIMIT, choicesContest_6);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_6, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_6);
    assertTrue(difficultyMatchesMax(difficultyContest_6, assertions, EPS));
  }

  /**
   * Contest 7.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest7() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(7, 7);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_7.contains(retrievedFirstChoice));
  }

  /**
   * Contest 7.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest7() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_7,
        ballotCountContest_7, DEFAULT_TIME_LIMIT, choicesContest_7);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_7, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_7);
    assertTrue(difficultyMatchesMax(difficultyContest_7, assertions, EPS));
  }

  /**
   * Contest 8.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest8() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(8, 8);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_8.contains(retrievedFirstChoice));
  }

  /**
   * Contest 8.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest8() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_8,
        ballotCountContest_8, DEFAULT_TIME_LIMIT, choicesContest_8);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_8, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_8);
    assertTrue(difficultyMatchesMax(difficultyContest_8, assertions, EPS));
  }

  /**
   * Contest 9.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest9() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(9, 9);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_9.contains(retrievedFirstChoice));
  }

  /**
   * Contest 9.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest9() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_9,
        ballotCountContest_9, DEFAULT_TIME_LIMIT, choicesContest_9);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_9, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_9);
    assertTrue(difficultyMatchesMax(difficultyContest_9, assertions, EPS));
  }

  /**
   * Contest 10.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest10() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(10, 10);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_10.contains(retrievedFirstChoice));
  }

  /**
   * Contest 10.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest10() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_10,
        ballotCountContest_10, DEFAULT_TIME_LIMIT, choicesContest_10);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_10, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_10);
    assertTrue(difficultyMatchesMax(difficultyContest_10, assertions, EPS));
  }

  /**
   * Contest 11.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest11() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(11, 11);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_11.contains(retrievedFirstChoice));
  }

  /**
   * Contest 11.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest11() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_11,
        ballotCountContest_11, DEFAULT_TIME_LIMIT, choicesContest_11);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_11, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_11);
    assertTrue(difficultyMatchesMax(difficultyContest_11, assertions, EPS));
  }

  /**
   * Contest 12.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest12() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(12, 12);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_12.contains(retrievedFirstChoice));
  }

  /**
   * Contest 12.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest12() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_12,
        ballotCountContest_12, DEFAULT_TIME_LIMIT, choicesContest_12);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_12, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_12);
    assertTrue(difficultyMatchesMax(difficultyContest_12, assertions, EPS));
  }

  /**
   * Contest 13.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest13() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(13, 13);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_13.contains(retrievedFirstChoice));
  }

  /**
   * Contest 13.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest13() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_13,
        ballotCountContest_13, DEFAULT_TIME_LIMIT, choicesContest_13);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_13, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_13);
    assertTrue(difficultyMatchesMax(difficultyContest_13, assertions, EPS));
  }

  /**
   * Contest 14.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest14() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(14, 14);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_14.contains(retrievedFirstChoice));
  }

  /**
   * Contest 14.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest14() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_14,
        ballotCountContest_14, DEFAULT_TIME_LIMIT, choicesContest_14);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_14, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_14);
    assertTrue(difficultyMatchesMax(difficultyContest_14, assertions, EPS));
  }

  /**
   * Contest 15.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest15() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(15, 15);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_15.contains(retrievedFirstChoice));
  }

  /**
   * Contest 15.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest15() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_15,
        ballotCountContest_15, DEFAULT_TIME_LIMIT, choicesContest_15);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_15, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_15);
    assertTrue(difficultyMatchesMax(difficultyContest_15, assertions, EPS));
  }

  /**
   * Contest 16.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest16() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(16, 16);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_16.contains(retrievedFirstChoice));
  }

  /**
   * Contest 16.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest16() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_16,
        ballotCountContest_16, DEFAULT_TIME_LIMIT, choicesContest_16);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_16, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_16);
    assertTrue(difficultyMatchesMax(difficultyContest_16, assertions, EPS));
  }

  /**
   * Contest 17.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest17() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(17, 17);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_17.contains(retrievedFirstChoice));
  }

  /**
   * Contest 17.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest17() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_17,
        ballotCountContest_17, DEFAULT_TIME_LIMIT, choicesContest_17);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_17, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_17);
    assertTrue(difficultyMatchesMax(difficultyContest_17, assertions, EPS));
  }

  /**
   * Contest 18.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest18() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(18, 18);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_18.contains(retrievedFirstChoice));
  }

  /**
   * Contest 18.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest18() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_18,
        ballotCountContest_18, DEFAULT_TIME_LIMIT, choicesContest_18);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_18, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_18);
    assertTrue(difficultyMatchesMax(difficultyContest_18, assertions, EPS));
  }

  /**
   * Contest 19.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest19() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(19, 19);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_19.contains(retrievedFirstChoice));
  }

  /**
   * Contest 19.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest19() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_19,
        ballotCountContest_19, DEFAULT_TIME_LIMIT, choicesContest_19);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_19, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_19);
    assertTrue(difficultyMatchesMax(difficultyContest_19, assertions, EPS));
  }

  /**
   * Contest 20.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest20() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(20, 20);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_20.contains(retrievedFirstChoice));
  }

  /**
   * Contest 20.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest20() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_20,
        ballotCountContest_20, DEFAULT_TIME_LIMIT, choicesContest_20);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_20, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_20);
    assertTrue(difficultyMatchesMax(difficultyContest_20, assertions, EPS));
  }

  /**
   * Contest 21.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest21() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(21, 21);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_21.contains(retrievedFirstChoice));
  }

  /**
   * Contest 21.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest21() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_21,
        ballotCountContest_21, DEFAULT_TIME_LIMIT, choicesContest_21);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_21, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_21);
    assertTrue(difficultyMatchesMax(difficultyContest_21, assertions, EPS));
  }

  /**
   * Contest 22.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest22() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(22, 22);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_22.contains(retrievedFirstChoice));
  }

  /**
   * Contest 22.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest22() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_22,
        ballotCountContest_22, DEFAULT_TIME_LIMIT, choicesContest_22);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_22, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_22);
    assertTrue(difficultyMatchesMax(difficultyContest_22, assertions, EPS));
  }

  /**
   * Contest 23.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest23() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(23, 23);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_23.contains(retrievedFirstChoice));
  }

  /**
   * Contest 23.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest23() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_23,
        ballotCountContest_23, DEFAULT_TIME_LIMIT, choicesContest_23);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_23, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_23);
    assertTrue(difficultyMatchesMax(difficultyContest_23, assertions, EPS));
  }

  /**
   * Contest 24.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest24() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(24, 24);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_24.contains(retrievedFirstChoice));
  }

  /**
   * Contest 24.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest24() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_24,
        ballotCountContest_24, DEFAULT_TIME_LIMIT, choicesContest_24);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_24, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_24);
    assertTrue(difficultyMatchesMax(difficultyContest_24, assertions, EPS));
  }

  /**
   * Contest 25.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest25() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(25, 25);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_25.contains(retrievedFirstChoice));
  }

  /**
   * Contest 25.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest25() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_25,
        ballotCountContest_25, DEFAULT_TIME_LIMIT, choicesContest_25);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_25, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_25);
    assertTrue(difficultyMatchesMax(difficultyContest_25, assertions, EPS));
  }

  /**
   * Contest 26.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest26() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(26, 26);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_26.contains(retrievedFirstChoice));
  }

  /**
   * Contest 26.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest26() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_26,
        ballotCountContest_26, DEFAULT_TIME_LIMIT, choicesContest_26);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_26, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_26);
    assertTrue(difficultyMatchesMax(difficultyContest_26, assertions, EPS));
  }

  /**
   * Contest 27.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest27() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(27, 27);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_27.contains(retrievedFirstChoice));
  }

  /**
   * Contest 27.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest27() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_27,
        ballotCountContest_27, DEFAULT_TIME_LIMIT, choicesContest_27);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_27, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_27);
    assertTrue(difficultyMatchesMax(difficultyContest_27, assertions, EPS));
  }

  /**
   * Contest 28.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest28() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(28, 28);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_28.contains(retrievedFirstChoice));
  }

  /**
   * Contest 28.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest28() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_28,
        ballotCountContest_28, DEFAULT_TIME_LIMIT, choicesContest_28);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_28, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_28);
    assertTrue(difficultyMatchesMax(difficultyContest_28, assertions, EPS));
  }

  /**
   * Contest 29.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest29() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(29, 29);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_29.contains(retrievedFirstChoice));
  }

  /**
   * Contest 29.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest29() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_29,
        ballotCountContest_29, DEFAULT_TIME_LIMIT, choicesContest_29);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_29, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_29);
    assertTrue(difficultyMatchesMax(difficultyContest_29, assertions, EPS));
  }

  /**
   * Contest 30.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest30() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(30, 30);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_30.contains(retrievedFirstChoice));
  }

  /**
   * Contest 30.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest30() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_30,
        ballotCountContest_30, DEFAULT_TIME_LIMIT, choicesContest_30);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_30, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_30);
    assertTrue(difficultyMatchesMax(difficultyContest_30, assertions, EPS));
  }

  /**
   * Contest 31.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest31() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(31, 31);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_31.contains(retrievedFirstChoice));
  }

  /**
   * Contest 31.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest31() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_31,
        ballotCountContest_31, DEFAULT_TIME_LIMIT, choicesContest_31);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_31, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_31);
    assertTrue(difficultyMatchesMax(difficultyContest_31, assertions, EPS));
  }

  /**
   * Contest 32.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest32() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(32, 32);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_32.contains(retrievedFirstChoice));
  }

  /**
   * Contest 32.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest32() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_32,
        ballotCountContest_32, DEFAULT_TIME_LIMIT, choicesContest_32);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winner.
    assertEquals(winnerContest_32, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_32);
    assertTrue(difficultyMatchesMax(difficultyContest_32, assertions, EPS));
  }

  /**
   * Contest 33.
   * Sanity check to make sure that the first vote's first preference has one of the candidate names
   * we expect for that contest.
   * TODO This test can be removed when the service is implemented and the other tests are passing.
   */
  @Test
  @Transactional
  void firstPreferenceOfFirstVoteHasAnExpectedCandidateName_contest33() {
    List<String[]> retrieved = cvrContestInfoRepository.getCVRs(33, 33);
    String retrievedFirstChoice = retrieved.getFirst()[0];
    assertTrue(choicesContest_33.contains(retrievedFirstChoice));
  }

  /**
   * Contest 33.
   * Generate assertions. Check the winner.
   * Then check the difficulty, to ensure it matches what is generated by raire-java directly (which in
   * turn has been tested against raire-rs).
   * @throws GenerateAssertionsException
   */
  @Test
  @Transactional
  @Disabled
  public void checkDifficulty_contest33() throws GenerateAssertionsException {
    GenerateAssertionsRequest request = new GenerateAssertionsRequest(nameContest_33,
        ballotCountContest_33, DEFAULT_TIME_LIMIT, choicesContest_33);

    // Generate assertions.
    GenerateAssertionsResponse response = generateAssertionsService.generateAssertions(request);

    // Check winners.
    assertEquals(winnerContest_33, response.winner);

    // Check difficulty.
    List<Assertion> assertions = assertionRepository.findByContestName(nameContest_33);
    assertTrue(difficultyMatchesMax(difficultyContest_33, assertions, EPS));
  }
}