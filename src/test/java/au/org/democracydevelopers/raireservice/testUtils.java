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

import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

public class testUtils {

  /**
   * To facilitate easier checking of retrieved/saved assertion content.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  /**
   * Print log statement indicating that we have started to run the given test.
   */
  public static void log(Logger logger, String test){
    logger.debug(String.format("RUNNING TEST %s", test));
  }

  /**
   * Utility to check that the API json response to a get assertions request contains the right metadata.
   * @param candidates the expected list of candidate names
   * @param contestName the expected contest name
   * @param riskLimit the expected risk limit
   * @param APIResponseBody the body of the response
   * @param eps margin of error for floating-point comparisons
   * @return true if the response json contains a 'metadata' field, with fields matching the candidates,
   * contestname and riskLimit.
   */
  public static boolean correctMetadata(List<String> candidates, String contestName, double riskLimit,
      String APIResponseBody, double eps) {
    JsonObject data =  GSON.fromJson(APIResponseBody, JsonObject.class);
    JsonObject metadataElement = data.get("metadata").getAsJsonObject();

    String retrievedContestName = metadataElement.get("contest").getAsString();
    double retrievedRiskLimit = metadataElement.get("risk_limit").getAsDouble();
    JsonArray retrievedCandidatesJson = metadataElement.getAsJsonArray("candidates");
    List<String> retrievedCandidates = new ArrayList<>();
    for (JsonElement jsonElement : retrievedCandidatesJson) {
      retrievedCandidates.add(jsonElement.getAsString());
    }

    return contestName.equals(retrievedContestName)
        && Math.abs(riskLimit - retrievedRiskLimit) < eps
        && setsNoDupesEqual(candidates, retrievedCandidates);
  }

  /**
   * Check that the APIResponseBody's solution has the expected margin and difficulty.
   * @param margin expected margin
   * @param difficulty expected difficulty
   * @param APIResponseBody the body of the response
   * @param eps margin of error for floating-point comparisons
   * @return true if the APIResponseBody's data matches the expected values.
   */
  public static boolean correctSolutionData(int margin, double difficulty, int numAssertions,
      String APIResponseBody, double eps) {
    JsonObject data =  GSON.fromJson(APIResponseBody, JsonObject.class);
    JsonObject solutionElement = (JsonObject) data.get("solution").getAsJsonObject().get("Ok");
    int retrievedMargin = solutionElement.get("margin").getAsInt();
    double retrievedDifficulty = solutionElement.get("difficulty").getAsDouble();
    JsonArray assertions = solutionElement.getAsJsonArray("assertions");
    return retrievedMargin == margin
        && Math.abs(retrievedDifficulty - difficulty) < eps
        && assertions.size() == numAssertions;
  }

  /**
   * Utility to check the relevant assertion data values, from an API response.
   * @param type either NEN or NEB
   * @param margin the expected raw margin
   * @param difficulty the expected difficulty
   * @param winner the expected winner, as an int
   * @param loser the expected loser, as an int
   * @param assumedContinuing the list of indices of assumed-continuing candidates (for NEN assertions)
   * @param APIResponseBody the response body to be tested
   * @param index the index of the assertion to be checked, in the assertion array.
   * @return true if the assertion's data match all the expected values. Candidate indices are compared
   * as strings.
   */
  public static boolean correctIndexedAPIAssertionData(String type, int margin, double difficulty,
      int winner, int loser, List<Integer> assumedContinuing, double risk, String APIResponseBody,
      int index, double eps) {
    JsonObject data =  GSON.fromJson(APIResponseBody, JsonObject.class);
    String assertion = data.get("solution").getAsJsonObject().get("Ok").getAsJsonObject()
        .get("assertions").getAsJsonArray().get(index).getAsJsonObject().toString();
    return correctAPIAssertionData(type, margin, difficulty, winner, loser, assumedContinuing, risk,
        assertion, eps);
  }

