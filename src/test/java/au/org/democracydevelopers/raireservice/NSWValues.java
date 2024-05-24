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

package au.org.democracydevelopers.raireservice;

import java.util.Arrays;
import java.util.List;

/**
 * This class summarises the contest names, candidate choices, expected winners and estimated
 * difficulties for all NSW 2021 IRV contests.
 * Difficulties are taken from raire-java::src/test/java/au/org/democracydevelopers/raire/TestNSW
 * which in turn tests against raire-rs.
 * Winners are taken from the New South Wales official election results at
 * https://pastvtr.elections.nsw.gov.au/LG2101/status/mayoral
 * The ballotCounts are derived from the data, but double-checked for exact match with the
 * NSWEC website.
 *
 */
public class NSWValues {

  // Contest Eurobodalla Mayoral
  public static final String nameContest_1 = "Eurobodalla Mayoral";
  public static final List<String> choicesContest_1 = List.of("WORTHINGTON Alison","GRACE David",
      "SMITH Gary","HATCHER Mat","HARRISON N (Tubby)","POLLOCK Rob","STARMER Karyn");
  public static final int BallotCount_1 = 25526;
  public static final double difficulty_1 = 23.079566003616637;
  public static final String winnerContest_1 = "HATCHER Mat";

  // Contest City of Lake Macquarie Mayoral
  public static final String nameContest_2 = "City of Lake Macquarie Mayoral";
  public static final List<String> choicesContest_2 = List.of("FRASER Kay","DAWSON Rosmairi",
      "CUBIS Luke","PAULING Jason");
  public static final int BallotCount_2 = 130336;
  public static final double difficulty_2 = 3.1113869658629745;
  public static final String winnerContest_2 = "FRASER Kay";

  // Contest City of Coffs Harbour Mayoral
  public static final String nameContest_3 = "City of Coffs Harbour Mayoral";
  public static final List<String> choicesContest_3 = List.of("SWAN Tegan","CECATO George",
      "ADENDORFF Michael","JUDGE Tony","PRYCE Rodger","PIKE Donna","AMOS Paul","TOWNLEY Sally",
      "ARKAN John","CASSELL Jonathan");
  public static final int BallotCount_3 = 45155;
  public static final double difficulty_3 = 8.571564160971906;
  public static final String winnerContest_3 = "AMOS Paul";

  // Contest Singleton Mayoral
  public static final String nameContest_4 = "Singleton Mayoral";
  public static final List<String> choicesContest_4 = List.of("MOORE Sue","THOMPSON Danny",
      "JARRETT Tony","CHARLTON Belinda");
  public static final int BallotCount_4 = 13755;
  public static final double difficulty_4 = 12.118942731277533;
  public static final String winnerContest_4 = "MOORE Sue";

  // Contest City of Newcastle Mayoral
  public static final String nameContest_5 = "City of Newcastle Mayoral";
  public static final List<String> choicesContest_5 = List.of("CHURCH John","NELMES Nuatali",
      "HOLDING Rod","MACKENZIE John","O'BRIEN Steve","BARRIE Jenny");
  public static final int BallotCount_5 = 100275;
  public static final double difficulty_5 = 5.913487055493307;
  public static final String winnerContest_5 = "NELMES Nuatali";

  // Contest Nambucca Valley Mayoral
  public static final String nameContest_6 = "Nambucca Valley Mayoral";
  public static final List<String> choicesContest_6 = List.of("JENVEY Susan","HOBAN Rhonda");
  public static final int BallotCount_6 = 12482;
  public static final double difficulty_6 = 2.7360806663743973;
  public static final String winnerContest_6 = "HOBAN Rhonda";

  // Contest City of Maitland Mayoral
  public static final String nameContest_7 = "City of Maitland Mayoral";
  public static final List<String> choicesContest_7 = List.of("BROWN John","MITCHELL Ben",
      "BAKER Loretta","PENFOLD Philip","SAFFARI Shahriar (Sean)","COOPER Michael","BURKE Brian");
  public static final int BallotCount_7 = 54181;
  public static final double difficulty_7 = 47.072980017376196;
  public static final String winnerContest_7 = "PENFOLD Philip";

