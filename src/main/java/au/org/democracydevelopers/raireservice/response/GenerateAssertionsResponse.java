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
import java.util.Map;

public class GenerateAssertionsResponse {
    public final String contestName;
    public final GenerateAssertionsResultOrError response;

    @ConstructorProperties({"contestName","response"})
    public GenerateAssertionsResponse(String contestName, GenerateAssertionsResultOrError winnerOrError) {
        this.contestName = contestName;
        this.response = winnerOrError;
    }

    /**
     * Generates a new GenerateAssertionsError that appropriately interprets the RAIRE error.
     * TODO Add further intelligent handling of more errors.
     * @param err the RAIRE error to be interpreted.
     */
    public GenerateAssertionsResponse(String contestName, RaireError err) {
        this.contestName = contestName;
        if(err instanceof RaireError.TiedWinners) {
            this.response = new GenerateAssertionsResultOrError(new GenerateAssertionsError.TiedWinners());
        } else {
            this.response = new GenerateAssertionsResultOrError(new GenerateAssertionsError.PlaceholderError());
        }
    }


    /// A wrapper around the raire-java Error type. Exactly one of the fields will be null.
    // Also incorporates errors from the service, e.g. malformed input.
    public static class GenerateAssertionsResultOrError {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final String Ok;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final GenerateAssertionsError Err;

        /** Only used by the Jackson serialization which can only have one constructor annotated :-( */
        @ConstructorProperties({"Ok","Err"})
        public GenerateAssertionsResultOrError(String Ok, GenerateAssertionsError Err) { this.Ok=Ok; this.Err=Err;}
        public GenerateAssertionsResultOrError(String Ok) { this.Ok=Ok; this.Err=null;}
        public GenerateAssertionsResultOrError(GenerateAssertionsError Err) { this.Ok=null; this.Err=Err;}

    }

}
