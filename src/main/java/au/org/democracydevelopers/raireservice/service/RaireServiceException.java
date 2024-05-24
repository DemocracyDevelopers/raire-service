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

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raire.RaireError.CouldNotRuleOut;
import au.org.democracydevelopers.raire.RaireError.InternalErrorDidntRuleOutLoser;
import au.org.democracydevelopers.raire.RaireError.InternalErrorRuledOutWinner;
import au.org.democracydevelopers.raire.RaireError.InternalErrorTrimming;
import au.org.democracydevelopers.raire.RaireError.InvalidCandidateNumber;
import au.org.democracydevelopers.raire.RaireError.InvalidNumberOfCandidates;
import au.org.democracydevelopers.raire.RaireError.InvalidTimeout;
import au.org.democracydevelopers.raire.RaireError.TiedWinners;
import au.org.democracydevelopers.raire.RaireError.TimeoutCheckingWinner;
import au.org.democracydevelopers.raire.RaireError.TimeoutFindingAssertions;
import au.org.democracydevelopers.raire.RaireError.TimeoutTrimmingAssertions;
import au.org.democracydevelopers.raire.RaireError.WrongWinner;
import java.util.Arrays;
import java.util.List;

/**
 * Exception indicating that assertion generation or retrieval failed.
 * These are mostly direct transcriptions of raire-java exceptions, for things such as tied winners
 * or timeout generating assertions.
 */
public class RaireServiceException extends Exception {

  /**
   * The string "error_code" - used for retrieving it from json etc.
   */
  public static String ERROR_CODE_KEY = "error_code";

  /**
   * The error code - an enum used to describe what went wrong. Returned in the http response for
   * colorado-rla to interpret for the user.
   */
  public RaireErrorCode errorCode;

  /**
   * Main constructor, for translating a RaireError into a RaireServiceException.
   * @param error the RaireError to be expressed as a RaireServiceException.
   */
  public RaireServiceException(RaireError error, List<String> candidates) {
    super(makeMessage(error, candidates));
    switch (error) {
      case TiedWinners e -> this.errorCode = RaireErrorCode.TIED_WINNERS;

      case TimeoutFindingAssertions e ->
          this.errorCode = RaireErrorCode.TIMEOUT_FINDING_ASSERTIONS;

      case TimeoutTrimmingAssertions e ->
          this.errorCode = RaireErrorCode.TIMEOUT_TRIMMING_ASSERTIONS;

      case TimeoutCheckingWinner e -> this.errorCode = RaireErrorCode.TIMEOUT_CHECKING_WINNER;

      case CouldNotRuleOut e -> this.errorCode = RaireErrorCode.COULD_NOT_RULE_OUT_ALTERNATIVE;

      // This is what we get if the candidate list entered in the request has the
      // right number but wrong names vs the database.
      case InvalidCandidateNumber e -> this.errorCode = RaireErrorCode.WRONG_CANDIDATE_NAMES;

      // Internal coding errors.
      case InvalidTimeout e -> this.errorCode = RaireErrorCode.INTERNAL_ERROR;

      case InternalErrorDidntRuleOutLoser e -> this.errorCode = RaireErrorCode.INTERNAL_ERROR;

      case InternalErrorRuledOutWinner e -> this.errorCode = RaireErrorCode.INTERNAL_ERROR;

      case InternalErrorTrimming e -> this.errorCode = RaireErrorCode.INTERNAL_ERROR;

      case InvalidNumberOfCandidates e -> this.errorCode = RaireErrorCode.INTERNAL_ERROR;

      case WrongWinner e -> this.errorCode = RaireErrorCode.INTERNAL_ERROR;
    }
  }

  /**
   * Message-only constructor, for the case where something other than a raire-java error went wrong
   * during assertion generation, for example a database error.
   *
   * @param message a human-readable message for the exception.
   * @param code a RaireErrorCode code to indicate the type of error that has arisen.
   */
  public RaireServiceException(String message, RaireErrorCode code) {
    super(message);
    this.errorCode = code;
  }

  /**
   * Error codes describing what went wrong, for returning via http to colorado-rla.
   */
  public enum RaireErrorCode {

    // Errors that the user can do something about.

    /**
     * Tied winners - the contest is a tie and therefore cannot be audited.
     */
    TIED_WINNERS,

    /**
     * The total number of auditable ballots for a contest is less than the number of CVRs in
     * the database that contain the contest.
     */
    INVALID_TOTAL_AUDITABLE_BALLOTS,

    /**
     * Time out checking winners - can happen if the contest is tied, or if it is complicated and
     * can't be distinguished from a tie.
     */
    TIMEOUT_CHECKING_WINNER,

    /**
     * Raire timed out trying to find assertions. It may succeed if given more time.
     */
    TIMEOUT_FINDING_ASSERTIONS,

    /**
     * Raire timed out trimming assertions. The assertions have been generated, but the audit could
     * be more efficient if the trimming step is re-run with more time allowed.
     */
    TIMEOUT_TRIMMING_ASSERTIONS,

    /**
     * RAIRE couldn't rule out some alternative winner.
     */
    COULD_NOT_RULE_OUT_ALTERNATIVE,

    /**
     * The list of candidate names in the request didn't match the database.
     */
    WRONG_CANDIDATE_NAMES,

    /**
     * The user has request to retrieve assertions for a contest for which no assertions have
     * been generated.
     */
    NO_ASSERTIONS_PRESENT,

    // Internal errors (that the user can do nothing about)

    /**
     * A catch-all for various kinds of errors that indicate a programming error: invalid
     * input errors such as InvalidNumberOfCandidates, InvalidTimeout, InvalidCandidateNumber -
     * these should all be caught before being sent to raire-java. Also database errors.
     * These are errors that the user can't do anything about.
     */
    INTERNAL_ERROR,
  }

  /**
   * Make a human-readable error message out of a raire-java error.
   *
   * @param error      the RaireError to be explained
   * @param candidates the list of candidate names as strings
   * @return a human-readable error message to be returned through the API.
   */
  private static String makeMessage(RaireError error, List<String> candidates) {

    String message;
    switch (error) {
      case TiedWinners e -> {
        List<String> tiedWinnersList = Arrays.stream(e.expected).mapToObj(candidates::get).toList();
        String tiedWinners = String.join(", ", tiedWinnersList);
        message = "Tied winners: " + tiedWinners + ".";
      }

      case TimeoutFindingAssertions e ->
          message = "Time out finding assertions - try again with longer timeout.";

      case TimeoutTrimmingAssertions e ->
          message = "Time out trimming assertions - the assertions are usable, but could be " +
              "reduced given more trimming time.";

      case TimeoutCheckingWinner e ->
          message = "Time out checking winner - the election is either tied or extremely complex.";

      case CouldNotRuleOut e -> {
        List<String> sequenceList = Arrays.stream(e.eliminationOrder).mapToObj(candidates::get).toList();
        String sequence = String.join(", ", sequenceList);
        message = "Could not rule out alternative elimination order: "+sequence+".";
      }

      // This is what we get if the candidate list entered in the request has the
      // right number but wrong names versus the database.
      case InvalidCandidateNumber e -> message = "Candidate list does not match database.";

      // Internal coding errors.
      default -> message = "Internal error";
    }
    return message;
  }
}