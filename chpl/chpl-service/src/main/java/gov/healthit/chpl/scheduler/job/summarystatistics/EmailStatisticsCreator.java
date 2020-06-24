package gov.healthit.chpl.scheduler.job.summarystatistics;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.concept.NonconformityStatusConcept;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceNonconformityEntity;

@Component
public class EmailStatisticsCreator {
    private static final Logger LOGGER = LogManager.getLogger("summaryStatisticsCreatorJobLogger");

    private static final Long NONCONFORMITY_SURVEILLANCE_RESULT = 1L;
    private static final String ONC_TEST_METHOD = "ONC Test Method";

    private SurveillanceStatisticsDAO surveillanceStatisticsDAO;
    private CertifiedProductDAO certifiedProductDAO;
    private CertificationResultDAO certificationResultDAO;
    private DeveloperStatisticsDAO developerStatisticsDAO;
    private ListingStatisticsDAO listingStatisticsDAO;
    private Environment env;

    @Autowired
    public EmailStatisticsCreator(SurveillanceStatisticsDAO surveillanceStatisticsDAO, CertifiedProductDAO certifiedProductDAO,
            CertificationResultDAO certificationResultDAO, DeveloperStatisticsDAO developerStatisticsDAO,
            ListingStatisticsDAO listingStatisticsDAO, Environment env) {
        this.surveillanceStatisticsDAO = surveillanceStatisticsDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.certificationResultDAO = certificationResultDAO;
        this.developerStatisticsDAO = developerStatisticsDAO;
        this.listingStatisticsDAO = listingStatisticsDAO;
        this.env = env;
    }

    @SuppressWarnings({"checkstyle:linelength", "checkstyle:methodlength"})
    @Transactional(readOnly = true)
    public Statistics getStatistics() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(getThreadCountForJob());

        LOGGER.info("Getting all current statistics.");

        Statistics stats = new Statistics();
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();

        LOGGER.info("Getting all 2015 listings.");
        List<CertifiedProductDetailsDTO> listingsAll2015 = certifiedProductDAO.findByEdition(CertificationEditionConcept.CERTIFICATION_EDITION_2015);
        LOGGER.info("Completing getting all 2015 listings.");

