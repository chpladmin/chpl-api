package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.scheduler.job.summarystatistics.EditionCriteria;

@Component
public class EmailStatisticsCreator {
    private static final Logger LOGGER = LogManager.getLogger("summaryStatisticsCreatorJobLogger");

    private DeveloperDataCreator developerDataCreator;
    private ProductDataCreator productDataCreator;
    private ListingDataCreator listingDataCreator;
    private SurveillanceDataCreator surveillanceDataCreator;
    private NonConformityDataCreator nonConformityDataCreator;
    private Environment env;

    @Autowired
    public EmailStatisticsCreator(DeveloperDataCreator developerDataCreator, ProductDataCreator productDataCreator,
            ListingDataCreator listingDataCreator,  SurveillanceDataCreator surveillanceDataCreator,
            NonConformityDataCreator nonConformityDataCreator, Environment env) {

        this.developerDataCreator = developerDataCreator;
        this.productDataCreator = productDataCreator;
        this.listingDataCreator = listingDataCreator;
        this.surveillanceDataCreator = surveillanceDataCreator;
        this.nonConformityDataCreator = nonConformityDataCreator;
        this.env = env;
    }

    @SuppressWarnings({"checkstyle:linelength", "checkstyle:methodlength"})
    @Transactional(readOnly = true)
    public EmailStatistics getStatistics(List<CertifiedProductDetailsDTO> allListings) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(getThreadCountForJob());

        LOGGER.info("Getting all current statistics.");

        EmailStatistics stats = new EmailStatistics();
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();

