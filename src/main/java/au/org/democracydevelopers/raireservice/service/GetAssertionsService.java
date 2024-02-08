package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import au.org.democracydevelopers.raireservice.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.repository.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.repository.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.request.ContestRequestByName;
import au.org.democracydevelopers.raireservice.response.GetAssertionError;
import au.org.democracydevelopers.raireservice.response.GetAssertionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GetAssertionsService {

    private final AssertionRepository assertionRepository;

    /**
     * The main method that actually does the work of this service. It
     * - inputs a request, with a contest identified by name,
     * - reads the relevant assertions from the database,
     * - converts it into the format that an assertion visualiser expects, that is, a RaireSolution,
     * - returns the result (or error).
     * @param request a ContestRequestByName - name of a single contest, with metadata
     * @return a RaireSolution - the resulting collection of assertions, with metadata, or an error.
     */
  public GetAssertionResponse getAssertions(ContestRequestByName request) {

      List<Assertion> assertions = assertionRepository.findByContestName(request.getContestName());
      List<String> candidates = request.getCandidates();

      // Convert assertions from database record into RAIRE export style with annotated difficulty.
      AssertionAndDifficulty[] assertionsWithDifficulty =
          assertions.stream().map(a ->
              new AssertionAndDifficulty(a.makeRaireAssertion(candidates), a.getDifficulty(), a.getMargin())).toList()
          .toArray(new AssertionAndDifficulty[0]);

      // Find overall data: difficulty, margin, winner.
      Optional<Assertion> maxDifficultyAssertion = assertions.stream().max(Comparator.comparingDouble(Assertion::getDifficulty));
      Optional<Assertion> minMarginAssertion = assertions.stream().min(Comparator.comparingInt(Assertion::getMargin));
      int overallWinner = request.getCandidates().indexOf(request.getWinner());

      // Assertions present. Everything as expected. Build the RAIRE result to return.
      if (assertionsWithDifficulty.length != 0 && maxDifficultyAssertion.isPresent() && minMarginAssertion.isPresent() && overallWinner != -1) {
          log.debug(String.format("Assertions successfully retrieved from database for contest %s.", request.getContestName()));

          RaireResult result = new RaireResult(
                  assertionsWithDifficulty,
                  maxDifficultyAssertion.get().getDifficulty(),
                  minMarginAssertion.get().getMargin(),
                  overallWinner,
                  candidates.size());
          // TODO Make useful metadata from stored info about which assertions have been confirmed.
          Map<String, Object> metadata = Map.of("candidates", candidates);
          return new GetAssertionResponse(metadata, new GetAssertionResponse.GetAssertionResultOrError(result));

      } else if(assertionsWithDifficulty.length == 0) {
          // If there are no assertions in the database, return an error. Note that this doesn't necessarily indicate
          // a serious problem - it might just be that no assertions have (yet) been generated for this contest.
          log.debug(String.format("No assertions present for contest %s.", request.getContestName()));
          log.info(String.format("No assertions present for contest %s.", request.getContestName()));
          return new GetAssertionResponse(new HashMap<>(),
                  new GetAssertionResponse.GetAssertionResultOrError(new GetAssertionError.NoAssertions()));
      } else {
          // If there are some assertions, but the other data consistency checks failed, then this is a
          // serious problem.
          log.debug(String.format("Error retrieving assertions for contest %s.", request.getContestName()));
          log.info(String.format("Error retrieving assertions for contest %s.", request.getContestName()));
          return new GetAssertionResponse(new HashMap<>(),
                  new GetAssertionResponse.GetAssertionResultOrError(new GetAssertionError.ErrorRetrievingAssertions()));
      }
  }
}
