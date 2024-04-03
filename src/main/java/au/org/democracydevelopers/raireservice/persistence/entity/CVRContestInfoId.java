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

import java.io.Serializable;

/**
 * The unique identifier for a CVRContestInfo entity is a CVR Id and a contest Id together.
 * This class defines a composite key for the CVRContestInfo entity.
 */
public class CVRContestInfoId implements Serializable {

  /**
   * Id of the CVR to which the CVRContestInfo refers.
   */
  private long cvrId;

  /**
   * Id of the contest whose vote data, for a given CVR, is being captured by the CVRContestInfo.
   */
  private long contestId;
}
