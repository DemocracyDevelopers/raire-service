package au.org.democracydevelopers.raireservice.repository;

import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssertionRepository extends JpaRepository<Assertion, Long> {
    List<Assertion> findByContestName(String contestName);
}
