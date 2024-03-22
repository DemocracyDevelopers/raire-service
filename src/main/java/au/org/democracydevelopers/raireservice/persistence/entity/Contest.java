/*
 * Sketch of abstract Assertion class (following conventions of other CORLA classes).
 *
 * Will need to implement PersistentEntity and Serializable interfaces, as shown.
 *
 * JPA hibernate annotations are speculative.
 */

/* This is very similar to the Assertion.java class in colorado-rla, with the Springboot upgrades copied from
 * CDOS's SampleSizeDemoApplication.
 * Function calls are not needed and have been removed.
 */

package au.org.democracydevelopers.raireservice.persistence.entity;

import jakarta.persistence.*;
// import lombok.Data;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Data
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
   * Construct a CVRContestInfo with specific data (for testing).
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