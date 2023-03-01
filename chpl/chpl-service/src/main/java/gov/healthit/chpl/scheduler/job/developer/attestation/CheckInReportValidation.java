package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.form.Form;
import gov.healthit.chpl.search.domain.ListingSearchResult;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingCriteriaService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CheckInReportValidation {
    private static final String RWT_VALIDATION_TRUE = "Has listing(s) with RWT criteria";
    private static final String RWT_VALIDATION_FALSE = "No listings with RWT criteria";
    private static final String ASSURANCES_VALIDATION_TRUE = "Has listing(s) with Assurances criteria (b)(6) or (b)(10)";
    private static final String ASSURANCES_VALIDATION_FALSE = "No listings with Assurances criteria (b)(6) or (b)(10)";
    private static final String API_VALIDATION_TRUE = "Has listing(s) with API criteria (g)(7)-(g)(10)";
    private static final String API_VALIDATION_FALSE = "No listings with API criteria (g)(7)-(g)(10)";

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
        assurancesCriteria = Arrays.asList(assurancesCriteriaKeys).stream().map(key -> certificationCriterionService.get(key)).collect(Collectors.toList());
        apiCriteria = Arrays.asList(apiCriteriaKeys).stream().map(key -> certificationCriterionService.get(key)).collect(Collectors.toList());
    }

    public String getRealWorldTestingValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
        if (isRealWorldTestingValid(allActiveListingsForDeveloper)) {
            return errorMessageUtil.getMessage("attestatation.check.in.report.rwtValidationTrue");
        } else {
            return errorMessageUtil.getMessage("attestatation.check.in.report.rwtValidationFalse");
        }
    }

    public String getAssurancesValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
        if (isAssurancesValid(allActiveListingsForDeveloper)) {
            return errorMessageUtil.getMessage("attestatation.check.in.report.assurancesValidationTrue");
        } else {
            return errorMessageUtil.getMessage("attestatation.check.in.report.assurancesValidationFalse");
        }
    }

    public String getApiValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
        if (isApiValid(allActiveListingsForDeveloper)) {
            return errorMessageUtil.getMessage("attestatation.check.in.report.apiValidationTrue");
        } else {
            return errorMessageUtil.getMessage("attestatation.check.in.report.apiValidationFalse");
        }
    }

    public String getApiWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (isApiValidAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm)
                || isNotApiValidAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm)) {
            return errorMessageUtil.getMessage("attestatation.check.in.report.apiResponseNotConsistent");
        } else {
            return null;
        }
    }

    private Boolean isApiValidAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return isApiValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getApiConditionId(),
                        AttestatationFormMetaData.getNotAppicableResponseId());
    }

    private Boolean isNotApiValidAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return !isApiValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getApiConditionId(),
                        AttestatationFormMetaData.getNotAppicableResponseId());
    }

    public String getAssurancesWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (isAssurancesValidAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm)
                || isNotAssurancesValidAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm)) {
            return errorMessageUtil.getMessage("attestatation.check.in.report.assurancesResponseNotConsistent");
        } else {
            return null;
        }
    }

    private Boolean isAssurancesValidAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return isAssurancesValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getAssurancesConditionId(),
                        AttestatationFormMetaData.getNotAppicableResponseId());
    }

    private Boolean isNotAssurancesValidAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return !isAssurancesValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getAssurancesConditionId(),
                        AttestatationFormMetaData.getNotAppicableResponseId());
    }

    public String getRealWordTestingWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (isRwtValidAndResponseIsNotApplicable(allActiveListingsForDeveloper, attestationForm)
                || isNotRwtValidAndResponseIsCompliant(allActiveListingsForDeveloper, attestationForm)) {
            return errorMessageUtil.getMessage("attestatation.check.in.report.rwtResponseNotConsistent");
        } else {
            return null;
        }
    }

    private Boolean isRwtValidAndResponseIsNotApplicable(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return isRealWorldTestingValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getRwtConditionId(),
                        AttestatationFormMetaData.getNotAppicableResponseId());
    }

    private Boolean isNotRwtValidAndResponseIsCompliant(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        return !isRealWorldTestingValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getRwtConditionId(),
                        AttestatationFormMetaData.getNotAppicableResponseId());
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
        return !CollectionUtils.isEmpty(getActiveListingDataWithAnyCriteriaForDeveloper(allActiveListingsForDeveloper, rwtCriteria));
    }

    private Boolean isAssurancesValid(List<ListingSearchResult> allActiveListingsForDeveloper) {
        return !CollectionUtils.isEmpty(getActiveListingDataWithAnyCriteriaForDeveloper(allActiveListingsForDeveloper, assurancesCriteria));
    }

    private Boolean isApiValid(List<ListingSearchResult> allActiveListingsForDeveloper) {
        return !CollectionUtils.isEmpty(getActiveListingDataWithAnyCriteriaForDeveloper(allActiveListingsForDeveloper, apiCriteria));
    }

    private List<ListingSearchResult> getActiveListingDataWithAnyCriteriaForDeveloper(List<ListingSearchResult> allActiveListingsForDeveloper, List<CertificationCriterion> criteria) {
        return allActiveListingsForDeveloper.stream()
                .filter(result -> result.getCriteriaMet().stream()
                        .filter(met -> isCriteriaInList(met.getId(), criteria))
                        .findAny()
                        .isPresent())
                .toList();
    }

    private boolean isCriteriaInList(Long criteriaId, List<CertificationCriterion> criteria) {
        return criteria.stream()
                .filter(crit -> crit.getId().equals(criteriaId))
                .findAny()
                .isPresent();
    }
}
