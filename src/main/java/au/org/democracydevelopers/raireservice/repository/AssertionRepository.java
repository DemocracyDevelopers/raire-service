package au.org.democracydevelopers.raireservice.repository;

import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import au.org.democracydevelopers.raireservice.repository.entity.NEBAssertion;
import au.org.democracydevelopers.raireservice.repository.entity.NENAssertion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface AssertionRepository extends JpaRepository<Assertion, Long> {

    @Query(value="select a from Assertion a where a.contestName = :contestName")
    List<Assertion> findByContestName(@Param("contestName") String contestName);

     default void saveRaireAssertions(AssertionAndDifficulty[] assertions, String contestName, Integer ballotCount, String[] candidates) {
        List<Assertion> storeableAssertions = Arrays.stream(assertions).map(a ->
                makeStoreable(a, contestName, ballotCount, candidates)).toList();

        this.saveAll(storeableAssertions);
    }

    /**
     * Convert the type of assertion received from RAIRE into the form colorado-rla needs
     * to store in the database.
     * @param assertionWD  The assertion (with RAIRE's estimated difficulty attached, which we don't use)
     * @param contestName  The contest name
     * @param ballotCount  The total number of ballots in the universe
     * @param candidates   The ordered list of candidate names, with respect to which the assertion IDs are written.
     * @return either an NEBAssertion or an NENAssertion, matching the type of assertion that was passed. This will
     *         have the input assertion's candidate ID's converted into names according to their index in @param candidates.
     */
    private Assertion makeStoreable(AssertionAndDifficulty assertionWD, String contestName, Integer ballotCount, String[] candidates) {
        au.org.democracydevelopers.raire.assertions.Assertion assertion = assertionWD.assertion;

        if(assertionWD.assertion.getClass() == NotEliminatedBefore.class) {
            NotEliminatedBefore nebAssertion = (NotEliminatedBefore) assertion;
            String winnerName = candidates[nebAssertion.winner];
            String loserName = candidates[nebAssertion.loser];
            return new NEBAssertion(contestName, winnerName, loserName, assertionWD.margin, ballotCount, assertionWD.difficulty);
        } else if (assertionWD.assertion.getClass() == NotEliminatedNext.class) {
            NotEliminatedNext nenAssertion = (NotEliminatedNext) assertion;
            String winnerName = candidates[nenAssertion.winner];
            String loserName = candidates[nenAssertion.loser];
            List<String> continuingByName = Arrays.stream(nenAssertion.continuing).mapToObj(i -> candidates[i]).toList();
            return new NENAssertion(contestName, winnerName, loserName, assertionWD.margin, ballotCount,
                    assertionWD.difficulty, continuingByName);
        } else {
            throw new IllegalStateException("Illegal Assertion: "+assertion);
        }
    }
}