        try {
            /////////////////////////////////////////////////////////////////////////////////////
            //Developer Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            //Used for multiple sections
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(result)));
            // Total # of Unique Developers (Regardless of Edition)
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopers(), executorService)
                    .thenAccept(result -> stats.setTotalDevelopers(result)));
            // Total # of Developers with 2014 Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopersWith2014Listings(), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersWith2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopersByCertifiedBodyWithListingsEachYear(), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersByCertifiedBodyWithListingsEachYear(result)));
            // Total # of Developers with Active 2014 Listings
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopersWithActive2014Listings(), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersWithActive2014Listings(result)));

            //**********************
            // 2015 Regular and Cures Listings
            //**********************
            // Total # of Developers with 2015 Listings or 2015 Cures Update Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueDevelopersCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountForAny2015ListingsByAcb(result)));
            // Total # of Developers with Active 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueDevelopersCountFor2015ActiveListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountForAny2015ActiveListingsByAcb(result)));
            // Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueDevelopersCountFor2015SuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountForAny2015SuspendedListingsByAcb(result)));

            //**********************
            // 2015 Cures Listings
            //**********************
            // Total # of Developers with 2015 Cures Update Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueDevelopersCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithCuresUpdatedListingsByAcb(result)));
            // Total # of Developers with Active 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueDevelopersCountFor2015ActiveListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithCuresUpdatedActiveListingsByAcb(result)));
            // Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueDevelopersCountFor2015SuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithCuresUpdatedSuspendedListingsByAcb(result)));

            //**********************
            // 2015 Regular Listings
            //**********************
            // Total # of Developers with 2015 Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueDevelopersCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithoutCuresUpdatedListingsByAcb(result)));
            // Total # of Developers with Active 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueDevelopersCountFor2015ActiveListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithoutCuresUpdatedActiveListingsByAcb(result)));
            // Total # of Developers with Suspended by ONC-ACB/Suspended by ONC 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueDevelopersCountFor2015SuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueDevelopersCountWithoutCuresUpdatedSuspendedListingsByAcb(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            //Product Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Used for multiple sections
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(), executorService)
                    .thenAccept(result -> stats.setTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(result)));
            //Total # of Certified Unique Products Regardless of Status or Edition - Including 2011)
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCertifiedProducts(), executorService)
                    .thenAccept(result -> stats.setTotalCertifiedProducts(result)));
            // Total # of Unique Products with 2014 Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2014Listings(), executorService)
                    .thenAccept(result -> stats.setTotalCPs2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCPListingsEachYearByCertifiedBody(), executorService)
                    .thenAccept(result -> stats.setTotalCPListingsEachYearByCertifiedBody(result)));
            // Total # of Unique Products with Active 2014 Listings
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCPs2014Listings(), executorService)
                    .thenAccept(result -> stats.setTotalCPsActive2014Listings(result)));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2014 Listings
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCPsSuspended2014Listings(), executorService)
                    .thenAccept(result -> stats.setTotalCPsSuspended2014Listings(result)));

            //**********************
            // 2015 Regular and Cures Listings
            //**********************
            // Total # of Unique Products with 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueProductsCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountForAny2015ListingsByAcb(result)));
            // Total # of Unique Products with Active 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountForAny2015ActiveListingsByAcb(result)));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings or 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.BOTH), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountForAny2015SuspendedListingsByAcb(result)));

            //**********************
            // 2015 Regular Listings
            //**********************
            // Total # of Unique Products with 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueProductsCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithoutCuresUpdatedListingsByAcb(result)));
            // Total # of Unique Products with Active 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithoutCuresUpdatedActiveListingsByAcb(result)));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.NON_CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithoutCuresUpdatedSuspendedListingsByAcb(result)));

            //**********************
            // 2015 Cures Listings
            //**********************
            // Total # of Unique Products with 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueProductsCountFor2015ListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithCuresUpdatedListingsByAcb(result)));
            // Total # of Unique Products with Active 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(result)));
            // Total # of Unique Products with Suspended by ONC-ACB/Suspended by ONC 2015 Cures Update Listings
            futures.add(CompletableFuture.supplyAsync(() -> getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(listingsAll2015, Edition2015Criteria.CURES), executorService)
                    .thenAccept(result -> stats.setUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(result)));

            //Total # of Unique Products with Active Listings (Regardless of Edition)
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCPsActiveListings(), executorService)
                    .thenAccept(result -> stats.setTotalCPsActiveListings(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            //Listing Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            //Total # of Listings (Regardless of Status or Edition)
            futures.add(CompletableFuture.supplyAsync(() -> getTotalListings(), executorService)
                    .thenAccept(result -> stats.setTotalListings(result)));
            // Total # of Active (Including Suspended by ONC/ONC-ACB 2014 Listings)
            futures.add(CompletableFuture.supplyAsync(() -> getTotalActive2014Listings(), executorService)
                    .thenAccept(result -> stats.setTotalActive2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalActiveListingsByCertifiedBody(), executorService)
                    .thenAccept(result -> stats.setTotalActiveListingsByCertifiedBody(result)));
            // Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Listings)
            futures.add(CompletableFuture.supplyAsync(() -> getTotalActive2015Listings(), executorService)
                    .thenAccept(result -> stats.setTotalActive2015Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getListingCountFor2015AndAltTestMethodsByAcb(listingsAll2015), executorService)
                    .thenAccept(result -> stats.setTotalListingsWithCertifiedBodyAndAlternativeTestMethods(result)));
            // Total # of Active (Including Suspended by ONC/ONC-ACB 2015 Cures Update Listings)
            futures.add(CompletableFuture.supplyAsync(() -> getActiveListingCountWithCuresUpdatedByAcb(listingsAll2015), executorService)
                    .thenAccept(result -> stats.setActiveListingCountWithCuresUpdatedByAcb(result)));
            // Total # of 2015 Cures Update Listings with Alternative Test Methods
            futures.add(CompletableFuture.supplyAsync(() -> getListingCountFor2015CuresUpdateAndAltTestMethodsByAcb(listingsAll2015), executorService)
                    .thenAccept(result -> stats.setListingCountWithCuresUpdatedAndAltTestMethodsByAcb(result)));
            // Total # of 2015 Listings and 2015 Cures Update listings(Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2015ListingsCount(listingsAll2015), executorService)
                    .thenAccept(result -> stats.setTotal2015Listings(result)));
            // Total # of 2015 Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2015ListingsCountWithoutCuresUpdated(listingsAll2015), executorService)
                    .thenAccept(result -> stats.setAllListingsCountWithoutCuresUpdated(result)));
            // Total # of 2015 Cures Updated Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getAllListingsCountWithCuresUpdate(listingsAll2015), executorService)
                    .thenAccept(result -> stats.setAllListingsCountWithCuresUpdated(result)));
            // Total # of 2014 Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2014Listings(), executorService)
                    .thenAccept(result -> stats.setTotal2014Listings(result)));
            // Total # of 2011 Listings (Regardless of Status)
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2011Listings(), executorService)
                    .thenAccept(result -> stats.setTotal2011Listings(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            // Surveillance Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> getTotalSurveillanceActivities(), executorService)
                    .thenAccept(result -> stats.setTotalSurveillanceActivities(result)));
            // Open Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> getTotalOpenSurveillanceActivities(), executorService)
                    .thenAccept(result -> stats.setTotalOpenSurveillanceActivities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalOpenSurveillancesByAcb(), executorService)
                    .thenAccept(result -> stats.setTotalOpenSurveillanceActivitiesByAcb(result)));
            // Closed Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> getTotalClosedSurveillanceActivities(), executorService)
                    .thenAccept(result -> stats.setTotalClosedSurveillanceActivities(result)));
            // Average Duration of Closed Surveillance (in days)
            futures.add(CompletableFuture.supplyAsync(() -> getAverageTimeToCloseSurveillance(), executorService)
                    .thenAccept(result -> stats.setAverageTimeToCloseSurveillance(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            // Non-Conformity Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of NCs
            futures.add(CompletableFuture.supplyAsync(() -> getTotalNonConformities(), executorService)
                    .thenAccept(result -> stats.setTotalNonConformities(result)));
            // Open NCs
            futures.add(CompletableFuture.supplyAsync(() -> getTotalOpenNonconformities(), executorService)
                    .thenAccept(result -> stats.setTotalOpenNonconformities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalOpenNonconformitiesByAcb(), executorService)
                    .thenAccept(result -> stats.setTotalOpenNonconformitiesByAcb(result)));
            // Closed NCs
            futures.add(CompletableFuture.supplyAsync(() -> getTotalClosedNonconformities(), executorService)
                    .thenAccept(result -> stats.setTotalClosedNonconformities(result)));
            // Average Time to Assess Conformity (in days)
            futures.add(CompletableFuture.supplyAsync(() -> getAverageTimeToAssessConformity(), executorService)
                    .thenAccept(result -> stats.setAverageTimeToAssessConformity(result)));
            // Average Time to Approve CAP (in days)
            futures.add(CompletableFuture.supplyAsync(() -> getAverageTimeToApproveCAP(), executorService)
                    .thenAccept(result -> stats.setAverageTimeToApproveCAP(result)));
            // Average Duration of CAP (in days) (includes closed and ongoing CAPs)
            futures.add(CompletableFuture.supplyAsync(() -> getAverageDurationOfCAP(), executorService)
                    .thenAccept(result -> stats.setAverageDurationOfCAP(result)));
            // Average Time from CAP Approval to Surveillance Close (in days)
            futures.add(CompletableFuture.supplyAsync(() -> getAverageTimeFromCAPApprovalToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setAverageTimeFromCAPApprovalToSurveillanceEnd(result)));
            // Average Time from CAP Close to Surveillance Close (in days)
            futures.add(CompletableFuture.supplyAsync(() -> getAverageTimeFromCAPEndToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setAverageTimeFromCAPEndToSurveillanceEnd(result)));
            // Average Duration of Closed Non-Conformities (in days)
            futures.add(CompletableFuture.supplyAsync(() -> getAverageTimeFromSurveillanceOpenToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setAverageTimeFromSurveillanceOpenToSurveillanceClose(result)));
            // Number of Open CAPs
            futures.add(CompletableFuture.supplyAsync(() -> getOpenCAPCountByAcb(), executorService)
                    .thenAccept(result -> stats.setOpenCAPCountByAcb(result)));
            // Number of Closed CAPs
            futures.add(CompletableFuture.supplyAsync(() -> getClosedCAPCountByAcb(), executorService)
                    .thenAccept(result -> stats.setClosedCAPCountByAcb(result)));

            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();
            LOGGER.info("All processes have completed");

        } finally {
            executorService.shutdown();
        }
        return stats;
    }

    private Long getTotalDevelopers() {
        Long total = developerStatisticsDAO.getTotalDevelopers(null);
        return total;
    }

    private Long getTotalDevelopersWith2014Listings() {
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(null, "2014", null);
        return total;
    }

    private Long getTotalDevelopersWithActive2014Listings() {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(null, "2014", activeStatuses);
    }

    private List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsEachYear() {
        return developerStatisticsDAO.getTotalDevelopersByCertifiedBodyWithListingsEachYear(null);
    }

    private List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear() {
        return developerStatisticsDAO.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(null);
    }

    private Long getTotalCertifiedProducts() {
        return listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(null, null, null);
    }

    private List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody() {
        return  listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBody(null);
    }

    private List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus() {
        return listingStatisticsDAO.getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(null);
    }

    private Long getTotalCPs2014Listings() {
        return listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(null, "2014", null);
    }

    private Long getTotalCPsSuspended2014Listings() {
        List<String> suspendedStatuses = new ArrayList<String>();
        suspendedStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        suspendedStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(null, "2014", suspendedStatuses);
    }

    private Long getTotalCPsActiveListings() {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        return listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(null, null, activeStatuses);
    }

    private Long getTotalActive2014Listings() {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return listingStatisticsDAO.getTotalListingsByEditionAndStatus(null, "2014", activeStatuses);
    }

    private Long getTotalActive2015Listings() {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByAcb.getName().toUpperCase());
        activeStatuses.add(CertificationStatusType.SuspendedByOnc.getName().toUpperCase());
        return listingStatisticsDAO.getTotal2015ListingsByStatus(activeStatuses);
    }

    private List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody() {
        return listingStatisticsDAO.getTotalActiveListingsByCertifiedBody(null);
    }

    private Long getTotal2014Listings() {
        return listingStatisticsDAO.getTotalListingsByEditionAndStatus(null, "2014", null);
    }

    private Long getTotalSurveillanceActivities() {
        return surveillanceStatisticsDAO.getTotalSurveillanceActivities(null);
    }

    private Long getTotalOpenSurveillanceActivities() {
        return surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(null);
    }

    private Long getTotalClosedSurveillanceActivities() {
        return surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(null);
    }

    private Long getAverageTimeToCloseSurveillance() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillances().stream()
                .filter(surv -> surv.getStartDate() != null
                && surv.getEndDate() != null)
                .collect(Collectors.toList());

        Long totalDuration = surveillances.stream()
                .map(surv -> Math.abs(ChronoUnit.DAYS.between(surv.getStartDate().toInstant(), surv.getEndDate().toInstant())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / surveillances.size();
    }

    private Long getTotalNonConformities() {
        return surveillanceStatisticsDAO.getTotalNonConformities(null);
    }

    private Long getTotalOpenNonconformities() {
        return surveillanceStatisticsDAO.getTotalOpenNonconformities(null);
    }

    private Long getTotalClosedNonconformities() {
        return surveillanceStatisticsDAO.getTotalClosedNonconformities(null);
    }

    private List<CertifiedBodyStatistics> getTotalOpenNonconformitiesByAcb() {
        return surveillanceStatisticsDAO.getTotalOpenNonconformitiesByAcb(null);
    }

    private List<CertifiedBodyStatistics> getTotalOpenSurveillancesByAcb() {
        return surveillanceStatisticsDAO.getTotalOpenSurveillanceActivitiesByAcb(null);
    }

    private Long getAverageTimeToAssessConformity() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getDateOfDetermination() != null)
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysToAssessNonconformtity(findSurveillanceForNonconformity(nc, surveillances), nc))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    private Long getAverageTimeToApproveCAP() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getDateOfDetermination() != null && nc.getCapApproval() != null)
                .distinct() // Want to figure out how to get rid of this
                .collect(Collectors.toList());

        Long totalDuration = nonconformities.stream()
                .map(nc -> Math
                        .abs(ChronoUnit.DAYS.between(nc.getDateOfDetermination().toInstant(), nc.getCapApproval().toInstant())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / nonconformities.size();
    }

    private Long getAverageDurationOfCAP() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null)
                .distinct() // Want to figure out how to get rid of this
                .collect(Collectors.toList());

        Long totalDuration = nonconformities.stream()
                .map(nc -> Math
                        .abs(ChronoUnit.DAYS.between(
                                nc.getCapApproval().toInstant(),
                                nc.getCapEndDate() != null ? nc.getCapEndDate().toInstant() : Instant.now())))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / nonconformities.size();
    }

    private Long getAverageTimeFromCAPApprovalToSurveillanceClose() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .filter(surv -> surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null)
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysFromCAPApprovalToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances), nc))
                .collect(Collectors.summingLong(n -> n.longValue()));
        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    private Long getAverageTimeFromCAPEndToSurveillanceClose() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformitiesWithDeterminationDate = surveillances.stream()
                .filter(surv -> surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapEndDate() != null
                && nc.getNonconformityStatus().getName().equals(NonconformityStatusConcept.CLOSED.getName()))
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformitiesWithDeterminationDate.stream()
                .map(nc -> getDaysFromCAPEndToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances), nc))
                .collect(Collectors.summingLong(n -> n.longValue()));

        return totalDuration / nonconformitiesWithDeterminationDate.size();
    }

    private Long getAverageTimeFromSurveillanceOpenToSurveillanceClose() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        List<SurveillanceNonconformityEntity> nonconformities = surveillances.stream()
                .filter(surv -> surv.getStartDate() != null
                && surv.getEndDate() != null)
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .filter(req -> req.getSurveillanceResultTypeId().equals(NONCONFORMITY_SURVEILLANCE_RESULT))
                .flatMap(req -> req.getNonconformities().stream())
                .distinct()
                .collect(Collectors.toList());

        Long totalDuration = nonconformities.stream()
                .map(nc -> getDaysFromSurveillanceOpenToSurveillanceClose(findSurveillanceForNonconformity(nc, surveillances)))
                .collect(Collectors.summingLong(n -> n.longValue()));

        return totalDuration / surveillances.size();
    }

    private Map<Long, Long> getOpenCAPCountByAcb() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        return surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null
                && nc.getCapEndDate() == null)
                .distinct()
                .map(nc -> new NonconformanceStatistic(
                        findSurveillanceForNonconformity(nc, surveillances).getCertifiedProduct().getCertificationBodyId(), nc))
                .collect(Collectors.groupingBy(stat -> stat.getCertificationBodyId(), Collectors.counting()));
    }

    private Map<Long, Long> getClosedCAPCountByAcb() {
        List<SurveillanceEntity> surveillances = surveillanceStatisticsDAO.getAllSurveillancesWithNonconformities();

        return surveillances.stream()
                .flatMap(surv -> surv.getSurveilledRequirements().stream())
                .flatMap(req -> req.getNonconformities().stream())
                .filter(nc -> nc.getCapApproval() != null
                && nc.getCapEndDate() != null)
                .distinct()
                .map(nc -> new NonconformanceStatistic(
                        findSurveillanceForNonconformity(nc, surveillances).getCertifiedProduct().getCertificationBodyId(), nc))
                .collect(Collectors.groupingBy(stat -> stat.getCertificationBodyId(), Collectors.counting()));
    }

    private List<CertifiedBodyStatistics> getUniqueDevelopersCountFor2015ListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        return certifiedProducts.stream()
                .filter(listing -> includeListing(listing, listingsToInclude))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalDevelopersWithListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getDeveloper().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private List<CertifiedBodyStatistics> getUniqueDevelopersCountFor2015ActiveListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        return certifiedProducts.stream()
                .filter(listing -> includeListing(listing, listingsToInclude)
                        && listing.getCertificationStatusName().equals(CertificationStatusType.Active.getName()))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalDevelopersWithListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getDeveloper().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private List<CertifiedBodyStatistics> getUniqueDevelopersCountFor2015SuspendedListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        return certifiedProducts.stream()
                .filter(cp -> includeListing(cp, listingsToInclude)
                        && (cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByAcb.getName())
                                || cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByOnc.getName())))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalDevelopersWithListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getDeveloper().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private List<CertifiedBodyStatistics> getUniqueProductsCountFor2015ListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        return certifiedProducts.stream()
                .filter(listing -> includeListing(listing, listingsToInclude))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getProduct().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private List<CertifiedBodyStatistics> getUniqueProductsCountWithCuresUpdatedActiveListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        return certifiedProducts.stream()
                .filter(listing -> includeListing(listing, listingsToInclude)
                        && listing.getCertificationStatusName().equals(CertificationStatusType.Active.getName()))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getProduct().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private List<CertifiedBodyStatistics> getUniqueProductsCountWithCuresUpdatedSuspendedListingsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts, Edition2015Criteria listingsToInclude) {
        return certifiedProducts.stream()
                .filter(cp -> includeListing(cp, listingsToInclude)
                        && (cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByAcb.getName())
                                || cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByOnc.getName())))
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .filter(distinctByKey(cp -> cp.getProduct().getId()))
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private List<CertifiedBodyStatistics> getActiveListingCountWithCuresUpdatedByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts) {
        return certifiedProducts.stream()
                .filter(cp -> (cp.getCertificationStatusName().equals(CertificationStatusType.Active.getName())
                        || cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByAcb.getName())
                        || cp.getCertificationStatusName().equals(CertificationStatusType.SuspendedByOnc.getName()))
                        && cp.getCuresUpdate())
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private List<CertifiedBodyStatistics> getListingCountFor2015AndAltTestMethodsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts) {
        return certifiedProducts.stream()
                .filter(cp -> doesListingHaveAlternativeTestMethod(cp.getId())
                        && !cp.getCuresUpdate())
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private List<CertifiedBodyStatistics> getListingCountFor2015CuresUpdateAndAltTestMethodsByAcb(
            List<CertifiedProductDetailsDTO> certifiedProducts) {
        return certifiedProducts.stream()
                .filter(cp -> doesListingHaveAlternativeTestMethod(cp.getId())
                        && cp.getCuresUpdate())
                .collect(Collectors.groupingBy(CertifiedProductDetailsDTO::getCertificationBodyName, Collectors.toList()))
                .entrySet().stream()
                .map(entry -> {
                    CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
                    stat.setName(entry.getKey());
                    stat.setTotalListings(entry.getValue().stream()
                            .collect(Collectors.counting()));
                    return stat;
                })
                .collect(Collectors.toList());
    }

    private Long getTotal2015ListingsCount(List<CertifiedProductDetailsDTO> certifiedProducts) {
        return certifiedProducts.stream()
                .filter(listing -> listing.getCertificationEditionId().equals(
                        CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId()))
                .count();
    }

    private Long getTotal2015ListingsCountWithoutCuresUpdated(List<CertifiedProductDetailsDTO> certifiedProducts) {
        return certifiedProducts.stream()
                .filter(listing -> listing.getCertificationEditionId().equals(
                        CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                        && !listing.getCuresUpdate())
                .count();
    }

    private Long getAllListingsCountWithCuresUpdate(List<CertifiedProductDetailsDTO> certifiedProducts) {
        return certifiedProducts.stream()
                .filter(listing -> listing.getCertificationEditionId().equals(
                        CertificationEditionConcept.CERTIFICATION_EDITION_2015.getId())
                        && listing.getCuresUpdate())
                .count();
    }

    private Long getTotal2011Listings() {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(null, "2011", null);
        return total;
    }

    private Long getTotalListings() {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(null, null, null);
        return total;
    }

    private boolean doesListingHaveAlternativeTestMethod(Long listingId) {
        return certificationResultDAO.getTestProceduresForListing(listingId).stream()
                .filter(crtp -> !crtp.getTestProcedure().getName().equals(ONC_TEST_METHOD))
                .findAny()
                .isPresent();
    }

    private class NonconformanceStatistic {
        private Long certificationBodyId;
        private SurveillanceNonconformityEntity nonconformity;

        NonconformanceStatistic(Long certificationBodyId, SurveillanceNonconformityEntity nonconformity) {
            this.certificationBodyId = certificationBodyId;
            this.nonconformity = nonconformity;
        }

        public Long getCertificationBodyId() {
            return certificationBodyId;
        }

        public SurveillanceNonconformityEntity getNonconformity() {
            return nonconformity;
        }
    }

    private SurveillanceEntity findSurveillanceForNonconformity(SurveillanceNonconformityEntity nonconformity,
            List<SurveillanceEntity> surveillances) {

        return surveillances.stream()
                .filter(surv -> surv.getSurveilledRequirements().stream()
                        .anyMatch(req -> req.getNonconformities().stream()
                                .anyMatch(nc -> nc.getId().equals(nonconformity.getId()))))
                .findFirst()
                .orElse(null);
    }

    private Long getDaysToAssessNonconformtity(SurveillanceEntity surveillance, SurveillanceNonconformityEntity nonconformity) {
        return Math.abs(ChronoUnit.DAYS.between(surveillance.getStartDate().toInstant(),
                nonconformity.getDateOfDetermination().toInstant()));
    }

    private Long getDaysFromCAPApprovalToSurveillanceClose(SurveillanceEntity surveillance,
            SurveillanceNonconformityEntity nonconformity) {
        return Math.abs(ChronoUnit.DAYS.between(
                nonconformity.getCapApproval().toInstant(),
                surveillance.getEndDate().toInstant()));
    }

    private Long getDaysFromCAPEndToSurveillanceClose(SurveillanceEntity surveillance,
            SurveillanceNonconformityEntity nonconformity) {
        return Math.abs(ChronoUnit.DAYS.between(
                nonconformity.getCapEndDate().toInstant(),
                surveillance.getEndDate().toInstant()));
    }

    private Long getDaysFromSurveillanceOpenToSurveillanceClose(SurveillanceEntity surveillance) {
        return Math.abs(ChronoUnit.DAYS.between(
                surveillance.getStartDate().toInstant(),
                surveillance.getEndDate().toInstant()));
    }

    private boolean includeListing(CertifiedProductDetailsDTO listing, Edition2015Criteria listingsToInclude) {
        switch (listingsToInclude) {
        case BOTH :
            return true;
        case CURES :
            return listing.getCuresUpdate();
        case NON_CURES :
            return !listing.getCuresUpdate();
        default :
            return false;
        }
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

}
