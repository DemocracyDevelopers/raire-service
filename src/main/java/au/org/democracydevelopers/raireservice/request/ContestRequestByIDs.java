package au.org.democracydevelopers.raireservice.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContestRequestByIDs {
  private String contestName;
  private int totalAuditableBallots;
  private Integer timeProvisionForResult;
  private List<String> candidates;
  private List<CountyAndContestID> countyAndContestIDs;
}
