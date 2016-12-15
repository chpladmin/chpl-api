package gov.healthit.chpl.domain;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.web.controller.CertificationIdController;
import gov.healthit.chpl.web.controller.SearchViewController;

@ContextConfiguration(classes = { gov.healthit.chpl.CHPLConfig.class })
@Component
public class CacheInitializorImpl {
	private static JWTAuthenticatedUser adminUser;
	private static final Logger logger = LogManager.getLogger(CacheInitializorImpl.class);
	
	@Autowired private CertificationIdController certificationIdController;
	@Autowired private SearchViewController searchViewController;
	
	@Async
	public void initializeSearchOptions() throws IOException, EntityRetrievalException {
		logger.info("Starting cache initialization for SearchViewController.getPopulateSearchData(false)");
		searchViewController.getPopulateSearchData(false);
		logger.info("Finished cache initialization for SearchViewController.getPopulateSearchData(false)");
	}
	
	@Async
	public void initializeCertificationIds() throws IOException{
		adminUser = new JWTAuthenticatedUser();
		adminUser.setFirstName("Administrator");
		adminUser.setId(-2L);
		adminUser.setLastName("Administrator");
		adminUser.setSubjectName("admin");
		adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
		SecurityContextHolder.getContext().setAuthentication(adminUser);
		logger.info("Starting cache initialization for CertificationIdController.getAll()");
		certificationIdController.getAll();
		logger.info("Finished cache initialization for CertificationIdController.getAll()");
	}
	
}
