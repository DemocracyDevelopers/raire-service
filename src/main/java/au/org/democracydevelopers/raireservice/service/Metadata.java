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

import java.util.List;

/**
 * This class defines the names of contest/audit metadata fields. Some of these are used when
 * forming the metadata map passed to raire-java in a generate assertions request, and when
 * forming contest metadata in the construction of a JSON assertion export (for visualisation).
 * Some are used when exporting assertions as CSV.
 */
public class Metadata {
  // Used for json export.
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
   * Metadata field name for the contest's total Auditable ballots.
   */
  public final static String TOTAL_BALLOTS = "total_auditable_ballots";

  /**
   * Status attribute describing a risk level.
   */
  public final static String STATUS_RISK = "risk";

  // Used for CSV export
  //
  // The first 6 are the values on which we compute maxima or minima in the csv preface.
  //
  /**
   * The absolute margin
   */
  public final static String MARGIN = "Margin";

  /**
   * The diluted margin, i.e. the absolute margin divided by the total ballots in the universe.
   */
  public final static String DILUTED_MARGIN = "Diluted margin";

  /**
   * The difficulty estimated by raire. This is directly proportional to the initial optimistic
   * sample size.
   */
  public final static String DIFFICULTY = "Raire difficulty";

  /**
   * The current calculated risk, based on the audit ballots observed so far.
   */
  public final static String CURRENT_RISK = "Current risk";

  /**
   * The optimistic samples to audit. Colorado-rla calculates this and we retrieve it from the
   * database.
   */
  public final static String OPTIMISTIC_SAMPLES = "Optimistic samples to audit";


  /**
   * The estimated samples to audit. Colorado-rla calculates this, and we retrieve it from the
   * database.
   */
  public final static String ESTIMATED_SAMPLES = "Estimated samples to audit";

  // Other headers used in parts of the csv
  public final static String CONTEST_NAME_HEADER = "Contest name";
  public final static String CANDIDATES_HEADER = "Candidates";
  public final static String WINNER_HEADER = "Winner";
  public final static String TOTAL_AUDITABLE_BALLOTS_HEADER = "Total universe";
  public final static String RISK_LIMIT_HEADER = "Risk limit";

  public final static List<String> extremumHeaders = List.of("Extreme item","Value","Assertion IDs");

  public final static List<String> csvHeaders = List.of(
     "ID",  // The assertion ID, starting at 1 for each csv file (not the ID in the database).
     "Type", // NEN or NEB.
     "Winner", // The winner of the assertion.
     "Loser", // The loser of the assertion.
     "Assumed continuing", // The set of assumed continuing candidates (NEN only).
     "Difficulty", // The difficulty estimated by raire.
     "Margin", // The absolute margin.
     "Diluted margin", // The diluted margin, i.e. absolute margin divided by universe size.
     "Risk", // The current calculated risk, based on audit ballot observations.
     "Estimated samples to audit", // colorado-rla's estimated samples to audit.
     "Optimistic samples to audit", // colorado-rla's optimistic samples to audit.
     "Two vote over count", // The number of two-vote overcounts for this assertion.
     "One vote over count", // The number of one-vote overcounts for this assertion.
     "Other discrepancy count", // The count of discrepancies that do not affect this assertion's score.
     "One vote under count", // The number of one-vote undercounts for this assertion.
     "Two vote under count"
  ); // The number of two-vote undercounts for this assertion.
}
