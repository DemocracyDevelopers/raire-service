package au.org.democracydevelopers.raireservice.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

import static java.util.Arrays.stream;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestRequestByIDs {
  private String contestName;
  private int totalAuditableBallots;
  private Integer timeProvisionForResult;
  private String[] candidates;
  private CountyAndContestIDs[] iDs;

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

  private class CountyAndContestIDs {
    Long countyID;
    Long contestID;
  }
}
