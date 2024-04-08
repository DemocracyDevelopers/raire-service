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

import au.org.democracydevelopers.raireservice.request.RequestValidationException;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.List;

@Entity
@DiscriminatorValue("NEN")
public class NENAssertion extends Assertion {

  /**
   * {@inheritDoc}
   */
  public NENAssertion() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  public NENAssertion(String contestName, String winner, String loser, int margin,
      long universeSize, double difficulty, List<String> assumedContinuing)
      throws RequestValidationException
  {
    super(contestName, winner, loser, margin, universeSize, difficulty, assumedContinuing);
  }

}
