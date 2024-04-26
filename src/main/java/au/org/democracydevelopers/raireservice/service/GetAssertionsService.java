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
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.time.TimeTaken;
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
public class GetAssertionsService {

  private final static Logger logger = LoggerFactory.getLogger(GetAssertionsService.class);

  /**
   * Metadata field name for the contest's candidates.
   */
  private final static String CANDIDATES = "candidates";

  /**
   * Metadata field name for the contest's risk limit.
   */
  private final static String RISK_LIMIT = "risk_limit";

  /**
   * Metadata field name for the contest's name.
   */
  private final static String CONTEST = "contest";

  private final AssertionRepository assertionRepository;

  /**
   * All-args constructor.
   * @param assertionRepository Repository for retrieval of assertions from the colorado-rla database.
   */
  public GetAssertionsService(AssertionRepository assertionRepository){
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
      throws RaireServiceException
  {
    try {
      List<Assertion> assertions = assertionRepository.findByContestName(request.contestName);

      // If the contest has no assertions, return an error.
      if (assertions.isEmpty()) {
        String msg = "No assertions have been generated for the contest " + request.contestName;
        logger.error("GetAssertionsService::getRaireSolution " + msg);
        throw new RaireServiceException(msg, RaireErrorCodes.NO_ASSERTIONS_PRESENT);
      }

      // Create contest metadata map, supplied as input when creating a RaireResult.
      Map<String, Object> metadata = new HashMap<String, Object>();
      metadata.put(CANDIDATES, request.candidates);
      metadata.put(RISK_LIMIT, request.riskLimit);
      metadata.put(CONTEST, request.contestName);

      // Translate the assertions extracted from the database into AssertionAndDifficulty objects,
      // keeping track of the maximum difficulty and minimum margin.
      List<AssertionAndDifficulty> translated = assertions.stream().map(
          a -> a.convert(request.candidates)).toList();

      // A RaireSolution contains a RaireResult, which itself will contain the assertions for
      // contest in the request. Default values for some of the attributes of RaireResult will
      // be used -- these attributes will not be serialised in the (eventual) export.
      final TimeTaken undefined = new TimeTaken(0, 0);
      double difficulty = 0;
      int margin = 0;
      int winner = -1;

      // Get maximum difficulty and minimum margin across assertions.
      OptionalDouble maxDifficulty = translated.stream().map(a -> a.difficulty).
          mapToDouble(v -> v).max();
      if(maxDifficulty.isPresent()){
        difficulty = maxDifficulty.getAsDouble();
      }

      OptionalInt minMargin = translated.stream().map(a -> a.margin).mapToInt(v -> v).min();
      if(minMargin.isPresent()){
        margin = minMargin.getAsInt();
      }

      RaireResult result = new RaireResult(translated.toArray(AssertionAndDifficulty[]::new),
          difficulty, margin, winner, request.candidates.size(), undefined, undefined,
          undefined, false);

      return new RaireSolution(metadata, new RaireResultOrError(result));
    }
    catch(RaireServiceException ex){
      throw ex;
    }
    catch(Exception ex){
      throw new RaireServiceException(ex.getMessage(), RaireErrorCodes.INTERNAL_ERROR);
    }
  }
}
