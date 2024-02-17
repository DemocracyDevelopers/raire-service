package au.org.democracydevelopers.raireservice.request;

import au.org.democracydevelopers.raireservice.response.Metadata;
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
  private List<String> candidates;
  private List<CountyAndContestIDs> iDs;

  private class CountyAndContestIDs {
    Long countyID;
    Long contestID;
  }
}
