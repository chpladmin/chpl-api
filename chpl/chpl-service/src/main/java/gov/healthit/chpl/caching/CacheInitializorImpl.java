package gov.healthit.chpl.caching;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.manager.impl.CertificationIdManagerImpl;

@ContextConfiguration(classes = { gov.healthit.chpl.CHPLConfig.class })
@Component
public class CacheInitializorImpl {
	private static final Logger logger = LogManager.getLogger(CacheInitializorImpl.class);
	
	@Autowired private CertificationIdManagerImpl certificationIdManager;
	
	@Async
	public void initializeSearchOptions() throws IOException, EntityRetrievalException {
		logger.info("Starting cache initialization for SearchViewController.getPopulateSearchData(false)");
		//searchViewController.getPopulateSearchData(false);
		logger.info("Finished cache initialization for SearchViewController.getPopulateSearchData(false)");
		logger.info("Starting cache initialization for SearchViewController.getPopulateSearchData(true)");
		//searchViewController.getPopulateSearchData(true);
		logger.info("Finished cache initialization for SearchViewController.getPopulateSearchData(true)");
	}
	
	@Async
	public void initializeCertificationIdManager() {
		logger.info("Starting cache initialization for CertificationIdManager.getAll()");
		certificationIdManager.getAll();
		logger.info("Finished cache initialization for CertificationIdManager.getAll()");
		logger.info("Starting cache initialization for CertificationIdManager.getAllWithProducts()");
		certificationIdManager.getAllWithProducts();
		logger.info("Finished cache initialization for CertificationIdManager.getAllWithProducts()");
	}
	
}
