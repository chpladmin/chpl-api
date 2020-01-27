package gov.healthit.chpl.validation.surveillance.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SurveillanceMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class SurveillanceComparisonRemovedCriteriaReviewerTest {
    private static final String NO_REQUIREMENT_ADDED_WITH_REMOVED_CRITERIA =
            "The requirement \"%s\" cannot be added because that criteria has been removed.";
    private static final String NO_NONCONFORMITY_ADDED_WITH_REMOVED_CRITERIA =
            "The nonconformity \"%s\" cannot be added because that criteria has been removed.";
    private static final String NO_REQUIREMENT_EDITED_WITH_REMOVED_CRITERIA =
            "The requirement \"%s\" cannot be modified because that criteria has been removed.";
    private static final String NO_NONCONFORMITY_EDITED_WITH_REMOVED_CRITERIA =
            "The nonconformity \"%s\" cannot be modified because that criteria has been removed.";

    @Autowired
    private SurveillanceMockUtil mockUtil;

    @Mock
    private CertificationCriterionDAO criterionDAO;

    @Mock
    private ErrorMessageUtil msgUtil;

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private FF4j ff4j;

    @InjectMocks
    private RemovedCriteriaComparisonReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(
                ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK))
                .thenReturn(true);

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NO_NONCONFORMITY_ADDED_WITH_REMOVED_CRITERIA, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.nonconformityNotAddedForRemovedCriteria"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NO_NONCONFORMITY_EDITED_WITH_REMOVED_CRITERIA, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.nonconformityNotEditedForRemovedCriteria"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NO_REQUIREMENT_ADDED_WITH_REMOVED_CRITERIA, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.requirementNotAddedForRemovedCriteria"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NO_REQUIREMENT_EDITED_WITH_REMOVED_CRITERIA, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.requirementNotEditedForRemovedCriteria"),
                ArgumentMatchers.anyString());
    }

    @Test
    public void testOncAllowedToUpdateSurveillanceEditRequirementWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleOnc()).thenReturn(true);

        Surveillance existingSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        Surveillance updatedSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        SurveillanceRequirement req = updatedSurveillance.getRequirements().iterator().next();
        req.setRequirement("170.315 (a)(1)");

        List<CertificationCriterionDTO> removedCriteria = new ArrayList<CertificationCriterionDTO>();
        CertificationCriterionDTO crit = new CertificationCriterionDTO();
        crit.setId(1L);
        crit.setNumber(req.getRequirement());
        crit.setRemoved(true);
        removedCriteria.add(crit);

        Mockito.when(
                criterionDAO.getAllByNumber(ArgumentMatchers.eq(req.getRequirement())))
                .thenReturn(removedCriteria);
        //TODO Fix this as part of OCD-3220

        reviewer.review(existingSurveillance, updatedSurveillance);

        assertFalse(hasRemovedCriteriaAddedToRequirementErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaAddedToNonconformityErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaEditedInRequirementErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaEditedInNonconformityErrorMessage(updatedSurveillance, req.getRequirement()));
    }

    @Test
    public void testAdminAllowedToUpdateSurveillanceEditRequirementWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAdmin()).thenReturn(true);

        Surveillance existingSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        Surveillance updatedSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        SurveillanceRequirement req = updatedSurveillance.getRequirements().iterator().next();
        req.setRequirement("170.315 (a)(1)");

        List<CertificationCriterionDTO> removedCriteria = new ArrayList<CertificationCriterionDTO>();
        CertificationCriterionDTO crit = new CertificationCriterionDTO();
        crit.setId(1L);
        crit.setNumber(req.getRequirement());
        crit.setRemoved(true);
        removedCriteria.add(crit);

        Mockito.when(
                criterionDAO.getAllByNumber(ArgumentMatchers.eq(req.getRequirement())))
               .thenReturn(removedCriteria);
        //TODO Fix this as part of OCD-3220

        reviewer.review(existingSurveillance, updatedSurveillance);

        assertFalse(hasRemovedCriteriaAddedToRequirementErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaAddedToNonconformityErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaEditedInRequirementErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaEditedInNonconformityErrorMessage(updatedSurveillance, req.getRequirement()));
    }

    @Test
    public void testAcbNotAllowedToUpdateSurveillanceEditRequirementWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        Surveillance existingSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        Surveillance updatedSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        SurveillanceRequirement req = updatedSurveillance.getRequirements().iterator().next();
        req.setRequirement("170.315 (a)(1)");

        List<CertificationCriterionDTO> removedCriteria = new ArrayList<CertificationCriterionDTO>();
        CertificationCriterionDTO crit = new CertificationCriterionDTO();
        crit.setId(1L);
        crit.setNumber(req.getRequirement());
        crit.setRemoved(true);
        removedCriteria.add(crit);

        Mockito.when(
                criterionDAO.getAllByNumber(ArgumentMatchers.eq(req.getRequirement())))
               .thenReturn(removedCriteria);
        //TODO Fix this as part of OCD-3220

        reviewer.review(existingSurveillance, updatedSurveillance);

        assertFalse(hasRemovedCriteriaAddedToRequirementErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaAddedToNonconformityErrorMessage(updatedSurveillance, req.getRequirement()));
        assertTrue(hasRemovedCriteriaEditedInRequirementErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaEditedInNonconformityErrorMessage(updatedSurveillance, req.getRequirement()));
    }

    @Test
    public void testAcbNotAllowedToUpdateSurveillanceEditNonconformityWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        Surveillance existingSurveillance = mockUtil.createOpenSurveillanceWithOpenNonconformity();
        Surveillance updatedSurveillance = mockUtil.createOpenSurveillanceWithOpenNonconformity();
        SurveillanceNonconformity nc = updatedSurveillance.getRequirements().iterator().next().getNonconformities().get(0);
        nc.setNonconformityType("170.315 (a)(1)");

        List<CertificationCriterionDTO> removedCriteria = new ArrayList<CertificationCriterionDTO>();
        CertificationCriterionDTO crit = new CertificationCriterionDTO();
        crit.setId(1L);
        crit.setNumber(nc.getNonconformityType());
        crit.setRemoved(true);
        removedCriteria.add(crit);

        Mockito.when(
                criterionDAO.getAllByNumber(ArgumentMatchers.eq(nc.getNonconformityType())))
               .thenReturn(removedCriteria);
        //TODO Fix this as part of OCD-3220

        reviewer.review(existingSurveillance, updatedSurveillance);

        assertFalse(hasRemovedCriteriaAddedToRequirementErrorMessage(updatedSurveillance, nc.getNonconformityType()));
        assertFalse(hasRemovedCriteriaAddedToNonconformityErrorMessage(updatedSurveillance, nc.getNonconformityType()));
        assertFalse(hasRemovedCriteriaEditedInRequirementErrorMessage(updatedSurveillance, nc.getNonconformityType()));
        assertTrue(hasRemovedCriteriaEditedInNonconformityErrorMessage(updatedSurveillance, nc.getNonconformityType()));
    }

    @Test
    public void testAcbNotAllowedToUpdateSurveillanceAddRequirementWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        Surveillance existingSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        Surveillance updatedSurveillance = mockUtil.createOpenSurveillanceNoNonconformity();
        SurveillanceRequirement req = mockUtil.createSurveillanceRequirement(
                null, "170.315 (b)(1)", SurveillanceResultType.NO_NON_CONFORMITY,
                SurveillanceRequirementType.CERTIFIED_CAPABILITY);
        updatedSurveillance.getRequirements().add(req);

        List<CertificationCriterionDTO> removedCriteria = new ArrayList<CertificationCriterionDTO>();
        CertificationCriterionDTO crit = new CertificationCriterionDTO();
        crit.setId(1L);
        crit.setNumber(req.getRequirement());
        crit.setRemoved(true);
        removedCriteria.add(crit);

        Mockito.when(
                criterionDAO.getAllByNumber(ArgumentMatchers.eq(req.getRequirement())))
               .thenReturn(removedCriteria);
        //TODO Fix this as part of OCD-3220

        reviewer.review(existingSurveillance, updatedSurveillance);

        assertTrue(hasRemovedCriteriaAddedToRequirementErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaAddedToNonconformityErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaEditedInRequirementErrorMessage(updatedSurveillance, req.getRequirement()));
        assertFalse(hasRemovedCriteriaEditedInNonconformityErrorMessage(updatedSurveillance, req.getRequirement()));
    }

    @Test
    public void testAcbNotAllowedToUpdateSurveillanceAddNonconformityWithRemovedCriteria() {
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);

        Surveillance existingSurveillance = mockUtil.createOpenSurveillanceWithOpenNonconformity();
        Surveillance updatedSurveillance = mockUtil.createOpenSurveillanceWithOpenNonconformity();
        SurveillanceNonconformity nc = mockUtil.createSurveillanceNonconformity(null, "170.315 (b)(2)",
                SurveillanceNonconformityStatus.OPEN);
        updatedSurveillance.getRequirements().iterator().next().getNonconformities().add(nc);

        List<CertificationCriterionDTO> removedCriteria = new ArrayList<CertificationCriterionDTO>();
        CertificationCriterionDTO crit = new CertificationCriterionDTO();
        crit.setId(1L);
        crit.setNumber(nc.getNonconformityType());
        crit.setRemoved(true);
        removedCriteria.add(crit);

        Mockito.when(
                criterionDAO.getAllByNumber(ArgumentMatchers.eq(nc.getNonconformityType())))
                .thenReturn(removedCriteria);
        //TODO Fix this as part of OCD-3220

        reviewer.review(existingSurveillance, updatedSurveillance);

        assertFalse(hasRemovedCriteriaAddedToRequirementErrorMessage(updatedSurveillance, nc.getNonconformityType()));
        assertTrue(hasRemovedCriteriaAddedToNonconformityErrorMessage(updatedSurveillance, nc.getNonconformityType()));
        assertFalse(hasRemovedCriteriaEditedInRequirementErrorMessage(updatedSurveillance, nc.getNonconformityType()));
        assertFalse(hasRemovedCriteriaEditedInNonconformityErrorMessage(updatedSurveillance, nc.getNonconformityType()));
    }

    private boolean hasRemovedCriteriaAddedToRequirementErrorMessage(Surveillance surv, String criteriaNumber) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NO_REQUIREMENT_ADDED_WITH_REMOVED_CRITERIA, criteriaNumber))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRemovedCriteriaAddedToNonconformityErrorMessage(Surveillance surv, String criteriaNumber) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NO_NONCONFORMITY_ADDED_WITH_REMOVED_CRITERIA, criteriaNumber))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRemovedCriteriaEditedInRequirementErrorMessage(Surveillance surv, String criteriaNumber) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NO_REQUIREMENT_EDITED_WITH_REMOVED_CRITERIA, criteriaNumber))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRemovedCriteriaEditedInNonconformityErrorMessage(Surveillance surv, String criteriaNumber) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NO_NONCONFORMITY_EDITED_WITH_REMOVED_CRITERIA, criteriaNumber))) {
                return true;
            }
        }
        return false;
    }

    private String formatMessage(final String message, final Object... args) {
        return String.format(message, args);
    }
}
