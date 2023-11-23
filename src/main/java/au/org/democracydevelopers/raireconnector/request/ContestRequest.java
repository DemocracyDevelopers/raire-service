package au.org.democracydevelopers.raireconnector.request;

import au.org.democracydevelopers.raire.irv.Vote;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import static java.util.Arrays.stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestRequest {
  private String contestName;
  private int totalAuditableBallots;
  private Integer timeProvisionForResult;
  private List<String> candidates;
  private List<String[]> votes;

  /*
   * Converts the input vote format (an ordered list of candidate names)
   * into the format expected by RAIRE (an ordered list of candidate indices, based on those of the candidates list),
   * then gathers all equal preference lists together, so they are represented as one 'Vote' object with the appropriate
   * count.
   */
  public Vote[] toRaireVotes() {
    var candidatesMap = buildCandidatesMap(candidates);

    // Iterate through the votes, converting them from a list of names to a list of IDs.
    // Store them in a map from preference orderings to the number of times that order was encountered.
    Map<int[], Integer> intPreferences = new HashMap<>();
    for (String[] preferences : votes) {

      int[] prefArray = stream(preferences) .map(candidatesMap::get).mapToInt(Integer::intValue).toArray();
      intPreferences.put(prefArray, intPreferences.getOrDefault(prefArray, 0) + 1);

    }

    // Convert each item in the map to a raire Vote with the same number & preference list.
    List<Vote> raireBallots = new ArrayList<>();
    intPreferences.forEach( (prefs,n) -> {
        raireBallots.add(new Vote(n, prefs));
      });

    Vote[] votearray = new Vote[raireBallots.size()];
    return raireBallots.toArray(votearray);
  }

  // Generates a map from candidate name to (arbitrary) ID. This ID is used to express votes to RAIRE.
  // It simply ignores multiple copies of the same candidate name.
  private Map<String, Integer> buildCandidatesMap(List<String> candidates) {
    int count = 0;
    Map<String, Integer> candidatesMap = new HashMap<>();
      for (String candidateName : candidates) {
        if (!candidatesMap.containsKey(candidateName)) {
          candidatesMap.put(candidateName, count++);
        }
    }
    return candidatesMap;
  }
}
