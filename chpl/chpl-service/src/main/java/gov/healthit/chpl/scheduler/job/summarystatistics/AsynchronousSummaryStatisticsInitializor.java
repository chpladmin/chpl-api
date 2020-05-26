package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

@Component("asynchronousSummaryStatisticsInitializor")
public class AsynchronousSummaryStatisticsInitializor {
    private Logger logger;

    private AsynchronousSummaryStatistics asyncStats;
    private DeveloperStatisticsDAO developerStatisticsDAO;
    private ListingStatisticsDAO listingStatisticsDAO;
    private SurveillanceStatisticsDAO surveillanceStatisticsDAO;
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationResultDAO certificationResultDAO;

    @Autowired
    public AsynchronousSummaryStatisticsInitializor(AsynchronousSummaryStatistics asyncStats,
            DeveloperStatisticsDAO developerStatisticsDAO, ListingStatisticsDAO listingStatisticsDAO,
            SurveillanceStatisticsDAO surveillanceStatisticsDAO, CertifiedProductDAO certifiedProductDAO,
            CertificationResultDAO certificationResultDAO) {
        this.asyncStats = asyncStats;
        this.developerStatisticsDAO = developerStatisticsDAO;
        this.listingStatisticsDAO = listingStatisticsDAO;
        this.surveillanceStatisticsDAO = surveillanceStatisticsDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.certificationResultDAO = certificationResultDAO;
    }

