package gov.healthit.chpl.attestation.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ff4j.FF4j;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.scheduler.job.developer.attestation.AttestationFormMetaData;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.search.domain.SearchRequest;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AttestationResponseValidationService {
    private static final Integer MAX_PAGE_SIZE = 100;

    private ListingApplicabilityService listingApplicabilityService;
    private ListingSearchService listingSearchService;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private FF4j ff4j;

    private Set<String> activeStatuses = Stream.of(
            CertificationStatusType.Active.getName(),
            CertificationStatusType.SuspendedByAcb.getName(),
            CertificationStatusType.SuspendedByOnc.getName())
            .collect(Collectors.toSet());

    public AttestationResponseValidationService(ListingApplicabilityService listingApplicabilityService,
            ListingSearchService listingSearchService, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions, FF4j ff4j) {
        this.listingApplicabilityService = listingApplicabilityService;
        this.listingSearchService = listingSearchService;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
    }

    public String getApiResponseNotApplicableMessage(Long developerId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        boolean isApiApplicable = listingApplicabilityService.isApiApplicable(allActiveListingsForDeveloper);
        if (isApiApplicable) {
            if (isDeveloper()) {
                return msgUtil.getMessage("attestation.developer.apiApplicableNotConsistent");
            } else if (isAcbOrAdmin()) {
                return msgUtil.getMessage("attestation.acb.apiApplicableNotConsistent");
            }
        }
        return null;
    }

    public String getApiResponseCompliantMessage(Long developerId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        boolean isApiApplicable = listingApplicabilityService.isApiApplicable(allActiveListingsForDeveloper);
        if (!isApiApplicable) {
            if (isDeveloper()) {
                return msgUtil.getMessage("attestation.developer.apiNotApplicableNotConsistent");
            } else if (isAcbOrAdmin()) {
                return msgUtil.getMessage("attestation.acb.apiNotApplicableNotConsistent");
            }
        }
        return null;
    }

    public Boolean isApiApplicableAndResponseIsNotApplicable(Long developerId, Form attestationForm) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isApiApplicableAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm);
    }

    public Boolean isApiNotApplicableAndResponseIsCompliant(Long developerId, Form attestationForm) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isApiNotApplicableAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm);
    }

    public Boolean isApiApplicableAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return listingApplicabilityService.isApiApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getApiConditionId(),
                        AttestationFormMetaData.getNotApplicableResponseId());
    }

    public Boolean isApiNotApplicableAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return !listingApplicabilityService.isApiApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getApiConditionId(),
                        AttestationFormMetaData.getCompliantResponseId());
    }

    public String getAssurancesResponseNotApplicableMessage(Long developerId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        boolean isAssurancesApplicable = listingApplicabilityService.isAssurancesApplicable(allActiveListingsForDeveloper);
        if (isAssurancesApplicable) {
            if (isDeveloper()) {
                return msgUtil.getMessage("attestation.developer.assurancesApplicableNotConsistent");
            } else if (isAcbOrAdmin()) {
                return msgUtil.getMessage("attestation.acb.assurancesApplicableNotConsistent");
            }
        }
        return null;
    }

    public String getAssurancesResponseCompliantMessage(Long developerId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        boolean isAssurancesApplicable = listingApplicabilityService.isAssurancesApplicable(allActiveListingsForDeveloper);
        if (!isAssurancesApplicable) {
            if (isDeveloper()) {
                return msgUtil.getMessage("attestation.developer.assurancesNotApplicableNotConsistent");
            } else if (isAcbOrAdmin()) {
                return msgUtil.getMessage("attestation.acb.assurancesNotApplicableNotConsistent");
            }
        }
        return null;
    }

    public Boolean isAssurancesApplicableAndResponseIsNotApplicable(Long developerId, Form attestationForm, Long attestationPeriodId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isAssurancesApplicableAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm, attestationPeriodId);
    }

    public Boolean isAssurancesNotApplicableAndResponseIsCompliant(Long developerId, Form attestationForm, Long attestationPeriodId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isAssurancesNotApplicableAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm, attestationPeriodId);
    }

    public Boolean isAssurancesApplicableAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, Long attestationPeriodId) {
        return listingApplicabilityService.isAssurancesApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId),
                        AttestationFormMetaData.getAssurancesCompliantIsNotApplicableResponseId(attestationPeriodId));
    }

    public Boolean isAssurancesNotApplicableAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, Long attestationPeriodId) {
        if (!ff4j.check(FeatureList.ERD_PHASE_3)) {
            return false;
        }
        return !listingApplicabilityService.isAssurancesApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId),
                        AttestationFormMetaData.getAssurancesCompliantIsApplicableResponseId(attestationPeriodId));
    }

    public String getRwtResponseNotApplicableMessage(Long developerId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        boolean isRealWorldTestingApplicable = listingApplicabilityService.isRealWorldTestingApplicable(allActiveListingsForDeveloper);
        if (isRealWorldTestingApplicable) {
            if (isDeveloper()) {
                return msgUtil.getMessage("attestation.developer.rwtApplicableNotConsistent");
            } else if (isAcbOrAdmin()) {
                return msgUtil.getMessage("attestation.acb.rwtApplicableNotConsistent");
            }
        }
        return null;
    }

    public String getRwtResponseCompliantMessage(Long developerId) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        boolean isRealWorldTestingApplicable = listingApplicabilityService.isRealWorldTestingApplicable(allActiveListingsForDeveloper);
        if (!isRealWorldTestingApplicable) {
            if (isDeveloper()) {
                return msgUtil.getMessage("attestation.developer.rwtNotApplicableNotConsistent");
            } else if (isAcbOrAdmin()) {
                return msgUtil.getMessage("attestation.acb.rwtNotApplicableNotConsistent");
            }
        }
        return null;
    }

    public Boolean isRwtApplicableAndResponseIsNotApplicable(Long developerId, Form attestationForm) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isRwtApplicableAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm);
    }

    public Boolean isRwtNotApplicableAndResponseIsCompliant(Long developerId, Form attestationForm) {
        List<ListingSearchResult> allActiveListingsForDeveloper = getActiveListingDataForDeveloper(developerId);
        return isRwtNotApplicableAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm);
    }

    public Boolean isRwtApplicableAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return listingApplicabilityService.isRealWorldTestingApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getRwtConditionId(),
                        AttestationFormMetaData.getNotApplicableResponseId());
    }

    public Boolean isRwtNotApplicableAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return !listingApplicabilityService.isRealWorldTestingApplicable(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getRwtConditionId(),
                        AttestationFormMetaData.getCompliantResponseId());
    }

    public Boolean doesFormResponseEqualResponse(Form attestationForm, Long conditionIdToCheck, Long expectedResult) {
        return attestationForm.extractFlatFormItems().stream()
                .filter(fi -> fi.getQuestion().getId().equals(conditionIdToCheck)
                        && fi.getSubmittedResponses().stream()
                                .filter(sr -> sr.getId().equals(expectedResult))
                                .findAny().isPresent())
                .findAny()
                .isPresent();
    }

    private List<ListingSearchResult> getActiveListingDataForDeveloper(Long developerId) {
        //TODO: I think we want this to be "active listings for the developer during the period start/end dates"
        //which I thought we would be able to get because we keep the listing status history in the search
        //request, but what about listings that might have changed ownership? Or a developer that doesn't
        //exist now (got Joined) but did exist during the period?
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .certificationEditions(Stream.of(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear()).collect(Collectors.toSet()))
                    .developerId(developerId)
                    .certificationStatuses(activeStatuses)
                    .pageSize(MAX_PAGE_SIZE)
                    .pageNumber(0)
                    .build();
            List<ListingSearchResult> searchResults = listingSearchService.getAllPagesOfSearchResults(searchRequest);
            return searchResults;
        } catch (ValidationException ex) {
            LOGGER.error("Could not retrieve listings from search request.", ex);
            return null;
        }
    }

    private boolean isDeveloper() {
        return resourcePermissions.isUserRoleDeveloperAdmin();
    }

    private boolean isAcbOrAdmin() {
        return resourcePermissions.isUserRoleAcbAdmin()
                || resourcePermissions.isUserRoleOnc()
                || resourcePermissions.isUserRoleAdmin();
    }
}
