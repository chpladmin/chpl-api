package gov.healthit.chpl.scheduler.job.certificationStatus;

import java.time.LocalDate;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.ListingSearchCacheRefresh;
import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.search.CertifiedProductSearchManager;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "updateCurrentCertificationStatusJobLogger")
public class UpdateCurrentCertificationStatusJob extends QuartzJob {
    public static final String JOB_NAME = "updateCurrentCertificationStatusJob";
    public static final String LISTING_ID = "listingId";
    public static final String USER = "user";
    public static final String ACTIVITY_ID = "activityId";
    public static final String CERTIFICATION_STATUS_EVENT_DAY = "certificationStatusEventDay";
    public static final String USER_PROVIDED_REASON = "reason";

    @Autowired
    private CertifiedProductDetailsManager cpdManager;

    @Autowired
    private TransactionalDeveloperBanHelper txDeveloperBanHelper;

    @Autowired
    private TransactionalSubscriptionObservationHelper txObservationHelper;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private CertifiedProductSearchManager certifiedProductSearchManager;

    private CertifiedProductSearchDetails currentListing;
    private JWTAuthenticatedUser user;
    private Long activityId;
    private LocalDate certificationStatusEventDay;
    private String reason;

    @ListingSearchCacheRefresh
    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Future-to-Current Certification Status Job. *********");

        parseJobInput(jobContext);
        if (user == null || currentListing == null || activityId == null || certificationStatusEventDay == null) {
            LOGGER.fatal("Missing some required job data.");
        } else {
            CertificationStatus currentStatus = currentListing.getCurrentStatus().getStatus();
            CertificationStatus yesterdaysStatus = currentListing.getStatusOnDate(DateUtil.toDate(LocalDate.now().minusDays(1))).getStatus();
            if (currentStatus.getName().equals(yesterdaysStatus.getName())) {
                LOGGER.error("Listing ID " + currentListing.getId() + " has current status " + currentStatus.getName()
                    + " which is the same as the listing status yesterday. Nothing to do.");
                return;
            } else {
                LOGGER.info("Listing ID " + currentListing.getId() + " has current status " + currentStatus.getName()
                    + " which is different from yesterday's status " + yesterdaysStatus.getName());

                setSecurityContext(user);
                try {
                    LOGGER.info("Handling any necessary developer bans related to certification status change...");
                    txDeveloperBanHelper.handleCertificationStatusChange(currentListing,
                            (JWTAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication(),
                            reason);
                } catch (Exception ex) {
                    LOGGER.error("There was a failure handling the certification status change of listing "
                            + currentListing.getId() + " from " + yesterdaysStatus.getName() + " to " + currentStatus.getName()
                            + ". Check the status of the developer because it may need to be under certification ban.");
                }

                try {
                    LOGGER.info("Handling any necessary subscription observation updates related to certification status change...");
                    txObservationHelper.handleCertificationStatusChange(currentListing, activityId);
                } catch (Exception ex) {
                    LOGGER.error("There was a failure handling the certification status change of listing "
                            + currentListing.getId() + " from " + yesterdaysStatus.getName() + " to " + currentStatus.getName()
                            + ". Subscribers may not be correctly notified of this change.");
                }

                LOGGER.info("Refreshing searchable listing collection (deprecated)");
                cacheManager.getCache(CacheNames.COLLECTIONS_LISTINGS).invalidate();
                certifiedProductSearchManager.getFlatListingCollection();
                LOGGER.info("Completed refreshing searchable listing collection (deprecated)");
            }
        }
        LOGGER.info("********* Completed the Future-to-Current Certification Status Job. *********");
    }

    private void parseJobInput(JobExecutionContext jobContext) {
        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        user = (JWTAuthenticatedUser) jobDataMap.get(USER);
        Long listingId = jobDataMap.getLong(LISTING_ID);
        if (listingId != null) {
            try {
                currentListing = cpdManager.getCertifiedProductDetailsNoCache(listingId);
            } catch (Exception ex) {
                LOGGER.error("Could not find listing with ID " + listingId, ex);
            }
        }
        activityId = jobDataMap.getLong(ACTIVITY_ID);
        certificationStatusEventDay = (LocalDate) jobDataMap.get(CERTIFICATION_STATUS_EVENT_DAY);
        reason = jobDataMap.getString(USER_PROVIDED_REASON);
    }
}