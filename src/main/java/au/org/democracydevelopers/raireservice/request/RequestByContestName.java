package au.org.democracydevelopers.raireservice.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestByContestName {
  private String contestName;
  private List<String> candidates;
  private BigDecimal riskLimit;


}
