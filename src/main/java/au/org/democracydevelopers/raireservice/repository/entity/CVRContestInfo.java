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

package au.org.democracydevelopers.raireservice.repository.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Entity
@Table(name = "cvr_contest_info")
public abstract class CVRContestInfo {

  /**
   * CVR Contest Info ID.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  /**
   * ID of contest for which this Assertion was generated.
   */
  @Column(name = "contest_id", nullable = false)
  private Long contestID;

  /**
   * ID of contest for which this Assertion was generated.
   */
  @Column(name = "county_id", nullable = false)
  private Long countyID;

  /**
   * The choices for this contest.
   */
  @Column(name = "choices", columnDefinition = "character varying (1024)")
  private String my_choices;

  /**
   * Construct an empty CVRContestInfo (for persistence).
   */
  public CVRContestInfo(){}
}
