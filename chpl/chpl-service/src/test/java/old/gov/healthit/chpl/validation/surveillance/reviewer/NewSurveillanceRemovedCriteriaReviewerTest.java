package old.gov.healthit.chpl.validation.surveillance.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.surveillance.reviewer.NewSurveillanceRemovedCriteriaReviewer;
import old.gov.healthit.chpl.util.SurveillanceMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class NewSurveillanceRemovedCriteriaReviewerTest {
    private static final String NO_REQUIREMENT_WITH_REMOVED_CRITERIA = "The requirement \"%s\" cannot be added because that criteria has been removed.";
    private static final String NO_NONCONFORMITY_WITH_REMOVED_CRITERIA = "The nonconformity \"%s\" cannot be added because that criteria has been removed.";

    @Autowired
    private SurveillanceMockUtil mockUtil;

    @Autowired
    private FF4j ff4j;

    @Mock
    private CertificationCriterionDAO criterionDAO;

    @Mock
    private ErrorMessageUtil msgUtil;

    @Mock
    private ResourcePermissions resourcePermissions;

    @InjectMocks
    private NewSurveillanceRemovedCriteriaReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(true).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK);
        Mockito.doReturn(true).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE);

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NO_NONCONFORMITY_WITH_REMOVED_CRITERIA, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.nonconformityNotAddedForRemovedCriteria"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NO_REQUIREMENT_WITH_REMOVED_CRITERIA, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.requirementNotAddedForRemovedCriteria"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testOncAllowedToCreateSurveillanceWithRequirementWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        Surveillance newSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        String criteriaNumber = newSurveillance.getRequirements().iterator().next().getRequirement();
        CertificationCriterionDTO removedCriteria = new CertificationCriterionDTO();
        removedCriteria.setId(1L);
        removedCriteria.setNumber(criteriaNumber);
        removedCriteria.setRemoved(true);

        Mockito.when(
                criterionDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(removedCriteria);

        reviewer.review(newSurveillance);

        assertFalse(hasRemovedCriteriaInRequirementErrorMessage(newSurveillance, criteriaNumber));
        assertFalse(hasRemovedCriteriaInNonconformityErrorMessage(newSurveillance, criteriaNumber));
    }

    @Test
    public void testOncAllowedToCreateSurveillanceWithNonconformityWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        Surveillance newSurveillance = mockUtil.createOpenSurveillanceWithOpenNonconformity();
        SurveillanceNonconformity nc = newSurveillance.getRequirements().iterator().next().getNonconformities().get(0);
        String criteriaNumber = nc.getNonconformityType();
        CertificationCriterionDTO removedCriteria = new CertificationCriterionDTO();
        removedCriteria.setId(1L);
        removedCriteria.setNumber(criteriaNumber);
        removedCriteria.setRemoved(true);

        Mockito.when(
                criterionDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(removedCriteria);

        reviewer.review(newSurveillance);

        assertFalse(hasRemovedCriteriaInRequirementErrorMessage(newSurveillance, criteriaNumber));
        assertFalse(hasRemovedCriteriaInNonconformityErrorMessage(newSurveillance, criteriaNumber));
    }

    @Test
    public void testAdminAllowedToCreateSurveillanceWithRequirementWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);

        Surveillance newSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        String criteriaNumber = newSurveillance.getRequirements().iterator().next().getRequirement();
        CertificationCriterionDTO removedCriteria = new CertificationCriterionDTO();
        removedCriteria.setId(1L);
        removedCriteria.setNumber(criteriaNumber);
        removedCriteria.setRemoved(true);

        Mockito.when(
                criterionDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(removedCriteria);

        reviewer.review(newSurveillance);

        assertFalse(hasRemovedCriteriaInRequirementErrorMessage(newSurveillance, criteriaNumber));
        assertFalse(hasRemovedCriteriaInNonconformityErrorMessage(newSurveillance, criteriaNumber));
    }

    @Test
    public void testAdminAllowedToCreateSurveillanceWithNonconformityWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);

        Surveillance newSurveillance = mockUtil.createOpenSurveillanceWithOpenNonconformity();
        SurveillanceNonconformity nc = newSurveillance.getRequirements().iterator().next().getNonconformities().get(0);
        String criteriaNumber = nc.getNonconformityType();
        CertificationCriterionDTO removedCriteria = new CertificationCriterionDTO();
        removedCriteria.setId(1L);
        removedCriteria.setNumber(criteriaNumber);
        removedCriteria.setRemoved(true);

        Mockito.when(
                criterionDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(removedCriteria);

        reviewer.review(newSurveillance);

        assertFalse(hasRemovedCriteriaInRequirementErrorMessage(newSurveillance, criteriaNumber));
        assertFalse(hasRemovedCriteriaInNonconformityErrorMessage(newSurveillance, criteriaNumber));
    }

    @Test
    public void testAcbNotAllowedToCreateSurveillanceWithRequirementWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        Surveillance newSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        String criteriaNumber = newSurveillance.getRequirements().iterator().next().getRequirement();
        CertificationCriterionDTO removedCriteria = new CertificationCriterionDTO();
        removedCriteria.setId(1L);
        removedCriteria.setNumber(criteriaNumber);
        removedCriteria.setRemoved(true);

        Mockito.when(
                criterionDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(removedCriteria);

        reviewer.review(newSurveillance);

        assertTrue(hasRemovedCriteriaInRequirementErrorMessage(newSurveillance, criteriaNumber));
        assertFalse(hasRemovedCriteriaInNonconformityErrorMessage(newSurveillance, criteriaNumber));
    }

    @Test
    public void testAcbNotAllowedToCreateSurveillanceWithNonconformityWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        Surveillance newSurveillance = mockUtil.createOpenSurveillanceWithOpenNonconformity();
        SurveillanceNonconformity nc = newSurveillance.getRequirements().iterator().next().getNonconformities().get(0);
        String criteriaNumber = nc.getNonconformityType();
        CertificationCriterionDTO removedCriteria = new CertificationCriterionDTO();
        removedCriteria.setId(1L);
        removedCriteria.setNumber(criteriaNumber);
        removedCriteria.setRemoved(true);

        Mockito.when(
                criterionDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(removedCriteria);

        reviewer.review(newSurveillance);

        assertFalse(hasRemovedCriteriaInRequirementErrorMessage(newSurveillance, criteriaNumber));
        assertTrue(hasRemovedCriteriaInNonconformityErrorMessage(newSurveillance, criteriaNumber));
    }

    @Test
    public void testAcbAllowedToCreateSurveillanceWithRequirementWithRegularCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        Surveillance newSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        String criteriaNumber = newSurveillance.getRequirements().iterator().next().getRequirement();
        CertificationCriterionDTO removedCriteria = new CertificationCriterionDTO();
        removedCriteria.setId(1L);
        removedCriteria.setNumber(criteriaNumber);
        removedCriteria.setRemoved(false);

        Mockito.when(
                criterionDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(removedCriteria);

        reviewer.review(newSurveillance);

        assertFalse(hasRemovedCriteriaInRequirementErrorMessage(newSurveillance, criteriaNumber));
        assertFalse(hasRemovedCriteriaInNonconformityErrorMessage(newSurveillance, criteriaNumber));
    }

    @Test
    public void testAcbAllowedToCreateSurveillanceWithNonconformityWithRegularCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        Surveillance newSurveillance = mockUtil.createOpenSurveillanceWithOpenNonconformity();
        SurveillanceNonconformity nc = newSurveillance.getRequirements().iterator().next().getNonconformities().get(0);
        String criteriaNumber = nc.getNonconformityType();
        CertificationCriterionDTO removedCriteria = new CertificationCriterionDTO();
        removedCriteria.setId(1L);
        removedCriteria.setNumber(criteriaNumber);
        removedCriteria.setRemoved(false);

        Mockito.when(
                criterionDAO.getByName(ArgumentMatchers.anyString()))
                .thenReturn(removedCriteria);

        reviewer.review(newSurveillance);

        assertFalse(hasRemovedCriteriaInRequirementErrorMessage(newSurveillance, criteriaNumber));
        assertFalse(hasRemovedCriteriaInNonconformityErrorMessage(newSurveillance, criteriaNumber));
    }

    private boolean hasRemovedCriteriaInRequirementErrorMessage(Surveillance surv, String criteriaNumber) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NO_REQUIREMENT_WITH_REMOVED_CRITERIA, criteriaNumber))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRemovedCriteriaInNonconformityErrorMessage(Surveillance surv, String criteriaNumber) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NO_NONCONFORMITY_WITH_REMOVED_CRITERIA, criteriaNumber))) {
                return true;
            }
        }
        return false;
    }

    private String formatMessage(final String message, final Object... args) {
        return String.format(message, args);
    }
}
