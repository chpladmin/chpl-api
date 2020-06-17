package gov.healthit.chpl.scheduler.job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.scheduler.SchedulerCertifiedProductSearchDetailsAsync;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public abstract class DownloadableResourceCreatorJob extends QuartzJob {
    private SimpleDateFormat filenameTimestampFormat;

    @Autowired
    private SchedulerCertifiedProductSearchDetailsAsync schedulerCertifiedProductSearchDetailsAsync;

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private CertifiedProductDAO certifiedProductDao;

    @Autowired
    private CertificationCriterionDAO criteriaDao;

    @Autowired
    private CertificationResultDAO certificationResultDao;

    @Autowired
    private CertificationResultDetailsDAO certificationResultDetailsDao;

    @Autowired
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    private Logger logger;

    public DownloadableResourceCreatorJob(Logger logger) {
        Logger rootLogger = LogManager.getLogger(DownloadableResourceCreatorJob.class);
        rootLogger.info("Constructor for DownloadableResourceCreatorJob invoked");
        filenameTimestampFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        setLogger(logger);
    }

    protected List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFutures(
            final List<CertifiedProductDetailsDTO> listings) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        SchedulerCertifiedProductSearchDetailsAsync cpsdAsync = getCertifiedProductDetailsAsyncRetrievalHelper();

        for (CertifiedProductDetailsDTO currListing : listings) {
            try {
                futures.add(cpsdAsync.getCertifiedProductDetail(currListing.getId(), getCpdManager()));
            } catch (EntityRetrievalException e) {
                logger.error("Could not retrieve certified product details for id: " + currListing.getId(), e);
            }
        }
        return futures;
    }

    protected Optional<CertifiedProductSearchDetails> getCertifiedProductSearchDetails(Long listingId) {
        try {
            return Optional.of(certifiedProductDetailsManager.getCertifiedProductDetails(listingId));
        } catch (EntityRetrievalException e) {
            logger.error(String.format("Could not retrieve listing: %s", listingId), e);
            return Optional.empty();
        }
    }

    protected List<Future<CertifiedProductSearchDetails>> getCertifiedProductSearchDetailsFuturesFromIds(
            final List<Long> listingIds) throws Exception {

        List<Future<CertifiedProductSearchDetails>> futures = new ArrayList<Future<CertifiedProductSearchDetails>>();
        SchedulerCertifiedProductSearchDetailsAsync cpsdAsync = getCertifiedProductDetailsAsyncRetrievalHelper();

        for (Long currListingId : listingIds) {
            try {
                futures.add(cpsdAsync.getCertifiedProductDetail(currListingId, getCpdManager()));
            } catch (EntityRetrievalException e) {
                logger.error("Could not retrieve certified product details for id: " + currListingId, e);
            }
        }
        return futures;
    }

    protected Map<Long, CertifiedProductSearchDetails> getMapFromFutures(
            final List<Future<CertifiedProductSearchDetails>> futures) {
        Map<Long, CertifiedProductSearchDetails> cpMap = new HashMap<Long, CertifiedProductSearchDetails>();
        for (Future<CertifiedProductSearchDetails> future : futures) {
            try {
                cpMap.put(future.get().getId(), future.get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Could not retrieve certified product details for unknown id.", e);
            }
        }
        return cpMap;
    }

    protected List<CertifiedProductSearchDetails> createOrderedListOfCertifiedProducts(
            final Map<Long, CertifiedProductSearchDetails> certifiedProducts,
            final List<CertifiedProductDetailsDTO> orderedListings) {

        List<CertifiedProductSearchDetails> ordered = new ArrayList<CertifiedProductSearchDetails>();

        for (CertifiedProductDetailsDTO listing : orderedListings) {
            if (certifiedProducts.containsKey(listing.getId())) {
                ordered.add(certifiedProducts.get(listing.getId()));
            }
        }

        return ordered;
    }

    protected List<CertifiedProductSearchDetails> createOrderedListOfCertifiedProductsFromIds(
            final Map<Long, CertifiedProductSearchDetails> certifiedProducts, final List<Long> orderedIds) {

        List<CertifiedProductSearchDetails> ordered = new ArrayList<CertifiedProductSearchDetails>();

        for (Long id : orderedIds) {
            if (certifiedProducts.containsKey(id)) {
                ordered.add(certifiedProducts.get(id));
            }
        }

        return ordered;
    }

    protected SchedulerCertifiedProductSearchDetailsAsync getCertifiedProductDetailsAsyncRetrievalHelper()
            throws BeansException {
        return this.schedulerCertifiedProductSearchDetailsAsync;
    }

}
