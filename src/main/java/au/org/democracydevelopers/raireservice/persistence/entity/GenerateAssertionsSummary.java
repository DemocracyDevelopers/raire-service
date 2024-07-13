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

package au.org.democracydevelopers.raireservice.persistence.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RAIRE (raire-java) generates a set of assertions for a given IRV contest, but it also returns
 * the winner and (possibly) an informative error. These need to be persisted so that IRV reports
 * can access the information. This class simply stores the outcome of a call to the
 * GenerateAssertionsService, one row per contest.
 */
@Entity
@Table(name = "generate_assertions_response_or_error")
public class GenerateAssertionsSummary {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsSummary.class);

  /**
   * Default winner to be used in the case where winner is unknown.
   */
  public static final String UNKNOWN_WINNER = "Unknown";

  /**
   * ID.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  /**
   * Version. Used for optimistic locking.
   */
  @Version
  @Column(name = "version", updatable = false, nullable = false)
  private long version;

  /**
   * Name of the contest.
   */
  @Column(name = "contest_name", unique = true, updatable = false, nullable = false)
  private String contestName;

  /**
   * Name of the winner of the contest, as determined by raire-java.
   */
  @Column(name = "winner", updatable = false, nullable = false)
  private String winner;

  /**
   * An error, if there was one, or emptystring if none. Errors mean there are no
   * assertions (nor winner).
   */
  @Column(name = "error", updatable = false, nullable = false)
  private String error;

  /**
   * A warning, if there was one, or emptystring if none. Warnings (e.g. TIME_OUT_TRIMMING_ASSERTIONS)
   * mean that assertion generation succeeded and the audit can continue, but re-running with longer
   * time allowed might be beneficial.
   */
  @Column(name = "warning", updatable = false, nullable = false)
  private String warning;

  /**
   * The message associated with the error or warning, for example the names of the tied winners.
   */
  @Column(name = "message", updatable = false, nullable = false)
  private String message;

  /**
   * Default no-args constructor (required for persistence).
   */
  public GenerateAssertionsSummary() {}


  /**
   * Construct GenerateAssertionsResponseOrError for a specific contest.
   * There may also be a warning, e.g. TIME_OUT_TRIMMING_ASSERTIONS.
   * @param contestName Contest for which the Assertion has been created.
   * @param winner Winner of the Assertion (name of a candidate in the contest).
   * @param error the error produced by raire, if any.
   * @param warning the warning produced by raire, if any.
   * @param message the message associated with the error, if any.
   * @throws IllegalArgumentException if the caller supplies an invalid combination of inputs, for
   * example a blank error with a blank winner.
   */
  public GenerateAssertionsSummary(String contestName, String winner,
                                   String error, String warning, String message) throws IllegalArgumentException
  {
    final String prefix = "[all args constructor]";
    logger.debug(String.format("%s Parameters: contest name %s; winner %s; error %s; warning %s.",
        prefix, contestName, winner, error, warning));

    if(contestName.isBlank()){
      String msg = String.format("%s Attempt to build GenerateAssertionsResponseOrError with blank contest name",
          prefix);
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }

    this.contestName = contestName;

    update(winner, error, warning, message);

    logger.debug(String.format("%s Construction complete.", prefix));
  }

  /**
   * Update all the data except the contestName. Intended for initialization, and for subsequent
   * runs of Generate Assertions, for the same contest.
   * @param winner  the winner's name, as computed by raire.
   * @param error   the error, if any.
   * @param warning the warning, if any.
   * @param message the message associated with the error (e.g. the names of tied winners).
   * @throws IllegalArgumentException if the data is inconsistent, i.e. both an error and a winner,
   * or neither.
   */
  public void update(String winner, String error, String warning, String message)
                                                                  throws IllegalArgumentException {
    final String prefix = "[update]";

    // There should be either a winner or an error.
    if(error.isBlank() && winner.isBlank()) {
      String msg = String.format("%s Attempt to build or update GenerateAssertionsResponseOrError " +
          "with blank error and blank winner name", prefix);
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }

    // There should not be both a winner and an error. (It's OK to have a winner and a warning.)
    if(!error.isBlank() && !winner.isBlank()) {
      String msg = String.format("%s Attempt to build or update GenerateAssertionsResponseOrError " +
          "with error and winner name", prefix);
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }

    this.winner = winner.isBlank() ? UNKNOWN_WINNER : winner;
    this.error = error;
    this.warning = warning;
    this.message = message;
  }

  /**
   * Whether all the data match, as strings, except for 'message' which only has to match a substring.
   * Used for testing.
   * @param contestName      the expected name of the contest.
   * @param winner           the expected winner's name.
   * @param error            the expected error, if any.
   * @param warning          the expected warning, if any.
   * @param messageSubstring a string expected to be a substring of the error message.
   * @return true if contestname, winner, error and warning all match, and messageSubstring is
   * a substring of message.
   */
  public boolean equalData(String contestName, String winner,
                           String error, String warning, String messageSubstring) {
    return this.contestName.equals(contestName)
        && this.winner.equals(winner)
        && this.error.equals(error)
        && this.warning.equals(warning)
        && StringUtils.containsIgnoreCase(this.message, messageSubstring);
  }
}