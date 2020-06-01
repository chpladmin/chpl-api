package gov.healthit.chpl.scheduler.job.summarystatistics;

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

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.Statistics;
import gov.healthit.chpl.entity.CertificationStatusType;

@Component()
public class HistoricalStatisticsCreator {
    private static final Logger LOGGER = LogManager.getLogger("summaryStatisticsCreatorJobLogger");

    private ListingStatisticsDAO listingStatisticsDAO;
    private DeveloperStatisticsDAO developerStatisticsDAO;
    private SurveillanceStatisticsDAO surveillanceStatisticsDAO;
    private Environment env;

    @Autowired
    public HistoricalStatisticsCreator(ListingStatisticsDAO listingStatisticsDAO, DeveloperStatisticsDAO developerStatisticsDAO,
            SurveillanceStatisticsDAO surveillanceStatisticsDAO, CertifiedProductDAO certifiedProductDAO, Environment env) {
        this.listingStatisticsDAO = listingStatisticsDAO;
        this.developerStatisticsDAO = developerStatisticsDAO;
        this.surveillanceStatisticsDAO = surveillanceStatisticsDAO;
        this.env = env;
    }

    @Transactional(readOnly = true)
    public Statistics getStatistics(DateRange dateRange) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(getThreadCountForJob());

        Statistics stats = new Statistics();
        stats.setDateRange(dateRange);
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();

        try {
            // developers
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopers(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalDevelopers(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopersWith2014Listings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersWith2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopersWith2015Listings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersWith2015Listings(result)));

            // listings
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCertifiedProducts(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalCertifiedProducts(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCPsActive2014Listings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalCPsActive2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCPsActive2015Listings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalCPsActive2015Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalCPsActiveListings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalCPsActiveListings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalListings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalListings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2014Listings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotal2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2015Listings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotal2015Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2011Listings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotal2011Listings(result)));

            // surveillance
            futures.add(CompletableFuture.supplyAsync(() -> getTotalSurveillanceActivities(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalSurveillanceActivities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalOpenSurveillanceActivities(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalOpenSurveillanceActivities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalClosedSurveillanceActivities(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalClosedSurveillanceActivities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalNonConformities(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalNonConformities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalOpenNonconformities(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalOpenNonconformities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalClosedNonconformities(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalClosedNonconformities(result)));

            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own ExecutorService
            // This is necessary so that the system can indicate that the job and it's threads are still running
            combinedFutures.get();

        } finally {
            executorService.shutdown();
        }
        return stats;
    }

    private Long getTotalDevelopers(DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopers(dateRange);
        return total;
    }

    private Long getTotalDevelopersWith2014Listings(DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2014", null);
        return total;
    }

    private Long getTotalDevelopersWith2015Listings(DateRange dateRange) {
        Long total = developerStatisticsDAO.getTotalDevelopersWithListingsByEditionAndStatus(dateRange, "2015", null);
        return total;
    }

    private Long getTotalCertifiedProducts(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(dateRange, null, null);
        return total;
    }

    private Long getTotalCPsActive2014Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2014", activeStatuses);
        return total;
    }

    private Long getTotalCPsActive2015Listings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, "2015", activeStatuses);
        return total;
    }

    private Long getTotalCPsActiveListings(DateRange dateRange) {
        List<String> activeStatuses = new ArrayList<String>();
        activeStatuses.add(CertificationStatusType.Active.getName().toUpperCase());
        Long total = listingStatisticsDAO
                .getTotalUniqueProductsByEditionAndStatus(dateRange, null, activeStatuses);
        return total;
    }

    private Long getTotalListings(DateRange dateRange) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(dateRange, null, null);
        return total;
    }

    private Long getTotal2014Listings(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(dateRange, "2014", null);
        return total;
    }

    private Long getTotal2015Listings(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(dateRange, "2015", null);
        return total;
    }

    private Long getTotal2011Listings(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(dateRange, "2011", null);
        return total;
    }

    private Long getTotalSurveillanceActivities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalSurveillanceActivities(dateRange);
        return total;
    }

    private Long getTotalOpenSurveillanceActivities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(dateRange);
        return total;
    }

    private Long getTotalClosedSurveillanceActivities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(dateRange);
        return total;
    }

    private Long getTotalNonConformities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalNonConformities(dateRange);
        return total;
    }

    private Long getTotalOpenNonconformities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalOpenNonconformities(dateRange);
        return total;
    }

    private Long getTotalClosedNonconformities(DateRange dateRange) {
        Long total = surveillanceStatisticsDAO.getTotalClosedNonconformities(dateRange);
        return total;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

}
