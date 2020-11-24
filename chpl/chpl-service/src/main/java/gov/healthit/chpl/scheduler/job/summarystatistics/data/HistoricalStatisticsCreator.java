package gov.healthit.chpl.scheduler.job.summarystatistics.data;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.dto.CertificationStatusEventDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import lombok.extern.log4j.Log4j2;
import one.util.streamex.StreamEx;

@Component()
@Log4j2(topic = "summaryStatisticsCreatorJobLogger")
public class HistoricalStatisticsCreator {
    private static final Long EDITION_2015_ID = 3L;
    private static final Long EDITION_2014_ID = 2L;

    private ListingStatisticsDAO listingStatisticsDAO;
    private SurveillanceStatisticsDAO surveillanceStatisticsDAO;
    private Environment env;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    @Autowired
    public HistoricalStatisticsCreator(ListingStatisticsDAO listingStatisticsDAO,
            SurveillanceStatisticsDAO surveillanceStatisticsDAO, CertifiedProductDAO certifiedProductDAO,
            Environment env) {
        this.listingStatisticsDAO = listingStatisticsDAO;
        this.surveillanceStatisticsDAO = surveillanceStatisticsDAO;
        this.env = env;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("checkstyle:linelength")
    public CsvStatistics getStatistics(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> statusesForAllListings, Date endDate)
            throws InterruptedException, ExecutionException {

        ExecutorService executorService = Executors.newFixedThreadPool(getThreadCountForJob());

        LOGGER.info("Getting csvRecord for end date " + sdf.format(endDate));

        CsvStatistics stats = new CsvStatistics();
        stats.setEndDate(endDate);
        List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>();

        try {
            // developers
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopers(allListings, statusesForAllListings, endDate), executorService)
                    .thenAccept(result -> stats.setTotalDevelopers(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopersWith2014Listings(allListings, statusesForAllListings, endDate), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersWith2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalDevelopersWith2015Listings(allListings, statusesForAllListings, endDate), executorService)
                    .thenAccept(result -> stats.setTotalDevelopersWith2015Listings(result)));

            // products
            futures.add(CompletableFuture.supplyAsync(() -> getTotalUniqueProducts(allListings, statusesForAllListings, endDate), executorService)
                    .thenAccept(result -> stats.setTotalUniqueProducts(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalProductsActive2014Listings(allListings, statusesForAllListings, endDate), executorService)
                    .thenAccept(result -> stats.setTotalUniqueProductsActive2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalProductsActive2015Listings(allListings, statusesForAllListings, endDate), executorService)
                    .thenAccept(result -> stats.setTotalUniqueProductsActive2015Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalUniqueProductsActiveListings(allListings, statusesForAllListings, endDate), executorService)
                    .thenAccept(result -> stats.setTotalUniqueProductsActiveListings(result)));

            // listings
            futures.add(CompletableFuture.supplyAsync(() -> getTotalListings(endDate), executorService)
                    .thenAccept(result -> stats.setTotalListings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2014Listings(endDate), executorService)
                    .thenAccept(result -> stats.setTotal2014Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2015Listings(endDate), executorService)
                    .thenAccept(result -> stats.setTotal2015Listings(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotal2011Listings(endDate), executorService)
                    .thenAccept(result -> stats.setTotal2011Listings(result)));

            // surveillance
            futures.add(CompletableFuture.supplyAsync(() -> getTotalSurveillanceActivities(endDate), executorService)
                    .thenAccept(result -> stats.setTotalSurveillanceActivities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalOpenSurveillanceActivities(endDate), executorService)
                    .thenAccept(result -> stats.setTotalOpenSurveillanceActivities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalClosedSurveillanceActivities(endDate), executorService)
                    .thenAccept(result -> stats.setTotalClosedSurveillanceActivities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalNonConformities(endDate), executorService)
                    .thenAccept(result -> stats.setTotalNonConformities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalOpenNonconformities(endDate), executorService)
                    .thenAccept(result -> stats.setTotalOpenNonconformities(result)));
            futures.add(CompletableFuture.supplyAsync(() -> getTotalClosedNonconformities(endDate), executorService)
                    .thenAccept(result -> stats.setTotalClosedNonconformities(result)));

            CompletableFuture<Void> combinedFutures = CompletableFuture
                    .allOf(futures.toArray(new CompletableFuture[futures.size()]));

            // This is not blocking - presumably because the job executes using it's own
            // ExecutorService
            combinedFutures.get();

            LOGGER.info("Finished getting csvRecord for end date " + sdf.format(endDate));
        } finally {
            executorService.shutdown();
        }
        return stats;
    }

    private Long getTotalDevelopers(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses, Date endDate) {

        List<DeveloperDTO> developers = allListings.stream()
                .filter(listing -> doesListingExistAsOfDate(listing.getId(),
                        allStatuses,
                        endDate))
                .map(listing -> listing.getDeveloper())
                .collect(Collectors.toList());

        return StreamEx.of(developers)
                .distinct(DeveloperDTO::getId)
                .count();
    }

    private Long getTotalDevelopersWith2014Listings(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses, Date endDate) {

        List<DeveloperDTO> developers = allListings.stream()
                .filter(listing -> listing.getCertificationEditionId().equals(EDITION_2014_ID)
                        && doesListingExistAsOfDate(listing.getId(), allStatuses, endDate))
                .map(listing -> listing.getDeveloper())
                .collect(Collectors.toList());

        return StreamEx.of(developers)
                .distinct(DeveloperDTO::getId)
                .count();
    }

    private Long getTotalDevelopersWith2015Listings(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses, Date endDate) {

        List<DeveloperDTO> developers = allListings.stream()
                .filter(listing -> listing.getCertificationEditionId().equals(EDITION_2015_ID)
                        && doesListingExistAsOfDate(listing.getId(), allStatuses, endDate))
                .map(listing -> listing.getDeveloper())
                .collect(Collectors.toList());

        return StreamEx.of(developers)
                .distinct(DeveloperDTO::getId)
                .count();

    }

    private Long getTotalUniqueProducts(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses, Date endDate) {

        List<ProductDTO> products = allListings.stream()
                .filter(listing -> doesListingExistAsOfDate(listing.getId(), allStatuses, endDate))
                .map(listing -> listing.getProduct())
                .collect(Collectors.toList());

        return StreamEx.of(products)
                .distinct(ProductDTO::getId)
                .count();

    }

    private Long getTotalProductsActive2014Listings(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses, Date endDate) {

        List<ProductDTO> products = allListings.stream()
                .filter(listing -> listing.getCertificationEditionId().equals(EDITION_2014_ID)
                        && isListingActiveAsOfDate(listing.getId(), allStatuses, endDate))
                .map(listing -> listing.getProduct())
                .collect(Collectors.toList());

        return StreamEx.of(products)
                .distinct(ProductDTO::getId)
                .count();
    }

    private Long getTotalProductsActive2015Listings(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses, Date endDate) {

        List<ProductDTO> products = allListings.stream()
                .filter(listing -> listing.getCertificationEditionId().equals(EDITION_2015_ID)
                        && isListingActiveAsOfDate(listing.getId(), allStatuses, endDate))
                .map(listing -> listing.getProduct())
                .collect(Collectors.toList());

        return StreamEx.of(products)
                .distinct(ProductDTO::getId)
                .count();
    }

    private Long getTotalUniqueProductsActiveListings(List<CertifiedProductDetailsDTO> allListings,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses, Date endDate) {

        List<ProductDTO> products = allListings.stream()
                .filter(listing -> isListingActiveAsOfDate(listing.getId(), allStatuses, endDate))
                .map(listing -> listing.getProduct())
                .collect(Collectors.toList());

        return StreamEx.of(products)
                .distinct(ProductDTO::getId)
                .count();
    }

    private Long getTotalListings(Date endDate) {
        Long total = listingStatisticsDAO
                .getTotalListingsByEditionAndStatus(endDate, null, null);
        return total;
    }

    private Long getTotal2014Listings(Date endDate) {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(endDate, "2014", null);
        return total;
    }

    private Long getTotal2015Listings(Date endDate) {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(endDate, "2015", null);
        return total;
    }

    private Long getTotal2011Listings(Date endDate) {
        Long total = listingStatisticsDAO.getTotalListingsByEditionAndStatus(endDate, "2011", null);
        return total;
    }

    private Long getTotalSurveillanceActivities(Date endDate) {
        Long total = surveillanceStatisticsDAO.getTotalSurveillanceActivities(endDate);
        return total;
    }

    private Long getTotalOpenSurveillanceActivities(Date endDate) {
        Long total = surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(endDate);
        return total;
    }

    private Long getTotalClosedSurveillanceActivities(Date endDate) {
        Long total = surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(endDate);
        return total;
    }

    private Long getTotalNonConformities(Date endDate) {
        Long total = surveillanceStatisticsDAO.getTotalNonConformities(endDate);
        return total;
    }

    private Long getTotalOpenNonconformities(Date endDate) {
        Long total = surveillanceStatisticsDAO.getTotalOpenNonconformities(endDate);
        return total;
    }

    private Long getTotalClosedNonconformities(Date endDate) {
        Long total = surveillanceStatisticsDAO.getTotalClosedNonconformities(endDate);
        return total;
    }

    private Integer getThreadCountForJob() throws NumberFormatException {
        return Integer.parseInt(env.getProperty("executorThreadCountForQuartzJobs"));
    }

    private boolean isListingActiveAsOfDate(Long certifiedProductId,
            Map<Long, List<CertificationStatusEventDTO>> allStatuses,
            Date asOfDate) {

        CertificationStatusEventDTO event = getStatusAsOfDate(allStatuses.get(certifiedProductId), asOfDate);
        if (event == null) {
            return false;
        } else {
            return event.getStatus().getStatus().toUpperCase().equals(CertificationStatusType.Active.getName()
                    .toUpperCase());
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

    private boolean doesListingExistAsOfDate(Long certifiedProductId, Map<Long, List<CertificationStatusEventDTO>> allStatuses,
            Date asOfDate) {

        return getStatusAsOfDate(allStatuses.get(certifiedProductId), asOfDate) != null;
    }
}
