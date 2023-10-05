package au.org.democracydevelopers.raireconnector.domain.raire;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit {

  private String type;
  @JsonProperty("total_auditable_ballots")
  private Integer totalAuditableBallots;
}
