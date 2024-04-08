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
import java.util.List;
import jakarta.persistence.*;

@Entity
@Table(name = "assertion")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "assertion_type")
public abstract class Assertion {

  /**
   * Assertion ID.
   */
  @Id
  @Column(updatable = false, nullable = false)
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Long id;

  /**
   * Name of the contest for which this Assertion was generated.
   */
  @Column(name = "contest_name", nullable = false)
  protected String contestName;

  /**
   * Winner of the Assertion (a candidate in the contest).
   */
  @Column(name = "winner", nullable = false)
  protected String winner;

  /**
   * Loser of the Assertion (a candidate in the contest).
   */
  @Column(name = "loser", nullable = false)
  protected String loser;

  /**
   * Assertion margin (note: this is not the *diluted* margin).
   */
  @Column(name = "margin", nullable = false)
  protected int margin;

  /**
   * List of candidates that the Assertion assumes are 'continuing' in the Assertion's context.
   */
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "assertion_context", joinColumns = @JoinColumn(name = "id"))
  protected List<String> assumedContinuing = new ArrayList<>();

  /**
   * Diluted margin for the Assertion. This is equal to the assertion margin divided by the
   * number of ballots in the relevant auditing universe.
   */
  @Column(name = "diluted_margin", nullable = false)
  protected double dilutedMargin = 0;

  /**
   * Assertion difficulty, as estimated by raire-java. (Note that raire-java has multiple ways
   * of estimating difficulty, and that these measurements are not necessarily in terms of numbers
   * of ballots. For example, one method may be: difficulty =  1 / assertion margin).
   */
  @Column(name = "difficulty", nullable = false)
  protected double difficulty = 0;

  /**
   * The expected number of samples to audit overall for the Assertion, assuming overstatements
   * continue at the current rate experienced in the audit.
   */
  @Column(nullable = false)
  private Integer my_estimated_samples_to_audit = 0;

  /**
   * The two-vote understatements recorded against the Assertion.
   */
  @Column(nullable = false)
  protected Integer my_two_vote_under_count = 0;

  /**
   * The one-vote understatements recorded against the Assertion.
   */
  @Column(nullable = false)
  protected Integer my_one_vote_under_count = 0;

  /**
   * The one-vote overstatements recorded against the Assertion.
   */
  @Column(nullable = false)
  protected Integer my_one_vote_over_count = 0;

  /**
   * The two-vote overstatements recorded against the Assertion.
   */
  @Column(nullable = false)
  protected Integer my_two_vote_over_count = 0;

  /**
   * Discrepancies recorded against the Assertion that are neither understatements nor
   * overstatements.
   */
  @Column(nullable = false)
  protected Integer my_other_count = 0;

  /**
   * Current risk measurement recorded against the Assertion. It is initialized to 1, as prior
   * to an audit starting, and without additional information, we assume maximum risk.
   */
  @Column(nullable = false)
  protected BigDecimal my_current_risk = BigDecimal.valueOf(1);
}
