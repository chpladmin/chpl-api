package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jfree.data.time.DateRange;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertificationStatusEventComparator;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeveloperAttestationReportJob implements Job {

    @Autowired
    private DeveloperDAO developerDAO;

    @Autowired
    private ListingSearchService listingSearchService;

    @Autowired
    private AttestationDAO attestationDAO;

    private Map<Long, List<CertifiedProductBasicSearchResult>> developerListings = new HashMap<Long, List<CertifiedProductBasicSearchResult>>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Attestation Report job. *********");
        try {
            AttestationPeriod mostRecentPastPeriod = getMostRecentPastAttestationPeriod();

            List<Developer> developers = getAllDevelopers().stream()
                    .filter(dev -> doesActiveListingExistDuringAttestationPeriod(getListingDataForDeveloper(dev), mostRecentPastPeriod))
                    .toList();

            List<DeveloperAttestationReport> reportRows = developers.stream()
                    .map(dev -> DeveloperAttestationReport.builder()
                            .developerName(dev.getName())
                            .developerCode(dev.getDeveloperCode())
                            .developerId(dev.getDeveloperId())
                            .pointOfContactName(dev.getContact().getFullName())
                            .pointOfContactEmail(dev.getContact().getEmail())
                            .attestationPeriod(String.format("%s - %s", mostRecentPastPeriod.getPeriodStart().toString(),
                                    mostRecentPastPeriod.getPeriodEnd().toString()))
                            .build())
                    .peek(row -> LOGGER.info(row.toString()))
                    .toList();

        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed Developer Attestation Report job. *********");
    }

    private Boolean doesActiveListingExistDuringAttestationPeriod(List<CertifiedProductBasicSearchResult> listingsForDeveloper, AttestationPeriod period) {
        return listingsForDeveloper.stream()
                .filter(listing -> isListingActiveDuringPeriod(listing, period))
                .findAny()
                .isPresent();
    }

    private Boolean isListingActiveDuringPeriod(CertifiedProductBasicSearchResult listing, AttestationPeriod period) {
        List<CertificationStatusEvent> statusEvents = listing.getStatusEvents().stream()
                .map(x ->  CertificationStatusEvent.builder()
                        .status(CertificationStatus.builder()
                                .name(x.split(":")[0])
                                .build())
                        .eventDate(toDate(LocalDate.parse(x.split(":")[1])).getTime())
                        .build())
                .toList();

        return isListingActiveDuringAttestationPeriod(statusEvents, period);
    }

    private List<CertifiedProductBasicSearchResult> getListingDataForDeveloper(Developer developer) {
        if (!developerListings.containsKey(developer.getDeveloperId())) {
            SearchRequest request = SearchRequest.builder()
                    .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                    .developer(developer.getName())
                    .build();

            try {
                SearchResponse response = listingSearchService.search(request);
                developerListings.put(developer.getDeveloperId(), response.getResults());
            } catch (ValidationException e) {
                LOGGER.error("Could not retrieve listings for developer {}.", developer.getName());
                LOGGER.error(e);
                developerListings.put(developer.getDeveloperId(), new ArrayList<CertifiedProductBasicSearchResult>());
            }
        }
        return developerListings.get(developer.getDeveloperId());
    }

    private List<Developer> getAllDevelopers() {
        return developerDAO.findAll().stream()
                .map(dto -> new Developer(dto))
                .toList();
    }

    public AttestationPeriod getMostRecentPastAttestationPeriod() {
        List<AttestationPeriod> periods = attestationDAO.getAllPeriods();
        if (periods == null || periods.size() == 0) {
            return null;
        }

        periods = periods.stream()
                .sorted(Comparator.comparing(AttestationPeriod::getPeriodEnd).reversed())
                .filter(per -> per.getPeriodEnd().isBefore(LocalDate.now()))
                .toList();

        if (periods == null || periods.size() == 0) {
            return null;
        }

        return periods.get(0);
    }

    private boolean isListingActiveDuringAttestationPeriod(List<CertificationStatusEvent> statusEvents, AttestationPeriod period) {
        List<DateRange> activeDateRanges = getDateRangesWithActiveStatus(statusEvents);
        return activeDateRanges.stream()
            .filter(activeDates -> toDate(period.getPeriodStart()).getTime() <= activeDates.getUpperMillis()
                    && toDate(period.getPeriodEnd()).getTime() >= activeDates.getLowerMillis())
            .findAny().isPresent();
    }

    private List<DateRange> getDateRangesWithActiveStatus(List<CertificationStatusEvent> listingStatusEvents) {
        List<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(),
                CertificationStatusType.SuspendedByAcb.getName(),
                CertificationStatusType.SuspendedByOnc.getName())
                .collect(Collectors.toList());
        listingStatusEvents.sort(new CertificationStatusEventComparator());
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
        ZoneId defaultZoneId = ZoneId.systemDefault();
        return  Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
    }

//    @Data
//    @AllArgsConstructor
//    private class ListingStatusEvent {
//        private String status;
//        private LocalDate statusDate;
//    }
}
