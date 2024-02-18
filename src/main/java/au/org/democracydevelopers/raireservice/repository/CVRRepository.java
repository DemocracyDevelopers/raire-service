package au.org.democracydevelopers.raireservice.repository;

import au.org.democracydevelopers.raireservice.repository.entity.CVRContestInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CVRRepository extends JpaRepository<CVRContestInfo, Long> {

    /**
     *
     * @param countyID The ID of the county
     * @param contestID The ID of the contest, in this county
     * @return The choices, as a single (unprocessed) string.
     */
    @Query(value = "select ci.choices from cvr_contest_info ci " +
            " where ci.county_id = :county_id " +
            " and ci.contest_id = :contest_id", nativeQuery = true)
    List<String> getCVRs(@Param("county_id") Long countyID, @Param("contest_id") Long contestID);

}