  // Contest Kempsey Mayoral
  public static final String nameContest_8 = "Kempsey Mayoral";
  public static final List<String> choicesContest_8 = List.of("HAUVILLE Leo","EVANS Andrew",
      "BAIN Arthur","CAMPBELL Liz","SAUL Dean","IRWIN Troy","RAEBURN Bruce");
  public static final int BallotCount_8 = 17585;
  public static final double difficulty_8 = 45.43927648578811;
  public static final String winnerContest_8 = "HAUVILLE Leo";

  // Contest Canada Bay Mayoral
  public static final String nameContest_9 = "Canada Bay Mayoral";
  public static final List<String> choicesContest_9 = List.of("TSIREKAS Angelo","LITTLE Julia",
      "MEGNA Michael","JAGO Charles","RAMONDINO Daniela");
  public static final int BallotCount_9 = 48542;
  public static final double difficulty_9 = 8.140533288613113;
  public static final String winnerContest_9 = "TSIREKAS Angelo";

  // Contest Richmond Valley Mayoral
  public static final String nameContest_10 = "Richmond Valley Mayoral";
  public static final List<String> choicesContest_10 = List.of("MUSTOW Robert","HAYES Robert");
  public static final int BallotCount_10 = 13405;
  public static final double difficulty_10 = 2.302868922865487;
  public static final String winnerContest_10 = "MUSTOW Robert";

  // Contest City of Sydney Mayoral
  public static final String nameContest_11 = "City of Sydney Mayoral";
  public static final List<String> choicesContest_11 = List.of("VITHOULKAS Angela",
      "WELDON Yvonne","SCOTT Linda","JARRETT Shauna","ELLSMORE Sylvie","MOORE Clover");
  public static final int BallotCount_11 = 118511;
  public static final double difficulty_11 = 3.6873366521468576;
  public static final String winnerContest_11 = "MOORE Clover";

  // Contest Byron Mayoral
  public static final String nameContest_12 = "Byron Mayoral";
  public static final List<String> choicesContest_12 = List.of("HUNTER Alan","CLARKE Bruce",
      "COOREY Cate","ANDERSON John","MCILRATH Christopher","LYON Michael","DEY Duncan",
      "PUGH Asren","SWIVEL Mark");
  public static final int BallotCount_12 = 18165;
  public static final double difficulty_12 = 17.13679245283019;
  public static final String winnerContest_12 = "LYON Michael";

  // Contest City of Broken Hill Mayoral
  public static final String nameContest_13 = "City of Broken Hill Mayoral";
  public static final List<String> choicesContest_13 = List.of("TURLEY Darriea","KENNEDY Tom",
      "GALLAGHER Dave");
  public static final int BallotCount_13 = 10812;
  public static final double difficulty_13 = 3.2773567747802366;
  public static final String winnerContest_13 = "KENNEDY Tom";

  // Contest City of Shellharbour Mayoral
  public static final String nameContest_14 = "City of Shellharbour Mayoral";
  public static final List<String> choicesContest_14 = List.of("HOMER Chris","SALIBA Marianne");
  public static final int BallotCount_14 = 46273;
  public static final double difficulty_14 = 17.83159922928709;
  public static final String winnerContest_14 = "HOMER Chris";

  // Contest City of Shoalhaven Mayoral
  public static final String nameContest_15 = "City of Shoalhaven Mayoral";
  public static final List<String> choicesContest_15 = List.of("GREEN Paul","KITCHENER Mark",
      "WHITE Patricia","WATSON Greg","DIGIGLIO Nina","FINDLEY Amanda");
  public static final int BallotCount_15 = 67030;
  public static final double difficulty_15 = 41.53035935563817;
  public static final String winnerContest_15 = "FINDLEY Amanda";

