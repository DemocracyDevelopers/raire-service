package au.org.democracydevelopers.raireconnector.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RaireConfig {
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

}
