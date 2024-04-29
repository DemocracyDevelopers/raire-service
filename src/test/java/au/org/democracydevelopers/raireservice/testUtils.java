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

import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.math.BigDecimal;
import java.util.List;

public class testUtils {

  /**
   * To facilitate easier checking of retrieved/saved assertion content.
   */
  private static final Gson GSON =
      new GsonBuilder().serializeNulls().disableHtmlEscaping().create();

  /**
   * Utility to check the relevant assertion data values from json.
   * @param margin the expected raw margin
   * @param dilutedMargin the expected diluted margin
   * @param difficulty the expected difficulty
   * @param winner the expected winner
   * @param loser the expected loser
   * @param assertion the assertion to be checked
   * @return true if the assertion's data match all the expected values.
   * TODO check how many dp are serialised for the BigDecimals and refine test string accordingly.
   * We may need something like BigDecimal.isCloseTo.
   */
  public static boolean correctAssertionData(int margin, BigDecimal dilutedMargin, BigDecimal difficulty,
      String winner, String loser, Assertion assertion) {

    String retrievedString = GSON.toJson(assertion);
    JsonObject data = GSON.fromJson(retrievedString, JsonObject.class);

    JsonElement marginElement = data.get("margin");
    JsonElement difficultyElement = data.get("difficulty");
    JsonElement dilutedMarginElement = data.get("dilutedMargin");
    JsonElement loserElement = data.get("loser");
    JsonElement winnerElement = data.get("winner");

    return (margin == GSON.fromJson(marginElement, Integer.class)
        && (difficulty.compareTo(GSON.fromJson(difficultyElement, BigDecimal.class)) == 0)
        && (dilutedMargin.compareTo(GSON.fromJson(dilutedMarginElement, BigDecimal.class)) == 0)
        && loser.equals(GSON.fromJson(loserElement, String.class)))
        && winner.equals(GSON.fromJson(winnerElement, String.class));
  }

  /**
   * Utility to check that the expected assumedContinuing list matches the one in the assertion, ignoring order.
   * @param expectedNames the list of candidate names expected to be in the 'assumed continuing' field.
   * @param assertion the assertion to be checked.
   * @return true if the NEN assertion's 'assumed continuing' list matches expectedNames, ignoring order.
   */
  public static boolean correctAssumedContinuing(List<String> expectedNames, Assertion assertion) {
    String retrievedString = GSON.toJson(assertion);
    JsonObject data = GSON.fromJson(retrievedString, JsonObject.class);
    JsonElement assumedContinuingElement = data.get("assumedContinuing");
    List<String> assertionContinuing = GSON.fromJson(assumedContinuingElement, new TypeToken<List<String>>(){}.getType());
    // First check there are no duplicates (we are assuming there are none in the expected list).
    List<String> assertionListWithoutDuplicates = assertionContinuing.stream().distinct().toList();
    return assertionListWithoutDuplicates.size() == assertionContinuing.size()
        // then check the contents are the same
        && assertionContinuing.size() == expectedNames.size()
        && assertionContinuing.containsAll(expectedNames);
  }
}
