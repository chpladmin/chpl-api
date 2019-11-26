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
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.DimensionalDataManager;

@Component
public class AsynchronousCacheInitialization {
    private static final Logger LOGGER = LogManager.getLogger(AsynchronousCacheInitialization.class);

    @Autowired
    private CertificationIdManager certificationIdManager;
    @Autowired
    private DimensionalDataManager dimensionalDataManager;
    @Autowired
    private CertifiedProductSearchManager certifiedProductSearchManager;

    @Async
    @Transactional
    public Future<Boolean> initializeSearchOptions() throws EntityRetrievalException {
        LOGGER.info("Starting cache initialization for SearchViewController.getPopulateSearchData()");
        dimensionalDataManager.getSearchableDimensionalData(true);
        dimensionalDataManager.getSearchableDimensionalData(false);
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

    @Async
    @Transactional
    public Future<Boolean> initializeCertificationIdsGetAll()
            throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for CertificationIdManager.getAll()");
        certificationIdManager.getAllCached();
        LOGGER.info("Finished cache initialization for CertificationIdManager.getAll()");
        return new AsyncResult<>(true);
    }

    @Async
    @Transactional
    public Future<Boolean> initializeCertificationIdsGetAllWithProducts()
            throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for CertificationIdManager.getAllWithProducts()");
        certificationIdManager.getAllWithProductsCached();
        LOGGER.info("Finished cache initialization for CertificationIdManager.getAllWithProducts()");
        return new AsyncResult<>(true);
    }
}
