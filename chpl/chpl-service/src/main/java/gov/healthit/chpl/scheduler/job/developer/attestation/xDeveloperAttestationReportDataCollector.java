package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.attestation.manager.AttestationCertificationBodyService;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.attestation.manager.AttestationSubmissionService;
import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.compliance.directreview.DirectReviewSearchService;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.search.domain.SearchSetOperator;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingCriteriaService;

@Component
public class xDeveloperAttestationReportDataCollector {
    private static final Integer MAX_PAGE_SIZE = 100;

    private static final Long INFORMATION_BLOCKING_ATTESTATION_ID = 1L;
    private static final Long ASSURANCES_ATTESTATION_ID = 7L;
    private static final Long COMMUNICATIONS_ATTESTATION_ID = 3L;
    private static final Long API_ATTESTATION_ID = 4L;
    private static final Long RWT_ATTESTATION_ID = 5L;

    private static final String RWT_VALIDATION_TRUE = "Has listing(s) with RWT criteria";
    private static final String RWT_VALIDATION_FALSE = "No listings with RWT criteria";
    private static final String ASSURANCES_VALIDATION_TRUE = "Has listing(s) with Assurances criteria (b)(6) or (b)(10)";
    private static final String ASSURANCES_VALIDATION_FALSE = "No listings with Assurances criteria (b)(6) or (b)(10)";
    private static final String API_VALIDATION_TRUE = "Has listing(s) with API criteria (g)(7)-(g)(10)";
    private static final String API_VALIDATION_FALSE = "No listings with API criteria (g)(7)-(g)(10)";

    private DeveloperAttestationPeriodCalculator devAttestationPeriodCalculator;
    private UserDeveloperMapDAO userDeveloperMapDao;
    private ListingSearchService listingSearchService;
    private AttestationSubmissionService attestationSubmissionService;
    private DirectReviewSearchService directReviewService;
    private CertificationBodyDAO certificationBodyDAO;
    private DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO;
    private AttestationPeriodService attestationPeriodService;
    private AttestationCertificationBodyService attestationCertificationBodyService;

    private List<CertificationCriterion> assurancesCriteria;
    private List<CertificationCriterion> apiCriteria;
    private List<CertificationCriterion> rwtCriteria;
    private Map<Long, List<ListingSearchResult>> developerListings = new HashMap<Long, List<ListingSearchResult>>();