        try {
            /////////////////////////////////////////////////////////////////////////////////////
            //Developer Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // 2014 Statistics
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2014, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2014WithAllStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2014, getActiveStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2014WithActiveStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2014, getSuspendedStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2014WithSuspendedStatuses(result)));

            // 2015 Cures Statistics
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2015_CURES, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2015CuresWithAllStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2015_CURES, getActiveStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2015CuresWithActiveStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2015_CURES, getSuspendedStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2015CuresWithSuspendedStatuses(result)));

            // 2015 Non-Cures Statistics
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2015_NON_CURES, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2015NonCuresWithAllStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2015_NON_CURES, getActiveStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2015NonCuresWithActiveStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2015_NON_CURES, getSuspendedStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2015NonCuresWithSuspendedStatuses(result)));

            // 2015 Cures And Non-Cures Statistics
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2015_CURES_AND_NON_CURES, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2015CuresAndNonCuresWithAllStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2015_CURES_AND_NON_CURES, getActiveStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2015CuresAndNonCuresWithActiveStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCount(allListings, EditionCriteria.EDITION_2015_CURES_AND_NON_CURES, getSuspendedStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEdition2015CuresAndNonCuresWithSuspendedStatuses(result)));

            // Total # of Unique Developers (Regardless of Edition)
            futures.add(CompletableFuture.supplyAsync(() -> developerDataCreator.getUniqueDeveloperCountTotal(allListings, EditionCriteria.ALL, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setDevelopersForEditionAllAndAllStatuses(result)));


            /////////////////////////////////////////////////////////////////////////////////////
            //Product Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            //Total # of Certified Unique Products Regardless of Status or Edition - Including 2011)
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCountTotal(allListings, EditionCriteria.ALL, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEditionAllAndAllStatuses(result)));

            // 2014 Statistics
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2014, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2014WithAllStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2014, getActiveStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2014WithActiveStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2014, getSuspendedStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2014WithSuspendedStatuses(result)));

            // 2015 Cures Statistics
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2015_CURES, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2015CuresWithAllStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2015_CURES, getActiveStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2015CuresWithActiveStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2015_CURES, getSuspendedStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2015CuresWithSuspendedStatuses(result)));

            // 2015 Non-Cures Statistics
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2015_NON_CURES, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2015NonCuresWithAllStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2015_NON_CURES, getActiveStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2015NonCuresWithActiveStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2015_NON_CURES, getSuspendedStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2015NonCuresWithSuspendedStatuses(result)));

            // 2015 Cures And Non-Cures Statistics
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2015_CURES_AND_NON_CURES, getAllStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2015CuresAndNonCuresWithAllStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2015_CURES_AND_NON_CURES, getActiveStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2015CuresAndNonCuresWithActiveStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCount(allListings, EditionCriteria.EDITION_2015_CURES_AND_NON_CURES, getSuspendedStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEdition2015CuresAndNonCuresWithSuspendedStatuses(result)));

            //Total # of Unique Products with Active Listings (Regardless of Edition)
            futures.add(CompletableFuture.supplyAsync(() -> productDataCreator.getUniqueProductCountTotal(allListings, EditionCriteria.ALL, getActiveStatuses()), executorService)
                    .thenAccept(result -> stats.setProductsForEditionAllAndActiveStatuses(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            //Listing Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCount(
                    allListings, EditionCriteria.EDITION_2014, getActiveAndSuspendedStatuses(), false), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2014WithActiveAndSuspendedStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCount(
                    allListings, EditionCriteria.EDITION_2015_CURES, getActiveAndSuspendedStatuses(), false), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2015CuresWithActiveAndSuspendedStatuses(result)));
            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCount(
                    allListings, EditionCriteria.EDITION_2015_NON_CURES, getActiveAndSuspendedStatuses(), false), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2015NonCuresWithActiveAndSuspendedStatuses(result)));

            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCountTotal(
                    allListings, EditionCriteria.EDITION_2011, getAllStatuses(), false), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2011Total(result)));
            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCountTotal(
                    allListings, EditionCriteria.EDITION_2014, getAllStatuses(), false), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2014Total(result)));
            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCountTotal(
                    allListings, EditionCriteria.EDITION_2015_CURES_AND_NON_CURES, getAllStatuses(), false), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2015NonCuresAndCuresTotal(result)));
            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCountTotal(
                    allListings, EditionCriteria.EDITION_2015_CURES, getAllStatuses(), false), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2015CuresTotal(result)));
            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCountTotal(
                    allListings, EditionCriteria.EDITION_2015_NON_CURES, getAllStatuses(), false), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2015NonCuresTotal(result)));

            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCount(
                    allListings, EditionCriteria.EDITION_2015_CURES, getAllStatuses(), true), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2015CuresWithAllStatusesAndAltTestMethods(result)));
            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCount(
                    allListings, EditionCriteria.EDITION_2015_NON_CURES, getAllStatuses(), true), executorService)
                    .thenAccept(result -> stats.setListingsForEdition2015NonCuresWithAllStatusesAndAltTestMethods(result)));

            futures.add(CompletableFuture.supplyAsync(() -> listingDataCreator.getUniqueListingCountTotal(
                    allListings, EditionCriteria.ALL, getAllStatuses(), false), executorService)
                    .thenAccept(result -> stats.setListingsForEditionAnyTotal(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            // Surveillance Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> surveillanceDataCreator.getTotalSurveillanceActivities(), executorService)
                    .thenAccept(result -> stats.setSurveillanceAllStatusTotal(result)));
            // Open Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> surveillanceDataCreator.getTotalOpenSurveillanceActivities(), executorService)
                    .thenAccept(result -> stats.setSurveillanceOpenStatus(result)));
            // Closed Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> surveillanceDataCreator.getTotalClosedSurveillanceActivities(), executorService)
                    .thenAccept(result -> stats.setSurveillanceClosedStatusTotal(result)));
            // Average Duration of Closed Surveillance (in days)
            futures.add(CompletableFuture.supplyAsync(() -> surveillanceDataCreator.getAverageTimeToCloseSurveillance(), executorService)
                    .thenAccept(result -> stats.setSurveillanceAvgTimeToClose(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            // Non-Conformity Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of NCs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getTotalNonConformities(), executorService)
                    .thenAccept(result -> stats.setNonconfStatusAllTotal(result)));
            // Open NCs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getTotalOpenNonconformities(), executorService)
                    .thenAccept(result -> stats.setNonconfStatusOpen(result)));
            // Closed NCs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getTotalClosedNonconformities(), executorService)
                    .thenAccept(result -> stats.setNonconfStatusClosedTotal(result)));
            // Average Time to Assess Conformity (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getAverageTimeToAssessConformity(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeToAssessConformity(result)));
            // Average Time to Approve CAP (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getAverageTimeToApproveCAP(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeToApproveCAP(result)));
            // Average Duration of CAP (in days) (includes closed and ongoing CAPs)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getAverageDurationOfCAP(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgDurationOfCAP(result)));
            // Average Time from CAP Approval to Surveillance Close (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getAverageTimeFromCAPApprovalToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeFromCAPAprrovalToSurveillanceEnd(result)));
            // Average Time from CAP Close to Surveillance Close (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getAverageTimeFromCAPEndToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeFromCAPEndToSurveillanceEnd(result)));
            // Average Duration of Closed Non-Conformities (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getAverageTimeFromSurveillanceOpenToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeFromSurveillanceOpenToSurveillanceClose(result)));
            // Number of Open CAPs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getOpenCAPCountByAcb(), executorService)
                    .thenAccept(result -> stats.setNonconfCAPStatusOpen(result)));
            // Number of Closed CAPs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCreator.getClosedCAPCountByAcb(), executorService)
                    .thenAccept(result -> stats.setNonconfCAPStatusClosed(result)));

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

    private List<CertificationStatusType> getActiveStatuses() {
        return Arrays.asList(CertificationStatusType.Active);
    }

    private List<CertificationStatusType> getSuspendedStatuses() {
        return Arrays.asList(CertificationStatusType.SuspendedByAcb, CertificationStatusType.SuspendedByOnc);
    }

    private List<CertificationStatusType> getAllStatuses() {
        return null;
    }

    private List<CertificationStatusType> getActiveAndSuspendedStatuses() {
        List<CertificationStatusType> statuses = new ArrayList<CertificationStatusType>();
        statuses.addAll(getActiveStatuses());
        statuses.addAll(getSuspendedStatuses());
        return statuses;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

}
