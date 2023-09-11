package au.org.democracydevelopers.raire.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //This Code will auto generate getter, setter classes, toSting and hashcode/equals methods for this class.
@NoArgsConstructor //generate a default constructor for this class.
@AllArgsConstructor // generate a parameterised constructor for this class
public class DemoPOJO {
  private String salutation;
  private String firstName;
  private String lastName;
}
