package au.org.democracydevelopers.raireconnector.domain.raire;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class Vote {
  @JsonProperty("n")
  private Integer count;
  @JsonProperty("prefs")
  private List<Integer> preference;
}
