package gov.healthit.chpl.attestation.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.jfree.data.time.DateRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.CertificationStatusUtil;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class AttestationCertificationBodyService {
    private static final Integer MAX_PAGE_SIZE = 100;

    private ListingSearchService listingSearchService;
    private AttestationPeriodService attestationPeriodService;
    private DeveloperManager developerManager;
    private CertificationBodyManager certificationBodyManager;
    private List<String> activeStatusNames = CertificationStatusUtil.getActiveStatusNames();

    @Autowired
    public AttestationCertificationBodyService(ListingSearchService listingSearchService, AttestationPeriodService attestationPeriodService,
            DeveloperManager developerManager, CertificationBodyManager certificationBodyManager) {
        this.listingSearchService = listingSearchService;
        this.attestationPeriodService = attestationPeriodService;
        this.developerManager = developerManager;
        this.certificationBodyManager = certificationBodyManager;
    }

    public List<CertificationBody> getAssociatedCertificationBodies(Long developerId) {
        return getAssociatedCertificationBodies(developerId, attestationPeriodService.getMostRecentPastAttestationPeriod().getId());
    }

    public List<CertificationBody> getAssociatedCertificationBodies(Long developerId, Long attestationPeriodId) {
        try {
            AttestationPeriod period = attestationPeriodService.getAllPeriods().stream()
                    .filter(per -> per.getId().equals(attestationPeriodId))
                    .findAny()
                    .get();
            return getListingDataForDeveloper(developerManager.getById(developerId)).stream()
                .filter(listing -> isListingActiveDuringPeriod(listing, period))
                .map(listing -> listing.getCertificationBody())
                .collect(Collectors.toSet()).stream()
                .map(pair -> getCertificationBody(pair.getId()))
                .toList();
        } catch (ValidationException | EntityRetrievalException e) {
            LOGGER.error("Could not identify Certification Body for Developer with id: {}", developerId);
            return null;
        }
    }

    private List<ListingSearchResult> getListingDataForDeveloper(Developer developer) throws ValidationException {
        SearchRequest request = SearchRequest.builder()
                .certificationStatuses(activeStatusNames.stream().collect(Collectors.toSet()))
                .developer(developer.getName())
                .pageSize(MAX_PAGE_SIZE)
                .build();
        return listingSearchService.getAllPagesOfSearchResults(request);
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
            .filter(i -> activeStatusNames.contains(listingStatusEvents.get(i).getStatus().getName()))
            .mapToObj(i -> new DateRange(new Date(listingStatusEvents.get(i).getEventDate()),
                    i < (listingStatusEvents.size() - 1) ? new Date(listingStatusEvents.get(i + 1).getEventDate())
                            //Math.max here to handle the case where status is a future date
                            : new Date(Math.max(System.currentTimeMillis(), listingStatusEvents.get(i).getEventDate()))))
            .collect(Collectors.toList());
    }

    private CertificationBody getCertificationBody(Long acbId) {
        try {
            return certificationBodyManager.getById(acbId);
        } catch (Exception e) {
            LOGGER.error("Could not identify Certification Body with id: {}", acbId);
            return null;
        }
    }

    private Date toDate(LocalDate localDate) {
        return  DateUtil.toDate(localDate);
    }
}
