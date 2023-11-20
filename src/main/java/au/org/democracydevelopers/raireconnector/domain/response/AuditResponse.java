package au.org.democracydevelopers.raireconnector.domain.response;

import au.org.democracydevelopers.raire.algorithm.RaireResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AuditResponse {
  private String contestName;
  private RaireResult result;
}
