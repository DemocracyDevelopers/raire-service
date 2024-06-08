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

package au.org.democracydevelopers.raireservice.response;

import java.beans.ConstructorProperties;

/**
 * The success response to a ContestRequest. This simply returns the winner, as
 * calculated by raire, along with the name of the contest for which the initial request was made.
 *
 * @param contestName The name of the contest.
 * @param winner      The winner of the contest, as calculated by raire.
 */
public record GenerateAssertionsResponse(String contestName, String winner) {

  /**
   * All args constructor.
   *
   * @param contestName the name of the contest.
   * @param winner      the name of the winner.
   */
  @ConstructorProperties({"contestName", "winner"})
  public GenerateAssertionsResponse {
  }
}