package au.org.democracydevelopers.raireservice.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
  private BigDecimal riskLimit;


}