  // Contest Mosman Mayoral
  public static final String nameContest_16 = "Mosman Mayoral";
  public static final List<String> choicesContest_16 = List.of("MOLINE Libby","BENDALL Roy",
      "HARDING Sarah","CORRIGAN Carolyn","MENZIES Simon");
  public static final int BallotCount_16 = 16425;
  public static final double difficulty_16 = 4.498767460969598;
  public static final String winnerContest_16 = "CORRIGAN Carolyn";

  // Contest City of Orange Mayoral
  public static final String nameContest_17 = "City of Orange Mayoral";
  public static final List<String> choicesContest_17 = List.of("HAMLING Jason","SPALDING Amanda",
      "JONES Neil","WHITTON Jeffery","DUFFY Kevin","SMITH Lesley","MILETO Tony");
  public static final int BallotCount_17 = 24355;
  public static final double difficulty_17 = 50.01026694045174;
  public static final String winnerContest_17 = "HAMLING Jason";

  // Contest City of Wollongong Mayoral
  public static final String nameContest_18 = "City of Wollongong Mayoral";
  public static final List<String> choicesContest_18 = List.of("GLYKIS Marie","DORAHY John",
      "BROWN Tania","BRADBERY Gordon","ANTHONY Andrew","COX Mithra");
  public static final int BallotCount_18 = 127240;
  public static final double difficulty_18 = 47.72693173293323;
  public static final String winnerContest_18 = "BRADBERY Gordon";

  // Contest Port Stephens Mayoral
  public static final String nameContest_19 = "Port Stephens Mayoral";
  public static final List<String> choicesContest_19 = List.of("ANDERSON Leah","PALMER Ryan");
  public static final int BallotCount_19 = 47807;
  public static final double difficulty_19 = 84.31569664902999;
  public static final String winnerContest_19 = "PALMER Ryan";

  // Contest Wollondilly Mayoral
  public static final String nameContest_20 = "Wollondilly Mayoral";
  public static final List<String> choicesContest_20 = List.of("KHAN Robert","BANASIK Michael",
      "DEETH Matthew","LAW Ray","GOULD Matt","HANNAN Judy");
  public static final int BallotCount_20 = 31355;
  public static final double difficulty_20 = 24.40077821011673;
  public static final String winnerContest_20 = "GOULD Matt";

  // Contest Hornsby Mayoral
  public static final String nameContest_21 = "Hornsby Mayoral";
  public static final List<String> choicesContest_21 = List.of("HEYDE Emma","RUDDOCK Philip");
  public static final int BallotCount_21 = 85656;
  public static final double difficulty_21 = 6.866762866762866;
  public static final String winnerContest_21 = "RUDDOCK Philip";

  // Contest Ballina Mayoral
  public static final String nameContest_22 = "Ballina Mayoral";
  public static final List<String> choicesContest_22 = List.of("WILLIAMS Keith","JOHNSON Jeff",
      "MCCARTHY Steve","JOHNSTON Eoin","CADWALLADER Sharon");
  public static final int BallotCount_22 = 26913;
  public static final double difficulty_22 = 7.285598267460747;
  public static final String winnerContest_22 = "CADWALLADER Sharon";

  // Contest Bellingen Mayoral
  public static final String nameContest_23 = "Bellingen Mayoral";
  public static final List<String> choicesContest_23 = List.of("ALLAN Steve","WOODWARD Andrew",
      "KING Dominic");
  public static final int BallotCount_23 = 8374;
  public static final double difficulty_23 = 3.3335987261146496;
  public static final String winnerContest_23 = "ALLAN Steve";

  // Contest City of Lismore Mayoral
  public static final String nameContest_24 = "City of Lismore Mayoral";
  public static final List<String> choicesContest_24 = List.of("KRIEG Steve","COOK Darlene",
      "HEALEY Patrick","GRINDON-EKINS Vanessa","ROB Big","BIRD Elly");
  public static final int BallotCount_24 = 26474;
  public static final double difficulty_24 = 2.929836210712705;
  public static final String winnerContest_24 = "KRIEG Steve";

