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
import gov.healthit.chpl.dao.CertifiedProductSearchResultDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.domain.SearchRequest;
import gov.healthit.chpl.domain.SurveillanceSearchOptions;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.SearchMenuManager;

@Component
public class AsynchronousCacheInitialization {
	private static final Logger logger = LogManager.getLogger(AsynchronousCacheInitialization.class);
	
	@Autowired private CertificationIdManager certificationIdManager;
	@Autowired private SearchMenuManager searchMenuManager;
	@Autowired private CertifiedProductSearchResultDAO certifiedProductSearchResultDAO;
	@Autowired private PendingCertifiedProductDAO pcpDao;
	@Autowired private CertificationStatusDAO statusDao;
	
	@Async
	@Transactional
	public void initializeSearchOptions() throws EntityRetrievalException{
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
	}
	
	@Async
	@Transactional
	public void initializePending() throws EntityRetrievalException{
		logger.info("Starting cache initialization for /pending");
		CertificationStatusDTO statusDto = statusDao.getByStatusName("Pending");
		pcpDao.findByStatus(statusDto.getId());
		logger.info("Finished cache initialization for /pending");
	}
	
	@Async
	@Transactional
	public void initializeSearch() throws EntityRetrievalException{
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
		certifiedProductSearchResultDAO.search(searchFilters);
		logger.info("Finished cache initialization for /search DAO method");
	}
	
	@Async
	@Transactional
	public void initializeCertificationIdsGetAll() throws IOException, EntityRetrievalException, InterruptedException {
		logger.info("Starting cache initialization for CertificationIdManager.getAll()");
		certificationIdManager.getAll();
		logger.info("Finished cache initialization for CertificationIdManager.getAll()");
	}
	
	@Async
	@Transactional
	public void initializeCertificationIdsGetAllWithProducts() throws IOException, EntityRetrievalException, InterruptedException {
		logger.info("Starting cache initialization for CertificationIdManager.getAllWithProducts()");
		certificationIdManager.getAllWithProducts();
		logger.info("Finished cache initialization for CertificationIdManager.getAllWithProducts()");
	}
	
}