  /**
   * Check that the data in an assertion expressed as json (of the form exported in the
   * get-assertions API) matches the data entered as function parameters.
   * @param type the assertion type (NEB or NEN).
   * @param margin the absolute margin
   * @param difficulty raire's estimated difficulty
   * @param winner the assertion winner's index in the candidate list
   * @param loser the assertion loser's index in the candidate list
   * @param assumedContinuing if NEB, blank; if NEN, the list of indices of the candidates assumed
   *                          to be continuing.
   * @param risk the current risk estimate.
   * @param assertionAsJson the assertion to be tested, as a json string.
   * @param eps the error tolerance for floating-point comparisons.
   * @return true if the input data matches the data extracted from the assertion as json.
   */
  public static boolean correctAPIAssertionData(String type, int margin, double difficulty,
      int winner, int loser, List<Integer> assumedContinuing, double risk, String assertionAsJson,
      double eps) {
    // It makes no sense to call this with NEB and a nonempty assumedContinuing.
    assert (type.equals("NEN") || assumedContinuing.isEmpty());

    JsonObject data = GSON.fromJson(assertionAsJson, JsonObject.class);
    JsonObject assertionData = data.get("assertion").getAsJsonObject();

    int retrievedMargin = data.get("margin").getAsInt();
    double difficultyElement = data.get("difficulty").getAsDouble();
    String retrievedType = assertionData.get("type").getAsString();
    int retrievedLoser = assertionData.get("loser").getAsInt();
    int retrievedWinner = assertionData.get("winner").getAsInt();
    double retrievedRisk = data.get("status").getAsJsonObject().get("risk").getAsDouble();

    boolean assumedContinuingRight;
    if(retrievedType.equals("NEN")) {
      // type is NEN. Get 'assumed continuing' and compare it with expected.
      assert (assertionData.has("continuing"));
      JsonArray retrievedAssumedContinuingData
          = assertionData.getAsJsonArray("continuing").getAsJsonArray();
      List<Integer> retrievedContinuing = new ArrayList<>();
      for (JsonElement jsonElement : retrievedAssumedContinuingData) {
        retrievedContinuing.add(jsonElement.getAsInt());
      }
      assumedContinuingRight = setsNoDupesEqual(assumedContinuing, retrievedContinuing);
    } else { // type is NEB. There should be no 'assumed continuing'.
      assumedContinuingRight = !assertionData.has("continuing");
    }

    return type.equals(retrievedType)
        && margin == retrievedMargin
        && Math.abs(difficulty - difficultyElement) < eps
        && loser == retrievedLoser
        && winner == retrievedWinner
        && Math.abs(risk - retrievedRisk) < eps
        && assumedContinuingRight;
  }


  /**
   * Utility to check the relevant assertion data values from json.
   * @param margin the expected raw margin
   * @param dilutedMargin the expected diluted margin - optional
   * @param difficulty the expected difficulty
   * @param winner the expected winner
   * @param loser the expected loser
   * @param assertion the assertion to be checked (either as an Assertion or as json)
   * @return true if the assertion's data match all the expected values.
   */
  public static boolean correctDBAssertionData(int margin, double dilutedMargin, double difficulty,
      String winner, String loser, Assertion assertion, double eps) {
    String assertionAsJson = GSON.toJson(assertion);
    JsonObject data = GSON.fromJson(assertionAsJson, JsonObject.class);

    int retrievedMargin = data.get("margin").getAsInt();
    double retrievedDilutedMargin = data.get("dilutedMargin").getAsDouble();
    double retrievedDifficulty = data.get("difficulty").getAsDouble();
    String retrievedLoser = data.get("loser").getAsString();
    String retrievedWinner = data.get("winner").getAsString();

    return margin == retrievedMargin
        && Math.abs(difficulty - retrievedDifficulty) < eps
        && Math.abs(dilutedMargin - retrievedDilutedMargin) < eps
        && loser.equals(retrievedLoser)
        && winner.equals(retrievedWinner);
  }


  /**
   * Utility to check that the expected assumedContinuing list matches the one in the assertion,
   * ignoring order.
   * @param expectedNames the list of candidate names expected to be in the 'assumed continuing' field.
   * @param assertion the assertion to be checked.
   * @return true if the NEN assertion's 'assumed continuing' list matches expectedNames, ignoring order.
   */
  public static boolean correctAssumedContinuing(List<String> expectedNames, Assertion assertion) {
    String retrievedString = GSON.toJson(assertion);
    JsonObject data = GSON.fromJson(retrievedString, JsonObject.class);
    JsonElement assumedContinuingElement = data.get("assumedContinuing");
    List<String> assertionContinuing
        = GSON.fromJson(assumedContinuingElement, new TypeToken<List<String>>(){}.getType());
    return setsNoDupesEqual(assertionContinuing, expectedNames);
  }

  /**
   * Check that the max difficulty of a list of assertions matches the expected difficulty.
   * @param expectedDifficulty the expected difficulty, generated by raire-java and raire-rs directly.
   * @param assertions the assertions to be tested
   * @param eps the margin of error allowed for equality-comparison.
   * @return true iff the maximum difficulty among the assertions equals expectedDifficulty.
   */
  public static boolean difficultyMatchesMax(double expectedDifficulty, List<Assertion> assertions,
      double eps) {
    double assertionDifficultyMax = max(assertions.stream().map(Assertion::getDifficulty).toList());
    return Math.abs(assertionDifficultyMax - expectedDifficulty) < eps;
  }

  /**
   * Checks whether two lists of strings have no duplicates and the same contents, ignoring order.
   * @param strings1 a list of strings
   * @param strings2 a list of strings
   * @return true iff the two lists contain no duplicates and the same list of strings, ignoring order.
   */
  private static <T> boolean setsNoDupesEqual(List<T> strings1, List<T> strings2) {
    List<T> strings1WithoutDuplicates = strings1.stream().distinct().toList();
    List<T> strings2WithoutDuplicates = strings2.stream().distinct().toList();
        // strings1 has no duplicates
    return strings1WithoutDuplicates.size() == strings1.size()
        // strings2 has no duplicates
        && strings2WithoutDuplicates.size() == strings2.size()
        // they are the same size
        && strings1.size() == strings2.size()
        // and they have the same contents.
        && strings1.containsAll(strings2);
  }
}
