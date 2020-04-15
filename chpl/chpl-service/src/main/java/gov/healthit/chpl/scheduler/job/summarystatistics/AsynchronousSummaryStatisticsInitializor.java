package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;

@Component("asynchronousSummaryStatisticsInitializor")
@EnableAsync
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
        Future<Long> totalActive2014Listings = null;
        Future<Long> totalActive2015Listings = null;
        Future<List<CertifiedBodyStatistics>> totalActiveListingsByCertifiedBody = null;
        Future<Long> totalDevelopersWithActive2014Listings = null;
        Future<Long> totalDevelopersWithActive2015Listings = null;
        Future<List<CertifiedBodyStatistics>> totalCPListingsEachYearByCertifiedBody = null;
        Future<List<CertifiedBodyStatistics>> totalCPListingsEachYearByCertifiedBodyAndCertificationStatus = null;
        Future<Long> totalCPs2014Listings = null;
        Future<Long> totalCPs2015Listings = null;
        Future<Long> totalCPsSuspended2014Listings = null;
        Future<Long> totalCPsSuspended2015Listings = null;
        Future<Long> totalListingsWithAlternateTestMethods = null;
        Future<List<CertifiedBodyAltTestStatistics>> totalListingsWithCertifiedBodyAndAlternativeTestMethods = null;

        Future<Long> averageTimeToAssessConformity = null;
        Future<Long> averageTimeToApproveCAP = null;
        Future<Long> averageDurationOfCAP = null;
        Future<Long> averageTimeFromCAPApprovalToSurveillanceEnd = null;
        Future<Long> averageTimeFromCAPEndToSurveillanceEnd = null;
        Future<Long> averageTimeFromSurveillanceOpenToSurveillanceClose = null;
        Future<Map<Long, Long>> openCAPCountByAcb = null;
        Future<Map<Long, Long>> closedCAPCountByAcb = null;
        Future<Long> averageTimeToCloseSurveillance = null;

        Future<List<CertifiedBodyStatistics>> uniqueDevelopersCountWithCuresUpdatedListingsByAcb = null;
        Future<List<CertifiedBodyStatistics>> uniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb = null;
        Future<List<CertifiedBodyStatistics>> uniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb = null;

        Future<List<CertifiedBodyStatistics>> uniqueProductsCountWithCuresUpdatedListingsByAcb = null;
        Future<List<CertifiedBodyStatistics>> uniqueProductsCountWithCuresUpdatedActiveListingsByAcb = null;
        Future<List<CertifiedBodyStatistics>> uniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb = null;

        Future<List<CertifiedBodyStatistics>> activeListingCountWithCuresUpdatedByAcb = null;

        Future<Long> allListingsCountWithCuresUpdated = null;
        Future<Long> allListingsCountWithCuresUpdatedWithAlternativeTestMethods = null;

        if (dateRange == null) {
            totalActive2014Listings = asyncStats.getTotalActive2014Listings(listingStatisticsDAO, dateRange);
            totalActive2015Listings = asyncStats.getTotalActive2015Listings(listingStatisticsDAO, dateRange);
            totalActiveListingsByCertifiedBody = asyncStats.getTotalActiveListingsByCertifiedBody(listingStatisticsDAO,
                    dateRange);
            totalDevelopersWithActive2014Listings = asyncStats.getTotalDevelopersWithActive2014Listings(developerStatisticsDAO,
                    dateRange);
            totalDevelopersWithActive2015Listings = asyncStats.getTotalDevelopersWithActive2015Listings(developerStatisticsDAO,
                    dateRange);
            totalCPListingsEachYearByCertifiedBody = asyncStats.getTotalCPListingsEachYearByCertifiedBody(listingStatisticsDAO,
                    dateRange);
            totalCPListingsEachYearByCertifiedBodyAndCertificationStatus = asyncStats
                    .getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(listingStatisticsDAO, dateRange);
            totalCPs2014Listings = asyncStats.getTotalCPs2014Listings(listingStatisticsDAO, dateRange);
            totalCPs2015Listings = asyncStats.getTotalCPs2015Listings(listingStatisticsDAO, dateRange);
            totalCPsSuspended2014Listings = asyncStats.getTotalCPsSuspended2014Listings(listingStatisticsDAO, dateRange);
            totalCPsSuspended2015Listings = asyncStats.getTotalCPsSuspended2015Listings(listingStatisticsDAO, dateRange);
            totalListingsWithAlternateTestMethods = asyncStats.getTotalListingsWithAlternateTestMethods(listingStatisticsDAO);
            totalListingsWithCertifiedBodyAndAlternativeTestMethods = asyncStats
                    .getTotalListingsWithCertifiedBodyAndAlternativeTestMethods(listingStatisticsDAO);

            averageTimeToAssessConformity = asyncStats.getAverageTimeToAssessConformity(surveillanceStatisticsDAO);
            averageTimeToApproveCAP = asyncStats.getAverageTimeToApproveCAP(surveillanceStatisticsDAO);
            averageDurationOfCAP = asyncStats.getAverageDurationOfCAP(surveillanceStatisticsDAO);
            averageTimeFromCAPApprovalToSurveillanceEnd = asyncStats
                    .getAverageTimeFromCAPApprovalToSurveillanceClose(surveillanceStatisticsDAO);
            averageTimeFromCAPEndToSurveillanceEnd = asyncStats
                    .getAverageTimeFromCAPEndToSurveillanceClose(surveillanceStatisticsDAO);
            averageTimeFromSurveillanceOpenToSurveillanceClose = asyncStats
                    .getAverageTimeFromSurveillanceOpenToSurveillanceClose(surveillanceStatisticsDAO);
            openCAPCountByAcb = asyncStats.getOpenCAPCountByAcb(surveillanceStatisticsDAO);
            closedCAPCountByAcb = asyncStats.getClosedCAPCountByAcb(surveillanceStatisticsDAO);
            averageTimeToCloseSurveillance = asyncStats.getAverageTimeToCloseSurveillance(surveillanceStatisticsDAO);

            uniqueDevelopersCountWithCuresUpdatedListingsByAcb = asyncStats
                    .getUniqueDevelopersCountWithCuresUpdatedListingsByAcb(certifiedProductDAO);
            uniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb = asyncStats
                    .getUniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb(certifiedProductDAO);
            uniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb = asyncStats
                    .getUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb(certifiedProductDAO);

            uniqueProductsCountWithCuresUpdatedListingsByAcb = asyncStats
                    .getUniqueProductsCountWithCuresUpdatedListingsByAcb(certifiedProductDAO);
            uniqueProductsCountWithCuresUpdatedActiveListingsByAcb = asyncStats
                    .getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(certifiedProductDAO);
            uniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb = asyncStats
                    .getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(certifiedProductDAO);

            activeListingCountWithCuresUpdatedByAcb = asyncStats
                    .getActiveListingCountWithCuresUpdatedByAcb(certifiedProductDAO);

            allListingsCountWithCuresUpdated = asyncStats.getAllListingsCountWithCuresUpdated(certifiedProductDAO);
            allListingsCountWithCuresUpdatedWithAlternativeTestMethods = asyncStats
                    .getAllListingsCountWithCuresUpdatedWithAlternativeTestMethods(certifiedProductDAO, certificationResultDAO);
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
        Future<Long> totalDeveloperswith2015Listings = asyncStats.getTotalDevelopersWith2015Listings(developerStatisticsDAO,
                dateRange);

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

        if (dateRange == null) {
            stats.setTotalActive2014Listings(totalActive2014Listings.get());
            stats.setTotalActive2015Listings(totalActive2015Listings.get());
            stats.setTotalActiveListingsByCertifiedBody(totalActiveListingsByCertifiedBody.get());
            stats.setTotalDevelopersWithActive2014Listings(totalDevelopersWithActive2014Listings.get());
            stats.setTotalDevelopersWithActive2015Listings(totalDevelopersWithActive2015Listings.get());
            stats.setTotalCPListingsEachYearByCertifiedBody(totalCPListingsEachYearByCertifiedBody.get());
            stats.setTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
                    totalCPListingsEachYearByCertifiedBodyAndCertificationStatus.get());
            stats.setTotalCPs2014Listings(totalCPs2014Listings.get());
            stats.setTotalCPs2015Listings(totalCPs2015Listings.get());
            stats.setTotalCPsSuspended2014Listings(totalCPsSuspended2014Listings.get());
            stats.setTotalCPsSuspended2015Listings(totalCPsSuspended2015Listings.get());
            stats.setTotalListingsWithAlternativeTestMethods(totalListingsWithAlternateTestMethods.get());
            stats.setTotalListingsWithCertifiedBodyAndAlternativeTestMethods(
                    totalListingsWithCertifiedBodyAndAlternativeTestMethods.get());

            stats.setAverageTimeToAssessConformity(averageTimeToAssessConformity.get());
            stats.setAverageTimeToApproveCAP(averageTimeToApproveCAP.get());
            stats.setAverageDurationOfCAP(averageDurationOfCAP.get());
            stats.setAverageTimeFromCAPApprovalToSurveillanceEnd(averageTimeFromCAPApprovalToSurveillanceEnd.get());
            stats.setAverageTimeFromCAPEndToSurveillanceEnd(averageTimeFromCAPEndToSurveillanceEnd.get());
            stats.setAverageTimeFromSurveillanceOpenToSurveillanceClose(averageTimeFromSurveillanceOpenToSurveillanceClose.get());
            stats.setOpenCAPCountByAcb(openCAPCountByAcb.get());
            stats.setClosedCAPCountByAcb(closedCAPCountByAcb.get());
            stats.setAverageTimeToCloseSurveillance(averageTimeToCloseSurveillance.get());

            stats.setUniqueDevelopersCountWithCuresUpdatedListingsByAcb(uniqueDevelopersCountWithCuresUpdatedListingsByAcb.get());
            stats.setUniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb(
                    uniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb.get());
            stats.setUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb(
                    uniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb.get());

            stats.setUniqueProductsCountWithCuresUpdatedListingsByAcb(uniqueProductsCountWithCuresUpdatedListingsByAcb.get());
            stats.setUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(
                    uniqueProductsCountWithCuresUpdatedActiveListingsByAcb.get());
            stats.setUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(
                    uniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb.get());

            stats.setActiveListingCountWithCuresUpdatedByAcb(activeListingCountWithCuresUpdatedByAcb.get());

            stats.setAllListingsCountWithCuresUpdated(allListingsCountWithCuresUpdated.get());
            stats.setAllListingsCountWithCuresUpdatedWithAlternativeTestMethods(
                    allListingsCountWithCuresUpdatedWithAlternativeTestMethods.get());
        }

        // developers
        stats.setTotalDevelopers(totalDevelopers.get());
        stats.setTotalDevelopersWith2014Listings(totalDevelopersWith2014Listings.get());

        stats.setTotalDevelopersByCertifiedBodyWithListingsEachYear(
                totalDevelopersByCertifiedBodyWithListingsEachYear.get());
        stats.setTotalDevsByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
                totalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear.get());
        stats.setTotalDevelopersWith2015Listings(totalDeveloperswith2015Listings.get());

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
