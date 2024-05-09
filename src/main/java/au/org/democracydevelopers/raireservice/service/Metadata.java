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

package au.org.democracydevelopers.raireservice.service;

/**
 * This class defines the names of contest/audit metadata fields. Some of these are used when
 * forming the metadata map passed to raire-java in a generate assertions request, and when
 * forming contest metadata in the construction of a JSON assertion export (for visualisation).
 */
public class Metadata {
  /**
   * Metadata field name for the contest's candidates.
   */
  public final static String CANDIDATES = "candidates";

  /**
   * Metadata field name for the contest's risk limit.
   */
  public final static String RISK_LIMIT = "risk_limit";

  /**
   * Metadata field name for the contest's name.
   */
  public final static String CONTEST = "contest";

  /**
   * Status attribute describing a risk level.
   */
  public static final String STATUS_RISK = "risk";
}
