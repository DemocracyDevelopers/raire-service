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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Entity
@Table(name = "contest")
public class Contest {

  /**
   * CVR Contest Info ID.
   */
  @Id
  @Column(updatable = false, nullable = false)
  // @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  /**
   * Description - should be either IRV or PLURALITY
   */
  @Column(name = "description", nullable = false)
  private String description;

  /**
   * The name of the contest
   */
  @Column(name = "name", nullable = false)
  private String name;

  /**
   * ID of contest for which this Assertion was generated.
   */
  @Column(name = "county_id", nullable = false)
  private Long countyID;

  /**
   * Version. Currently not used.
   */
  @Column(name = "version")
  private Long version;

  /**
   * Construct an empty CVRContestInfo (for persistence).
   */
  public Contest(){}
}