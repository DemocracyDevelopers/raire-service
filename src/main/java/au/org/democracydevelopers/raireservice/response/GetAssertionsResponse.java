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

import au.org.democracydevelopers.raire.RaireSolution;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.beans.ConstructorProperties;
import java.util.Map;

public class GetAssertionsResponse {
    public final Map<String,Object> metadata;
    public final GetAssertionResultOrError solution;

    @ConstructorProperties({"metadata","solution"})
    public GetAssertionsResponse(Map<String, Object> metadata, GetAssertionResultOrError solutionOrError) {
        this.metadata = metadata;
        this.solution = solutionOrError;
    }


    /// A wrapper around the raire-java Error type. Exactly one of the fields will be null.
    public static class GetAssertionResultOrError {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final RetrievedRaireResult Ok;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final RaireServiceError Err;

        /** Only used by the Jackson serialization which can only have one constructor annotated :-( */
        @ConstructorProperties({"Ok","Err"})
        public GetAssertionResultOrError(RetrievedRaireResult Ok, RaireServiceError Err) { this.Ok=Ok; this.Err=Err;}
        public GetAssertionResultOrError(RetrievedRaireResult Ok) { this.Ok=Ok; this.Err=null;}
        public GetAssertionResultOrError(RaireServiceError Err) { this.Ok=null; this.Err=Err;}

        public GetAssertionResultOrError(RaireSolution.RaireResultOrError solution) {
            if (solution.Ok != null) {
                Ok = new RetrievedRaireResult(solution.Ok.assertions, solution.Ok.difficulty,
                        solution.Ok.margin, solution.Ok.num_candidates);
                Err = null;
            } else {
                // FIXME At the moment, this is not properly translating the Raire Error.
                assert (solution.Err != null);
                Err = new RaireServiceError.ErrorRetrievingAssertions();
                Ok = null;
            }
        }
    }

}
