package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.ArrayList;
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
import gov.healthit.chpl.exception.ValidationException;
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

    private Set<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(), CertificationStatusType.SuspendedByAcb.getName(), CertificationStatusType.SuspendedByOnc.getName()).collect(Collectors.toSet());

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

    public List<CheckInReport> collect() throws EntityRetrievalException {
        return getDevelopersActiveListingsDuringMostRecentPastAttestationPeriod().stream()
                // .filter(developer -> developer.getId().equals(1993L))
                .map(developer -> getCheckInReport(developer)).sorted((o1, o2) -> o1.getDeveloperName().compareTo(o2.getDeveloperName())).toList();
    }

    private CheckInReport getCheckInReport(Developer developer) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developer, LOGGER);
        CheckInAttestation checkInAttestation = checkInReportSourceService.getCheckinReport(developer, attestationManager.getMostRecentPastAttestationPeriod());

        LOGGER.info("Getting data for Developer: {} ({})", developer.getName(), developer.getId());

        CheckInReport checkInReport = null;
        if (checkInAttestation == null) {
            LOGGER.info("..........No attestations found", developer.getName(), developer.getId());
            checkInReport = convert(developer);
        } else if (checkInAttestation.getSource().equals(CheckInReportSource.CHANGE_REQUEST)) {
            LOGGER.info("..........Change Request attestations found", developer.getName(), developer.getId());
            checkInReport = convert(checkInAttestation.getChangeRequest(), allActiveListingsForDeveloper);
        } else if (checkInAttestation.getSource().equals(CheckInReportSource.DEVELOPER_ATTESTATION)) {
            LOGGER.info("..........Published attestations found", developer.getName(), developer.getId());
            checkInReport = convert(developer, checkInAttestation.getAttestationSubmission(), allActiveListingsForDeveloper);
        }
        return checkInReport;
    }

    private CheckInReport convert(Developer developer) {
        List<CertificationBody> acbs = developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(developer.getId());
        return CheckInReport.builder().developerName(developer.getName()).developerCode(developer.getDeveloperCode()).developerId(developer.getId()).published(false)
                .relevantAcbs(acbs.stream().map(acb -> acb.getName()).collect(Collectors.joining("; "))).build();
    }

    private CheckInReport convert(ChangeRequest cr, List<ListingSearchResult> allActiveListingsForDeveloper) {
        ChangeRequestAttestationSubmission crAttestation = (ChangeRequestAttestationSubmission) cr.getDetails();
        CheckInReport checkInReport = convert(cr.getDeveloper(), crAttestation.getForm(), allActiveListingsForDeveloper, crAttestation.getAttestationPeriod().getId());
        checkInReport.setSubmittedDate(cr.getSubmittedDateTime());
        checkInReport.setPublished(false);
        checkInReport.setCurrentStatusName(cr.getCurrentStatus().getChangeRequestStatusType().getName());
        checkInReport.setLastStatusChangeDate(cr.getCurrentStatus().getStatusChangeDateTime());
        checkInReport.setRelevantAcbs(cr.getCertificationBodies().stream().map(acb -> acb.getName()).collect(Collectors.joining("; ")));
        checkInReport.setSignature(crAttestation.getSignature());
        checkInReport.setSignatureEmail(crAttestation.getSignatureEmail());
        return checkInReport;
    }

    private CheckInReport convert(Developer developer, AttestationSubmission attestation, List<ListingSearchResult> allActiveListingsForDeveloper) {
        List<CertificationBody> acbs = developerCertificationBodyMapDAO.getCertificationBodiesForDeveloper(developer.getId());
        CheckInReport checkInReport = convert(developer, attestation.getForm(), allActiveListingsForDeveloper, attestation.getAttestationPeriod().getId());
        checkInReport.setPublished(true);
        checkInReport.setSignature(attestation.getSignature());
        checkInReport.setSignatureEmail(attestation.getSignatureEmail());
        checkInReport.setRelevantAcbs(acbs.stream().map(acb -> acb.getName()).collect(Collectors.joining("; ")));
        return checkInReport;
    }

    private CheckInReport convert(Developer developer, Form form, List<ListingSearchResult> allActiveListingsForDeveloper, Long attestationPeriodId) {
        return CheckInReport.builder()
                .developerName(developer.getName())
                .developerCode(developer.getDeveloperCode())
                .developerId(developer.getId())
                .informationBlockingResponse(getAttestationResponse(form, AttestatationFormMetaData.getInformationBlockingConditionId()))
                .informationBlockingNoncompliantResponse(getAttestationOptionalResponse(form, AttestatationFormMetaData.getInformationBlockingConditionId()))
                .assurancesResponse(getAttestationResponse(form, AttestatationFormMetaData.getAssurancesConditionId(attestationPeriodId)))
                .assurancesNoncompliantResponse(getAttestationOptionalResponse(form, AttestatationFormMetaData.getAssurancesConditionId(attestationPeriodId)))
                .communicationsResponse(getAttestationResponse(form, AttestatationFormMetaData.getCommunicationConditionId()))
                .communicationsNoncompliantResponse(getAttestationOptionalResponse(form, AttestatationFormMetaData.getCommunicationConditionId()))
                .rwtResponse(getAttestationResponse(form, AttestatationFormMetaData.getRwtConditionId()))
                .rwtNoncompliantResponse(getAttestationOptionalResponse(form, AttestatationFormMetaData.getRwtConditionId()))
                .apiResponse(getAttestationResponse(form, AttestatationFormMetaData.getApiConditionId()))
                .apiNoncompliantResponse(getAttestationOptionalResponse(form, AttestatationFormMetaData.getApiConditionId()))
                .totalSurveillances(getTotalSurveillances(developer, allActiveListingsForDeveloper, LOGGER))
                .totalSurveillanceNonconformities(getTotalSurveillanceNonconformities(developer, allActiveListingsForDeveloper, LOGGER))
                .openSurveillanceNonconformities(getOpenSurveillanceNonconformities(developer, allActiveListingsForDeveloper, LOGGER))
                .totalDirectReviewNonconformities(getTotalDirectReviewNonconformities(developer, LOGGER))
                .openDirectReviewNonconformities(getOpenDirectReviewNonconformities(developer, LOGGER))
                .assurancesValidation(checkInReportValidation.getAssurancesValidationMessage(allActiveListingsForDeveloper))
                .realWorldTestingValidation(checkInReportValidation.getRealWorldTestingValidationMessage(allActiveListingsForDeveloper))
                .apiValidation(checkInReportValidation.getApiValidationMessage(allActiveListingsForDeveloper))
                .warnings(getWarnings(allActiveListingsForDeveloper, form, attestationPeriodId))
                .build();
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

    private List<ListingSearchResult> getActiveListingDataForDeveloper(Developer developer, Logger logger) {
        if (developerListings.get(developer.getId()) != null) {
            return developerListings.get(developer.getId());
        } else {
            try {
                SearchRequest searchRequest = SearchRequest.builder()
                        .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                        .developerId(developer.getId())
                        .certificationStatuses(activeStatuses).pageSize(MAX_PAGE_SIZE).pageNumber(0).build();
                List<ListingSearchResult> searchResults = listingSearchService.getAllPagesOfSearchResults(searchRequest);
                developerListings.put(developer.getId(), searchResults);
                return searchResults;
            } catch (ValidationException ex) {
                logger.error("Could not retrieve listings from search request.", ex);
                return null;
            }
        }
    }

    private String getWarnings(List<ListingSearchResult> allActiveListingsForDeveloper, Form form, Long attestationPeriodId) {
        List<String> warnings = new ArrayList<String>();
        warnings.add(checkInReportValidation.getApiWarningMessage(allActiveListingsForDeveloper, form));
        warnings.add(checkInReportValidation.getAssurancesWarningMessage(allActiveListingsForDeveloper, form, attestationPeriodId));
        warnings.add(checkInReportValidation.getRealWordTestingWarningMessage(allActiveListingsForDeveloper, form));

        return warnings.stream()
                .filter(w -> w != null)
                .collect(Collectors.joining("; "));
    }
}
