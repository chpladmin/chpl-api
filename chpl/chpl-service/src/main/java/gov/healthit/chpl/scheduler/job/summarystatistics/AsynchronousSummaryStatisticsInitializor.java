package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public Statistics getCurrentStatistics() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        asyncStats.setLogger(getLogger());
        getLogger().info("Getting all current statistics.");

        Statistics stats = new Statistics();
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        List<CertifiedProductDetailsDTO> listingsAll2015 = certifiedProductDAO.findByEdition("2015");

        try {
            /////////////////////////////////////////////////////////////////////////////////////
            //Developer Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            //Used for multiple sections
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(null), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(result)));
            // Total # of Unique Developers (Regardless of Edition)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalDevelopers(null), executorService)
                    .thenAccept(result -> stats.setTotalDevelopers(result)));
            // Total # of Developers with 2014 Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalDevelopersWith2014Listings(null), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersWith2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalDevelopersByCertifiedBodyWithListingsEachYear(null), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersByCertifiedBodyWithListingsEachYear(result)));
            // Total # of Developers with Active 2014 Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalDevelopersWithActive2014Listings(null), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersWithActive2014Listings(result)));

            //**********************
            // 2015 Regular and Cures Listings
            //**********************
            // Total # of Developers with 2015 Listings or 2015 Cures Update Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueDevelopersCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountForAny2015ListingsByAcb(result)));
            // Total # of Developers with Active 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueDevelopersCountFor2015ActiveListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountForAny2015ActiveListingsByAcb(result)));
            // Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueDevelopersCountFor2015SuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountForAny2015SuspendedListingsByAcb(result)));

            //**********************
            // 2015 Cures Listings
            //**********************
            // Total # of Developers with 2015-Cures Update Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueDevelopersCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithCuresUpdatedListingsByAcb(result)));
            // Total # of Developers with Active 2015-Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueDevelopersCountFor2015ActiveListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb(result)));
            // Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015-Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueDevelopersCountFor2015SuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb(result)));

            //**********************
            // 2015 Regular Listings
            //**********************
            // Total # of Developers with 2015 Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueDevelopersCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithoutCuresUpdatedListingsByAcb(result)));
            // Total # of Developers with Active 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueDevelopersCountFor2015ActiveListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithoutCuresUpdatedActiveListingsByAcb(result)));
            // Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueDevelopersCountFor2015SuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithoutCuresUpdatedSuspendedListingsByAcb(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            //Product Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Used for multiple sections
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(null), executorService)
                    .thenAccept(result -> stats.setTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(result)));
            //Total # of Certified Unique Products Regardless of Status or Edition - Including 2011)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalCertifiedProducts(null), executorService)
                    .thenAccept(result -> stats.setTotalCertifiedProducts(result)));
            // Total # of Unique Products with 2014 Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotal2014Listings(null), executorService)
                    .thenAccept(result -> stats.setTotalCPs2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalCPListingsEachYearByCertifiedBody(null), executorService)
                    .thenAccept(result -> stats.setTotalCPListingsEachYearByCertifiedBody(result)));
            // Total # of Unique Products with Active 2014 Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalCPs2014Listings(null), executorService)
                    .thenAccept(result -> stats.setTotalCPsActive2014Listings(result)));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2014 Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalCPsSuspended2014Listings(null), executorService)
                    .thenAccept(result -> stats.setTotalCPsSuspended2014Listings(result)));

            //**********************
            // 2015 Regular and Cures Listings
            //**********************
            // Total # of Unique Products with 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueProductsCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountForAny2015ListingsByAcb(result)));
            // Total # of Unique Products with Active 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountForAny2015ActiveListingsByAcb(result)));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountForAny2015SuspendedListingsByAcb(result)));

            //**********************
            // 2015 Regular Listings
            //**********************
            // Total # of Unique Products with 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueProductsCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithoutCuresUpdatedListingsByAcb(result)));
            // Total # of Unique Products with Active 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithoutCuresUpdatedActiveListingsByAcb(result)));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithoutCuresUpdatedSuspendedListingsByAcb(result)));

            //**********************
            // 2015 Cures Listings
            //**********************
            // Total # of Unique Products with 2015-Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueProductsCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithCuresUpdatedListingsByAcb(result)));
            // Total # of Unique Products with Active 2015-Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(result)));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015-Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(result)));

            //Total # of Unique Products with Active Listings (Regardless of Edition)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalCPsActiveListings(null), executorService)
                    .thenAccept(result -> stats.setTotalCPsActiveListings(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            //Listing Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of Active (Including Suspended by ONC/ONC-ACB 2014 Listings)
            //stats.setTotalActive2014Listings(asyncStats.getTotalActive2014Listings(null));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalActive2014Listings(null), executorService)
                    .thenAccept(result -> stats.setTotalActive2014Listings(result)));
            //stats.setTotalActiveListingsByCertifiedBody(asyncStats.getTotalActiveListingsByCertifiedBody(null));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalActiveListingsByCertifiedBody(null), executorService)
                    .thenAccept(result -> stats.setTotalActiveListingsByCertifiedBody(result)));
            // Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Listings)
            //stats.setTotalActive2015Listings(asyncStats.getTotalActive2015Listings(null));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalActive2015Listings(null), executorService)
                    .thenAccept(result -> stats.setTotalActive2015Listings(result)));
            //Total # of 2015 Listings with Alternative Test Methods
            //stats.setTotalListingsWithAlternativeTestMethods(asyncStats.getTotalListingsWithAlternateTestMethods());
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalListingsWithAlternateTestMethods(), executorService)
                    .thenAccept(result -> stats.setTotalListingsWithAlternativeTestMethods(result)));
            // Total # of 2015 Listings with Alternative Test Methods
            //stats.setTotalListingsWithCertifiedBodyAndAlternativeTestMethods(asyncStats.getTotalListingsWithCertifiedBodyAndAlternativeTestMethods());
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalListingsWithCertifiedBodyAndAlternativeTestMethods(), executorService)
                    .thenAccept(result -> stats.setTotalListingsWithCertifiedBodyAndAlternativeTestMethods(result)));
            // Total # of Active (Including Suspended by ONC/ONC-ACB 2015-Cures Update Listings)
            //stats.setActiveListingCountWithCuresUpdatedByAcb(asyncStats.getActiveListingCountWithCuresUpdatedByAcb(listingsAll2015));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getActiveListingCountWithCuresUpdatedByAcb(listingsAll2015), executorService)
                    .thenAccept(result -> stats.setActiveListingCountWithCuresUpdatedByAcb(result)));
            // Total # of 2015-Cures Update Listings with Alternative Test Methods
            //stats.setListingCountWithCuresUpdatedAndAltTestMethodsByAcb(asyncStats.getListingCountFor2015AndAltTestMethodsByAcb(listingsAll2015, certificationResultDAO));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getListingCountFor2015AndAltTestMethodsByAcb(listingsAll2015, certificationResultDAO), executorService)
                    .thenAccept(result -> stats.setListingCountWithCuresUpdatedAndAltTestMethodsByAcb(result)));
            // Total # of 2015-Cures Updated Listings (Regardless of Status)
            //stats.setAllListingsCountWithCuresUpdated(asyncStats.getAllListingsCountWithCuresUpdated(listingsAll2015));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getAllListingsCountWithCuresUpdated(listingsAll2015), executorService)
                    .thenAccept(result -> stats.setAllListingsCountWithCuresUpdated(result)));


            /////////////////////////////////////////////////////////////////////////////////////
            // Surveillance Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalSurveillanceActivities(null), executorService)
                    .thenAccept(result -> stats.setTotalSurveillanceActivities(result)));
            // Open Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalOpenSurveillanceActivities(null), executorService)
                    .thenAccept(result -> stats.setTotalOpenSurveillanceActivities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalOpenSurveillancesByAcb(null), executorService)
                    .thenAccept(result -> stats.setTotalOpenSurveillanceActivitiesByAcb(result)));
            // Closed Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalClosedSurveillanceActivities(null), executorService)
                    .thenAccept(result -> stats.setTotalClosedSurveillanceActivities(result)));
            // Average Duration of Closed Surveillance (in days)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getAverageTimeToCloseSurveillance(), executorService)
                    .thenAccept(result -> stats.setAverageTimeToCloseSurveillance(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            // Non-Conformity Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of NCs
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalNonConformities(null), executorService)
                    .thenAccept(result -> stats.setTotalNonConformities(result)));
            // Open NCs
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalOpenNonconformities(null), executorService)
                    .thenAccept(result -> stats.setTotalOpenNonconformities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalOpenNonconformitiesByAcb(null), executorService)
                    .thenAccept(result -> stats.setTotalOpenNonconformitiesByAcb(result)));
            // Closed NCs
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getTotalClosedNonconformities(null), executorService)
                    .thenAccept(result -> stats.setTotalClosedNonconformities(result)));
            // Average Time to Assess Conformity (in days)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getAverageTimeToAssessConformity(), executorService)
                    .thenAccept(result -> stats.setAverageTimeToAssessConformity(result)));
            // Average Time to Approve CAP (in days)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getAverageTimeToApproveCAP(), executorService)
                    .thenAccept(result -> stats.setAverageTimeToApproveCAP(result)));
            // Average Duration of CAP (in days) (includes closed and ongoing CAPs)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getAverageDurationOfCAP(), executorService)
                    .thenAccept(result -> stats.setAverageDurationOfCAP(result)));
            // Average Time from CAP Approval to Surveillance Close (in days)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getAverageTimeFromCAPApprovalToSurveillanceClose(surveillanceStatisticsDAO), executorService)
                    .thenAccept(result -> stats.setAverageTimeFromCAPApprovalToSurveillanceEnd(result)));
            // Average Time from CAP Close to Surveillance Close (in days)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getAverageTimeFromCAPEndToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setAverageTimeFromCAPEndToSurveillanceEnd(result)));
            // Average Duration of Closed Non-Conformities (in days)
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getAverageTimeFromSurveillanceOpenToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setAverageTimeFromSurveillanceOpenToSurveillanceClose(result)));
            // Number of Open CAPs
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getOpenCAPCountByAcb(), executorService)
                    .thenAccept(result -> stats.setOpenCAPCountByAcb(result)));
            // Number of Closed CAPs
            futures.add(CompletableFuture.supplyAsync(() -> asyncStats.getClosedCAPCountByAcb(), executorService)
                    .thenAccept(result -> stats.setClosedCAPCountByAcb(result)));

            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();
            getLogger().info("All processes have completed");

        } finally {
            executorService.shutdown();
        }
        return stats;
    }


    @Transactional
    @Async
    public Future<Statistics> getStatistics(DateRange dateRange) throws InterruptedException, ExecutionException {
        asyncStats.setLogger(getLogger());

        if (dateRange == null) {
            getLogger().info("Getting all current statistics.");
        } else {
            getLogger().info("Getting statistics for start date " + dateRange.getStartDate() + " end date "
                    + dateRange.getEndDate());
        }

        Statistics stats = new Statistics();
        stats.setDateRange(dateRange);

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
