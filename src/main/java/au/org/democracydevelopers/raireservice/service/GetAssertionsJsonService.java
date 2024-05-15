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

package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raire.RaireSolution;

import au.org.democracydevelopers.raire.RaireSolution.RaireResultOrError;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raireservice.response.RaireResultMixIn;
import au.org.democracydevelopers.raireservice.persistence.entity.Assertion;
import au.org.democracydevelopers.raireservice.persistence.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.request.GetAssertionsRequest;
import au.org.democracydevelopers.raireservice.service.RaireServiceException.RaireErrorCodes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Collection of functions responsible for retrieving assertions from the colorado-rla database,
 * through the use of an AssertionRepository, and packaging them in a form suitable for export
 * in desired forms. Currently, assertions are packaged and returned in the form of a RaireSolution.
 * Assertions are retrieved for a contest as specified in a GetAssertionsRequest.
 */
@Service
public class GetAssertionsJsonService {

  private final static Logger logger = LoggerFactory.getLogger(GetAssertionsJsonService.class);

  private final AssertionRepository assertionRepository;

  /**
   * All-args constructor.
   * @param assertionRepository Repository for retrieval of assertions from the colorado-rla database.
   */
  public GetAssertionsJsonService(AssertionRepository assertionRepository){
    this.assertionRepository = assertionRepository;
  }

  /**
   * Given a request to retrieve assertions for a given contest, return these assertions as part
   * of a RaireSolution. This RaireSolution may be serialised to produce a JSON export suitable
   * for use by an assertion visualiser.
   * @param request Request to retrieve assertions for a specific contest.
   * @return A RaireSolution containing any assertions generated for the contest in the request.
   * @throws RaireServiceException when no assertions exist for the contest, or an error has
   * arisen during retrieval of assertions.
   */
  public RaireSolution getRaireSolution(GetAssertionsRequest request)
      throws RaireServiceException {
    final String prefix = "[getRaireSolution]";
    try {
      logger.debug(String.format("%s Preparing to build a RaireSolution for serialisation into " +
              "assertion visualiser report for contest %s.", prefix, request.contestName));
      logger.debug(String.format("%s (Database access) Retrieve all assertions for contest %s.",
          prefix, request.contestName));

      // Retrieve the assertions.
      List<Assertion> assertions = assertionRepository.findByContestName(request.contestName);

      // If the contest has no assertions, return an error.
      if (assertions.isEmpty()) {
        final String msg = String.format("%s No assertions have been generated for the contest %s.",
            prefix, request.contestName);
        logger.error(msg);
        throw new RaireServiceException(msg, RaireErrorCodes.NO_ASSERTIONS_PRESENT);
      }

      // Create contest metadata map, supplied as input when creating a RaireResult.
      logger.debug(String.format("%s Creating contest metadata map (candidates: %s), " +
          "risk limit (%s), and contest name (%s).", prefix, request.candidates, request.riskLimit,
          request.contestName));
      Map<String, Object> metadata = new HashMap<>();
      metadata.put(Metadata.CANDIDATES, request.candidates);
      metadata.put(Metadata.RISK_LIMIT, request.riskLimit);
      metadata.put(Metadata.CONTEST, request.contestName);

      // Translate the assertions extracted from the database into AssertionAndDifficulty objects,
      // keeping track of the maximum difficulty and minimum margin.
      logger.debug(String.format("%s Converting %d assertions into raire-java format.", prefix,
          assertions.size()));
      List<AssertionAndDifficulty> translated = assertions.stream().map(
          a -> a.convert(request.candidates)).toList();

      logger.debug(String.format("%s %d assertions translated to json.", prefix,
          assertions.size()));
      double difficulty = 0;
      int margin = 0;

      // Get maximum difficulty and minimum margin across assertions.
      OptionalDouble maxDifficulty = translated.stream().map(a -> a.difficulty).
          mapToDouble(v -> v).max();
      if(maxDifficulty.isPresent()){
        difficulty = maxDifficulty.getAsDouble();
      }

      logger.debug(String.format("%s Maximum difficulty across assertions: %f.",
          prefix, difficulty));

      OptionalInt minMargin = translated.stream().map(a -> a.margin).mapToInt(v -> v).min();
      if(minMargin.isPresent()){
        margin = minMargin.getAsInt();
      }

      logger.debug(String.format("%s Minimum margin across assertions: %d.", prefix, margin));

      // Using a version of RaireResult in which certain attributes will be ignored in
      // serialisation.
      RaireResultMixIn result = new RaireResultMixIn(translated.toArray(AssertionAndDifficulty[]::new),
          difficulty, margin, request.candidates.size());

      RaireSolution solution = new RaireSolution(metadata, new RaireResultOrError(result));
      logger.debug(String.format("%s Constructed RaireSolution for return and serialisation.", prefix));
      return solution;
    }
    catch(RaireServiceException ex){
      logger.error(String.format("%s RaireServiceException caught. Passing to caller: %s",
          prefix, ex.getMessage()));
      throw ex;
    }
    catch(Exception ex){
      logger.error(String.format("%s Generic exception caught. Passing to caller: %s",
          prefix, ex.getMessage()));
      throw new RaireServiceException(ex.getMessage(), RaireErrorCodes.INTERNAL_ERROR);
    }
  }
}
