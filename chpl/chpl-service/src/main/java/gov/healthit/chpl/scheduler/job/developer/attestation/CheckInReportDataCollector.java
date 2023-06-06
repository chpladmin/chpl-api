package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestAttestationSubmission;
import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingCriteriaService;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "developerAttestationCheckinReportJobLogger")
public class CheckInReportDataCollector {
    private static final Integer MAX_PAGE_SIZE = 100;

    private AttestationManager attestationManager;
    private DeveloperAttestationPeriodCalculator developerAttestationPeriodCalculator;
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;
    private ListingSearchService listingSearchService;
    private DirectReviewSearchService directReviewSearchService;
    private CheckInReportSourceService checkInReportSourceService;
    private CheckInReportValidation checkInReportValidation;

    private Map<Long, List<ListingSearchResult>> developerListings = new HashMap<Long, List<ListingSearchResult>>();

    private Set<String> activeStatuses = Stream.of(
            CertificationStatusType.Active.getName(),
            CertificationStatusType.SuspendedByAcb.getName(),
            CertificationStatusType.SuspendedByOnc.getName())
            .collect(Collectors.toSet());

    public CheckInReportDataCollector(AttestationManager attestationManager, DeveloperAttestationPeriodCalculator developerAttestationPeriodCalculator,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO, ListingSearchService listingSearchService,
            DirectReviewSearchService directReviewSearchService, CertificationCriterionService certificationCriterionService, RealWorldTestingCriteriaService realWorldTestingCriteriaService,
            CheckInReportSourceService checkInReportSourceService, CheckInReportValidation checkInReportValidation,
            @Value("${assurancesCriteriaKeys}") String[] assurancesCriteriaKeys,
            @Value("${apiCriteriaKeys}") String[] apiCriteriaKeys) {

        this.attestationManager = attestationManager;
        this.developerAttestationPeriodCalculator = developerAttestationPeriodCalculator;
        this.developerCertificationBodyMapDAO = developerCertificationBodyMapDAO;
        this.listingSearchService = listingSearchService;
        this.directReviewSearchService = directReviewSearchService;
        this.checkInReportSourceService = checkInReportSourceService;
        this.checkInReportValidation = checkInReportValidation;
    }

    public List<CheckInReport> collect(List<Long> acbIds) throws EntityRetrievalException {
        return getCheckInReports(acbIds);
    }

    private List<CheckInReport> getCheckInReports(List<Long> acbIds) {
        AttestationPeriod mostRecentAttestationPeriod = attestationManager.getMostRecentPastAttestationPeriod();
        Map<Long, List<ListingSearchResult>> activeListingsByDeveloper = getMapOfActiveListingSearchResultsByDeveloper();
        LOGGER.info("Found {} developers that should have attesations for period", activeListingsByDeveloper.entrySet().size());

        return getDevelopersActiveListingsDuringMostRecentPastAttestationPeriod().stream()
                .filter(developer -> isDeveloperManagedBySelectedAcbs(developer, acbIds))
                .map(developer -> getCheckInReport(developer, getActiveListingDataForDeveloper(developer, activeListingsByDeveloper)))
                .map(report -> {
                    report.setAttestationPeriod(String.format("%s - %s", mostRecentAttestationPeriod.getPeriodStart().toString(), mostRecentAttestationPeriod.getPeriodEnd().toString()));
                    return report;
                })
                .sorted((o1, o2) -> o1.getDeveloperName().compareTo(o2.getDeveloperName())).toList();
    }

    private CheckInReport getCheckInReport(Developer developer, List<ListingSearchResult> allActiveListingsForDeveloper) {
        CheckInAttestation checkInAttestation = checkInReportSourceService.getCheckinReport(developer, attestationManager.getMostRecentPastAttestationPeriod(), LOGGER);
        LOGGER.info("Getting attestation data for Developer: {} ({})", developer.getName(), developer.getId());
        return convert(developer, checkInAttestation, allActiveListingsForDeveloper);
    }

    private CheckInReport convert(Developer developer, CheckInAttestation checkInAttestation, List<ListingSearchResult> allActiveListingsForDeveloper) {
        CheckInReport checkInReport = new CheckInReport();
        Form form = null;
        checkInReport = addDeveloperInformation(checkInReport, developer);
        if (checkInAttestation.getChangeRequest() != null) {
            form = ((ChangeRequestAttestationSubmission) checkInAttestation.getChangeRequest().getDetails()).getForm();
            checkInReport = addChangeRequestInformation(checkInReport, checkInAttestation.getChangeRequest());
            checkInReport = addResponses(checkInReport, form, ((ChangeRequestAttestationSubmission) checkInAttestation.getChangeRequest().getDetails()).getAttestationPeriod().getId());
            checkInReport = addValidation(checkInReport, form, ((ChangeRequestAttestationSubmission) checkInAttestation.getChangeRequest().getDetails()).getAttestationPeriod(), allActiveListingsForDeveloper);
        }
        if (checkInAttestation.getAttestationSubmission() != null) {
            form = checkInAttestation.getAttestationSubmission().getForm();
            checkInReport = addPublishedAttestationInformation(checkInReport, developer, checkInAttestation.getAttestationSubmission());
            checkInReport = addResponses(checkInReport, form, checkInAttestation.getAttestationSubmission().getAttestationPeriod().getId());
            checkInReport = addValidation(checkInReport, form, checkInAttestation.getAttestationSubmission().getAttestationPeriod(), allActiveListingsForDeveloper);
        }
        checkInReport = addComplianceInformation(checkInReport, developer, allActiveListingsForDeveloper);

        return checkInReport;
    }

