package au.org.democracydevelopers.raireservice.persistence.repository;

import au.org.democracydevelopers.raireservice.persistence.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

  /**
   * Select contests by contest name
   * Spring syntactic sugar for the obvious SELECT query.
   * @param contestName - the name of the contest.
   * @return - the contests with that name, as retrieved from the database.
   * Note: we may not actually want to find them all - it might suffice just to check whether
   * any exist.
   */
  List<Contest> findByName(String contestName);

  /**
   * Select contests by contest ID and county ID.
   * @param contestID the ID of the contest
   * @param countyID the ID of the county
   * @return the (singleton or empty) list of matching contests.
   */
  @Query(value = "select c from Contest c where c.id = :contestID and c.countyID = :countyID")
  List<Contest> findByContestAndCountyID(@Param("contestID") Long contestID,
      @Param("countyID") Long countyID);

}