package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DependentCriteriaReviewerTest {

    private Environment env;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationCriterionDAO certificationCriterionDAO;

    private DependentCriteriaReviewer dependentCriteriaReviewer;

    @Before
    public void before() {
        env = Mockito.mock(Environment.class);
        // For criteria ids 1,2,3,4,5, criteria 166, 167 are required
        Mockito.when(env.getProperty("requiredCriteriaDependencies")).thenReturn(
                "[{\"dependentCriteria\": [1,2,3,4,5], \"requiredCriteria\": [166,167]}]");

        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage("listing.criteria.dependentCriteriaRequired", ArgumentMatchers.any(),
                ArgumentMatchers.any()))
                .thenReturn("Attesting to Criteria %s requires that Criteria %s must also be attested to.");

        certificationCriterionDAO = Mockito.mock(CertificationCriterionDAO.class);

        dependentCriteriaReviewer = new DependentCriteriaReviewer(env, certificationCriterionDAO, errorMessageUtil);
        // Force call the postconstruct method
        dependentCriteriaReviewer.postConstruct();
    }

    @Test
    public void review_NoDependentCriteria_NoMessages() {

    }

    @Test
    public void review_DependentCriteriaAndNoRequiredCriteria_MultipleMesssages() {

    }

    @Test
    public void review_DependentCriteriaAndOneRequiredCriteria_OneErrorMessage() {

    }

    @Test
    void review_DependentCriteriaAndAllRequiredCriteria_NoMessages() {

    }
}
