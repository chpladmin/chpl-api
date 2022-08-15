package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.jfree.data.time.DateRange;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.dao.AttestationDAO;
import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import gov.healthit.chpl.attestation.manager.AttestationCertificationBodyService;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.attestation.report.validation.AttestationValidationService;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResponse;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.service.DirectReviewSearchService;
import gov.healthit.chpl.util.DateUtil;

@Component
public class DeveloperAttestationReportDataCollection {
    private static final Integer MAX_PAGE_SIZE = 100;

    private static final Long INFORMATION_BLOCKING_ATTESTATION_ID = 1L;
    private static final Long ASSURANCES_ATTESTATION_ID = 2L;
    private static final Long COMMUNICATIONS_ATTESTATION_ID = 3L;
    private static final Long API_ATTESTATION_ID = 4L;
    private static final Long RWT_ATTESTATION_ID = 5L;

    private static final String RWT_VALIDATION_TRUE = "Has listing(s) with RWT criteria";
    private static final String RWT_VALIDATION_FALSE = "No listings with RWT criteria";
    private static final String ASSURANCES_VALIDATION_TRUE = "Has listing(s) with Assurances criteria (b)(6) or (b)(10)";
    private static final String ASSURANCES_VALIDATION_FALSE = "No listings with Assurances criteria (b)(6) or (b)(10)";
    private static final String API_VALIDATION_TRUE = "Has listing(s) with API criteria (g)(7)-(g)(10)";
    private static final String API_VALIDATION_FALSE = "No listings with API criteria (g)(7)-(g)(10)";

    private DeveloperDAO developerDAO;
    private UserDeveloperMapDAO userDeveloperMapDao;
    private ListingSearchService listingSearchService;
    private AttestationDAO attestationDAO;
    private DirectReviewSearchService directReviewService;
    private AttestationValidationService attestationValidationService;
    private CertificationBodyDAO certificationBodyDAO;
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;
    private AttestationPeriodService attestationPeriodService;
    private AttestationCertificationBodyService attestationCertificationBodyService;

    private Map<Long, List<ListingSearchResult>> developerListings = new HashMap<Long, List<ListingSearchResult>>();

    public DeveloperAttestationReportDataCollection(DeveloperDAO developerDAO,
            UserDeveloperMapDAO userDeveloperMapDao, ListingSearchService listingSearchService,
            AttestationDAO attestationDAO,
            DirectReviewSearchService directReviewService, AttestationValidationService attestationValidationService, CertificationBodyDAO certificationBodyDAO,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO, AttestationPeriodService attestationPeriodService,
            AttestationCertificationBodyService attestationCertificationBodyService) {

        this.developerDAO = developerDAO;
        this.userDeveloperMapDao = userDeveloperMapDao;
        this.listingSearchService = listingSearchService;
        this.attestationDAO = attestationDAO;
        this.directReviewService = directReviewService;
        this.attestationValidationService = attestationValidationService;
        this.certificationBodyDAO = certificationBodyDAO;
        this.developerCertificationBodyMapDAO = developerCertificationBodyMapDAO;
        this.attestationPeriodService = attestationPeriodService;
        this.attestationCertificationBodyService = attestationCertificationBodyService;
}

