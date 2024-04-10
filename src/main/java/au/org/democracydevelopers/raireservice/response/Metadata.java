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

package au.org.democracydevelopers.raireservice.response;

import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import java.math.BigDecimal;
import java.util.List;

/**
 * The metadata expected by the assertion-explainer, to be returned as part of a Get Assertions Response.
 * This includes the contest name, candidate list and risk Limit, copied directly from the request.
 * It also includes the assertion risks, in the same order as the assertions themselves, as calculated
 * by colorado-rla and stored in the database during the audit. This can be displayed by the assertion
 * explainer and used to show the user which assertions are confirmed or near confirmed, or not confirmed.
 */
public class Metadata {

  /**
   * The list of candidate names.
   */
  public final List<String> candidates;

  /**
   * The contest name.
   */
  public final String contest;

  /**
   * The audit risk limit. This is expected to be the same for all contests in colorado-rla,
   * but for raire it really does not need to be.
   */
  public final BigDecimal riskLimit;

  /**
   * The ordered list of current risk estimates, one for each assertion, in the same order as
   * the array of assertions that will accompany this metadata. This is constantly updated throughout
   * the audit process.
   */
  public final List<BigDecimal> assertionRisks;

  /**
   * This is effectively the all-args constructor - the first three args are packaged into the
   * GetAssertionsRequest.
   * @param request the Get Assertions Request, which provides the contest name, list of candidate names
   *                and risk limit.
   * @param assertionRisks an array of current estimates of risks, one for each assertion.
   */
  public Metadata(GetAssertionsRequest request, List<BigDecimal> assertionRisks) {
    this.candidates = request.candidates;
    this.contest = request.contestName;
    this.riskLimit = request.riskLimit;
    this.assertionRisks = assertionRisks;
  }
}
