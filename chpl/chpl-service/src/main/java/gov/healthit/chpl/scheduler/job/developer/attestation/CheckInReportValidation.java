package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingCriteriaService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;

@Component
public class CheckInReportValidation {
    private static final String YES = "Yes";
    private static final String NO = "No";

    private ErrorMessageUtil errorMessageUtil;

    private List<CertificationCriterion> assurancesCriteria;
    private List<CertificationCriterion> apiCriteria;
    private List<CertificationCriterion> rwtCriteria;

    @Autowired
    public CheckInReportValidation(RealWorldTestingCriteriaService realWorldTestingCriteriaService,
            CertificationCriterionService certificationCriterionService,
            ErrorMessageUtil errorMessageUtil,
            @Value("${assurancesCriteriaKeys}") String[] assurancesCriteriaKeys,
            @Value("${apiCriteriaKeys}") String[] apiCriteriaKeys) {

        this.errorMessageUtil = errorMessageUtil;

        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        rwtCriteria = realWorldTestingCriteriaService.getEligibleCriteria(currentYear);
        assurancesCriteria = Arrays.asList(assurancesCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
        apiCriteria = Arrays.asList(apiCriteriaKeys).stream()
                .map(key -> certificationCriterionService.get(key))
                .collect(Collectors.toList());
    }

    public String getRealWorldTestingValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        String warning = NullSafeEvaluator.eval(() -> getRealWordTestingWarningMessage(allActiveListingsForDeveloper, attestationForm), "");
        if (isRealWorldTestingValid(allActiveListingsForDeveloper)) {
            return YES + (warning != "" ? " - " : "") + warning;
        } else {
            return NO + (warning != "" ? " - " : "") + warning;
        }
    }

    public String getAssurancesValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, AttestationPeriod period) {
        String warning = NullSafeEvaluator.eval(() -> getAssurancesWarningMessage(allActiveListingsForDeveloper, attestationForm, period.getId()), "");
        if (isAssurancesValid(allActiveListingsForDeveloper)) {
            return YES + (warning != "" ? " - " : "") + warning;
        } else {
            return NO + (warning != "" ? " - " : "") + warning;
        }
    }

    public String getApiValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        String warning = NullSafeEvaluator.eval(() -> getApiWarningMessage(allActiveListingsForDeveloper, attestationForm), "");
        if (isApiValid(allActiveListingsForDeveloper)) {
            return YES + (warning != "" ? " - " : "") + warning;
        } else {
            return NO + (warning != "" ? " - " : "") + warning;
        }
    }

    public String getApiWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (isApiValidAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm)
                || isNotApiValidAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm)) {
            return errorMessageUtil.getMessage("attestation.checkInReport.apiResponseNotConsistent");
        } else {
            return null;
        }
    }

    private Boolean isApiValidAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return isApiValid(allActiveListingsForDeveloper)
                && (doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getApiConditionId(),
                        AttestationFormMetaData.getNotAppicableResponseId())
                        || doesFormResponseEqualResponse(attestationForm,
                                AttestationFormMetaData.getApiConditionId(),
                                AttestationFormMetaData.getNonCompliantResponseId()));
    }

    private Boolean isNotApiValidAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return !isApiValid(allActiveListingsForDeveloper)
                && (doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getApiConditionId(),
                        AttestationFormMetaData.getNotAppicableResponseId())
                        || doesFormResponseEqualResponse(attestationForm,
                                AttestationFormMetaData.getApiConditionId(),
                                AttestationFormMetaData.getCompliantResponseId()));
    }

    public String getAssurancesWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, Long attestationPeriodId) {
        if (isAssurancesValidAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm, attestationPeriodId)
                || isNotAssurancesValidAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm, attestationPeriodId)) {
            return errorMessageUtil.getMessage("attestation.checkInReport.assurancesResponseNotConsistent");
        } else {
            return null;
        }
    }

    private Boolean isAssurancesValidAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, Long attestationPeriodId) {
        return isAssurancesValid(allActiveListingsForDeveloper)
                && (doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId),
                        AttestationFormMetaData.getNotAppicableResponseId())
                        || doesFormResponseEqualResponse(attestationForm,
                                AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId),
                                AttestationFormMetaData.getNonCompliantResponseId()));
    }

    private Boolean isNotAssurancesValidAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm, Long attestationPeriodId) {
        return !isAssurancesValid(allActiveListingsForDeveloper)
                && (doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId),
                        AttestationFormMetaData.getNotAppicableResponseId())
                        || doesFormResponseEqualResponse(attestationForm,
                                AttestationFormMetaData.getAssurancesConditionId(attestationPeriodId),
                                AttestationFormMetaData.getCompliantResponseId()));
    }

    public String getRealWordTestingWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (isRwtValidAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm)
                || isNotRwtValidAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm)) {
            return errorMessageUtil.getMessage("attestation.checkInReport.rwtResponseNotConsistent");
        } else {
            return null;
        }
    }

    private Boolean isRwtValidAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return isRealWorldTestingValid(allActiveListingsForDeveloper)
                && (doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getRwtConditionId(),
                        AttestationFormMetaData.getNotAppicableResponseId())
                        || doesFormResponseEqualResponse(attestationForm,
                                AttestationFormMetaData.getRwtConditionId(),
                                AttestationFormMetaData.getNonCompliantResponseId()));
    }

    private Boolean isNotRwtValidAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return !isRealWorldTestingValid(allActiveListingsForDeveloper)
                && (doesFormResponseEqualResponse(attestationForm,
                        AttestationFormMetaData.getRwtConditionId(),
                        AttestationFormMetaData.getNotAppicableResponseId())
                        || doesFormResponseEqualResponse(attestationForm,
                                AttestationFormMetaData.getRwtConditionId(),
                                AttestationFormMetaData.getCompliantResponseId()));
    }

    private Boolean doesFormResponseEqualResponse(Form attestationForm, Long conditionIdToCheck, Long expectedResult) {
        return attestationForm.extractFlatFormItems().stream()
                .filter(fi -> fi.getQuestion().getId().equals(conditionIdToCheck)
                        && fi.getSubmittedResponses().stream()
                                .filter(sr -> sr.getId().equals(expectedResult))
                                .findAny().isPresent())
                .findAny()
                .isPresent();
    }

    private Boolean isRealWorldTestingValid(List<ListingSearchResult> allActiveListingsForDeveloper) {
        return getDoesListingAttestToAnySpecifiedCriteria(allActiveListingsForDeveloper, rwtCriteria);
    }

    private Boolean isAssurancesValid(List<ListingSearchResult> allActiveListingsForDeveloper) {
        return getDoesListingAttestToAnySpecifiedCriteria(allActiveListingsForDeveloper, assurancesCriteria);
    }

    private Boolean isApiValid(List<ListingSearchResult> allActiveListingsForDeveloper) {
        return getDoesListingAttestToAnySpecifiedCriteria(allActiveListingsForDeveloper, apiCriteria);
    }

    private Boolean getDoesListingAttestToAnySpecifiedCriteria(List<ListingSearchResult> allActiveListingsForDeveloper, List<CertificationCriterion> criteria) {
        return allActiveListingsForDeveloper.stream()
                .filter(result -> result.getCriteriaMet().stream()
                        .filter(met -> isCriteriaInList(met.getId(), criteria))
                        .findAny()
                        .isPresent())
                .findAny()
                .isPresent();
    }

    private boolean isCriteriaInList(Long criteriaId, List<CertificationCriterion> criteria) {
        return criteria.stream()
                .filter(crit -> crit.getId().equals(criteriaId))
                .findAny()
                .isPresent();
    }
}
