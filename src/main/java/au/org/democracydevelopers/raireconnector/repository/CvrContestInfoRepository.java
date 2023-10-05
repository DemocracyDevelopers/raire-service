package au.org.democracydevelopers.raireconnector.repository;

import au.org.democracydevelopers.raireconnector.repository.entity.CvrContestInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CvrContestInfoRepository extends JpaRepository<CvrContestInfo, Long> {
  List<CvrContestInfo> findByContestIdIn(List<Integer> contestIds);
}
