package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.util.ArrayList;
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

import gov.healthit.chpl.search.domain.ListingSearchResult;

@Component
public class StatisticsSnapshotCreator {
    private static final Logger LOGGER = LogManager.getLogger("summaryStatisticsCreatorJobLogger");

    private SurveillanceDataCreator surveillanceDataCreator;
    private NonConformityDataCreator nonConformityDataCreator;
    private DirectReviewDataCreator directReviewDataCreator;
    private Environment env;

    @Autowired
    public StatisticsSnapshotCreator(SurveillanceDataCreator surveillanceDataCreator,
            NonConformityDataCreator nonConformityDataCreator,
            DirectReviewDataCreator directReviewDataCreator,
            Environment env) {

        this.surveillanceDataCreator = surveillanceDataCreator;
        this.nonConformityDataCreator = nonConformityDataCreator;
        this.directReviewDataCreator = directReviewDataCreator;
        this.env = env;
    }

    @Transactional(readOnly = true)
    public StatisticsSnapshot getStatistics(List<ListingSearchResult> allListings) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(getThreadCountForJob());

        LOGGER.info("Getting all current statistics.");

        StatisticsSnapshot stats = new StatisticsSnapshot();
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();

        try {

            //Save a few basic details about all listings so we can calculate statistics as-needed
            List<Listing> statListings = allListings.stream()
                    .map(listing -> Listing.builder()
                            .id(listing.getId())
                            .developerId(listing.getDeveloper().getId())
                            .productId(listing.getProduct().getId())
                            .acbId(listing.getCertificationBody().getId())
                            .statusId(listing.getCertificationStatus().getId())
                            .build())
                    .toList();
            stats.setListings(statListings);

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

            /////////////////////////////////////////////////////////////////////////////////////
            // Direct Review Statistics
            /////////////////////////////////////////////////////////////////////////////////////
            //Total # of Direct Review Activities
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCreator.getTotalDirectReviews(), executorService)
                    .thenAccept(result -> stats.setTotalDirectReviews(result)));
            //Open Direct Review Activities
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCreator.getOpenDirectReviews(), executorService)
                    .thenAccept(result -> stats.setOpenDirectReviews(result)));
            //Closed Direct Review Activities
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCreator.getClosedDirectReviews(), executorService)
                    .thenAccept(result -> stats.setClosedDirectReviews(result)));
            //Average Duration of Closed Direct Review Activities (in days)
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCreator.getAverageTimeToCloseDirectReview(), executorService)
                    .thenAccept(result -> stats.setAverageDaysOpenDirectReviews(result)));
            //Total # of Direct Review NCs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCreator.getTotalNonConformities(), executorService)
                    .thenAccept(result -> stats.setTotalNonConformities(result)));
            //Open NCs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCreator.getOpenNonConformities(), executorService)
                    .thenAccept(result -> stats.setOpenNonConformities(result)));
            //Closed NCs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCreator.getClosedNonConformities(), executorService)
                    .thenAccept(result -> stats.setClosedNonConformities(result)));
            //Number of Open CAPs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCreator.getOpenCaps(), executorService)
                    .thenAccept(result -> stats.setOpenCaps(result)));
            //Number of Closed CAPs
            futures.add(CompletableFuture.supplyAsync(() -> directReviewDataCreator.getClosedCaps(), executorService)
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

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

}
