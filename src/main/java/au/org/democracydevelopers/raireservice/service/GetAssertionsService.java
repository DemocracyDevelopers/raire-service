package au.org.democracydevelopers.raireservice.service;

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raire.RaireSolution;
import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import au.org.democracydevelopers.raireservice.repository.AssertionRepository;
import au.org.democracydevelopers.raireservice.repository.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.repository.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.request.ContestRequestByName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
  public RaireSolution getAssertions(ContestRequestByName request) {

      List<Assertion> assertions = assertionRepository.findByContestName(request.getContestName());
      List<String> candidates = request.getCandidates();

      // Convert assertions from database record into RAIRE export style with annotated difficulty.
      AssertionAndDifficulty[] assertionsWithDifficulty =
          assertions.stream().map(a ->
              new AssertionAndDifficulty(makeRaireAssertion(a, candidates), a.getDifficulty(), a.getMargin())).toList()
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
          return new RaireSolution(metadata, new RaireSolution.RaireResultOrError(result));

          // TODO Improve error handling. We may not actually want a RAIRE error at this point, since it may be a
          // database retrieval error.
          // Indeed, it may not even be an error.  Distinguish empty assertion sets (which are not necessarily an error)
          // from inconsistent ones (which certainly are an error).
          // Build an empty RAIRE result to return.
      } else  // if(assertionsWithDifficulty.length == 0) {
      {
          log.debug(String.format("No assertions present for contest %s.", request.getContestName()));
          RaireResult result = new RaireResult(
                  assertionsWithDifficulty,
                  0,
                  0,
                  overallWinner,
                  candidates.size());
          // TODO Make useful metadata from stored info about which assertions have been confirmed.
          Map<String, Object> metadata = Map.of("candidates", candidates);
          return new RaireSolution(metadata, new RaireSolution.RaireResultOrError(result));
      }
  }

    private au.org.democracydevelopers.raire.assertions.Assertion makeRaireAssertion(Assertion a, List<String> candidates) {

      // Find index of winner, loser and (if relevant) continuing candidates in candidate list.
      int winner = candidates.indexOf(a.getWinner());
      int loser = candidates.indexOf(a.getLoser());
      int[] continuing =  a.getAssumedContinuing().stream().mapToInt(candidates::indexOf).toArray();

      // If it's an NEB assertion, return a RAIRE NEB assertion.
      if( a instanceof NEBAssertion && winner != -1 && loser != -1) {
        return new NotEliminatedBefore(winner, loser);

        // If it's an NEN assertion, convert the continuing candidates to indices, then return the RAIRE NEN assertion.
      } else if (a instanceof NENAssertion && winner != -1 && loser != -1 && Arrays.stream(continuing).noneMatch(i -> i == -1)) {
          return new NotEliminatedNext(winner, loser, continuing);

      } else {
          // This obviously should not happen if all the assertions in the database are valid NEN or NEB. The check is important
          // in case future versions of the software introduce other types of assertion.
          log.error(String.format("Invalid assertion retrieved from database: %s", a));
          throw new RuntimeException(String.format("Invalid assertion retrieved from database: %s", a));
      }
    }
}
