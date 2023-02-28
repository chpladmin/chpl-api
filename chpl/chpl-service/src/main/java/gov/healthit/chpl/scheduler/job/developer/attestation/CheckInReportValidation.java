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

    private List<CertificationCriterion> assurancesCriteria;
    private List<CertificationCriterion> apiCriteria;
    private List<CertificationCriterion> rwtCriteria;

    @Autowired
    public CheckInReportValidation(RealWorldTestingCriteriaService realWorldTestingCriteriaService, CertificationCriterionService certificationCriterionService,
            @Value("${assurancesCriteriaKeys}") String[] assurancesCriteriaKeys, @Value("${apiCriteriaKeys}") String[] apiCriteriaKeys) {

        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        rwtCriteria = realWorldTestingCriteriaService.getEligibleCriteria(currentYear);
        assurancesCriteria = Arrays.asList(assurancesCriteriaKeys).stream().map(key -> certificationCriterionService.get(key)).collect(Collectors.toList());
        apiCriteria = Arrays.asList(apiCriteriaKeys).stream().map(key -> certificationCriterionService.get(key)).collect(Collectors.toList());
    }

    public String getRealWorldTestingValidation(List<ListingSearchResult> allActiveListingsForDeveloper) {
        List<ListingSearchResult> apiEligibleListings = getActiveListingDataWithAnyCriteriaForDeveloper(allActiveListingsForDeveloper, rwtCriteria);
        if (!CollectionUtils.isEmpty(apiEligibleListings)) {
            return RWT_VALIDATION_TRUE;
        } else {
            return RWT_VALIDATION_FALSE;
        }
    }

    public String getAssurancesValidation(List<ListingSearchResult> allActiveListingsForDeveloper) {
        List<ListingSearchResult> assurancesEligibleListings = getActiveListingDataWithAnyCriteriaForDeveloper(allActiveListingsForDeveloper, assurancesCriteria);
        if (!CollectionUtils.isEmpty(assurancesEligibleListings)) {
            return ASSURANCES_VALIDATION_TRUE;
        } else {
            return ASSURANCES_VALIDATION_FALSE;
        }
    }

    public String getApiValidation(List<ListingSearchResult> allActiveListingsForDeveloper) {
        List<ListingSearchResult> apiEligibleListings = getActiveListingDataWithAnyCriteriaForDeveloper(allActiveListingsForDeveloper, apiCriteria);
        if (!CollectionUtils.isEmpty(apiEligibleListings)) {
            return API_VALIDATION_TRUE;
        } else {
            return API_VALIDATION_FALSE;
        }
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
