package gov.healthit.chpl.scheduler.job.onetime;

import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowConsumer;
import static gov.healthit.chpl.util.LambdaExceptionUtil.rethrowFunction;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.activity.history.ListingActivityUtil;
import gov.healthit.chpl.activity.history.explorer.ChplProductNumberChangedActivityExplorer;
import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.dao.CertifiedProductChplProductNumberHistoryDao;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductChplProductNumberHistory;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.DateUtil;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "populateChplProductNumberHistoryJobLogger")
public class PopulateChplProductNumberHistoryJob implements Job {

    @Autowired
    private ChplProductNumberChangedActivityExplorer chplProductNumberChangedactivityExplorer;

    @Autowired
    private CertifiedProductsWithNewStyleNumberDao certifiedProductsWithNewStyleNumberDao;

    @Autowired
    @Qualifier("transactionalCertifiedProductChplProductNumberHistoryDao")
    private TransactionalCertifiedProductChplProductNumberHistoryDao chplProductNumberChangeDao;

    @Autowired
    private ListingActivityUtil activityUtil;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Populate CHPL Product Number History job *********");
        try {
            LOGGER.info("Getting all listings with new-style CHPL Product Numbers...");
            List<CertifiedProduct> allListingsWithNewStyleChplProductNumbers
                = certifiedProductsWithNewStyleNumberDao.findWithNewStyleChplProductNumber();
            LOGGER.info("Found " + allListingsWithNewStyleChplProductNumbers.size() + " listings with new-style CHPL Product Numbers.");
            allListingsWithNewStyleChplProductNumbers.stream()
                .forEach(listing -> addPreviousChplProductNumbersForListing(listing));
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("********* Completed the Populate CHPL Product Number History job *********");
    }

    private void addPreviousChplProductNumbersForListing(CertifiedProduct listing) {
        try {
            LOGGER.info("Looking for activities where the CHPL Product Number changed for listing " + listing.getId() + ".");
            List<ActivityDTO> activitiesWithChplProductNumberChanges
                = chplProductNumberChangedactivityExplorer.getActivities(new ListingActivityQuery(listing.getId()));
            LOGGER.info("Found " + activitiesWithChplProductNumberChanges.size() + " activities where the CHPL "
                    + "Product Number changed for listing " + listing.getId() + ".");

            List<CertifiedProductChplProductNumberHistory> chplProductNumberHistory
                = chplProductNumberChangeDao.getHistoricalChplProductNumbers(listing.getId());

            activitiesWithChplProductNumberChanges.stream()
                .map(rethrowFunction(activity -> toChplProductNumberHistory(activity)))
                .filter(historyItem -> historyItem != null)
                .filter(historyItem -> !doesChplProductNumberHistoryContain(historyItem, chplProductNumberHistory))
                .forEach(rethrowConsumer(historyItem -> addChplProductNumberHistory(listing.getId(), historyItem)));
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Error getting previous CHPL product numbers for listing " + listing.getId(), ex);
        } catch (EntityCreationException ex) {
            LOGGER.error("Error creating new CHPL product number history entry for listing " + listing.getId() + ".", ex);
        } catch (Exception ex) {
            LOGGER.error("Unexpected exception processing listing " + listing.getId() + ".", ex);
        }
    }

    private CertifiedProductChplProductNumberHistory toChplProductNumberHistory(ActivityDTO activity) {
        CertifiedProductSearchDetails origListing = null;
        if (activity.getOriginalData() != null) {
            origListing = activityUtil.getListing(activity.getOriginalData());
        }

        if (origListing == null) {
            LOGGER.error("No CertifiedProductSearchDetails object was found in the original data of activity with ID " + activity.getId());
            return null;
        }

        return CertifiedProductChplProductNumberHistory.builder()
                .chplProductNumber(origListing.getChplProductNumber())
                .endDateTime(DateUtil.toLocalDateTime(activity.getActivityDate().getTime()))
                .build();
    }

    private boolean doesChplProductNumberHistoryContain(CertifiedProductChplProductNumberHistory historyItem,
            List<CertifiedProductChplProductNumberHistory> chplProductNumberHistory) {
        boolean result = chplProductNumberHistory.contains(historyItem);
        if (result) {
            LOGGER.info("CHPL Product Number History already contains " + historyItem.getChplProductNumber() + " on " + historyItem.getEndDateTime());
        }
        return result;
    }

    private void addChplProductNumberHistory(Long listingId, CertifiedProductChplProductNumberHistory historyItem)
        throws EntityCreationException {
        LOGGER.info("Adding CHPL Product Number History " + historyItem.getChplProductNumber() + " on " + historyItem.getEndDateTime()
                + " to listing " + listingId + ".");
        chplProductNumberChangeDao.createChplProductNumberHistoryMapping(listingId, historyItem);
    }

    @Component("certifiedProductsWithNewStyleNumberDao")
    @NoArgsConstructor
    private static class CertifiedProductsWithNewStyleNumberDao extends BaseDAOImpl {

        @Transactional
        public List<CertifiedProduct> findWithNewStyleChplProductNumber() {
            List<CertifiedProductDetailsEntity> entities = entityManager.createQuery(
                    "SELECT DISTINCT cp "
                    + "FROM CertifiedProductDetailsEntity cp "
                    + "WHERE chplProductNumber NOT LIKE 'CHP-%' "
                    + "AND deleted = false",
                    CertifiedProductDetailsEntity.class).getResultList();

            List<CertifiedProduct> results = new ArrayList<CertifiedProduct>();
            for (CertifiedProductDetailsEntity entity : entities) {
                CertifiedProduct cp = CertifiedProduct.builder()
                        .id(entity.getId())
                        .chplProductNumber(entity.getChplProductNumber())
                        .certificationDate(entity.getCertificationDate().getTime())
                        .certificationStatus(entity.getCertificationStatusName())
                        .curesUpdate(entity.getCuresUpdate())
                        .edition(entity.getYear())
                        .lastModifiedDate(entity.getLastModifiedDate().getTime())
                        .build();
                results.add(cp);
            }
            return results;
        }
    }

    @Component("transactionalCertifiedProductChplProductNumberHistoryDao")
    @NoArgsConstructor
    private static class TransactionalCertifiedProductChplProductNumberHistoryDao extends CertifiedProductChplProductNumberHistoryDao {

        @Transactional
        public Long createChplProductNumberHistoryMapping(Long listingId, CertifiedProductChplProductNumberHistory historyItem) throws EntityCreationException {
            return super.createChplProductNumberHistoryMapping(listingId, historyItem);
        }

        @Transactional
        public List<CertifiedProductChplProductNumberHistory> getHistoricalChplProductNumbers(Long listingId) throws EntityRetrievalException {
            return super.getHistoricalChplProductNumbers(listingId);
        }
    }
}