  // Contest City of Willoughby Mayoral
  public static final String nameContest_25 = "City of Willoughby Mayoral";
  public static final List<String> choicesContest_25 = List.of("ROZOS Angelo","CAMPBELL Craig",
      "TAYLOR Tanya");
  public static final int BallotCount_25 = 37942;
  public static final double difficulty_25 = 14.990912682734097;
  public static final String winnerContest_25 = "TAYLOR Tanya";

  // Contest The Hills Shire Mayoral
  public static final String nameContest_26 = "The Hills Shire Mayoral";
  public static final List<String> choicesContest_26 = List.of("SHAHAMAT Vida","GANGEMI Peter",
      "ROZYCKI Jerzy (George)","TRACEY Ryan","YAZDANI Ereboni (Alexia)");
  public static final int BallotCount_26 = 105384;
  public static final double difficulty_26 = 3.6801229221958374;
  public static final String winnerContest_26 = "GANGEMI Peter";

  // Contest City of Cessnock Mayoral
  public static final String nameContest_27 = "City of Cessnock Mayoral";
  public static final List<String> choicesContest_27 = List.of("MURRAY Janet","SUVAAL Jay",
      "MOORES John","OLSEN Ian");
  public static final int BallotCount_27 = 36497;
  public static final double difficulty_27 = 6.466513111268604;
  public static final String winnerContest_27 = "SUVAAL Jay";

  // Contest City of Griffith Mayoral
  public static final String nameContest_28 = "City of Griffith Mayoral";
  public static final List<String> choicesContest_28 = List.of("MERCURI Rina","NAPOLI Anne",
      "ZAPPACOSTA Dino","LA ROCCA Mariacarmina (Carmel)","CURRAN Doug");
  public static final int BallotCount_28 = 14179;
  public static final double difficulty_28 = 3.9320576816417083;
  public static final String winnerContest_28 = "CURRAN Doug";

  // Contest Port Macquarie-Hastings Mayoral
  public static final String nameContest_29 = "Port Macquarie-Hastings Mayoral";
  public static final List<String> choicesContest_29 = List.of("PINSON Peta","GATES Steven",
      "SHEPPARD Rachel","INTEMANN Lisa","LIPOVAC Nik");
  public static final int BallotCount_29 = 54499;
  public static final double difficulty_29 = 2.8524547262640008;
  public static final String winnerContest_29 = "PINSON Peta";

  // Contest City of Liverpool Mayoral
  public static final String nameContest_30 = "City of Liverpool Mayoral";
  public static final List<String> choicesContest_30 = List.of("HAGARTY Nathan","MORSHED Asm",
      "ANDJELKOVIC Milomir (Michael)","HARLE Peter","MANNOUN Ned");
  public static final int BallotCount_30 = 115177;
  public static final double difficulty_30 = 45.416798107255524;
  public static final String winnerContest_30 = "MANNOUN Ned";

  // Contest Uralla Mayoral
  public static final String nameContest_31 = "Uralla Mayoral";
  public static final List<String> choicesContest_31 = List.of("BELL Robert","LEDGER Natasha",
      "STRUTT Isabel");
  public static final int BallotCount_31 = 3781;
  public static final double difficulty_31 = 1.6297413793103448;
  public static final String winnerContest_31 = "BELL Robert";

  // Contest Hunter's Hill Mayoral
  public static final String nameContest_32 = "Hunter's Hill Mayoral";
  public static final List<String> choicesContest_32 = List.of("GUAZZAROTTO David","MILES Zac",
      "QUINN Richard","WILLIAMS Ross");
  public static final int BallotCount_32 = 8356;
  public static final double difficulty_32 = 38.330275229357795;
  public static final String winnerContest_32 = "MILES Zac";

  // Contest Burwood Mayoral
  public static final String nameContest_33 = "Burwood Mayoral";
  public static final List<String> choicesContest_33 = List.of("HULL David","MURRAY Alan",
      "CUTCHER Ned","FAKER John");
  public static final int BallotCount_33 = 17797;
  public static final double difficulty_33 = 2.5269061479483175;
  public static final String winnerContest_33 = "FAKER John";

