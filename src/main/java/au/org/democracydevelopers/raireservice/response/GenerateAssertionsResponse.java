/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raireservice.response;

import au.org.democracydevelopers.raire.RaireError;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.beans.ConstructorProperties;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateAssertionsResponse {
    public final String contestName;
    public final GenerateAssertionsResultOrError response;

    @ConstructorProperties({"contestName","response"})
    public GenerateAssertionsResponse(String contestName, GenerateAssertionsResultOrError winnerOrError) {
        this.contestName = contestName;
        this.response = winnerOrError;
    }

    /**
     * Generates a new GenerateAssertionsResponse that appropriately interprets the RAIRE error.
     * The tied winners indicate an inherent problem with the election.
     * TimeoutFindingAssertions and TimeoutTrimmingAssertions both indicate that raire ran out of time.
     * The other kinds of errors should not happen.
     *
     * See Design doc V1.1, Sec 4.1.2.
     *
     * @param candidates The list of candidate names.
     * @param err        The RAIRE error to be interpreted.
     */
    public GenerateAssertionsResponse(String contestName, String[] candidates, RaireError err) {
        this.contestName = contestName;
        GenerateAssertionsResultOrError internalError = new GenerateAssertionsResultOrError(new RaireServiceError.InternalError());
        GenerateAssertionsResultOrError couldNotAnalyzeError = new GenerateAssertionsResultOrError(new RaireServiceError.CouldNotAnalyzeElection());

        switch (err) {
            // Tied candidates - convert their indices to names (strings)
            case RaireError.TiedWinners e ->  {
                List<String> tiedCandidateNames = Arrays.stream(e.expected).mapToObj(i -> candidates[i]).toList();
                this.response = new GenerateAssertionsResultOrError(new RaireServiceError.TiedWinners(tiedCandidateNames));
            }

            // Time out finding assertions - return difficulty at time of stopping.
            case RaireError.TimeoutFindingAssertions e -> this.response
                    = new GenerateAssertionsResultOrError(new RaireServiceError.TimeoutFindingAssertions(e.difficultyAtTimeOfStopping));

            // Time out trimming - return as is.
            case RaireError.TimeoutTrimmingAssertions e -> this.response
                    = new GenerateAssertionsResultOrError(new RaireServiceError.TimeoutTrimmingAssertions());

            // These errors are indications of a weird and complex election. It's unclear that either of these
            // ever happen unless there's a tie, but it's possible that it might happen in very strange elections
            // that are almost tied, or are actually tied but are so complicated that comprehensively analyzing the
            // tie is not feasible.
            case RaireError.TimeoutCheckingWinner            e -> this.response = couldNotAnalyzeError;
            case RaireError.CouldNotRuleOut                  e -> this.response = couldNotAnalyzeError;

            // These errors shouldn't happen - they indicate either that raire-service sent the wrong information to
            // raire-java, or that raire-java had an internal error.
            // In the case of Invalid timeout, we should catch it and return an error before we send it to RAIRE.
            // Similarly, an InvalidNumberOfCandidates should never happen because a request with empty candidate list
            // should be rejected.
            case RaireError.InternalErrorDidntRuleOutLoser   e -> this.response = internalError;
            case RaireError.InternalErrorRuledOutWinner      e -> this.response = internalError;
            case RaireError.InternalErrorTrimming            e -> this.response = internalError;
            case RaireError.InvalidTimeout                   e -> this.response = internalError;
            case RaireError.InvalidCandidateNumber           e -> this.response = internalError;
            case RaireError.InvalidNumberOfCandidates        e -> this.response = internalError;
            case RaireError.WrongWinner                      e -> this.response = internalError;
            case RaireError                                  e -> this.response = internalError;
            // default ->  this.response = new GenerateAssertionsResultOrError(new RaireServiceError.PlaceholderError());
        }
    }


    /// A wrapper around the raire-java Error type. Exactly one of the fields will be null.
    // Also incorporates errors from the service, e.g. malformed input.
    public static class GenerateAssertionsResultOrError {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final String Ok;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final RaireServiceError Err;

        /** Only used by the Jackson serialization which can only have one constructor annotated :-( */
        @ConstructorProperties({"Ok","Err"})
        public GenerateAssertionsResultOrError(String Ok, RaireServiceError Err) { this.Ok=Ok; this.Err=Err;}
        public GenerateAssertionsResultOrError(String Ok) { this.Ok=Ok; this.Err=null;}
        public GenerateAssertionsResultOrError(RaireServiceError Err) { this.Ok=null; this.Err=Err;}

    }

}
