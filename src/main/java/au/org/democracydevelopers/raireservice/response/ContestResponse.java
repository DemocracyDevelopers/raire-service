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

/**
 * An abstract class designed to deal with all the errors resulting from failures associated
 * with generic contest-name-based requests, i.e. those of a ContestRequest. Examples include
 * invalid or blank contest names, invalid or blank candidates, non-existent contests and non-IRV
 * contests.
 * The non-error responses are dealt with by each appropriate subclass.
 */
public abstract class ContestResponse {

  protected String contestName;
  private Exception e;

  public ContestResponse(String contestName) {
    this.contestName = contestName;
  }
}
