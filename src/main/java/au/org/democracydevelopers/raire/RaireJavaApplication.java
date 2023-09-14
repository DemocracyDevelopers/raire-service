package au.org.democracydevelopers.raire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaRepositories
public class RaireJavaApplication {

  public static void main(String[] args) {
    SpringApplication.run(RaireJavaApplication.class, args);
  }

}
