package au.org.democracydevelopers.raireconnector.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "contest")
public class Contest {

  @Id
  private long id;
  private String description;
  private String name;
  private Integer sequenceNumber;
  private Long version;
  private Integer votesAllowed;
  private Integer winnersAllowed;
  private Long countyId;
}
