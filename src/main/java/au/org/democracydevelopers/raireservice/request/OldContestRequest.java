package au.org.democracydevelopers.raireservice.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import static java.util.Arrays.stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OldContestRequest {
  private String contestName;
  private int totalAuditableBallots;
  private Integer timeProvisionForResult;
  private String[] candidates;
  private List<String[]> votes;

  /**
   * Makes the metadata structure required for use by raire
   * @returns a map from string to data which includes the relevant election metadata input to raire:
   *  - candidates - a list of strings describing the candidate names
   *  - contest - the name of the contest
   *  - totalAuditableBallots - which allows for correct difficulty computations if the universe size is larger than
   *    the number of ballots in this contest
   */
  public Map<String, Object> getMetadata() {
    var candidatesAndMetadata = new HashMap<String, Object>();
    candidatesAndMetadata.put("candidates", candidates);
    candidatesAndMetadata.put("contest", contestName);
    candidatesAndMetadata.put("totalAuditableBallots", totalAuditableBallots);
    return candidatesAndMetadata;
  }

  /**
   * Checks for no repeated votes, which is the only kind of invalidity a vote can have at this point.
   * @returns true if there are no repeated choices in any of the votes.
   *
   * Note it does _not_ check whether the candidate names in a vote match those in the candidates list - this
   * is checked by raire-java during vote consolidation.
   */
  public boolean votesAreValid() {
    return votes.stream().allMatch(this::noRepeats);
  }

  /**
   * Checks that there are no repeated strings.
   * @param v - a list of strings
   * @returns true if there are no repeated strings.
   */
  private boolean noRepeats(String[] v) {
    Set<String> names = new HashSet<>();
    // add returns true if element is _not_ already in the set. If any are false, it's a repeat.
    return stream(v).allMatch(names::add);
  }
}
