package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jfree.data.time.DateRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResponse;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.DateUtil;

@Component
public class DeveloperAttestationPeriodCalculator {
    private static final Integer MAX_PAGE_SIZE = 100;

    private DeveloperDAO developerDao;
    private AttestationPeriodService attestationPeriodService;
    private ListingSearchService listingSearchService;

    private List<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(),
            CertificationStatusType.SuspendedByAcb.getName(),
            CertificationStatusType.SuspendedByOnc.getName())
            .collect(Collectors.toList());

    @Autowired
    public DeveloperAttestationPeriodCalculator(DeveloperDAO developerDao,
            AttestationPeriodService attestationPeriodService,
            ListingSearchService listingSearchService) {
        this.developerDao = developerDao;
        this.attestationPeriodService = attestationPeriodService;
        this.listingSearchService = listingSearchService;
    }

    public List<Developer> getDevelopersWithActiveListingsDuringMostRecentPastAttestationPeriod(Logger logger) {
        AttestationPeriod mostRecentPastPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
        logger.info("Most recent past attestation period: {} - {} ", mostRecentPastPeriod.getPeriodStart().toString(), mostRecentPastPeriod.getPeriodEnd().toString());

        return getAllDevelopers().stream()
                .filter(dev -> doesActiveListingExistDuringAttestationPeriod(getListingDataForDeveloper(dev, logger), mostRecentPastPeriod))
                .toList();
    }

    public List<Developer> getDevelopersWithActiveListingsDuringMostRecentPastAttestationPeriodAndMissingChangeRequest(Logger logger) {

    }

    private List<Developer> getAllDevelopers() {
        return developerDao.findAll();
    }

    private Boolean doesActiveListingExistDuringAttestationPeriod(List<ListingSearchResult> listingsForDeveloper, AttestationPeriod period) {
        return listingsForDeveloper.stream()
                .filter(listing -> isListingActiveDuringPeriod(listing, period))
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

    private List<ListingSearchResult> getListingDataForDeveloper(Developer developer, Logger logger) {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                .developer(developer.getName())
                .pageSize(MAX_PAGE_SIZE)
                .pageNumber(0)
                .build();
        return getAllPagesOfSearchResults(searchRequest, logger);
    }

    private List<ListingSearchResult> getAllPagesOfSearchResults(SearchRequest searchRequest, Logger logger) {
        List<ListingSearchResult> searchResults = new ArrayList<ListingSearchResult>();
        try {
            logger.debug(searchRequest.toString());
            ListingSearchResponse searchResponse = listingSearchService.findListings(searchRequest);
            searchResults.addAll(searchResponse.getResults());
            while (searchResponse.getRecordCount() > searchResults.size()) {
                searchRequest.setPageSize(searchResponse.getPageSize());
                searchRequest.setPageNumber(searchResponse.getPageNumber() + 1);
                logger.debug(searchRequest.toString());
                searchResponse = listingSearchService.findListings(searchRequest);
                searchResults.addAll(searchResponse.getResults());
            }
            logger.info("Found {} total listings for developer {}.", searchResults.size(), searchRequest.getDeveloper());
        } catch (ValidationException ex) {
            logger.error("Could not retrieve listings from search request.", ex);
        }
        return searchResults;
    }
}
