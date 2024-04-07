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

import java.util.Objects;

/**
 * String utilities for testing.
 */
public class StringUtils {

  /**
   * True if str1 contains str2, ignoring case.
   * @param str1 a possible super-string
   * @param str2 a possible sub-string
   * @return true iff str1 contains str2, ignoring case.
   */
  public static boolean containsIgnoreCase(String str1, String str2) {
    return str1.toLowerCase().contains(str2.toLowerCase());
  }
}
