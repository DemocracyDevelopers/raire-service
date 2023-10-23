package au.org.democracydevelopers.raireconnector.repository;

import au.org.democracydevelopers.raireconnector.repository.entity.Contest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

  List<Contest> findByNameIn(List<String> name);
}
