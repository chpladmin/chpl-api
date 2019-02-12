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

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.SearchMenuManager;

@Component
public class AsynchronousCacheInitialization {
    private static final Logger LOGGER = LogManager.getLogger(AsynchronousCacheInitialization.class);

    @Autowired
    private SearchMenuManager searchMenuManager;
    @Autowired
    private CertifiedProductSearchManager certifiedProductSearchManager;

    @Async
    @Transactional
    public Future<Boolean> initializeSearchOptions() throws EntityRetrievalException {
        LOGGER.info("Starting cache initialization for SearchViewController.getPopulateSearchData()");
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
        LOGGER.info("Finished cache initialization for SearchViewController.getPopulateSearchData()");
        return new AsyncResult<>(true);
    }

    @Async
    @Transactional
    public Future<Boolean> initializeBasicSearch() throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for CertifiedProductSearchManager.search()");
        certifiedProductSearchManager.search();
        LOGGER.info("Finished cache initialization for CertifiedProductSearchManager.search()");
        return new AsyncResult<>(true);
    }
}
