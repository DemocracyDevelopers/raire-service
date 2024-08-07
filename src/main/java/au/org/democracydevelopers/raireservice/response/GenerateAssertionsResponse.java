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

import au.org.democracydevelopers.raire.RaireError;

import java.beans.ConstructorProperties;

/**
 * The response to a ContestRequest, describing the status of assertion generation.
 * All four states of the two booleans are possible - for example, generation may succeed, but
 * receive a TIME_OUT_TRIMMING_ASSERTIONS warning, in which case retry will be true.
 * @param contestName The name of the contest.
 * @param succeeded   Whether assertion generation succeeded.
 * @param retry       Whether it is worth retrying assertion generation.
 */
public record GenerateAssertionsResponse(String contestName, boolean succeeded, boolean retry) {

  /**
   * All args constructor, for deserialization.
   * @param contestName The name of the contest.
   * @param succeeded   Whether assertion generation succeeded.
   * @param retry       Whether it is worth retrying assertion generation.
   */
  @ConstructorProperties({"contestName", "succeeded", "retry"})
  public GenerateAssertionsResponse {
  }

  /**
   * Failure. If the error is one of the timeouts, it is worth retrying.
   * @param contestName The name of the contest.
   * @param error       The raire error, used to determine whether retry should be recommended.
   * Retry is set to true if the error is one of the timeouts, otherwise false.
   */
  public GenerateAssertionsResponse(String contestName, RaireError error)  {
      this(contestName, false,
          error instanceof RaireError.TimeoutCheckingWinner
          || error instanceof RaireError.TimeoutFindingAssertions
          || error instanceof RaireError.TimeoutTrimmingAssertions
          );
  }

  // Success. Retry only if the TIME_OUT_TRIMMING_ASSERTIONS flag is true.
  public GenerateAssertionsResponse(String contestName, boolean timeOutTrimmingAssertions) {

    // If time out trimming assertions is true, then retry should be true.
    this(contestName, true, timeOutTrimmingAssertions);
  }
}