    private CheckInReport addDeveloperInformation(CheckInReport checkInReport, Developer developer) {
        List<CertificationBody> acbs = developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(developer.getId());
        checkInReport.setDeveloperName(developer.getName());
        checkInReport.setDeveloperCode(developer.getDeveloperCode());
        checkInReport.setDeveloperId(developer.getId());
        checkInReport.setPublished(false);
        checkInReport.setRelevantAcbs(acbs.stream()
                .map(acb -> acb.getName())
                .collect(Collectors.joining("; ")));
        return checkInReport;
    }

    private CheckInReport addChangeRequestInformation(CheckInReport checkInReport, ChangeRequest cr) {
        ChangeRequestAttestationSubmission crAttestation = (ChangeRequestAttestationSubmission) cr.getDetails();

        checkInReport.setSubmittedDate(cr.getSubmittedDateTime());
        checkInReport.setPublished(false);
        checkInReport.setCurrentStatusName(cr.getCurrentStatus().getChangeRequestStatusType().getName());
        checkInReport.setLastStatusChangeDate(cr.getCurrentStatus().getStatusChangeDateTime());
        checkInReport.setRelevantAcbs(cr.getCertificationBodies().stream().map(acb -> acb.getName()).collect(Collectors.joining("; ")));
        checkInReport.setSignature(crAttestation.getSignature());
        checkInReport.setSignatureEmail(crAttestation.getSignatureEmail());
        return checkInReport;
    }

    private CheckInReport addPublishedAttestationInformation(CheckInReport checkInReport, Developer developer, AttestationSubmission attestation) {
        List<CertificationBody> acbs = developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(developer.getId());
        checkInReport.setPublished(true);
        checkInReport.setSignature(attestation.getSignature());
        checkInReport.setSignatureEmail(attestation.getSignatureEmail());
        checkInReport.setRelevantAcbs(acbs.stream().map(acb -> acb.getName()).collect(Collectors.joining("; ")));
        return checkInReport;
    }

