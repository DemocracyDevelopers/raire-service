package au.org.democracydevelopers.raireservice.response;

import au.org.democracydevelopers.raireservice.repository.entity.Assertion;
import au.org.democracydevelopers.raireservice.request.ContestRequestByName;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Makes the metadata structure required for use by raire
 * @returns a map from string to data which includes the relevant election metadata input to raire:
 *  - candidates - a list of strings describing the candidate names
 *  - contest - the name of the contest
 *  - totalAuditableBallots - which allows for correct difficulty computations if the universe size is larger than
 *    the number of ballots in this contest
 */

public class Metadata {
    private Map<String,Object> metadata = new HashMap<>();

    public Map<String,Object> getMetadata() {
        return metadata;
    }

    public void AddRisks(List<Assertion> assertions) {
        List<BigDecimal> risks = assertions.stream().map(Assertion::getMy_current_risk).toList();
        metadata.put("assertionRisks", risks);
    }
    public Metadata(ContestRequestByName request) {
            metadata.put("candidates", request.getCandidates());
            metadata.put("contest", request.getContestName());
            metadata.put("totalAuditableBallots", request.getTotalAuditableBallots());
            metadata.put("winner", request.getWinner());
            metadata.put("riskLimit", request.getRiskLimit());
    }
}