    @Transactional
    @Async
    public Future<Statistics> getStatistics(DateRange dateRange)
            throws InterruptedException, ExecutionException {
        asyncStats.setLogger(getLogger());

        if (dateRange == null) {
            getLogger().info("Getting all current statistics.");
        } else {
            getLogger().info("Getting statistics for start date " + dateRange.getStartDate() + " end date "
                    + dateRange.getEndDate());
        }

        Statistics stats = new Statistics();
        stats.setDateRange(dateRange);

        if (dateRange == null) {
            List<CertifiedProductDetailsDTO> listingsAll2015 = certifiedProductDAO.findByEdition("2015");

            /////////////////////////////////////////////////////////////////////////////////////
            //Developer Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of Developers with Active 2014 Listings
            stats.setTotalDevelopersWithActive2014Listings(asyncStats.getTotalDevelopersWithActive2014Listings(dateRange));
            // Total # of Developers with 2015 Listings or 2015 Cures Update Listings (Regardless of Status)
            stats.setUniqueDevelopersCountForAny2015ListingsByAcb(asyncStats.getUniqueDevelopersCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH));
            // Total # of Developers with Active 2015 Listings or 2015 Cures Update Listings
            stats.setUniqueDevelopersCountForAny2015ActiveListingsByAcb(asyncStats.getUniqueDevelopersCountFor2015ActiveListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH));
            // Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings
            stats.setUniqueDevelopersCountForAny2015SuspendedListingsByAcb(asyncStats.getUniqueDevelopersCountFor2015SuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH));
            // Total # of Developers with 2015-Cures Update Listings (Regardless of Status)
            stats.setUniqueDevelopersCountWithCuresUpdatedListingsByAcb(asyncStats.getUniqueDevelopersCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES));
            // Total # of Developers with Active 2015-Cures Update Listings
            stats.setUniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb(asyncStats.getUniqueDevelopersCountFor2015ActiveListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES));
            // Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015-Cures Update Listings
            stats.setUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb(asyncStats.getUniqueDevelopersCountFor2015SuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES));
            // Total # of Developers with 2015 Listings (Regardless of Status)
            stats.setUniqueDevelopersCountWithoutCuresUpdatedListingsByAcb(asyncStats.getUniqueDevelopersCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.CURES));
            // Total # of Developers with Active 2015 Listings
            stats.setUniqueDevelopersCountWithoutCuresUpdatedActiveListingsByAcb(asyncStats.getUniqueDevelopersCountFor2015ActiveListingsByAcb(listingsAll2015, Edition2015Criteria.CURES));
            // Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings
            stats.setUniqueDevelopersCountWithoutCuresUpdatedSuspendedListingsByAcb(asyncStats.getUniqueDevelopersCountFor2015SuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.CURES));

            /////////////////////////////////////////////////////////////////////////////////////
            //Product Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Used for multiple sections
            stats.setTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(asyncStats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(dateRange));

            // Total # of Unique Products with 2014 Listings (Regardless of Status)
            stats.setTotalCPListingsEachYearByCertifiedBody(asyncStats.getTotalCPListingsEachYearByCertifiedBody(dateRange));
            // Total # of Unique Products with Active 2014 Listings
            stats.setTotalCPs2014Listings(asyncStats.getTotalCPs2014Listings(dateRange));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2014 Listings
            stats.setTotalCPsSuspended2014Listings(asyncStats.getTotalCPsSuspended2014Listings(dateRange));
            // Total # of Unique Products with 2015 Listings or 2015 Cures Update Listings
            stats.setUniqueProductsCountForAny2015ListingsByAcb(asyncStats.getUniqueProductsCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH));
            // Total # of Unique Products with Active 2015 Listings or 2015 Cures Update Listings
            stats.setUniqueProductsCountForAny2015ActiveListingsByAcb(asyncStats.getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings
            stats.setUniqueProductsCountForAny2015SuspendedListingsByAcb(asyncStats.getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH));
            // Total # of Unique Products with 2015 Listings
            stats.setUniqueProductsCountWithoutCuresUpdatedListingsByAcb(asyncStats.getUniqueProductsCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.CURES));
            // Total # of Unique Products with Active 2015 Listings
            stats.setUniqueProductsCountWithoutCuresUpdatedActiveListingsByAcb(asyncStats.getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(listingsAll2015, Edition2015Criteria.CURES));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings
            stats.setUniqueProductsCountWithoutCuresUpdatedSuspendedListingsByAcb(asyncStats.getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.CURES));
            // Total # of Unique Products with 2015-Cures Update Listings
            stats.setUniqueProductsCountWithCuresUpdatedListingsByAcb(asyncStats.getUniqueProductsCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES));
            // Total # of Unique Products with Active 2015-Cures Update Listings
            stats.setUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(asyncStats.getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015-Cures Update Listings
            stats.setUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(asyncStats.getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES));

            /////////////////////////////////////////////////////////////////////////////////////
            //Listing Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of Active (Including Suspended by ONC/ONC-ACB 2014 Listings)
            stats.setTotalActive2014Listings(asyncStats.getTotalActive2014Listings(null));
            stats.setTotalActiveListingsByCertifiedBody(asyncStats.getTotalActiveListingsByCertifiedBody(dateRange));
            // Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Listings)
            stats.setTotalActive2015Listings(asyncStats.getTotalActive2015Listings(dateRange));
            //Total # of 2015 Listings with Alternative Test Methods
            stats.setTotalListingsWithAlternativeTestMethods(asyncStats.getTotalListingsWithAlternateTestMethods());
            // Total # of 2015 Listings with Alternative Test Methods
            stats.setTotalListingsWithCertifiedBodyAndAlternativeTestMethods(asyncStats.getTotalListingsWithCertifiedBodyAndAlternativeTestMethods());


            // Total # of Active (Including Suspended by ONC/ONC-ACB 2015-Cures Update Listings)
            stats.setActiveListingCountWithCuresUpdatedByAcb(asyncStats.getActiveListingCountWithCuresUpdatedByAcb(listingsAll2015));
            // Total # of 2015-Cures Update Listings with Alternative Test Methods
            stats.setListingCountWithCuresUpdatedAndAltTestMethodsByAcb(asyncStats.getListingCountFor2015AndAltTestMethodsByAcb(listingsAll2015, certificationResultDAO));
            // Total # of 2015-Cures Updated Listings (Regardless of Status)
            stats.setAllListingsCountWithCuresUpdated(asyncStats.getAllListingsCountWithCuresUpdated(listingsAll2015));

            /////////////////////////////////////////////////////////////////////////////////////
            // Surveillance Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Average Duration of Closed Surveillance (in days)
            stats.setAverageTimeToCloseSurveillance(asyncStats.getAverageTimeToCloseSurveillance());

            /////////////////////////////////////////////////////////////////////////////////////
            // Non-Conformity Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Average Time to Assess Conformity (in days)
            stats.setAverageTimeToAssessConformity(asyncStats.getAverageTimeToAssessConformity());
            // Average Time to Approve CAP (in days)
            stats.setAverageTimeToApproveCAP(asyncStats.getAverageTimeToApproveCAP());
            // Average Duration of CAP (in days) (includes closed and ongoing CAPs)
            stats.setAverageDurationOfCAP(asyncStats.getAverageDurationOfCAP());
            // Average Time from CAP Approval to Surveillance Close (in days)
            stats.setAverageTimeFromCAPApprovalToSurveillanceEnd(asyncStats.getAverageTimeFromCAPApprovalToSurveillanceClose(surveillanceStatisticsDAO));
            // Average Time from CAP Close to Surveillance Close (in days)
            stats.setAverageTimeFromCAPEndToSurveillanceEnd(asyncStats.getAverageTimeFromCAPEndToSurveillanceClose());
            // Average Duration of Closed Non-Conformities (in days)
            stats.setAverageTimeFromSurveillanceOpenToSurveillanceClose(asyncStats.getAverageTimeFromSurveillanceOpenToSurveillanceClose());
            // Number of Open CAPs
            stats.setOpenCAPCountByAcb(asyncStats.getOpenCAPCountByAcb());
            // Number of Closed CAPs
            stats.setClosedCAPCountByAcb(asyncStats.getClosedCAPCountByAcb());

        }

        // developers
        Future<Long> totalDevelopers = asyncStats.getTotalDevelopers(developerStatisticsDAO, dateRange);
        Future<Long> totalDevelopersWith2014Listings = asyncStats.getTotalDevelopersWith2014Listings(developerStatisticsDAO,
                dateRange);

        Future<List<CertifiedBodyStatistics>> totalDevelopersByCertifiedBodyWithListingsEachYear = asyncStats
                .getTotalDevelopersByCertifiedBodyWithListingsEachYear(developerStatisticsDAO, dateRange);
        Future<List<CertifiedBodyStatistics>> totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear = asyncStats
                .getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
                        developerStatisticsDAO, dateRange);

        // listings
        Future<Long> totalCertifiedProducts = asyncStats.getTotalCertifiedProducts(listingStatisticsDAO, dateRange);

        Future<Long> totalCPsActive2014Listings = asyncStats.getTotalCPsActive2014Listings(listingStatisticsDAO, dateRange);

        Future<Long> totalCPsActive2015Listings = asyncStats.getTotalCPsActive2015Listings(listingStatisticsDAO, dateRange);

        Future<Long> totalCPsActiveListings = asyncStats.getTotalCPsActiveListings(listingStatisticsDAO, dateRange);
        Future<Long> totalListings = asyncStats.getTotalListings(listingStatisticsDAO, dateRange);
        Future<Long> total2014Listings = asyncStats.getTotal2014Listings(listingStatisticsDAO, dateRange);
        Future<Long> total2015Listings = asyncStats.getTotal2015Listings(listingStatisticsDAO, dateRange);
        Future<Long> total2011Listings = asyncStats.getTotal2011Listings(listingStatisticsDAO, dateRange);

        // surveillance
        Future<Long> totalSurveillanceActivities = asyncStats.getTotalSurveillanceActivities(surveillanceStatisticsDAO,
                dateRange);
        Future<Long> totalOpenSurveillanceActivities = asyncStats.getTotalOpenSurveillanceActivities(surveillanceStatisticsDAO,
                dateRange);
        Future<Long> totalClosedSurveillanceActivities = asyncStats
                .getTotalClosedSurveillanceActivities(surveillanceStatisticsDAO, dateRange);
        Future<Long> totalNonConformities = asyncStats.getTotalNonConformities(surveillanceStatisticsDAO, dateRange);
        Future<Long> totalOpenNonConformities = asyncStats.getTotalOpenNonconformities(surveillanceStatisticsDAO, dateRange);
        Future<Long> totalClosedNonConformities = asyncStats.getTotalClosedNonconformities(surveillanceStatisticsDAO, dateRange);

        Future<List<CertifiedBodyStatistics>> totalOpenNonConformitiesByAcb = asyncStats
                .getTotalOpenNonconformitiesByAcb(surveillanceStatisticsDAO, dateRange);
        Future<List<CertifiedBodyStatistics>> totalOpenNonSurveillancesByAcb = asyncStats
                .getTotalOpenSurveillancesByAcb(surveillanceStatisticsDAO, dateRange);


        // developers
        stats.setTotalDevelopers(totalDevelopers.get());
        stats.setTotalDevelopersWith2014Listings(totalDevelopersWith2014Listings.get());

        stats.setTotalDevelopersByCertifiedBodyWithListingsEachYear(
                totalDevelopersByCertifiedBodyWithListingsEachYear.get());
        stats.setTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
                totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear.get());
        //stats.setTotalDevelopersWith2015Listings(totalDeveloperswith2015Listings.get());

        // listings
        stats.setTotalCertifiedProducts(totalCertifiedProducts.get());
        stats.setTotalCPsActiveListings(totalCPsActiveListings.get());
        stats.setTotalCPsActive2014Listings(totalCPsActive2014Listings.get());
        stats.setTotalCPsActive2015Listings(totalCPsActive2015Listings.get());
        stats.setTotalListings(totalListings.get());
        stats.setTotal2014Listings(total2014Listings.get());
        stats.setTotal2015Listings(total2015Listings.get());
        stats.setTotal2011Listings(total2011Listings.get());
        // surveillance
        stats.setTotalSurveillanceActivities(totalSurveillanceActivities.get());
        stats.setTotalOpenSurveillanceActivities(totalOpenSurveillanceActivities.get());
        stats.setTotalClosedSurveillanceActivities(totalClosedSurveillanceActivities.get());
        stats.setTotalNonConformities(totalNonConformities.get());
        stats.setTotalOpenNonconformities(totalOpenNonConformities.get());
        stats.setTotalClosedNonconformities(totalClosedNonConformities.get());

        stats.setTotalOpenNonconformitiesByAcb(totalOpenNonConformitiesByAcb.get());
        stats.setTotalOpenSurveillanceActivitiesByAcb(totalOpenNonSurveillancesByAcb.get());

        return new AsyncResult<>(stats);
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = LogManager.getLogger(AsynchronousSummaryStatisticsInitializor.class);
        }
        return logger;
    }
}
