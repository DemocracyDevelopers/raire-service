package au.org.democracydevelopers.raireconnector.domain.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contest {
  private Integer contestId;
  private Integer totalAuditableBallots;
  private Integer timeProvisionForResult;
}
