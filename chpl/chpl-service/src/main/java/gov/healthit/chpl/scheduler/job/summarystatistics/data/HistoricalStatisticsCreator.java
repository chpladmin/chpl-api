package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationStatusEventDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.CertificationStatusType;

@Component()
public class HistoricalStatisticsCreator {
    private static final Logger LOGGER = LogManager.getLogger("summaryStatisticsCreatorJobLogger");
    private static final Long EDITION_2015_ID = 3L;
    private static final Long EDITION_2014_ID = 2L;

    private ListingStatisticsDAO listingStatisticsDAO;
    private DeveloperStatisticsDAO developerStatisticsDAO;
    private SurveillanceStatisticsDAO surveillanceStatisticsDAO;
    private CertificationStatusEventDAO certificationStatusEventDAO;
    private Environment env;

    @Autowired
    public HistoricalStatisticsCreator(ListingStatisticsDAO listingStatisticsDAO, DeveloperStatisticsDAO developerStatisticsDAO,
            SurveillanceStatisticsDAO surveillanceStatisticsDAO, CertifiedProductDAO certifiedProductDAO,
            CertificationStatusEventDAO certificationStatusEventDAO, Environment env) {
        this.listingStatisticsDAO = listingStatisticsDAO;
        this.developerStatisticsDAO = developerStatisticsDAO;
        this.surveillanceStatisticsDAO = surveillanceStatisticsDAO;
        this.certificationStatusEventDAO = certificationStatusEventDAO;
        this.env = env;
    }

    @Transactional(readOnly = true)
    public CsvStatistics getStatistics(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> statusesForAllListings, DateRange dateRange)
            throws InterruptedException, ExecutionException {

        ExecutorService executorService = Executors.newFixedThreadPool(getThreadCountForJob());

        //Get all of the statuses for all of the listings
        //Map<Long, List<CertificationStatusEventDTO>> statusesForAllListings = getAllStatusesForAllListings();

        CsvStatistics stats = new CsvStatistics();
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

            //products
            futures.add(CompletableFuture.supplyAsync(() -> getTotalUniqueProducts(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalUniqueProducts(result)));
            futures.add(CompletableFuture.supplyAsync(() ->
                    getTotalProductsActive2014Listings(allListings, statusesForAllListings, dateRange), executorService)
                    .thenAccept(result -> stats.setTotalProductsActive2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() ->
                    getTotalProductsActive2015Listings(allListings, statusesForAllListings, dateRange), executorService)
                    .thenAccept(result -> stats.setTotalProductsActive2015Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalProductsActiveListings(dateRange), executorService)
                    .thenAccept(result -> stats.setTotalProductsActiveListings(result)));

            // listings
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

    private Long getTotalUniqueProducts(DateRange dateRange) {
        Long total = listingStatisticsDAO.getTotalUniqueProductsByEditionAndStatus(dateRange, null, null);
        return total;
    }

    private Long getTotalProductsActive2014Listings(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses, DateRange dateRange) {

        return allListings.stream()
                .filter(listing -> listing.getCertificationEditionId().equals(EDITION_2014_ID)
                        && isListingActiveAsOfDate(listing.getId(), allStatuses, dateRange.getEndDate()))
                .count();
    }

    private Long getTotalProductsActive2015Listings(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses, DateRange dateRange) {

        return allListings.stream()
                .filter(listing -> listing.getCertificationEditionId().equals(EDITION_2015_ID)
                        && isListingActiveAsOfDate(listing.getId(), allStatuses, dateRange.getEndDate()))
                .count();
    }

    private Long getTotalProductsActiveListings(DateRange dateRange) {
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

    private boolean isListingActiveAsOfDate(Long certifiedProductId, Map<Long, List<CertificationStatusEventDTO>> allStatuses,
            Date asOfDate) {

        CertificationStatusEventDTO event = getStatusAsOfDate(allStatuses.get(certifiedProductId), asOfDate);
        if (event == null) {
            return false;
        } else {
            return event.getStatus().getStatus().toUpperCase().equals(CertificationStatusType.Active.getName().toUpperCase());
        }
    }

    private CertificationStatusEventDTO getStatusAsOfDate(List<CertificationStatusEventDTO> events, Date asOfDate) {
        List<CertificationStatusEventDTO> eventsSorted = events.stream()
                .sorted((ev1, ev2) -> ev1.getEventDate().compareTo(ev2.getEventDate()))
                .collect(Collectors.toList());

        CertificationStatusEventDTO result = null;
        for (CertificationStatusEventDTO event : eventsSorted) {
            if (event.getEventDate().before(asOfDate)) {
                result = event;
            }
        }
        return result;
    }

    private Map<Long, List<CertificationStatusEventDTO>> getAllStatusesForAllListings() {
        Map<Long, List<CertificationStatusEventDTO>> map = certificationStatusEventDAO.findAll().stream()
                .collect(Collectors.groupingBy(CertificationStatusEventDTO::getCertifiedProductId));

        Map<Long, List<CertificationStatusEventDTO>> syncdMap = new Hashtable<Long, List<CertificationStatusEventDTO>>();
        syncdMap.putAll(map);
        return syncdMap;
    }
}
