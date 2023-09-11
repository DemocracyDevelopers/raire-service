package au.org.democracydevelopers.raire.service;

import au.org.democracydevelopers.raire.domain.DemoPojo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class DemoService {

  public DemoPojo buildDefaultPojo(DemoPojo demoPojo) {
    return new DemoPojo(StringUtils.toRootUpperCase(demoPojo.getSalutation()), StringUtils.toRootUpperCase(demoPojo.getFirstName()), StringUtils.toRootUpperCase(demoPojo.getLastName()));
  }
}
