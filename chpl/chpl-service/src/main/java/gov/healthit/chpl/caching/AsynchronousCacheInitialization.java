package gov.healthit.chpl.caching;

import java.io.IOException;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.search.CertifiedProductSearchManager;
import gov.healthit.chpl.service.DirectReviewCachingService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AsynchronousCacheInitialization {
    private CertificationIdManager certificationIdManager;
    private DimensionalDataManager dimensionalDataManager;
    private CertifiedProductSearchManager certifiedProductSearchManager;
    private DirectReviewCachingService drService;

    @Autowired
    public AsynchronousCacheInitialization(CertificationIdManager certificationIdManager,
            DimensionalDataManager dimensionalDataManager,
            CertifiedProductSearchManager certifiedProductSearchManager,
            DirectReviewCachingService drService) {
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
    public Future<Boolean> initializeBasicSearchAndDirectReviews() throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for Direct Reviews");
        drService.populateDirectReviewsCache();
        LOGGER.info("Finished cache initialization for Direct Reviews");
        LOGGER.info("Starting cache initialization for CertifiedProductSearchManager.searchBasic()");
        certifiedProductSearchManager.getSearchListingCollection();
        LOGGER.info("Finishing cache initialization for CertifiedProductSearchManager.searchBasic()");
        LOGGER.info("Starting cache initialization for CertifiedProductSearchManager.search()");
        certifiedProductSearchManager.getFlatListingCollection();
        LOGGER.info("Finished cache initialization for CertifiedProductSearchManager.search()");
        LOGGER.info("Starting cache initialization for CertifiedProductSearchManager.searchLegacy()");
        certifiedProductSearchManager.searchLegacy();
        LOGGER.info("Finished cache initialization for CertifiedProductSearchManager.searchLegacy()");
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
