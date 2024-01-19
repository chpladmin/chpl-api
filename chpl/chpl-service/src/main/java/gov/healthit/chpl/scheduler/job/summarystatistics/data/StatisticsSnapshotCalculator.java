package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.search.ListingSearchManager;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "summaryStatisticsCreatorJobLogger")
@Component
public class StatisticsSnapshotCalculator {
    private ListingSearchManager listingSearchManager;
    private SurveillanceDataCalculator surveillanceDataCalculator;
    private NonConformityDataCalculator nonConformityDataCalculator;
    private DirectReviewDataCalculator directReviewDataCalculator;
    private Environment env;

    @Autowired
    public StatisticsSnapshotCalculator(ListingSearchManager listingSearchManager,
            SurveillanceDataCalculator surveillanceDataCalculator,
            NonConformityDataCalculator nonConformityDataCalculator,
            DirectReviewDataCalculator directReviewDataCalculator,
            Environment env) {

        this.listingSearchManager = listingSearchManager;
        this.surveillanceDataCalculator = surveillanceDataCalculator;
        this.nonConformityDataCalculator = nonConformityDataCalculator;
        this.directReviewDataCalculator = directReviewDataCalculator;
        this.env = env;
    }

    @Transactional(readOnly = true)
    public StatisticsSnapshot getStatistics() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(getThreadCountForJob());
        LOGGER.info("Getting all current statistics.");

