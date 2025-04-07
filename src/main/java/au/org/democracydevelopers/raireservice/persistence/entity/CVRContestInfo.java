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

import au.org.democracydevelopers.raireservice.persistence.converters.StringArrayConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.springframework.data.annotation.ReadOnlyProperty;

/**
 * Each CVR contains vote information for a series of contests. A CVRContestInfo will contain
 * the vote information for a specific contest on a specific CVR. For the purpose of
 * assertion-generation and retrieval by RAIRE, we only care about the choice data, contest and
 * county IDs associated with a given entry in the cvr_contest_info table.
 */
@Entity
@Table(name = "cvr_contest_info")
@IdClass(CVRContestInfoId.class)
public class CVRContestInfo {

  /**
   * Unique identifier for a CVRContestInfo instance. It is comprised of a
   * CVR ID and a contest ID. These two together form a unique key.
   */
  @Id
  @Column(name = "cvr_id", updatable = false, insertable = false, nullable = false)
  @ReadOnlyProperty
  private long cvrId;

  /**
   * Unique identifier for a CVRContestInfo instance. It is comprised of a
   * CVR ID and a contest ID. These two together form a unique key.
   */
  @Id
  @Column(name = "contest_id", updatable = false, insertable = false, nullable = false)
  @ReadOnlyProperty
  private long contestId;

  /**
   * Ranked order of choices on the CVR for the relevant contest (in order of most preferred
   * to least). This is stored in a single String in the database (for efficiency), and converted
   * into an array of String for later processing.
   */
  @Column(name="choices", updatable = false, insertable = false, nullable = false)
  @Convert(converter = StringArrayConverter.class)
  @ReadOnlyProperty
  private String[] choices;

  /**
   * ID for the county associated with the contest associated with this CVRContestInfo.
   */
  @Column(name = "county_id", updatable = false, insertable = false, nullable = false)
  @ReadOnlyProperty
  private long countyId;

  /**
   * Get the ID of the county to which the contest belongs.
   * @return the county ID for the contest associated with this CVRContestInfo.
   */
  public long getCountyId(){
    return countyId;
  }

  /**
   * Get the ID of the contest for which this CVRContestInfo is capturing vote data for.
   * @return the ID of the contest associated with this CVRContestInfo.
   */
  public long getContestId(){ return contestId; }

  /**
   * Get the ranked choices associated with this CVRContestInfo.
   * @return the ranked choices, as a list of choice names, for this CVRContestInfo.
   */
  public String[] getChoices(){ return choices; }

  /**
   * Get the ID of the CVR associated with this vote data,
   * @return the ID of the CVR associated with this CVRContestInfo instance.
   */
  public long getCVRId() { return cvrId; }
}
