package au.org.democracydevelopers.raire.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class ElectionData {

  private JsonNode metadata;
  @JsonProperty("num_candidates")
  private Integer numberOfCandidates;
  private List<Vote> votes;
  private String winner;
  private Audit audit;
  private Integer totalVotes;
}