        StatisticsSnapshot stats = new StatisticsSnapshot();
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();
        try {
            LOGGER.info("Getting all listings.");
            List<ListingSearchResult> allListings = listingSearchManager.getAllListings();
            LOGGER.info("Completed getting all " + allListings.size() + " listings.");

            List<CertificationBodyStatusStatistic> developerCountsByStatus = new ArrayList<CertificationBodyStatusStatistic>();
            List<CertificationBodyStatusStatistic> productCountsByStatus = new ArrayList<CertificationBodyStatusStatistic>();
            List<CertificationBodyStatusStatistic> listingsByAcbAndStatus = new ArrayList<CertificationBodyStatusStatistic>();

            allListings.stream()
                .forEach(listing -> {
                    putInDeveloperBucket(listing, developerCountsByStatus);
                    putInProductBucket(listing, productCountsByStatus);
                    putInListingBucket(listing, listingsByAcbAndStatus);
                });

            stats.setDeveloperCountsByStatus(developerCountsByStatus);
            stats.setProductCountsByStatus(productCountsByStatus);
            stats.setListingCountsByStatus(listingsByAcbAndStatus);

            /////////////////////////////////////////////////////////////////////////////////////
            // Surveillance Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> surveillanceDataCalculator.getTotalSurveillanceActivities(), executorService)
                    .thenAccept(result -> stats.setSurveillanceAllStatusTotal(result)));
            // Open Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> surveillanceDataCalculator.getTotalOpenSurveillanceActivities(), executorService)
                    .thenAccept(result -> stats.setSurveillanceOpenStatus(result)));
            // Closed Surveillance Activities
            futures.add(CompletableFuture.supplyAsync(() -> surveillanceDataCalculator.getTotalClosedSurveillanceActivities(), executorService)
                    .thenAccept(result -> stats.setSurveillanceClosedStatusTotal(result)));
            // Average Duration of Closed Surveillance (in days)
            futures.add(CompletableFuture.supplyAsync(() -> surveillanceDataCalculator.getAverageTimeToCloseSurveillance(), executorService)
                    .thenAccept(result -> stats.setSurveillanceAvgTimeToClose(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            // Non-Conformity Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            // Total # of NCs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getTotalNonConformities(), executorService)
                    .thenAccept(result -> stats.setNonconfStatusAllTotal(result)));
            // Open NCs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getTotalOpenNonconformities(), executorService)
                    .thenAccept(result -> stats.setNonconfStatusOpen(result)));
            // Closed NCs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getTotalClosedNonconformities(), executorService)
                    .thenAccept(result -> stats.setNonconfStatusClosedTotal(result)));
            // Average Time to Assess Conformity (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getAverageTimeToAssessConformity(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeToAssessConformity(result)));
            // Average Time to Approve CAP (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getAverageTimeToApproveCAP(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeToApproveCAP(result)));
            // Average Duration of CAP (in days) (includes closed and ongoing CAPs)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getAverageDurationOfCAP(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgDurationOfCAP(result)));
            // Average Time from CAP Approval to Surveillance Close (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getAverageTimeFromCAPApprovalToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeFromCAPAprrovalToSurveillanceEnd(result)));
            // Average Time from CAP Close to Surveillance Close (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getAverageTimeFromCAPEndToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeFromCAPEndToSurveillanceEnd(result)));
            // Average Duration of Closed Non-Conformities (in days)
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getAverageTimeFromSurveillanceOpenToSurveillanceClose(), executorService)
                    .thenAccept(result -> stats.setNonconfAvgTimeFromSurveillanceOpenToSurveillanceClose(result)));
            // Number of Open CAPs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getOpenCAPCountByAcb(), executorService)
                    .thenAccept(result -> stats.setNonconfCAPStatusOpen(result)));
            // Number of Closed CAPs
            futures.add(CompletableFuture.supplyAsync(() -> nonConformityDataCalculator.getClosedCAPCountByAcb(), executorService)
                    .thenAccept(result -> stats.setNonconfCAPStatusClosed(result)));

            /////////////////////////////////////////////////////////////////////////////////////
            // Direct Review Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            //Total # of Direct Review Activities
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCalculator.getTotalDirectReviews(), executorService)
                    .thenAccept(result -> stats.setTotalDirectReviews(result)));
            //Open Direct Review Activities
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCalculator.getOpenDirectReviews(), executorService)
                    .thenAccept(result -> stats.setOpenDirectReviews(result)));
            //Closed Direct Review Activities
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCalculator.getClosedDirectReviews(), executorService)
                    .thenAccept(result -> stats.setClosedDirectReviews(result)));
            //Average Duration of Closed Direct Review Activities (in days)
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCalculator.getAverageTimeToCloseDirectReview(), executorService)
                    .thenAccept(result -> stats.setAverageDaysOpenDirectReviews(result)));
            //Total # of Direct Review NCs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCalculator.getTotalNonConformities(), executorService)
                    .thenAccept(result -> stats.setTotalNonConformities(result)));
            //Open NCs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCalculator.getOpenNonConformities(), executorService)
                    .thenAccept(result -> stats.setOpenNonConformities(result)));
            //Closed NCs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCalculator.getClosedNonConformities(), executorService)
                    .thenAccept(result -> stats.setClosedNonConformities(result)));
            //Number of Open CAPs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCalculator.getOpenCaps(), executorService)
                    .thenAccept(result -> stats.setOpenCaps(result)));
            //Number of Closed CAPs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCalculator.getClosedCaps(), executorService)
                    .thenAccept(result -> stats.setClosedCaps(result)));

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

    private void putInDeveloperBucket(ListingSearchResult listing, List<CertificationBodyStatusStatistic> developerCountsByStatus) {
        Optional<CertificationBodyStatusStatistic> devStatisticForAcbAndStatus = developerCountsByStatus.stream()
            .filter(devStatistic -> devStatistic.getStatusId().equals(listing.getCertificationStatus().getId())
                    && devStatistic.getAcbId().equals(listing.getCertificationBody().getId()))
            .findAny();
        if (devStatisticForAcbAndStatus.isEmpty()) {
            //create new statistic
            Set<Long> devIds = new LinkedHashSet<Long>();
            devIds.add(listing.getDeveloper().getId());
            CertificationBodyStatusStatistic statistic = CertificationBodyStatusStatistic.builder()
                    .acbId(listing.getCertificationBody().getId())
                    .acbName(listing.getCertificationBody().getName())
                    .statusId(listing.getCertificationStatus().getId())
                    .ids(devIds)
                    .build();
            developerCountsByStatus.add(statistic);
        } else {
            //add dev id to existing
            CertificationBodyStatusStatistic statistic = devStatisticForAcbAndStatus.get();
            statistic.getIds().add(listing.getDeveloper().getId());
        }
    }

    private void putInProductBucket(ListingSearchResult listing, List<CertificationBodyStatusStatistic> productCountsByStatus) {
        Optional<CertificationBodyStatusStatistic> productStatisticForAcbAndStatus = productCountsByStatus.stream()
                .filter(productStatistic -> productStatistic.getStatusId().equals(listing.getCertificationStatus().getId())
                        && productStatistic.getAcbId().equals(listing.getCertificationBody().getId()))
                .findAny();
        if (productStatisticForAcbAndStatus.isEmpty()) {
            //create new statistic
            Set<Long> productIds = new LinkedHashSet<Long>();
            productIds.add(listing.getProduct().getId());
            CertificationBodyStatusStatistic statistic = CertificationBodyStatusStatistic.builder()
                    .acbId(listing.getCertificationBody().getId())
                    .acbName(listing.getCertificationBody().getName())
                    .statusId(listing.getCertificationStatus().getId())
                    .ids(productIds)
                    .build();
            productCountsByStatus.add(statistic);
        } else {
            //add product id to existing
            CertificationBodyStatusStatistic statistic = productStatisticForAcbAndStatus.get();
            statistic.getIds().add(listing.getProduct().getId());
        }
    }

    private void putInListingBucket(ListingSearchResult listing, List<CertificationBodyStatusStatistic> listingsByAcbAndStatus) {
        Optional<CertificationBodyStatusStatistic> listingStatisticForAcbAndStatus = listingsByAcbAndStatus.stream()
                .filter(listingStatistic -> listingStatistic.getStatusId().equals(listing.getCertificationStatus().getId())
                        && listingStatistic.getAcbId().equals(listing.getCertificationBody().getId()))
                .findAny();
        if (listingStatisticForAcbAndStatus.isEmpty()) {
            //create new statistic
            Set<Long> listingIds = new LinkedHashSet<Long>();
            listingIds.add(listing.getId());
            CertificationBodyStatusStatistic statistic = CertificationBodyStatusStatistic.builder()
                    .acbId(listing.getCertificationBody().getId())
                    .acbName(listing.getCertificationBody().getName())
                    .statusId(listing.getCertificationStatus().getId())
                    .ids(listingIds)
                    .build();
            listingsByAcbAndStatus.add(statistic);
        } else {
            //add listing id to existing
            CertificationBodyStatusStatistic statistic = listingStatisticForAcbAndStatus.get();
            statistic.getIds().add(listing.getId());
        }
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

}
