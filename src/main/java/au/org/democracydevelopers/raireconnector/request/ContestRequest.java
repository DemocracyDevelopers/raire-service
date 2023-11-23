package au.org.democracydevelopers.raireconnector.request;

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
  private String[] candidates;
  private List<String[]> votes;

  public Map<String, Object> getMetadata() {
    var candidatesAndMetadata = new HashMap<String, Object>();
    candidatesAndMetadata.put("candidates", candidates);
    candidatesAndMetadata.put("contest", contestName);
    candidatesAndMetadata.put("totalAuditableBallots", totalAuditableBallots);
    return candidatesAndMetadata;
  }

  /* checks for no repeated votes, which is the only kind of invalidity a vote can have at this point.
   * Note it does _not_ check whether the candidate names in a vote match those in the candidates list - this
   * is checked by raire-java during vote consolidation.
   */
  public boolean votesAreValid() {
    return votes.stream().allMatch(this::noRepeats);
  }

  private boolean noRepeats(String[] v) {
    Set<String> names = new HashSet<>();
    // add returns true if element is _not_ already in the set. If any are false, it's a repeat.
    return stream(v).allMatch(names::add);
  }
}
