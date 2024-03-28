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

package au.org.democracydevelopers.raireservice.request;

/**
 * Exception indicating that a request failed validation. For a ContestRequest (including
 * GenerateAssertionsRequest and GetAssertionRequest) this may be because:
 * - the contest name is blank, or the candidate list is empty,
 * - there is no contest of the requested name in the database,
 * - the contest is not an IRV contest,
 * - one of the numbers in the request (such as a time limit, risk limit, ballot count) is outside
 *   the required range.
 */
public class RequestValidationException extends Exception {

  public RequestValidationException(String s) {
    super(s);
  }
}
