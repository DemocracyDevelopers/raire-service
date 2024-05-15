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

import static au.org.democracydevelopers.raireservice.util.CSVUtils.escapeThenJoin;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.persistence.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.ReadOnlyProperty;

/**
 * RAIRE (raire-java) generates a set of assertions for a given IRV contest. The different types of
 * assertion that RAIRE can generate are defined as subclasses of this base Assertion class. For a
 * description of what assertions are and the role they play in an IRV audit, see the Guide to
 * RAIRE. This class has ReadOnlyProperty annotations against attributes as raire-service creates
 * assertions to be stored in the database, but never modified existing assertions that are present
 * in the database. The only type of 'modification' that the raire-service will do, if required,
 * is delete assertions from the database for a specific contest, re-generate them, and store
 * the new assertions in the database.
 */
@Entity
@Table(name = "assertion")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "assertion_type")
public abstract class Assertion {

  private static final Logger logger = LoggerFactory.getLogger(Assertion.class);

  /**
   * Assertion ID.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @ReadOnlyProperty
  private long id;

  /**
   * Version. Used for optimistic locking.
   */
  @Version
  @Column(name = "version", updatable = false, nullable = false)
  @ReadOnlyProperty
  private long version;

  /**
   * Name of the contest for which this Assertion was generated.
   */
  @Column(name = "contest_name", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected String contestName;

  @Column(name = "winner", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected String winner;

  /**
   * Loser of the Assertion (a candidate in the contest).
   */
  @Column(name = "loser", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected String loser;

  /**
   * Assertion margin (note: this is not the *diluted* margin).
   */
  @Column(name = "margin", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected int margin;

  /**
   * Assertion difficulty, as estimated by raire-java. (Note that raire-java has multiple ways
   * of estimating difficulty, and that these measurements are not necessarily in terms of numbers
   * of ballots. For example, one method may be: difficulty =  1 / assertion margin).
   */
  @Column(name = "difficulty", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected double difficulty;

  /**
   * List of candidates that the Assertion assumes are 'continuing' in the Assertion's context.
   * Note that this is always empty for NEB assertions.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "assertion_assumed_continuing", joinColumns = @JoinColumn(name = "id"))
  @Column(updatable = false, nullable = false)
  @ReadOnlyProperty
  protected List<String> assumedContinuing = new ArrayList<>();

  /**
   * Diluted margin for the Assertion. This is equal to the assertion margin divided by the
   * number of ballots in the relevant auditing universe.
   */
  @Column(name = "diluted_margin", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected double dilutedMargin;

  /**
   * A map between CVR ID and the discrepancy recorded against that CVR for this assertion
   * in the assertions contest, if one exists. CVRs are only present in this map if
   * a discrepancy exists between it and the paper ballot in the assertions contest.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "assertion_discrepancies", joinColumns = @JoinColumn(name = "id"))
  @MapKeyColumn(name = "cvr_id")
  @Column(name = "discrepancy", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected Map<Long,Integer> cvrDiscrepancy = new HashMap<>();

  /**
   * The expected number of samples to audit overall for the Assertion, assuming overstatements
   * continue at the current rate experienced in the audit.
   */
  @Column(name = "estimated_samples_to_audit", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected int estimatedSamplesToAudit = 0;

  /**
   * The expected number of samples to audit overall for the Assertion, assuming no further
   * overstatements will be encountered in the audit.
   */
  @Column(name = "optimistic_samples_to_audit", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected int optimisticSamplesToAudit = 0;

  /**
   * The two-vote understatements recorded against the Assertion.
   */
  @Column(name = "two_vote_under_count", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected int twoVoteUnderCount = 0;

  /**
   * The one-vote understatements recorded against the Assertion.
   */
  @Column(name = "one_vote_under_count", updatable = false, nullable = false)
  protected int oneVoteUnderCount = 0;

  /**
   * The one-vote overstatements recorded against the Assertion.
   */
  @Column(name = "one_vote_over_count", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected int oneVoteOverCount = 0;

  /**
   * The two-vote overstatements recorded against the Assertion.
   */
  @Column(name = "two_vote_over_count", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected int twoVoteOverCount = 0;

  /**
   * Discrepancies recorded against the Assertion that are neither understatements nor
   * overstatements.
   */
  @Column(name = "other_count", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected int otherCount = 0;

  /**
   * Current risk measurement recorded against the Assertion. It is initialized to 1, as prior
   * to an audit starting, and without additional information, we assume maximum risk.
   */
  @Column(name = "current_risk", updatable = false, nullable = false)
  @ReadOnlyProperty
  protected BigDecimal currentRisk = new BigDecimal("1.00");

  /**
   * Default no-args constructor (required for persistence).
   */
  public Assertion() {}

  /**
   * Construct an Assertion for a specific contest.
   * @param contestName Contest for which the Assertion has been created.
   * @param winner Winner of the Assertion (name of a candidate in the contest).
   * @param loser Loser of the Assertion (name of a candidate in the contest).
   * @param margin Absolute margin of the Assertion.
   * @param universeSize Total number of ballots in the auditing universe of the Assertion.
   * @param difficulty Assertion difficulty, as computed by raire-java.
   * @param assumedContinuing List of candidates, by name, that the Assertion assumes is continuing.
   * @throws IllegalArgumentException if the caller supplies a non-positive universe size, invalid
   * margin, or invalid combination of winner, loser and list of assumed continuing candidates.
   */
  public Assertion(String contestName, String winner, String loser, int margin,
      long universeSize, double difficulty, List<String> assumedContinuing)
      throws IllegalArgumentException
  {
    this.contestName = contestName;
    this.winner = winner;
    this.loser = loser;
    this.margin = margin;

    if(universeSize <= 0){
      String msg = "An assertion must have a positive universe size.";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }

    if(margin < 0 || margin > universeSize){
      String msg = "An assertion must have a non-negative margin that is less than universe size";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }

    if(winner.equals(loser)){
      String msg = "The winner and loser of an assertion must not be the same candidate.";
      logger.error(msg);
      throw new IllegalArgumentException(msg);
    }

    this.dilutedMargin = margin / (double) universeSize;

    this.difficulty = difficulty;
    this.assumedContinuing = assumedContinuing;
  }

  /**
   * Get the id. Used for sorting when assertions are output as csv.
   * @return the id.
   */
  public long getId() {
    return id;
  }


  /**
   * Get the winner by name. Used for CSV output.
   * @return the winner.
   */
  public String getWinner() {
    return winner;
  }

  /**
   * Get the loser by name. Used for CSV output.
   * @return the loser.
   */
  public String getLoser() {
    return loser;
  }

  /**
   * Get the margin. Used for CSV output.
   * @return the margin.
   */
  public Integer getMargin() {
    return margin;
  }

  /**
   * Get the difficulty. Used for CSV output.
   * @return the difficulty.
   */
  public double getDifficulty() {
    return difficulty;
  }

  /**
   * Get the diluted margin. Used for CSV output.
   * @return the diluted margin.
   */
  public double getDilutedMargin() {
    return dilutedMargin;
  }

  /**
   * Get the estimated samples to audit. Used for CSV output.
   * @return the estimated samples to audit.
   */
  public int getEstimatedSamplesToAudit() {
    return estimatedSamplesToAudit;
  }

  /**
   * Get the optimistic samples to audit. Used for CSV output.
   * @return the optimistic samples to audit.
   */
  public int getOptimisticSamplesToAudit() {
    return optimisticSamplesToAudit;
  }

  /**
   * Get the current risk calculation. Used for CSV output.
   * @return the risk.
   */
  public BigDecimal getCurrentRisk() {
    return currentRisk;
  }

  /**
   * Construct and return a raire-java representation of this Assertion. This utility is
   * ultimately used to construct an assertions report export in the same format that raire-java
   * exports. This report is formed by serialising a RaireSolution object which itself contains
   * assertions as AssertionAndDifficulty objects.
   * @param candidates The candidates in this assertion's contest.
   * @return a representation of this Assertion as an AssertionAndDifficulty object.
   * @throws IllegalArgumentException when the provided candidate list is inconsistent with the
   *                                  data stored in the assertion.
   */
  public abstract AssertionAndDifficulty convert(List<String> candidates)
      throws IllegalArgumentException;

  /**
   * Return as a list of strings intended for a CSV row, in the same order as the csvHeaders in
   * Metadata.java.
   * Note that some of these (such as names and numbers > 999) may have commas - the receiving
   * function needs to apply escapeThenJoin.
   * Floating-point numbers are formatted to 4 d.p, except the (BigDecimal) current risk, which is
   * given to its full precision.
   * @return The assertion data, as a list of csv-escaped strings.
   */
  public List<String> asCSVRow() {
    var fm = new DecimalFormat("0.0###");
    return List.of(
        gettAssertionType(),
        winner,
        loser,
        escapeThenJoin(assumedContinuing),
        fm.format(difficulty),
        margin+"",
        fm.format(dilutedMargin),
        currentRisk.toString(),
        estimatedSamplesToAudit+"",
        optimisticSamplesToAudit+"",
        twoVoteOverCount+"",
        oneVoteOverCount+"",
        otherCount+"",
        oneVoteUnderCount+"",
        twoVoteUnderCount+""
    );
  }

  /**
   * Print the assertion type, either NEN or NEB.
   */
  abstract String gettAssertionType();
}