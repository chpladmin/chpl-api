package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jfree.data.time.DateRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.DateUtil;

@Component
public class DeveloperAttestationPeriodCalculator {
    private static final Integer MAX_PAGE_SIZE = 100;

    private DeveloperDAO developerDao;
    private ListingSearchService listingSearchService;
    private List<String> activeStatuses = CertificationStatusUtil.getActiveStatusNames();

    @Autowired
    public DeveloperAttestationPeriodCalculator(DeveloperDAO developerDao,
            ListingSearchService listingSearchService) {
        this.developerDao = developerDao;
        this.listingSearchService = listingSearchService;
    }

    public List<Developer> getDevelopersWithActiveListingsDuringAttestationPeriod(AttestationPeriod period, Logger logger) {
        Map<Long, List<ListingSearchResult>> listingsByDeveloper = getMapOfListingSearchResultsByDeveloper(logger);

        return getAllDevelopers().stream()
                .filter(dev -> {
                    List<ListingSearchResult> listingsForDeveloper = getListingDataForDeveloper(dev, listingsByDeveloper, logger);
                    return doesActiveListingExistDuringAttestationPeriod(listingsForDeveloper, period)
                            && doesCurrentActiveListingExist(listingsForDeveloper);
                })
                .toList();
    }

    private List<Developer> getAllDevelopers() {
        return developerDao.findAll();
    }

    private Boolean doesActiveListingExistDuringAttestationPeriod(List<ListingSearchResult> listingsForDeveloper, AttestationPeriod period) {
        if (CollectionUtils.isEmpty(listingsForDeveloper)) {
            return false;
        }
        return listingsForDeveloper.stream()
                .filter(listing -> isListingActiveDuringPeriod(listing, period))
                .findAny()
                .isPresent();
    }

    private Boolean doesCurrentActiveListingExist(List<ListingSearchResult> listings) {
        if (CollectionUtils.isEmpty(listings)) {
            return false;
        }
        return listings.stream()
                .filter(listing -> listing.isCertificateActive())
                .findAny()
                .isPresent();
    }

    private Boolean isListingActiveDuringPeriod(ListingSearchResult listing, AttestationPeriod period) {
        List<CertificationStatusEvent> statusEvents = listing.getStatusEvents().stream()
                .map(statusEventSearchResult ->  CertificationStatusEvent.builder()
                        .status(CertificationStatus.builder()
                                .name(statusEventSearchResult.getStatus().getName())
                                .build())
                        .eventDate(toDate(statusEventSearchResult.getStatusStart()).getTime())
                        .build())
                .sorted(Comparator.comparing(CertificationStatusEvent::getEventDate))
                .toList();

        return isListingActiveDuringAttestationPeriod(statusEvents, period);
    }

    private boolean isListingActiveDuringAttestationPeriod(List<CertificationStatusEvent> statusEvents, AttestationPeriod period) {
        List<DateRange> activeDateRanges = getDateRangesWithActiveStatus(statusEvents);
        return activeDateRanges.stream()
            .filter(activeDates -> toDate(period.getPeriodStart()).getTime() <= activeDates.getUpperMillis()
                    && toDate(period.getPeriodEnd()).getTime() >= activeDates.getLowerMillis())
            .findAny().isPresent();
    }

    private List<DateRange> getDateRangesWithActiveStatus(List<CertificationStatusEvent> listingStatusEvents) {
        //Assumes statuses are sorted
        return IntStream.range(0, listingStatusEvents.size())
            .filter(i -> listingStatusEvents.get(i) != null && listingStatusEvents.get(i).getStatus() != null
                && !StringUtils.isEmpty(listingStatusEvents.get(i).getStatus().getName()))
            .filter(i -> activeStatuses.contains(listingStatusEvents.get(i).getStatus().getName()))
            .mapToObj(i -> new DateRange(new Date(listingStatusEvents.get(i).getEventDate()),
                    i < (listingStatusEvents.size() - 1) ? new Date(listingStatusEvents.get(i + 1).getEventDate())
                            //Math.max here to handle the case where status is a future date
                            : new Date(Math.max(System.currentTimeMillis(), listingStatusEvents.get(i).getEventDate()))))
            .collect(Collectors.toList());
    }

    private Date toDate(LocalDate localDate) {
        return  DateUtil.toDate(localDate);
    }

    private List<ListingSearchResult> getListingDataForDeveloper(Developer developer, Map<Long, List<ListingSearchResult>> listingsByDeveloper, Logger logger) {
        logger.info("Getting listing data for developer " + developer.getId());
        if (listingsByDeveloper.containsKey(developer.getId())) {
            return listingsByDeveloper.get(developer.getId());
        } else {
            return null;
        }
    }

    private Map<Long, List<ListingSearchResult>> getMapOfListingSearchResultsByDeveloper(Logger logger) {
        return getActiveListings(logger).stream()
                .collect(Collectors.groupingBy(listingSearchResult -> listingSearchResult.getDeveloper().getId()));
    }

    private List<ListingSearchResult> getActiveListings(Logger logger) {
        logger.info("Getting all active listings");
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationStatuses(CertificationStatusUtil.getActiveStatusNames().stream().collect(Collectors.toSet()))
                .pageSize(MAX_PAGE_SIZE)
                .pageNumber(0)
                .build();
        List<ListingSearchResult> activeListings = listingSearchService.getAllPagesOfSearchResults(searchRequest, logger);
        logger.info("Found " + activeListings.size() + " active listings");
        return activeListings;
    }
}
