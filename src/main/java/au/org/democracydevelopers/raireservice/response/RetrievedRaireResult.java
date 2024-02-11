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

import au.org.democracydevelopers.raire.assertions.*;

import java.beans.ConstructorProperties;

/**
 * Summary of the main result of Raire. Contains all the info from raire/algorithm/RaireResult
 * that we store in (and hence can retrieve from) the database. That is, all that's useful for assessing the
 * assertions, without the instant feedback about how the algorithm went (which is used when assertions are first
 * generated, not when they're retrieved from the database).
 *
 * Note this does _not_ include the winner, which is part of the human verification process.
 */
public class RetrievedRaireResult {
    public AssertionAndDifficulty[] assertions;
    public double difficulty;
    public int margin; // The smallest margin in votes in one of the assertions. Provided primarily for informational purposes.
    public int num_candidates;

    /**
     * Used for building JSON objects when retrieving from the database
     */
    @ConstructorProperties({"assertions", "difficulty", "margin", "num_candidates"})
    public RetrievedRaireResult(AssertionAndDifficulty[] assertions, double difficulty, int margin, int num_candidates) {
        this.assertions = assertions;
        this.difficulty = difficulty;
        this.margin = margin;
        this.num_candidates = num_candidates;
    }
}
