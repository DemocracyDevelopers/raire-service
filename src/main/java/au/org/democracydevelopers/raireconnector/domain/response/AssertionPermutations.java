package au.org.democracydevelopers.raireconnector.domain.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AssertionPermutations {
  public int winner;
  @JsonProperty("num_candidates")
  public int numCandidates;
  public int margin;
  public double difficulty;
  @JsonProperty("time_to_determine_winners")
  public TimeTaken timeToDetermineWinners;
  @JsonProperty("time_to_find_assertions")
  public TimeTaken timeToFindAssertions;
  @JsonProperty("time_to_trim_assertions")
  public TimeTaken timeToTrimAssertions;
  public List<AssertionResult> assertions;
}
