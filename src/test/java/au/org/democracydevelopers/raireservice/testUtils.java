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

import static java.util.Collections.max;

import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.service.Metadata;
import au.org.democracydevelopers.raireservice.util.DoubleComparator;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

/**
 * Utility methods for use in test classes, including some default values for making testing easier.
 */
public class testUtils {

  public final static String baseURL = "http://localhost:";
  public final static String generateAssertionsEndpoint = "/raire/generate-assertions";
  public final static String getAssertionsJSONEndpoint = "/raire/get-assertions-json";
  public final static String getAssertionsCSVEndpoint = "/raire/get-assertions-csv";

  public final static List<String> aliceAndBob = List.of("Alice","Bob");
  public final static List<String> aliceAndBobAndCharlie = List.of("Alice","Bob","Charlie");
  public final static String oneNEBAssertionContest = "One NEB Assertion Contest";
  public final static String oneNENAssertionContest = "One NEN Assertion Contest";
  public final static String oneNEBOneNENAssertionContest = "One NEN NEB Assertion Contest";
  public final static String ballinaMayoral = "Ballina Mayoral";

  /**
   * A default winner, used in requests - not checked.
   */
  public final static String defaultWinner = "A winner";
  public final static String defaultWinnerJSON = "\"winner\":\""+defaultWinner+"\"";

  /**
   * A default ballot count, used in requests - not checked.
   */
  public final static int defaultCount = 100;
  public final static String defaultCountJson = "\"totalAuditableBallots\":"+defaultCount;

  /**
   * Comparator for doubles within a specific tolerance.
   */
  private static final DoubleComparator doubleComparator = new DoubleComparator();

  /**
   * Print log statement indicating that a specific test has started running.
   */
  public static void log(Logger logger, String test){
    logger.debug(String.format("RUNNING TEST: %s.",test));
  }

  /**
   * Utility to check that the response to a get assertions request contains the right metadata. Use
   * this one for API tests, where the value has been serialized & deserialized.
   *
   * @param candidates  the expected list of candidate names
   * @param contestName the expected contest name
   * @param riskLimit   the expected risk limit
   * @param metadata    the metadata from the response, in which the riskLimit is interpreted as a
   *                    double by the deserializer.
   * @param riskLimitClass the class in which the risk limit is expressed. Use BigDecimal for things
   *                       derived directly from the service, double for values that have been
   *                       serialized and deserialized via the API.
   * @return true if the response's metadata fields match the candidates, contestname and riskLimit.
   */
  public static boolean correctMetadata(List<String> candidates, String contestName,
      BigDecimal riskLimit, Map<String, Object> metadata, Type riskLimitClass) throws ClassCastException {

    BigDecimal retrievedRiskLimit;
    if(riskLimitClass == Double.class) {
      retrievedRiskLimit = BigDecimal.valueOf((double) metadata.get(Metadata.RISK_LIMIT));
    } else if (riskLimitClass == BigDecimal.class) {
      retrievedRiskLimit = (BigDecimal) metadata.get(Metadata.RISK_LIMIT);
    } else {
      // We can only deal with doubles and BigDecimals.
      return false;
    }

    String retrievedContestName = metadata.get(Metadata.CONTEST).toString();
    List<String> retrievedCandidates = (List<String>) metadata.get(Metadata.CANDIDATES);

    return contestName.equals(retrievedContestName)
        && riskLimit.compareTo(retrievedRiskLimit) == 0
        && setsNoDupesEqual(candidates, retrievedCandidates);
  }

  /**
   * Check that the RaireResult's solution has the expected margin and difficulty.
   * @param margin expected margin
   * @param difficulty expected difficulty
   * @param numAssertions the expected number of assertions
   * @param result the RaireResult in the body of the response
   * @return true if the result's data matches the expected values.
   */
  public static boolean correctSolutionData(int margin, double difficulty, int numAssertions,
      RaireResult result) {

    int retrievedMargin = result.margin;
    double retrievedDifficulty = result.difficulty;
    return retrievedMargin == margin
        && doubleComparator.compare(retrievedDifficulty, difficulty) == 0
        && result.assertions.length == numAssertions;
  }

  /**
   * Utility to check the relevant assertion attributes against expected values.
   * @param margin the expected raw margin
   * @param dilutedMargin the expected diluted margin - optional
   * @param difficulty the expected difficulty
   * @param winner the expected winner
   * @param loser the expected loser
   * @param assertion the assertion to be checked (either as an Assertion or as json)
   * @param assumedContinuing the list of candidate names expected to be in the
   *                          'assumed continuing' field.
   * @return true if the assertion's data match all the expected values.
   */
  public static boolean correctDBAssertionData(int margin, double dilutedMargin, double difficulty,
      String winner, String loser, List<String> assumedContinuing, Assertion assertion) {

    return margin == assertion.getMargin()
        && doubleComparator.compare(difficulty, assertion.getDifficulty()) == 0
        && doubleComparator.compare(dilutedMargin, assertion.getDilutedMargin()) == 0
        && loser.equals(assertion.getLoser())
        && winner.equals(assertion.getWinner())
        && setsNoDupesEqual(assertion.getAssumedContinuing(), assumedContinuing);
  }

