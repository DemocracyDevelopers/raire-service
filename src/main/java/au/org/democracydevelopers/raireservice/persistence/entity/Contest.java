/*
Copyright 2024 Democracy Developers
The Raire Service is designed to connect colorado-rla and its associated database to
the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).

This file is part of raire-service.
raire-service is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License along with raire-service. If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.persistence.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.ReadOnlyProperty;

/*
 * A contest class used for reading contest data out of the corla database.
 * This class omits the fields that are not relevant to input validation - we only care about
 * checking whether the request we received makes sense for IRV assertion generation or retrieval.
 */
@Entity
@Table(name = "contest")
public class Contest {

  /**
   * CVR Contest Info ID.
   */
  @Id
  @Column(nullable = false, updatable = false)
  @ReadOnlyProperty
  private long id;

  /**
   * The name of the contest
   */
  @ReadOnlyProperty
  @Column(name = "name", nullable = false, updatable = false)
  private String name;

  public String getName() {
    return name;
  }

  /**
   * Description - should be either IRV or PLURALITY
   */
  @ReadOnlyProperty
  @Column(name = "description", nullable = false, updatable = false)
  private String description;

  public String getDescription() {
    return description;
  }

  /**
   * ID of county.
   */
  @ReadOnlyProperty
  @Column(name = "county_id", nullable = false, updatable = false)
  private long countyID;

  public long getCountyID() {
    return countyID;
  }

  /**
   * Version. Used for optimistic locking.
   */
  @ReadOnlyProperty
  @Column(name = "version", nullable = false)
  private long version;

  public long getVersion() {
    return version;
  }
}