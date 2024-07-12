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

import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCode;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.ReadOnlyProperty;

/**
 * RAIRE (raire-java) generates a set of assertions for a given IRV contest, but it also returns
 * the winner and (possibly) an informative error. These need to be persisted so that IRV reports
 * can access the information. This class simply stores the outcome of a call to the
 * GenerateAssertionsService, one row per contest.
 */
@Entity
@Table(name = "generate_assertions_response_or_error")
public abstract class GenerateAssertionsResponseOrError {

  private static final Logger logger = LoggerFactory.getLogger(GenerateAssertionsResponseOrError.class);

  private static final String ResultOK = "OK";

  /**
   * ID.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ReadOnlyProperty
  private long id;

  /**
   * Version. Used for optimistic locking.
   */
  @Version
  @Column(name = "version", updatable = false, nullable = false)
  @ReadOnlyProperty
  private long version;

  /**
   * Name of the contest.
   */
  @Column(name = "contest_name", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected String contestName;

  /**
   * Name of the winner of the contest, as determined by raire-java.
   */
  @Column(name = "winner", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected String winner;

  /**
   * An error or warning, if there was one, or ResultOK if none. Most errors mean there are no
   * assertions (nor winner), but some warnings (e.g. TIME_OUT_TRIMMING_ASSERTIONS) do have
   * assertions and a winner, and allow the audit to continue.
   */
  @Column(name = "error_or_warning", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected String errorOrWarning;

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
   * @param errorOrWarning the error or warning produced by raire.
   * @throws IllegalArgumentException if the caller supplies an invalid combination of inputs, for
   * example "isOK" with a blank winner, or a winner with an error that prevents finding a winner.
   */
  public GenerateAssertionsResponseOrError(String contestName, String winner, boolean isOK,
                                           RaireErrorCode errorOrWarning) throws IllegalArgumentException
  {
    final String prefix = "[all args constructor]";
    logger.debug(String.format("%s Parameters: contest name %s; winner %s; isOK %s; error/warning %s.",
        prefix, contestName, winner, isOK, errorOrWarning));

    this.contestName = contestName;
    this.winner = winner;
    this.errorOrWarning = isOK ? ResultOK : errorOrWarning.toString();

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