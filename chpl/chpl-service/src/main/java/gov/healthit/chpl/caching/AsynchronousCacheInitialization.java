package gov.healthit.chpl.caching;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationIdManager;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.search.CertifiedProductSearchManager;
import gov.healthit.chpl.search.ListingSearchManager;
import gov.healthit.chpl.service.DirectReviewCachingService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AsynchronousCacheInitialization {
    private CertificationIdManager certificationIdManager;
    private DimensionalDataManager dimensionalDataManager;
    private CertifiedProductSearchManager certifiedProductSearchManager;
    private ListingSearchManager listingSearchManager;
    private DirectReviewCachingService drService;

    @Autowired
    public AsynchronousCacheInitialization(CertificationIdManager certificationIdManager,
            DimensionalDataManager dimensionalDataManager,
            CertifiedProductSearchManager certifiedProductSearchManager,
            ListingSearchManager listingSearchManager,
            DirectReviewCachingService drService) {
        this.certificationIdManager = certificationIdManager;
        this.dimensionalDataManager = dimensionalDataManager;
        this.certifiedProductSearchManager = certifiedProductSearchManager;
        this.listingSearchManager = listingSearchManager;
        this.drService = drService;
    }

    @Async
    public void initializeSearchOptions() throws EntityRetrievalException {
        LOGGER.info("Starting cache initialization for SearchViewController.getPopulateSearchData()");
        dimensionalDataManager.getSearchableDimensionalData(true);
        dimensionalDataManager.getSearchableDimensionalData(false);
        dimensionalDataManager.getDimensionalData(true);
        dimensionalDataManager.getDimensionalData(false);
        LOGGER.info("Finished cache initialization for SearchViewController.getPopulateSearchData()");
    }

    @Async
    public void initializeBasicSearchAndDirectReviews() throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for Direct Reviews");
        drService.populateDirectReviewsCache();
        LOGGER.info("Finished cache initialization for Direct Reviews");
        LOGGER.info("Starting cache initialization for searchable listing collection");
        listingSearchManager.getAllListings();
        LOGGER.info("Finishing cache initialization for searchable listing collection");
        LOGGER.info("Starting cache initialization for listing collection");
        certifiedProductSearchManager.getFlatListingCollection();
        LOGGER.info("Finished cache initialization for listing collection");
    }

    @Async
    public void initializeCertificationIdsGetAll()
            throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for CertificationIdManager.getAll()");
        certificationIdManager.getAllCached();
        LOGGER.info("Finished cache initialization for CertificationIdManager.getAll()");
    }

    @Async
    public void initializeCertificationIdsGetAllWithProducts()
            throws IOException, EntityRetrievalException, InterruptedException {
        LOGGER.info("Starting cache initialization for CertificationIdManager.getAllWithProducts()");
        certificationIdManager.getAllWithProductsCached();
        LOGGER.info("Finished cache initialization for CertificationIdManager.getAllWithProducts()");
    }
}
