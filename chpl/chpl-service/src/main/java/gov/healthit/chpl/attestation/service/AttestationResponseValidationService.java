package gov.healthit.chpl.attestation.service;

import java.util.List;

import org.ff4j.FF4j;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.scheduler.job.developer.attestation.AttestationFormMetaData;
import gov.healthit.chpl.search.ListingSearchService;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AttestationResponseValidationService {

    private ListingApplicabilityService listingApplicabilityService;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private FF4j ff4j;

    public AttestationResponseValidationService(ListingApplicabilityService listingApplicabilityService,
            ListingSearchService listingSearchService, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions, FF4j ff4j) {
        this.listingApplicabilityService = listingApplicabilityService;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
    }

    public String getApiResponseNotApplicableMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
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

    public String getApiResponseCompliantMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
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

    public String getAssurancesResponseNotApplicableMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
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

    public String getAssurancesResponseCompliantMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
        if (!ff4j.check(FeatureList.ERD_PHASE_3)) {
            return null;
        }

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

    public String getRwtResponseNotApplicableMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
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

    public String getRwtResponseCompliantMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
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

    private boolean isDeveloper() {
        return resourcePermissions.isUserRoleDeveloperAdmin();
    }

    private boolean isAcbOrAdmin() {
        return resourcePermissions.isUserRoleAcbAdmin()
                || resourcePermissions.isUserRoleOnc()
                || resourcePermissions.isUserRoleAdmin();
    }
}
