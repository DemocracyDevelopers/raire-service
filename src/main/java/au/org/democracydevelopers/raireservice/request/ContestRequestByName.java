package au.org.democracydevelopers.raireservice.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestRequestByName {
  private String contestName;
  private int totalAuditableBallots;
  private List<String> candidates;
  private String winner;

  /**
   * Makes the metadata structure required for use by raire
   * @returns a map from string to data which includes the relevant election metadata input to raire:
   *  - candidates - a list of strings describing the candidate names
   *  - contest - the name of the contest
   *  - totalAuditableBallots - which allows for correct difficulty computations if the universe size is larger than
   *    the number of ballots in this contest
   */
  // TODO Consider abstracting this into a superclass.
  public Map<String, Object> getMetadata() {
    var candidatesAndMetadata = new HashMap<String, Object>();
    candidatesAndMetadata.put("candidates", candidates);
    candidatesAndMetadata.put("contest", contestName);
    candidatesAndMetadata.put("totalAuditableBallots", totalAuditableBallots);
    candidatesAndMetadata.put("winner", winner);
    return candidatesAndMetadata;
  }
}
