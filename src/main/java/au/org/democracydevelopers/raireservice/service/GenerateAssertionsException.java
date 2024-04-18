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
 * Exception indicating that assertion generation failed.
 * These are mostly direct transcriptions of raire-java exceptions, for things such as tied winners
 * or timeout generating assertions.
 */
public class GenerateAssertionsException extends Exception {

  public RaireErrorCodes errorCode;

  /**
   * Main constructor, for translating a RaireError into a GenerateAssertionsException.
   * @param error the RaireError to be expressed as a GenerateAssertionsException.
   */
  public GenerateAssertionsException(RaireError error, List<String> candidates) {
    super(makeMessage(error, candidates));
    switch (error) {
      case TiedWinners e -> this.errorCode = RaireErrorCodes.TIED_WINNERS;
      case TimeoutFindingAssertions e ->
          this.errorCode = RaireErrorCodes.TIMEOUT_FINDING_ASSERTIONS;
      case TimeoutTrimmingAssertions e ->
          this.errorCode = RaireErrorCodes.TIMEOUT_TRIMMING_ASSERTIONS;
      case TimeoutCheckingWinner e -> this.errorCode = RaireErrorCodes.TIMEOUT_CHECKING_WINNER;
      case CouldNotRuleOut e -> this.errorCode = RaireErrorCodes.COULD_NOT_RULE_OUT_ALTERNATIVE;

      // I think this is what we get if the candidate list entered in the request has the
      // right number but wrong names vs the database. It's therefore not (really) an internal error
      // - it's a colorado-rla error.
      // TODO add a test for this case. (See Issue.)
      case InvalidCandidateNumber e -> this.errorCode = RaireErrorCodes.WRONG_CANDIDATE_NAMES;

      // Internal coding errors.
      case InvalidTimeout e -> this.errorCode = RaireErrorCodes.INTERNAL_ERROR;
      case InternalErrorDidntRuleOutLoser e -> this.errorCode = RaireErrorCodes.INTERNAL_ERROR;
      case InternalErrorRuledOutWinner e -> this.errorCode = RaireErrorCodes.INTERNAL_ERROR;
      case InternalErrorTrimming e -> this.errorCode = RaireErrorCodes.INTERNAL_ERROR;
      case InvalidNumberOfCandidates e -> this.errorCode = RaireErrorCodes.INTERNAL_ERROR;
      case WrongWinner e -> this.errorCode = RaireErrorCodes.INTERNAL_ERROR;

      // TODO We shouldn't have generic Raire errors.
      // When the RaireError class is sealed, we'll be able to delete this line.
      case RaireError e -> throw new IllegalStateException("Unexpected value: " + error);
    }
  }

  /**
   * Message-only constructor, for the case where something other than a raire error went wrong
   * during assertion generation, for example a database error.
   *
   * @param message a human-readable message for the exception.
   */
  public GenerateAssertionsException(String message) {
    super(message);
    this.errorCode = RaireErrorCodes.INTERNAL_ERROR;
  }

  public enum RaireErrorCodes {

    // Errors that the user can do something about.

    /**
     * Tied winners - the contest is a tie and therefore cannot be audited.
     */
    TIED_WINNERS,

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
     * TODO check that this does indeed reflect how we get this error.
     */
    WRONG_CANDIDATE_NAMES,

    // Internal errors (that the user can do nothing about)

    /**
     * A catch-all for various kinds of raire errors that indicate a programming error: invalid
     * input errors such as InvalidNumberOfCandidates, InvalidTimeout, InvalidCandidateNumber -
     * these should all be caught before being sent to raire. Also database errors. These are errors
     * that the user can't do anything about.
     */
    INTERNAL_ERROR,
  }

  /**
   * Make a human-readable error message out of the raire error.
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
          message = "Time out trimming assertions - the assertions are usable, but could be reduced given more trimming time.";
      case TimeoutCheckingWinner e ->
          message = "Time out checking winner - the election is either tied or extremely complex.";
      case CouldNotRuleOut e -> {
        List<String> sequenceList = Arrays.stream(e.eliminationOrder).mapToObj(candidates::get).toList();
        String sequence = String.join(", ", sequenceList);
        message = "Could not rule out alternative elimination order: "+sequence+".";
      }

      // I think this is what we get if the candidate list entered in the request has the
      // right number but wrong names vs the database.
      // TODO add a test for this case. (See Issue.)
      case InvalidCandidateNumber e -> message = "Candidate list does not match database.";

      // Internal coding errors.
      default -> message = "Internal error";
    }
    return message;
  }
}