package gov.healthit.chpl.caching;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dao.impl.CertifiedProductSearchResultDAOImpl;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SurveillanceSearchOptions;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.manager.impl.CertificationIdManagerImpl;
import gov.healthit.chpl.manager.impl.SearchMenuManagerImpl;

@Component
public class CacheInitializorImpl {
	private static final Logger logger = LogManager.getLogger(CacheInitializorImpl.class);
	
	@Autowired private CertificationIdManagerImpl certificationIdManager;
	@Autowired private SearchMenuManagerImpl searchMenuManager;
	@Autowired private CertifiedProductSearchResultDAOImpl certifiedProductSearchManager;
	@Autowired PendingCertifiedProductDAO pcpDao;
	@Autowired CertificationStatusDAO statusDao;
	
	@Async
	@Transactional
	public void initializeCaches() throws IOException, EntityRetrievalException, InterruptedException {
		logger.info("Starting cache initialization for SearchViewController.getPopulateSearchData()");
		searchMenuManager.getCertBodyNames();
		searchMenuManager.getEditionNames(false);
		searchMenuManager.getEditionNames(true);
		searchMenuManager.getCertificationStatuses();
		searchMenuManager.getPracticeTypeNames();
		searchMenuManager.getClassificationNames();
		searchMenuManager.getProductNames();
		searchMenuManager.getDeveloperNames();
		searchMenuManager.getCQMCriterionNumbers(false);
		searchMenuManager.getCQMCriterionNumbers(true);
		searchMenuManager.getCertificationCriterionNumbers(false);
		searchMenuManager.getCertificationCriterionNumbers(true);
		logger.info("Finished cache initialization for SearchViewController.getPopulateSearchData()");
		
		logger.info("Starting cache initialization for CertificationIdManager.getAll()");
		certificationIdManager.getAll();
		logger.info("Finished cache initialization for CertificationIdManager.getAll()");
		logger.info("Starting cache initialization for CertificationIdManager.getAllWithProducts()");
		certificationIdManager.getAllWithProducts();
		logger.info("Finished cache initialization for CertificationIdManager.getAllWithProducts()");
		
		logger.info("Starting cache initialization for pendingCPManager.getPending()");
		CertificationStatusDTO statusDto = statusDao.getByStatusName("Pending");
		pcpDao.findByStatus(statusDto.getId());
		logger.info("Finished cache initialization for pendingCPManager.getPending()");
		
		logger.info("Starting cache initialization for /search DAO method");
		SearchRequest searchFilters = new SearchRequest();
		List<String> certBodies = new ArrayList<String>();
		certBodies.add("Drummond Group");
		certBodies.add("ICSA Labs");
		certBodies.add("InfoGard");
		List<String> certCriteria = new ArrayList<String>();
		List<String> certEditions = new ArrayList<String>();
		certEditions.add("2014");
		certEditions.add("2015");
		List<String> certStatuses = new ArrayList<String>();
		certStatuses.add("Active");
		certStatuses.add("Suspended by ONC-ACB");
		certStatuses.add("Withdrawn by Developer");
		certStatuses.add("Withdrawn by ONC-ACB");
		certStatuses.add("Suspended by ONC");
		certStatuses.add("Terminated by ONC");
		List<String> cqms = new ArrayList<String>();
		Set<SurveillanceSearchOptions> survs = new HashSet<SurveillanceSearchOptions>();
		 
		searchFilters.setCertificationBodies(certBodies);
		searchFilters.setCertificationCriteria(certCriteria);
		searchFilters.setCertificationDateEnd(null);
		searchFilters.setCertificationDateStart(null);
		searchFilters.setCertificationEditions(certEditions);
		searchFilters.setCertificationStatuses(certStatuses);
		searchFilters.setCqms(cqms);
		searchFilters.setDeveloper(null);
		searchFilters.setHasHadSurveillance(null);
		searchFilters.setOrderBy("developer");
		searchFilters.setPageNumber(0);
		searchFilters.setPageSize(50);
		searchFilters.setPracticeType(null);
		searchFilters.setProduct(null);
		searchFilters.setSearchTerm(null);
		searchFilters.setSortDescending(false);
		searchFilters.setSurveillance(survs);
		searchFilters.setVersion(null);
		certifiedProductSearchManager.search(searchFilters);
		logger.info("Finished cache initialization for /search DAO method");
	}
	
}
