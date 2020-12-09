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
import gov.healthit.chpl.exception.JiraRequestFailedException;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.CertifiedProductSearchManager;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.service.DirectReviewService;

@Component
public class AsynchronousCacheInitialization {
    private static final Logger LOGGER = LogManager.getLogger(AsynchronousCacheInitialization.class);

    private CertificationIdManager certificationIdManager;
    private DimensionalDataManager dimensionalDataManager;
    private CertifiedProductSearchManager certifiedProductSearchManager;
    private DirectReviewService drService;

    @Autowired
    public AsynchronousCacheInitialization(CertificationIdManager certificationIdManager,
            DimensionalDataManager dimensionalDataManager,
            CertifiedProductSearchManager certifiedProductSearchManager,
            DirectReviewService drService) {
        this.certificationIdManager = certificationIdManager;
        this.dimensionalDataManager = dimensionalDataManager;
        this.certifiedProductSearchManager = certifiedProductSearchManager;
        this.drService = drService;
    }

    @Async
    @Transactional
    public Future<Boolean> initializeSearchOptions() throws EntityRetrievalException {
        LOGGER.info("Starting cache initialization for SearchViewController.getPopulateSearchData()");
        dimensionalDataManager.getSearchableDimensionalData(true);
        dimensionalDataManager.getSearchableDimensionalData(false);
        dimensionalDataManager.getDimensionalData(true);
        dimensionalDataManager.getDimensionalData(false);
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
    public Future<Boolean> initializeDirectReviews() throws IOException, JiraRequestFailedException, InterruptedException {
        LOGGER.info("Starting cache initialization for Direct Reviews");
        drService.populateDirectReviewsCache();
        LOGGER.info("Finished cache initialization for Direct Reviews");
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
