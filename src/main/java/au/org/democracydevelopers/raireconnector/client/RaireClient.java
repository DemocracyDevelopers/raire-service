package au.org.democracydevelopers.raireconnector.client;

import au.org.democracydevelopers.raireconnector.domain.raire.ElectionData;
import au.org.democracydevelopers.raireconnector.domain.response.RaireResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class RaireClient {

  private final RestTemplate restTemplate;

  @Value("${raire.url}")
  private String raireUrl;

  public RaireResponse getRaireResponse(ElectionData electionData) {
    ResponseEntity<RaireResponse> raireResponseEntity = restTemplate.postForEntity(raireUrl,
        electionData, RaireResponse.class);
    log.info("Received Raire Audit Result {}", raireResponseEntity.getBody());
    return raireResponseEntity.getBody();
  }

}
