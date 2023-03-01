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

@Component
public class CheckInReportValidation {
    private static final String RWT_VALIDATION_TRUE = "Has listing(s) with RWT criteria";
    private static final String RWT_VALIDATION_FALSE = "No listings with RWT criteria";
    private static final String ASSURANCES_VALIDATION_TRUE = "Has listing(s) with Assurances criteria (b)(6) or (b)(10)";
    private static final String ASSURANCES_VALIDATION_FALSE = "No listings with Assurances criteria (b)(6) or (b)(10)";
    private static final String API_VALIDATION_TRUE = "Has listing(s) with API criteria (g)(7)-(g)(10)";
    private static final String API_VALIDATION_FALSE = "No listings with API criteria (g)(7)-(g)(10)";

    // private AttestatationFormMetaData attestatationFormMetaData;

    private List<CertificationCriterion> assurancesCriteria;
    private List<CertificationCriterion> apiCriteria;
    private List<CertificationCriterion> rwtCriteria;

    @Autowired
    public CheckInReportValidation(RealWorldTestingCriteriaService realWorldTestingCriteriaService,
            CertificationCriterionService certificationCriterionService,
            // AttestatationFormMetaData attestationFormMetaData,
            @Value("${assurancesCriteriaKeys}") String[] assurancesCriteriaKeys,
            @Value("${apiCriteriaKeys}") String[] apiCriteriaKeys) {

        // this.attestatationFormMetaData = attestationFormMetaData;

        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        rwtCriteria = realWorldTestingCriteriaService.getEligibleCriteria(currentYear);
        assurancesCriteria = Arrays.asList(assurancesCriteriaKeys).stream().map(key -> certificationCriterionService.get(key)).collect(Collectors.toList());
        apiCriteria = Arrays.asList(apiCriteriaKeys).stream().map(key -> certificationCriterionService.get(key)).collect(Collectors.toList());
    }

    public String getRealWorldTestingValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
        if (isRealWorldTestingValid(allActiveListingsForDeveloper)) {
            return RWT_VALIDATION_TRUE;
        } else {
            return RWT_VALIDATION_FALSE;
        }
    }

    public String getAssurancesValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
        if (isAssurancesValid(allActiveListingsForDeveloper)) {
            return ASSURANCES_VALIDATION_TRUE;
        } else {
            return ASSURANCES_VALIDATION_FALSE;
        }
    }

    public String getApiValidationMessage(List<ListingSearchResult> allActiveListingsForDeveloper) {
        if (isApiValid(allActiveListingsForDeveloper)) {
            return API_VALIDATION_TRUE;
        } else {
            return API_VALIDATION_FALSE;
        }
    }

    public String getApiWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (isApiValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getApiConditionId(),
                        AttestatationFormMetaData.getNotAppicableResponseId())) {
            return "API response is not consistent with CHPL data";
        } else if (!isApiValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getApiConditionId(),
                        AttestatationFormMetaData.getCompliantResponseId())) {
            return "API response is not consistent with CHPL data";
        } else {
            return null;
        }

    }

    public String getAssurancesWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (isAssurancesValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getAssurancesConditionId(),
                        AttestatationFormMetaData.getNotAppicableResponseId())) {
            return "Assurances response is not consistent with CHPL data";
        } else if (!isAssurancesValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getAssurancesConditionId(),
                        AttestatationFormMetaData.getCompliantResponseId())) {
            return "Assurnaces response is not consistent with CHPL data";
        } else {
            return null;
        }
    }

    public String getRealWordTestingWarningMessage(List<ListingSearchResult> allActiveListingsForDeveloper, Form attestationForm) {
        if (isRealWorldTestingValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getRwtConditionId(),
                        AttestatationFormMetaData.getNotAppicableResponseId())) {
            return "Assurances response is not consistent with CHPL data";
        } else if (!isRealWorldTestingValid(allActiveListingsForDeveloper)
                && doesFormResponseEqualResponse(attestationForm,
                        AttestatationFormMetaData.getRwtConditionId(),
                        AttestatationFormMetaData.getCompliantResponseId())) {
            return "Assurnaces response is not consistent with CHPL data";
        } else {
            return null;
        }
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
