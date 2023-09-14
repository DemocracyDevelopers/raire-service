package au.org.democracydevelopers.raire.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cvr_contest_info")
public class CvrContestInfo {
  @Id
  private Long cvrId;
  private Integer countyId;
  private String choices;
  private String comment;
  private String consensus;
  private Integer contestId;
  private Long index;
}
