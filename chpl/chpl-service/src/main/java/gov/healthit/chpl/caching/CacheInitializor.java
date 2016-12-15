package gov.healthit.chpl.caching;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.EntityRetrievalException;

@Component
public class CacheInitializor {
	@Autowired private CacheInitializorImpl cacheInitializorImpl;

	  @PostConstruct
	  public void initialize() throws IOException, EntityRetrievalException {
		  cacheInitializorImpl.initializeSearchOptions();
		  cacheInitializorImpl.initializeCertificationIds();
	  }
}
