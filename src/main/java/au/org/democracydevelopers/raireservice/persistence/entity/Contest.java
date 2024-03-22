/*
Copyright 2024 Democracy Developers
The Raire Service is designed to connect colorado-rla and its associated database to
the raire assertion generation engine (https://github.com/DemocracyDevelopers/raire-java).

This file is part of raire-service.
raire-service is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
raire-service is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License along with ConcreteSTV. If not, see <https://www.gnu.org/licenses/>.
*/

package au.org.democracydevelopers.raireservice.persistence.entity;

import jakarta.persistence.*;

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
  @Column(updatable = false, nullable = false)
  // Generation is only used for testing.
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  /**
   * Description - should be either IRV or PLURALITY
   */
  @Column(name = "description", nullable = false)
  public String description;

  /**
   * The name of the contest
   */
  @Column(name = "name", nullable = false)
  public String name;

  /**
   * ID of contest for which this Assertion was generated.
   */
  @Column(name = "county_id", nullable = false)
  public Long countyID;

  /**
   * Version. Currently not used.
   */
  @Column(name = "version")
  public Long version;

  /**
   * Construct an empty CVRContestInfo (for persistence).
   */
  public Contest(){}

  /**
   * Construct a CVRContestInfo with specific data (used only for testing).
   */
  public Contest(String description, String name, Long countyID, Long version) {
    if(description == null || name == null ) {
      throw new RuntimeException("Contest initialized with null values: "
          + "description = "+description+" name = "+name);
    }

    this.description = description;
    this.name = name;
    this.countyID = countyID;
    this.version = version;
  }
}