  /**
   * Utility to check the relevant assertion attributes against expected values.
   * @param type the type ("NEN" or "NEB")
   * @param margin the expected raw margin
   * @param difficulty the expected difficulty
   * @param winner the expected winner
   * @param loser the expected loser
   * @param assertionAndDifficulty the (raire-java) assertionAndDifficulty to be checked
   * @param assumedContinuing the list of candidate names expected to be in the
   *                          'assumed continuing' field.
   * @return true if the assertion's type and data match all the expected values.
   */
  public static boolean correctAssertionData(String type, int margin, double difficulty, int winner,
      int loser, List<Integer> assumedContinuing, double risk,
      au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty assertionAndDifficulty) {

    // Check for the right margin, difficulty and risk
    boolean rightMarginAndDifficulty =
        assertionAndDifficulty.margin == margin
        && (doubleComparator.compare(assertionAndDifficulty.difficulty, difficulty)==0)
        && (doubleComparator.compare( (double) assertionAndDifficulty.status.get("risk"), risk) == 0);

    if(assertionAndDifficulty.assertion instanceof NotEliminatedNext nen) {
      // If it's an NEN assertion, check that that's the expected type, and that all the other data
      // match
      List<Integer> nenAssumedContinuing = Arrays.stream(nen.continuing).boxed().toList();
      return type.equals("NEN")
          && nen.winner == winner
          && nen.loser == loser
          && setsNoDupesEqual(nenAssumedContinuing, assumedContinuing)
          && rightMarginAndDifficulty;

    } else if(assertionAndDifficulty.assertion instanceof NotEliminatedBefore neb) {
      // If it's an NEB assertion, check that that's the expected type, that the assumedContinuing
      // list is empty, and that all the other data match.
      return type.equals("NEB")
          && neb.winner == winner
          && neb.loser == loser
          && assumedContinuing.isEmpty()
          && rightMarginAndDifficulty;
    }

    // Not an instance of a type we recognise.
    return false;
  }

  /**
   * Returns true if the attributes of the given assertion are equal to those provided as input
   * to this method.
   * @param id Expected assertion id.
   * @param margin Expected assertion (raw) margin.
   * @param dilutedMargin Expected assertion diluted margin.
   * @param difficulty Expected assertion difficulty.
   * @param winner Expected assertion winner.
   * @param loser Expected assertion loser.
   * @param assumedContinuing Expected assumed continuing candidates.
   * @param cvrDiscrepancies Expected map of CVR id to assertion discrepancies.
   * @param estimatedSamplesToAudit Expected number of estimated samples to audit.
   * @param optimisticSamplesToAudit Expected number of optimistic samples to audit.
   * @param twoVoteUnderCount Expected number of two vote understatements.
   * @param oneVoteUnderCount Expected number of one vote understatements.
   * @param oneVoteOverCount Expected number of one vote overstatements.
   * @param twoVoteOverCount Expected number of two vote overstatements.
   * @param otherCount Expected number of 'other' discrepancies.
   * @param currentRisk Expected current risk.
   * @param contestName Expected name of the assertion's contest.
   * @param assertion Assertion to be checked.
   * @return True if the given assertion's attributes are as expected.
   */
  public static boolean correctDBAssertionData(long id, int margin, double dilutedMargin,
      double difficulty, String winner, String loser, List<String> assumedContinuing,
      Map<Long,Integer> cvrDiscrepancies, int estimatedSamplesToAudit, int optimisticSamplesToAudit,
      int twoVoteUnderCount, int oneVoteUnderCount, int oneVoteOverCount, int twoVoteOverCount,
      int otherCount, BigDecimal currentRisk, String contestName, Assertion assertion){

    boolean test = correctDBAssertionData(margin, dilutedMargin, difficulty, winner,
        loser, assumedContinuing, assertion);

    return test && assertion.getEstimatedSamplesToAudit() == estimatedSamplesToAudit &&
        assertion.getOptimisticSamplesToAudit() == optimisticSamplesToAudit &&
        assertion.getOneVoteUnderCount() == oneVoteUnderCount &&
        assertion.getOneVoteOverCount() == oneVoteOverCount &&
        assertion.getTwoVoteUnderCount() == twoVoteUnderCount &&
        assertion.getTwoVoteOverCount() == twoVoteOverCount &&
        assertion.getOtherCount() == otherCount &&
        assertion.getCurrentRisk().compareTo(currentRisk) == 0 &&
        assertion.getCvrDiscrepancy().equals(cvrDiscrepancies) &&
        assertion.getId() == id &&
        assertion.getContestName().equals(contestName);
  }

  /**
   * Check that the max difficulty of a list of assertions matches the expected difficulty.
   * @param expectedDifficulty the expected difficulty, generated by raire-java and raire-rs directly.
   * @param assertions the assertions to be tested
   * @return true iff the maximum difficulty among the assertions equals expectedDifficulty.
   */
  public static boolean difficultyMatchesMax(double expectedDifficulty, List<Assertion> assertions) {
    double assertionDifficultyMax = max(assertions.stream().map(Assertion::getDifficulty).toList());
    return doubleComparator.compare(assertionDifficultyMax, expectedDifficulty) == 0;
  }

  /**
   * Checks whether two lists of strings have no duplicates and the same contents, ignoring order.
   * @param list1 a list of things of type T
   * @param list2 a list of things of type T
   * @return true iff the two lists contain no duplicates and the same set of items, ignoring order.
   */
  private static <T> boolean setsNoDupesEqual(List<T> list1, List<T> list2) {
    List<T> list1WithoutDuplicates = list1.stream().distinct().toList();
    List<T> list2WithoutDuplicates = list2.stream().distinct().toList();
        // list1 has no duplicates
    return list1WithoutDuplicates.size() == list1.size()
        // list2 has no duplicates
        && list2WithoutDuplicates.size() == list2.size()
        // they are the same size
        && list1.size() == list2.size()
        // and they have the same contents.
        && list1.containsAll(list2);
  }
}
