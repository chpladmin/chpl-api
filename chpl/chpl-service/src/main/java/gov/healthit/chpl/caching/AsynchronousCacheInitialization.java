package gov.healthit.chpl.caching;

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.SearchMenuManager;

@Component
public class AsynchronousCacheInitialization {
	private static final Logger logger = LogManager.getLogger(AsynchronousCacheInitialization.class);
	
	@Autowired private CertificationIdManager certificationIdManager;
	@Autowired private SearchMenuManager searchMenuManager;
	@Autowired private PendingCertifiedProductDAO pcpDao;
	@Autowired private CertificationStatusDAO statusDao;
	
	@Async
	@Transactional
	public Future<Boolean> initializeSearchOptions() throws EntityRetrievalException{
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
		return new AsyncResult<>(true);
	}
	
	@Async
	@Transactional
	public Future<Boolean> initializePending() throws EntityRetrievalException{
		logger.info("Starting cache initialization for /pending");
		CertificationStatusDTO statusDto = statusDao.getByStatusName("Pending");
		pcpDao.findByStatus(statusDto.getId());
		logger.info("Finished cache initialization for /pending");
		return new AsyncResult<>(true);
	}
	
	@Async
	@Transactional
	public Future<Boolean> initializeCertificationIdsGetAll() throws IOException, EntityRetrievalException, InterruptedException {
		logger.info("Starting cache initialization for CertificationIdManager.getAll()");
		certificationIdManager.getAll();
		logger.info("Finished cache initialization for CertificationIdManager.getAll()");
		return new AsyncResult<>(true);
	}
	
	@Async
	@Transactional
	public Future<Boolean> initializeCertificationIdsGetAllWithProducts() throws IOException, EntityRetrievalException, InterruptedException {
		logger.info("Starting cache initialization for CertificationIdManager.getAllWithProducts()");
		certificationIdManager.getAllWithProducts();
		logger.info("Finished cache initialization for CertificationIdManager.getAllWithProducts()");
		return new AsyncResult<>(true);
	}
	
}
