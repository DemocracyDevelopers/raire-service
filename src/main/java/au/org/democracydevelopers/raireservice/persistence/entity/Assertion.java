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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import jakarta.persistence.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RAIRE generates a set of assertions for a given IRV contest. The different types of assertion
 * that RAIRE can generate are defined as subclasses of this base Assertion class. For a description
 * of what assertions are and the role they play in an IRV audit, see the Guide to RAIRE.
 */
@Entity
@Table(name = "assertion")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "assertion_type")
public abstract class Assertion {

  protected static final Logger logger = LoggerFactory.getLogger(Assertion.class);

  /**
   * Assertion ID.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  /**
   * Version. Used for optimistic locking.
   */
  @Version
  @Column(name = "version", updatable = false, nullable = false)
  private long version;

  /**
   * Name of the contest for which this Assertion was generated.
   */
  @Column(name = "contest_name", updatable = false, nullable = false)
  protected String contestName;

  @Column(name = "winner", updatable = false, nullable = false)
  protected String winner;

  /**
   * Loser of the Assertion (a candidate in the contest).
   */
  @Column(name = "loser", updatable = false, nullable = false)
  protected String loser;

  /**
   * Assertion margin (note: this is not the *diluted* margin).
   */
  @Column(name = "margin", updatable = false, nullable = false)
  protected int margin;

  /**
   * Assertion difficulty, as estimated by raire-java. (Note that raire-java has multiple ways
   * of estimating difficulty, and that these measurements are not necessarily in terms of numbers
   * of ballots. For example, one method may be: difficulty =  1 / assertion margin).
   */
  @Column(name = "difficulty", updatable = false, nullable = false)
  protected double difficulty;

  /**
   * List of candidates that the Assertion assumes are 'continuing' in the Assertion's context.
   * Note that this is always empty for NEB assertions.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "assertion_assumed_continuing", joinColumns = @JoinColumn(name = "id"))
  @Column(updatable = false, nullable = false)
  protected List<String> assumedContinuing = new ArrayList<>();

  /**
   * Diluted margin for the Assertion. This is equal to the assertion margin divided by the
   * number of ballots in the relevant auditing universe.
   */
  @Column(name = "diluted_margin", updatable = false, nullable = false)
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
  protected Map<Long,Integer> cvrDiscrepancy = new HashMap<>();

  /**
   * The expected number of samples to audit overall for the Assertion, assuming overstatements
   * continue at the current rate experienced in the audit.
   */
  @Column(name = "estimated_samples_to_audit", updatable = false, nullable = false)
  protected int estimatedSamplesToAudit = 0;

  /**
   * The expected number of samples to audit overall for the Assertion, assuming no further
   * overstatements will be encountered in the audit.
   */
  @Column(name = "optimistic_samples_to_audit", updatable = false, nullable = false)
  protected int optimisticSamplesToAudit = 0;

  /**
   * The two-vote understatements recorded against the Assertion.
   */
  @Column(name = "two_vote_under_count", updatable = false, nullable = false)
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
  protected int oneVoteOverCount = 0;

  /**
   * The two-vote overstatements recorded against the Assertion.
   */
  @Column(name = "two_vote_over_count", updatable = false, nullable = false)
  protected int twoVoteOverCount = 0;

  /**
   * Discrepancies recorded against the Assertion that are neither understatements nor
   * overstatements.
   */
  @Column(name = "other_count", updatable = false, nullable = false)
  protected int otherCount = 0;

  /**
   * Current risk measurement recorded against the Assertion. It is initialized to 1, as prior
   * to an audit starting, and without additional information, we assume maximum risk.
   */
  @Column(name = "current_risk", updatable = false, nullable = false)
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

}
