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
public class GenerateAssertionsResponseOrError {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsResponseOrError.class);

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
  @Column(name = "contest_name", updatable = false, nullable = false)
  private String contestName;

  /**
   * Whether assertion generation was successful. If this is true, there should be assertions
   * and a winner; if false, there should be an error.
   */
  @Column(name = "is_ok", updatable = false, nullable = false)
  private boolean isOK;

  /**
   * Name of the winner of the contest, as determined by raire-java.
   */
  @Column(name = "winner", updatable = false, nullable = false)
  private String winner;

  /**
   * An error, if there was one, or ResultOK if none. Errors mean there are no
   * assertions (nor winner), but some warnings (e.g. TIME_OUT_TRIMMING_ASSERTIONS) do have
   * assertions and a winner, and allow the audit to continue.
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
  public GenerateAssertionsResponseOrError() {}


  /**
   * Construct GenerateAssertionsResponseOrError for a specific contest.
   * If isOK is true there must be a non-blank winner. There may also be a warning,
   * e.g. TIME_OUT_TRIMMING_ASSERTIONS.
   * @param contestName Contest for which the Assertion has been created.
   * @param winner Winner of the Assertion (name of a candidate in the contest).
   * @param isOK indication of whether assertion generation was successful.
   * @param error the error produced by raire, if any.
   * @param warning the warning produced by raire, if any.
   * @param message the message associated with the error, if any.
   * @throws IllegalArgumentException if the caller supplies an invalid combination of inputs, for
   * example "isOK" with a blank winner, or a winner with an error that prevents finding a winner.
   */
  public GenerateAssertionsResponseOrError(String contestName, String winner, boolean isOK,
                      String error, String warning, String message) throws IllegalArgumentException
  {
    final String prefix = "[all args constructor]";
    logger.debug(String.format("%s Parameters: contest name %s; winner %s; isOK %s; error %s; warning %s.",
        prefix, contestName, winner, isOK, error, warning));

    this.contestName = contestName;
    this.winner = winner;
    this.error = isOK ? "" : error;
    this.warning = warning;
    this.message = message;

    if(contestName.isBlank()){
      String msg = String.format("%s Attempt to build GenerateAssertionsResponseOrError with blank contest name",
          prefix);
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }

    if(isOK && winner.isBlank()) {
      String msg = String.format("%s Attempt to build GenerateAssertionsResponseOrError with Result OK" +
              "but blank winner name", prefix);
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }

    logger.debug(String.format("%s Construction complete.", prefix));
  }
}