    public xDeveloperAttestationReportDataCollector(DeveloperAttestationPeriodCalculator devAttestationPeriodCalculator,
            UserDeveloperMapDAO userDeveloperMapDao, ListingSearchService listingSearchService,
            AttestationSubmissionService attestationSubmissionService,
            DirectReviewSearchService directReviewService,
            CertificationBodyDAO certificationBodyDAO,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDAO,
            AttestationPeriodService attestationPeriodService,
            AttestationCertificationBodyService attestationCertificationBodyService,
            RealWorldTestingCriteriaService realWorldTestingCriteriaService,
            CertificationCriterionService certificationCriterionService,
            @Value("${assurancesCriteriaKeys}") String[] assurancesCriteriaKeys,
            @Value("${apiCriteriaKeys}") String[] apiCriteriaKeys) {

        this.devAttestationPeriodCalculator = devAttestationPeriodCalculator;
        this.userDeveloperMapDao = userDeveloperMapDao;
        this.listingSearchService = listingSearchService;
        this.attestationSubmissionService = attestationSubmissionService;
        this.directReviewService = directReviewService;
        this.certificationBodyDAO = certificationBodyDAO;
        this.developerCertificationBodyMapDAO = developerCertificationBodyMapDAO;
        this.attestationPeriodService = attestationPeriodService;
        this.attestationCertificationBodyService = attestationCertificationBodyService;

        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        rwtCriteria = realWorldTestingCriteriaService.getEligibleCriteria(currentYear);

        assurancesCriteria = Arrays.asList(assurancesCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());

        apiCriteria = Arrays.asList(apiCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
    }

    private Set<String> activeStatuses = Stream.of(CertificationStatusType.Active.getName(),
            CertificationStatusType.SuspendedByAcb.getName(),
            CertificationStatusType.SuspendedByOnc.getName())
            .collect(Collectors.toSet());

    public List<xDeveloperAttestationReport> collect(List<Long> selectedAcbIds, Logger logger) {
        AttestationPeriod mostRecentPastPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
        logger.info("Most recent past attestation period: {} - {} ", mostRecentPastPeriod.getPeriodStart().toString(), mostRecentPastPeriod.getPeriodEnd().toString());
        logger.info("Selected AcbsId: {}", selectedAcbIds.stream()
                .map(id -> id.toString())
                .collect(Collectors.joining(", ")));

        List<Developer> developers = devAttestationPeriodCalculator.getDevelopersWithActiveListingsDuringMostRecentPastAttestationPeriod(logger);

        List<xDeveloperAttestationReport> reportRows = developers.stream()
                .filter(dev -> isDeveloperManagedBySelectedAcbs(dev, selectedAcbIds))
                .map(dev -> toDeveloperAttestationReport(dev, mostRecentPastPeriod, logger))
                .sorted(Comparator.comparing(xDeveloperAttestationReport::getDeveloperName))
                .toList();

        logger.info("Total Report Rows found: {}", reportRows.size());
        developerListings.clear();
        return reportRows;
    }

    private xDeveloperAttestationReport toDeveloperAttestationReport(Developer dev,
            AttestationPeriod mostRecentPastPeriod, Logger logger) {
        AttestationSubmission attestation = getDeveloperAttestation(dev.getId(), mostRecentPastPeriod.getId());

        return xDeveloperAttestationReport.builder()
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
            .informationBlockingResponse(getAttestationResponse(attestation, INFORMATION_BLOCKING_ATTESTATION_ID))
            .informationBlockingNoncompliantResponse(getAttestationOptionalResponse(attestation, INFORMATION_BLOCKING_ATTESTATION_ID))
            .assurancesResponse(getAttestationResponse(attestation, ASSURANCES_ATTESTATION_ID))
            .assurancesNoncompliantResponse(getAttestationOptionalResponse(attestation, ASSURANCES_ATTESTATION_ID))
            .communicationsResponse(getAttestationResponse(attestation, COMMUNICATIONS_ATTESTATION_ID))
            .communicationsNoncompliantResponse(getAttestationOptionalResponse(attestation, COMMUNICATIONS_ATTESTATION_ID))
            .apiResponse(getAttestationResponse(attestation, API_ATTESTATION_ID))
            .apiNoncompliantResponse(getAttestationOptionalResponse(attestation, API_ATTESTATION_ID))
            .rwtResponse(getAttestationResponse(attestation, RWT_ATTESTATION_ID))
            .rwtNoncompliantResponse(getAttestationOptionalResponse(attestation, RWT_ATTESTATION_ID))
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
    }

    private AttestationSubmission getDeveloperAttestation(Long developerId, Long attestationPeriodId) {
        List<AttestationSubmission> attestations =
                attestationSubmissionService.getAttestationSubmissions(developerId, attestationPeriodId);

        if (attestations != null && attestations.size() > 0) {
            return attestations.get(0);
        } else {
            return null;
        }
    }

    private List<CertificationBody> getActiveAcbs() {
        return certificationBodyDAO.findAllActive().stream()
                .map(dto -> new CertificationBody(dto))
                .toList();
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

    private String getAttestationResponse(AttestationSubmission attestationSubmission, Long conditionId) {
        if (attestationSubmission == null) {
            return "";
        }
        return attestationSubmission.getForm().formatResponse(conditionId);
    }

    private String getAttestationOptionalResponse(AttestationSubmission attestationSubmission, Long conditionId) {
        if (attestationSubmission == null) {
            return "";
        }
        return attestationSubmission.getForm().formatOptionalResponsesForCondition(conditionId);
    }

    private String getSubmitterName(AttestationSubmission attestation) {
        return attestation != null ? attestation.getSignature() : "";
    }

    private String getSubmitterEmail(AttestationSubmission attestation) {
        return attestation != null ? attestation.getSignatureEmail() : "";
    }

    private Long getTotalSurveillances(Developer developer, Logger logger) {
        return getActiveListingDataForDeveloper(developer, logger).stream()
                .map(listing -> addSurveillanceCount(listing))
                .collect(Collectors.summingLong(Long::longValue));
    }

    private Long getTotalSurveillanceNonconformities(Developer developer, Logger logger) {
        return getActiveListingDataForDeveloper(developer, logger).stream()
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
        return getActiveListingDataForDeveloper(developer, logger).stream()
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
        List<ListingSearchResult> apiEligibleListings = getActiveListingDataWithAnyCriteriaForDeveloper(developer, rwtCriteria, logger);
        if (!CollectionUtils.isEmpty(apiEligibleListings)) {
            return RWT_VALIDATION_TRUE;
        } else {
            return RWT_VALIDATION_FALSE;
        }
    }

    private String getAssurancesValidation(Developer developer, Logger logger) {
        List<ListingSearchResult> assurancesEligibleListings = getActiveListingDataWithAnyCriteriaForDeveloper(developer, assurancesCriteria, logger);
        if (!CollectionUtils.isEmpty(assurancesEligibleListings)) {
            return ASSURANCES_VALIDATION_TRUE;
        } else {
            return ASSURANCES_VALIDATION_FALSE;
        }
    }

    private String getApiValidation(Developer developer, Logger logger) {
        List<ListingSearchResult> apiEligibleListings = getActiveListingDataWithAnyCriteriaForDeveloper(developer, apiCriteria, logger);
        if (!CollectionUtils.isEmpty(apiEligibleListings)) {
            return API_VALIDATION_TRUE;
        } else {
            return API_VALIDATION_FALSE;
        }
    }

    private List<ListingSearchResult> getActiveListingDataForDeveloper(Developer developer, Logger logger) {
        if (developerListings.get(developer.getId()) != null) {
            return developerListings.get(developer.getId());
        } else {
            SearchRequest searchRequest = SearchRequest.builder()
                    .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                    .developer(developer.getName())
                    .certificationStatuses(activeStatuses)
                    .pageSize(MAX_PAGE_SIZE)
                    .pageNumber(0)
                    .build();
            List<ListingSearchResult> searchResults = listingSearchService.getAllPagesOfSearchResults(searchRequest, logger);
            developerListings.put(developer.getId(), searchResults);
            return searchResults;
        }
    }

    private List<ListingSearchResult> getActiveListingDataWithAnyCriteriaForDeveloper(Developer developer, List<CertificationCriterion> criteria,
            Logger logger) {
        SearchRequest searchRequest = SearchRequest.builder()
                .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                .developer(developer.getName())
                .certificationStatuses(activeStatuses)
                .certificationCriteriaIds(criteria.stream().map(criterion -> criterion.getId()).collect(Collectors.toSet()))
                .certificationCriteriaOperator(SearchSetOperator.OR)
                .pageSize(MAX_PAGE_SIZE)
                .pageNumber(0)
                .build();
        return listingSearchService.getAllPagesOfSearchResults(searchRequest, logger);
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
