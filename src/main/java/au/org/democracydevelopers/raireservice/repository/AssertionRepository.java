package au.org.democracydevelopers.raireservice.repository;

import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssertionRepository extends JpaRepository<Assertion, Long> {

    @Query(value="select a from Assertion a where a.contestName = :contestName")
    List<Assertion> findByContestName(@Param("contestName") String contestName);


    /*
    @Query(value="select a from Assertion a", nativeQuery = true)
    List<Assertion> findAllAssertions();
    */
}