  public static List<Expected> expectedSolutionData = Arrays.asList(
      new Expected(nameContest_1, choicesContest_1, BallotCount_1, difficulty_1, winnerContest_1),
      new Expected(nameContest_2, choicesContest_2, BallotCount_2, difficulty_2, winnerContest_2),
      new Expected(nameContest_3, choicesContest_3, BallotCount_3, difficulty_3, winnerContest_3),
      new Expected(nameContest_4, choicesContest_4, BallotCount_4, difficulty_4, winnerContest_4),
      new Expected(nameContest_5, choicesContest_5, BallotCount_5, difficulty_5, winnerContest_5),
      new Expected(nameContest_6, choicesContest_6, BallotCount_6, difficulty_6, winnerContest_6),
      new Expected(nameContest_7, choicesContest_7, BallotCount_7, difficulty_7, winnerContest_7),
      new Expected(nameContest_8, choicesContest_8, BallotCount_8, difficulty_8, winnerContest_8),
      new Expected(nameContest_9, choicesContest_9, BallotCount_9, difficulty_9, winnerContest_9),
      new Expected(nameContest_10, choicesContest_10, BallotCount_10, difficulty_10, winnerContest_10),
      new Expected(nameContest_11, choicesContest_11, BallotCount_11, difficulty_11, winnerContest_11),
      new Expected(nameContest_12, choicesContest_12, BallotCount_12, difficulty_12, winnerContest_12),
      new Expected(nameContest_13, choicesContest_13, BallotCount_13, difficulty_13, winnerContest_13),
      new Expected(nameContest_14, choicesContest_14, BallotCount_14, difficulty_14, winnerContest_14),
      new Expected(nameContest_15, choicesContest_15, BallotCount_15, difficulty_15, winnerContest_15),
      new Expected(nameContest_16, choicesContest_16, BallotCount_16, difficulty_16, winnerContest_16),
      new Expected(nameContest_17, choicesContest_17, BallotCount_17, difficulty_17, winnerContest_17),
      new Expected(nameContest_18, choicesContest_18, BallotCount_18, difficulty_18, winnerContest_18),
      new Expected(nameContest_19, choicesContest_19, BallotCount_19, difficulty_19, winnerContest_19),
      new Expected(nameContest_20, choicesContest_20, BallotCount_20, difficulty_20, winnerContest_20),
      new Expected(nameContest_21, choicesContest_21, BallotCount_21, difficulty_21, winnerContest_21),
      new Expected(nameContest_22, choicesContest_22, BallotCount_22, difficulty_22, winnerContest_22),
      new Expected(nameContest_23, choicesContest_23, BallotCount_23, difficulty_23, winnerContest_23),
      new Expected(nameContest_24, choicesContest_24, BallotCount_24, difficulty_24, winnerContest_24),
      new Expected(nameContest_25, choicesContest_25, BallotCount_25, difficulty_25, winnerContest_25),
      new Expected(nameContest_26, choicesContest_26, BallotCount_26, difficulty_26, winnerContest_26),
      new Expected(nameContest_27, choicesContest_27, BallotCount_27, difficulty_27, winnerContest_27),
      new Expected(nameContest_28, choicesContest_28, BallotCount_28, difficulty_28, winnerContest_28),
      new Expected(nameContest_29, choicesContest_29, BallotCount_29, difficulty_29, winnerContest_29),
      new Expected(nameContest_30, choicesContest_30, BallotCount_30, difficulty_30, winnerContest_30),
      new Expected(nameContest_31, choicesContest_31, BallotCount_31, difficulty_31, winnerContest_31),
      new Expected(nameContest_32, choicesContest_32, BallotCount_32, difficulty_32, winnerContest_32),
      new Expected(nameContest_33, choicesContest_33, BallotCount_33, difficulty_33, winnerContest_33)
  );

  /**
   * Collected data about a contest that raire has solved
   * @param contestName the name of the contest
   * @param choices the candidate names
   * @param ballotCount the total auditable ballots
   * @param difficulty the expected difficulty
   * @param winner the expected winner
   */
  public record Expected (
      String contestName,
      List<String> choices,
      int ballotCount,
      double difficulty,
      String winner
  ) {}
}
