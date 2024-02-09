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

import au.org.democracydevelopers.raireservice.repository.converters.LongIntegerMapConverter;
import au.org.democracydevelopers.raireservice.response.GetAssertionException;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@Slf4j
@Data
@Entity
@Table(name = "assertion")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "assertion_type")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "@type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NEBAssertion.class, name = "NEB"),
        @JsonSubTypes.Type(value = NENAssertion.class, name = "NEN")
})
public abstract class Assertion {

  /**
   * Assertion ID.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  /**
   * The version (for optimistic locking).
   */
  @Version
  private Long version;

  /**
   * Name of contest for which this Assertion was generated.
   */
  @Column(name = "contest_name", nullable = false)
  protected String contestName;

  /**
   * Winner of the assertion (a candidate in the contest).
   */
  @Column(name = "winner", nullable = false)
  protected String winner;

  /**
   * Loser of the assertion (a candidate in the contest).
   */
  @Column(name = "loser", nullable = false)
  protected String loser;

  /**
   * Assertion margin.
   */
  @Column(name = "margin", nullable = false)
  protected int margin;

  /**
   * Diluted margin for the assertion.
   */
  @Column(name = "diluted_margin", nullable = false)
  protected double dilutedMargin = 0;

  /**
   * Assertion difficulty, as estimated by RAIRE.
   */
  @Column(name = "difficulty", nullable = false)
  protected double difficulty = 0;

  /**
   * List of candidates that the assertion assumes are `continuing' in the
   * assertion's context.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "assertion_context",
          joinColumns = @JoinColumn(name = "id"))
  protected List<String> assumedContinuing = new ArrayList<>();

  /**
   * The number of samples to audit overall assuming no further overstatements.
   */
  @Column(nullable = false, name = "optimistic_samples_to_audit")
  private Integer my_optimistic_samples_to_audit = 0;

  /**
   * Map between CVR ID and the discrepancy calculated for it (and its A-CVR) in the context
   * of this assertion, based on the last call to computeDiscrepancy(). Calls to computeDiscrepancy()
   * will update this map. Two options for how we persist this data. We can use existing
   * functionality in colorado-rla (the LongIntegerMapConverter class). This will add a column to
   * the assertion table. The second option is to create a new table: "assertion_cvr_discrepancy" which
   * will have columns "id,crv_id,discrepancy", where "id" corresponds to this Assertion's ID, "cvr_id"
   * to the ID of the CVR that is involved in the discrepancy, and "discrepancy" the value of the
   * discrepancy from -2 to 2.
   */
  @Convert(converter = LongIntegerMapConverter.class)
  @Column(columnDefinition = "text")
  protected Map<Long,Integer> cvrDiscrepancy = new HashMap<>();

  /**
   * The expected number of samples to audit overall assuming overstatements
   * continue at the current rate.
   */
  @Column(nullable = false, name = "estimated_samples_to_audit")
  private Integer my_estimated_samples_to_audit = 0;

  /**
   * The two-vote understatements recorded so far.
   */
  @Column(nullable = false, name = "two_vote_under_count")
  protected Integer my_two_vote_under_count = 0;

  /**
   * The one-vote understatements recorded so far.
   */
  @Column(nullable = false, name = "one_vote_under_count")
  protected Integer my_one_vote_under_count = 0;

  /**
   * The one-vote overstatements recorded so far.
   */
  @Column(nullable = false, name = "one_vote_over_count")
  protected Integer my_one_vote_over_count = 0;

  /**
   * The two-vote overstatements recorded so far.
   */
  @Column(nullable = false, name = "two_vote_over_count")
  protected Integer my_two_vote_over_count = 0;

  /**
   * Discrepancies recorded so far that are neither understatements nor overstatements.
   */
  @Column(nullable = false, name = "other_count")
  protected Integer my_other_count = 0;

  /**
   * Current risk measurement
   * Initialize at 1 because, when we have no audit info, we assume maximum risk.
   */
  @Column(nullable = false, name = "current_risk")
  private BigDecimal my_current_risk = BigDecimal.valueOf(1);

  /**
   * Creates an Assertion for a specific contest. The assertion has a given winner, loser,
   * margin, and list of candidates that are assumed to be continuing in the assertion's
   * context.
   *
   * @param contestName       Assertion has been created for this contest.
   * @param winner            Winning candidate (from contest contestID) of the assertion.
   * @param loser             Losing candidate (from contest contestID) of the assertion.
   * @param margin            Margin of the assertion.
   * @param universeSize      Size of the universe for this audit, i.e. overall ballot count.
   * @param difficulty        Estimated difficulty of assertion.
   * @param assumedContinuing List of candidates that assertion assumes are continuing.
   */
  public Assertion(String contestName, String winner, String loser, int margin,
                   long universeSize, double difficulty, List<String> assumedContinuing) {
    this.contestName = contestName;
    this.winner = winner;
    this.loser = loser;
    this.margin = margin;

    assert universeSize != 0 : "Assertion constructor: can't work with zero universeSize.";
    this.dilutedMargin = margin / (double) universeSize;

    this.difficulty = difficulty;
    this.assumedContinuing = assumedContinuing;
  }

    /**
     * Construct an empty assertion (for persistence).
     */
  public Assertion(){}

  /**
   * Return the raire-java style of this assertion, for export as json. The main difference is that candidates are
   * referred to by their index in the list of candidate names, rather than by name directly.
   * @param candidates The list of candidate names as strings.
   * @return A raire-java style Assertion - either NEN or NEB as appropriate - with the same data as this.
   * @throws RuntimeException if the data retrieved from the database is not consistent with a valid NEN assertion.
   */
  public abstract au.org.democracydevelopers.raire.assertions.Assertion makeRaireAssertion(List<String> candidates) throws GetAssertionException;
}
