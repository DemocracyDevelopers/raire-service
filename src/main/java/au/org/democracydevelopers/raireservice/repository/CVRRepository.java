package au.org.democracydevelopers.raireservice.repository;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.repository.converters.StringListConverter;
import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import au.org.democracydevelopers.raireservice.repository.entity.CVRContestInfo;
import au.org.democracydevelopers.raireservice.repository.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.repository.entity.NENAssertion;
import au.org.democracydevelopers.raireservice.request.ContestRequestByIDs;
import au.org.democracydevelopers.raireservice.request.CountyAndContestID;
import com.opencsv.bean.CsvToBean;
import org.hibernate.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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