    private List<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(),
            CertificationStatusType.SuspendedByAcb.getName(),
            CertificationStatusType.SuspendedByOnc.getName())
            .collect(Collectors.toList());

    public List<DeveloperAttestationReport> collect(List<Long> selectedAcbIds, Logger logger) {
        AttestationPeriod mostRecentPastPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
        logger.info("Most recent past attestation period: {} - {} ", mostRecentPastPeriod.getPeriodStart().toString(), mostRecentPastPeriod.getPeriodEnd().toString());
        logger.info("Selected AcbsId: {}", selectedAcbIds.stream()
                .map(id -> id.toString())
                .collect(Collectors.joining(", ")));

        List<Developer> developers = getAllDevelopers().stream()
                .filter(dev -> doesActiveListingExistDuringAttestationPeriod(getListingDataForDeveloper(dev, logger), mostRecentPastPeriod))
                .toList();

        List<DeveloperAttestationReport> reportRows = developers.stream()
                .filter(dev -> isDeveloperManagedBySelectedAcbs(dev, selectedAcbIds))
                .map(dev -> {
                    DeveloperAttestationSubmission attestation = getDeveloperAttestation(dev.getId(), mostRecentPastPeriod.getId());

                    return DeveloperAttestationReport.builder()
                        .developerName(dev.getName())
                        .developerCode(dev.getDeveloperCode())
                        .developerId(dev.getId())
                        .pointOfContactName(getPointOfContactFullName(dev))
                        .pointOfContactEmail(getPointOfContactEmail(dev))
                        .developerUsers(getDeveloperUsers(dev))
                        .attestationStatus(attestation != null ? "Published" : "")
                        .attestationPublishDate(attestation != null ? attestation.getDatePublished() : null)
                        .attestationPeriod(String.format("%s - %s", mostRecentPastPeriod.getPeriodStart().toString(),
                                mostRecentPastPeriod.getPeriodEnd().toString()))
                        .informationBlocking(getAttestationResponse(attestation, INFORMATION_BLOCKING_ATTESTATION_ID))
                        .assurances(getAttestationResponse(attestation, ASSURANCES_ATTESTATION_ID))
                        .communications(getAttestationResponse(attestation, COMMUNICATIONS_ATTESTATION_ID))
                        .applicationProgrammingInterfaces(getAttestationResponse(attestation, API_ATTESTATION_ID))
                        .realWorldTesting(getAttestationResponse(attestation, RWT_ATTESTATION_ID))
                        .submitterName(getSubmitterName(attestation))
                        .submitterEmail(getSubmitterEmail(attestation))
                        .totalSurveillances(getTotalSurveillances(dev, logger))
                        .totalSurveillanceNonconformities(getTotalSurveillanceNonconformities(dev, logger))
                        .openSurveillanceNonconformities(getOpenSurveillanceNonconformities(dev, logger))
                        .totalDirectReviewNonconformities(getTotalDirectReviewNonconformities(dev, logger))
                        .openDirectReviewNonconformities(getOpenDirectReviewNonconformities(dev, logger))
                        .assurancesValidation(getAssurancesValidation(dev, logger))
                        .realWorldTestingValidation(getRealWorldTestingValidation(dev, logger))
                        .apiValidation(getApiValidation(dev, logger))
                        .activeAcbs(getActiveAcbs())
                        .developerAcbMap(getDeveloperAcbMapping(dev, logger))
                        .build();
                })
                .sorted(Comparator.comparing(DeveloperAttestationReport::getDeveloperName))
                .toList();

        logger.info("Total Report Rows found: {}", reportRows.size());
        developerListings.clear();
        return reportRows;
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

    private List<ListingSearchResult> getListingDataForDeveloper(Developer developer, Logger logger) {
        if (!developerListings.containsKey(developer.getId())) {
            SearchRequest request = SearchRequest.builder()
                    .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                    .developer(developer.getName())
                    .pageSize(MAX_PAGE_SIZE)
                    .build();

            try {
                ListingSearchResponse response = listingSearchService.findListings(request);
                developerListings.put(developer.getId(), response.getResults());
            } catch (ValidationException e) {
                logger.error("Could not retrieve listings for developer {}.", developer.getName());
                logger.error(e);
                developerListings.put(developer.getId(), new ArrayList<ListingSearchResult>());
            }
        }
        return developerListings.get(developer.getId());
    }

    private List<Developer> getAllDevelopers() {
        return developerDAO.findAll();
    }

    private DeveloperAttestationSubmission getDeveloperAttestation(Long developerId, Long attestationPeriodId) {
        List<DeveloperAttestationSubmission> attestations =
                attestationDAO.getDeveloperAttestationSubmissionsByDeveloperAndPeriod(developerId, attestationPeriodId);

        if (attestations != null && attestations.size() > 0) {
            return attestations.get(0);
        } else {
            return null;
        }
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

    private List<CertificationBody> getActiveAcbs() {
        return certificationBodyDAO.findAllActive().stream()
                .map(dto -> new CertificationBody(dto))
                .toList();
    }

    private Date toDate(LocalDate localDate) {
        return  DateUtil.toDate(localDate);
    }

    private String getPointOfContactFullName(Developer developer) {
        return (developer != null && developer.getContact() != null && developer.getContact().getFullName() != null) ? developer.getContact().getFullName() : "";
    }

    private String getPointOfContactEmail(Developer developer) {
        return (developer != null && developer.getContact() != null && developer.getContact().getEmail() != null) ? developer.getContact().getEmail() : "";
    }

    private List<String> getDeveloperUsers(Developer developer) {
        List<UserDeveloperMapDTO> userDeveloperMaps = userDeveloperMapDao.getByDeveloperId(developer.getId());
        if (CollectionUtils.isEmpty(userDeveloperMaps)) {
            return null;
        }
        return userDeveloperMaps.stream()
            .filter(udm -> udm.getUser() != null)
            .map(udm -> formatUserData(udm.getUser()))
            .collect(Collectors.toList());
    }

    private String formatUserData(UserDTO user) {
        String userContactText = "";
        if (!StringUtils.isEmpty(user.getFullName())) {
            userContactText = user.getFullName();
        }
        if (!StringUtils.isEmpty(user.getEmail())) {
            if (!StringUtils.isEmpty(userContactText)) {
                userContactText += " ";
            }
            userContactText += "<" + user.getEmail() + ">";
        }
        return userContactText;
    }

    private String getAttestationResponse(DeveloperAttestationSubmission attestation, Long attestationId) {
        if (attestation == null) {
            return "";
        } else {
             AttestationSubmittedResponse response = attestation.getResponses().stream()
                    .filter(resp -> resp.getAttestation().getId().equals(attestationId))
                    .findAny()
                    .orElse(null);
            return response != null
                ? String.format("%s : %s", response.getAttestation().getCondition().getName(), response.getResponse().getResponse())
                : "";
        }
    }

    private String getSubmitterName(DeveloperAttestationSubmission attestation) {
        return attestation != null ? attestation.getSignature() : "";
    }

    private String getSubmitterEmail(DeveloperAttestationSubmission attestation) {
        return attestation != null ? attestation.getSignatureEmail() : "";
    }

    private Long getTotalSurveillances(Developer developer, Logger logger) {
        return getListingDataForDeveloper(developer, logger).stream()
                .filter(listing -> activeStatuses.contains(listing.getCertificationStatus().getName()))
                .map(listing -> addSurveillanceCount(listing))
                .collect(Collectors.summingLong(Long::longValue));
    }

    private Long getTotalSurveillanceNonconformities(Developer developer, Logger logger) {
        return getListingDataForDeveloper(developer, logger).stream()
                .filter(listing -> activeStatuses.contains(listing.getCertificationStatus().getName()))
                .map(listing -> addOpenAndClosedNonconformityCount(listing))
                .collect(Collectors.summingLong(Long::longValue));
    }

    private Long addSurveillanceCount(ListingSearchResult listing) {
        return listing.getSurveillanceCount() != null ? listing.getSurveillanceCount() : 0L;
    }

    private Long addOpenAndClosedNonconformityCount(ListingSearchResult listing) {
        Long closed = listing.getClosedSurveillanceNonConformityCount() != null ? listing.getClosedSurveillanceNonConformityCount() : 0L;
        Long open = listing.getOpenSurveillanceNonConformityCount() != null ? listing.getOpenSurveillanceNonConformityCount() : 0L;
        return closed + open;
    }

    private Long getOpenSurveillanceNonconformities(Developer developer, Logger logger) {
        return getListingDataForDeveloper(developer, logger).stream()
                .filter(listing -> activeStatuses.contains(listing.getCertificationStatus().getName()))
                .map(listing -> listing.getOpenSurveillanceNonConformityCount())
                .collect(Collectors.summingLong(Long::longValue));
    }

    private Long getTotalDirectReviewNonconformities(Developer developer, Logger logger) {
        return directReviewService.getDeveloperDirectReviews(developer.getId(), logger).stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .count();
    }

private Long getOpenDirectReviewNonconformities(Developer developer, Logger logger) {
        return directReviewService.getDeveloperDirectReviews(developer.getId(), logger).stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .filter(nc -> nc.getNonConformityStatus().equalsIgnoreCase(DirectReviewNonConformity.STATUS_OPEN))
                .count();
    }

    private String getRealWorldTestingValidation(Developer developer, Logger logger) {
        if (attestationValidationService.validateRealWorldTesting(developer, getListingDataForDeveloper(developer, logger))) {
            return RWT_VALIDATION_TRUE;
        } else {
            return RWT_VALIDATION_FALSE;
        }
    }

    private String getAssurancesValidation(Developer developer, Logger logger) {
        if (attestationValidationService.validateAssurances(developer, getListingDataForDeveloper(developer, logger))) {
            return ASSURANCES_VALIDATION_TRUE;
        } else {
            return ASSURANCES_VALIDATION_FALSE;
        }
    }

    private String getApiValidation(Developer developer, Logger logger) {
        if (attestationValidationService.validateApi(developer, getListingDataForDeveloper(developer, logger))) {
            return API_VALIDATION_TRUE;
        } else {
            return API_VALIDATION_FALSE;
        }
    }

    private Map<Pair<Long, Long>, Boolean> getDeveloperAcbMapping(Developer developer, Logger logger) {
        Map<Pair<Long, Long>, Boolean> developerAcbMap = new HashMap<Pair<Long, Long>, Boolean>();

        attestationCertificationBodyService.getAssociatedCertificationBodies(developer.getId()).stream()
            .forEach(acb -> developerAcbMap.put(Pair.of(developer.getId(), acb.getId()), true));

        return developerAcbMap;
    }

    private Boolean isDeveloperManagedBySelectedAcbs(Developer developer, List<Long> acbIds) {
        return developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(developer.getId()).stream()
                .filter(acb -> acbIds.contains(acb.getId()))
                .findAny()
                .isPresent();
    }
}
