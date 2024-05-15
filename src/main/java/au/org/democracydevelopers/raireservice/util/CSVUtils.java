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

package au.org.democracydevelopers.raireservice.util;

import java.util.List;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Functions for doing basic string operations useful for turning some data into a row of a csv file.
 * (csv) escape, then join, is used by the get-assertions-csv endpoint.
 *
 */
public class CSVUtils {

  /**
   * Take a list of strings and format them as a csv row, escaping each individually and then
   * joining them all with a comma.
   * @param data The list of strings to be joined
   * @return The same data, as an appropriately-escaped csv row, with each element of the input list
   *         in one cell.
   */
  public static String escapeThenJoin(List<String> data) {
    return String.join(", ", data.stream().map(StringEscapeUtils::escapeCsv).toList());
  }
}