    private CheckInReport addResponses(CheckInReport checkInReport, Form form, Long attestationPeriodId) {
        checkInReport.setInformationBlockingResponse(getAttestationResponse(form, AttestationFormMetaData.getInformationBlockingConditionId()));
        checkInReport.setInformationBlockingNoncompliantResponse(getAttestationOptionalResponse(form, AttestationFormMetaData.getInformationBlockingConditionId()));
        checkInReport.setAssurancesResponse(getAttestationResponse(form, AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId)));
        checkInReport.setAssurancesNoncompliantResponse(getAttestationOptionalResponse(form, AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId)));
        checkInReport.setCommunicationsResponse(getAttestationResponse(form, AttestationFormMetaData.getCommunicationConditionId()));
        checkInReport.setCommunicationsNoncompliantResponse(getAttestationOptionalResponse(form, AttestationFormMetaData.getCommunicationConditionId()));
        checkInReport.setRwtResponse(getAttestationResponse(form, AttestationFormMetaData.getRwtConditionId()));
        checkInReport.setRwtNoncompliantResponse(getAttestationOptionalResponse(form, AttestationFormMetaData.getRwtConditionId()));
        checkInReport.setApiResponse(getAttestationResponse(form, AttestationFormMetaData.getApiConditionId()));
        checkInReport.setApiNoncompliantResponse(getAttestationOptionalResponse(form, AttestationFormMetaData.getApiConditionId()));
        return checkInReport;
    }

    private CheckInReport addComplianceInformation(CheckInReport checkInReport, Developer developer, List<ListingSearchResult> allActiveListingsForDeveloper) {
        checkInReport.setTotalSurveillances(getTotalSurveillances(developer, allActiveListingsForDeveloper, LOGGER));
        checkInReport.setTotalSurveillanceNonconformities(getTotalSurveillanceNonconformities(developer, allActiveListingsForDeveloper, LOGGER));
        checkInReport.setOpenSurveillanceNonconformities(getOpenSurveillanceNonconformities(developer, allActiveListingsForDeveloper, LOGGER));
        checkInReport.setTotalDirectReviewNonconformities(getTotalDirectReviewNonconformities(developer, LOGGER));
        checkInReport.setOpenDirectReviewNonconformities(getOpenDirectReviewNonconformities(developer, LOGGER));
        return checkInReport;
    }

    private CheckInReport addValidation(CheckInReport checkInReport, Form form, AttestationPeriod period, List<ListingSearchResult> allActiveListingsForDeveloper) {
        checkInReport.setAssurancesValidation(checkInReportValidation.getAssurancesValidationMessage(allActiveListingsForDeveloper, form, period));
        checkInReport.setRealWorldTestingValidation(checkInReportValidation.getRealWorldTestingValidationMessage(allActiveListingsForDeveloper, form));
        checkInReport.setApiValidation(checkInReportValidation.getApiValidationMessage(allActiveListingsForDeveloper, form));
        return checkInReport;
    }

    private List<Developer> getDevelopersActiveListingsDuringMostRecentPastAttestationPeriod() {
        AttestationPeriod mostRecentPastPeriod = attestationManager.getMostRecentPastAttestationPeriod();
        return developerAttestationPeriodCalculator.getDevelopersWithActiveListingsDuringAttestationPeriod(mostRecentPastPeriod, LOGGER);
    }

    private String getAttestationResponse(Form form, Long conditionId) {
        if (form == null) {
            return "";
        }
        return form.formatResponse(conditionId);
    }

    private String getAttestationOptionalResponse(Form form, Long conditionId) {
        if (form == null) {
            return "";
        }
        return form.formatOptionalResponsesForCondition(conditionId);
    }

    private Long getTotalSurveillances(Developer developer, List<ListingSearchResult> allActiveListingsForDeveloper, Logger logger) {
        return allActiveListingsForDeveloper.stream()
                .map(listing -> addSurveillanceCount(listing))
                .collect(Collectors.summingLong(Long::longValue));
    }

    private Long getTotalSurveillanceNonconformities(Developer developer, List<ListingSearchResult> allActiveListingsForDeveloper, Logger logger) {
        return allActiveListingsForDeveloper.stream()
                .map(listing -> addOpenAndClosedNonconformityCount(listing))
                .collect(Collectors.summingLong(Long::longValue));
    }

    private Long getOpenSurveillanceNonconformities(Developer developer, List<ListingSearchResult> allActiveListingsForDeveloper, Logger logger) {
        return allActiveListingsForDeveloper.stream()
                .map(listing -> listing.getOpenSurveillanceNonConformityCount())
                .collect(Collectors.summingLong(Long::longValue));
    }

    private Long getTotalDirectReviewNonconformities(Developer developer, Logger logger) {
        return directReviewSearchService.getDeveloperDirectReviews(developer.getId(), logger).stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .count();
    }

    private Long getOpenDirectReviewNonconformities(Developer developer, Logger logger) {
        return directReviewSearchService.getDeveloperDirectReviews(developer.getId(), logger).stream()
                .flatMap(dr -> dr.getNonConformities().stream())
                .filter(nc -> nc.getNonConformityStatus().equalsIgnoreCase(DirectReviewNonConformity.STATUS_OPEN))
                .count();
    }

    private Long addSurveillanceCount(ListingSearchResult listing) {
        return listing.getSurveillanceCount() != null ? listing.getSurveillanceCount() : 0L;
    }

    private Long addOpenAndClosedNonconformityCount(ListingSearchResult listing) {
        Long closed = listing.getClosedSurveillanceNonConformityCount() != null ? listing.getClosedSurveillanceNonConformityCount() : 0L;
        Long open = listing.getOpenSurveillanceNonConformityCount() != null ? listing.getOpenSurveillanceNonConformityCount() : 0L;
        return closed + open;
    }

    private List<ListingSearchResult> getActiveListingDataForDeveloper(Developer developer, Map<Long, List<ListingSearchResult>> listingsByDeveloper) {
        if (listingsByDeveloper.containsKey(developer.getId())) {
            return listingsByDeveloper.get(developer.getId());
        } else {
            return null;
        }
    }

    private Map<Long, List<ListingSearchResult>> getMapOfActiveListingSearchResultsByDeveloper() {
        return get2015ActiveListing().stream()
                .collect(Collectors.groupingBy(listingSearchResult -> listingSearchResult.getDeveloper().getId()));
    }

    private List<ListingSearchResult> get2015ActiveListing() {
        LOGGER.info("Getting all active listings for 2015 Edition");
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                .certificationStatuses(activeStatuses)
                .pageSize(MAX_PAGE_SIZE)
                .pageNumber(0)
                .build();
        return listingSearchService.getAllPagesOfSearchResults(searchRequest, LOGGER);
    }

    private Boolean isDeveloperManagedBySelectedAcbs(Developer developer, List<Long> acbIds) {
        return developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(developer.getId()).stream()
                .filter(acb -> acbIds.contains(acb.getId()))
                .findAny()
                .isPresent();
    }
}
