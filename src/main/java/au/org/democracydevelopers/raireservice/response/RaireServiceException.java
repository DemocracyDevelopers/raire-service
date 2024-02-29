/*
  Copyright 2024 Democracy Developers

  This file is part of raire-service.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raireservice.response;

/** Exceptions the RAIRE service may produce. The real detail is in the RaireServiceError class. */
public class RaireServiceException extends Exception {
    public final RaireServiceError error;
    public RaireServiceException(RaireServiceError error) {
        this.error = error;
    }
}


