package gov.healthit.chpl.validation.surveillance.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;
import gov.healthit.chpl.util.SurveillanceMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class SurveillanceNonconformityReviewerTest {
    private static final String NONCONFORMITY_REQUIRED = "Surveillance Requirement \"%s\" has a result of 'Non-Conformity' but no nonconformities were found.";
    private static final String NONCONFORMITY_TYPE_REQUIRED = "Nonconformity type (reg text number or other value) is required for surveillance requirement \"%s\".";
    private static final String NONCONFORMITY_TYPE_INVALID = "Nonconformity type \"%s\" must match either a criterion the surveilled product has attested to or one of the following: \"%s\", \"%s\", \"%s\", or \"%s\".";
    private static final String NONCONFORMITY_STATUS_REQUIRED = "Nonconformity status is required for requirement %s, nonconformity %s.";
    private static final String NONCONFORMITY_STATUS_INVALID = "No non-conformity status with name \"%s\" was found for requirement \"%s\", nonconformity \"%s\".";

    @Autowired
    private SurveillanceMockUtil mockUtil;

    @Autowired
    private ListingMockUtil listingMockUtil;

    @Mock
    private CertificationResultDetailsDAO certResultDetailsDao;

    @Mock
    private SurveillanceDAO survDao;

    @Mock
    private ErrorMessageUtil msgUtil;

    @InjectMocks
    private SurveillanceNonconformityReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        List<CertificationResultDetailsDTO> certResults = new ArrayList<CertificationResultDetailsDTO>();
        CertificationResultDetailsDTO certResult = listingMockUtil.create2015CertResultDetails(1L, "170.315 (a)(1)", true);
        certResults.add(certResult);
        Mockito.when(
                certResultDetailsDao.getCertificationResultsForSurveillanceListing(ArgumentMatchers.any()))
                .thenReturn(certResults);

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NONCONFORMITY_REQUIRED, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.nonConformityNotFound"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NONCONFORMITY_TYPE_REQUIRED, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.nonConformityTypeRequired"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NONCONFORMITY_TYPE_INVALID, (String) args[1],
                        (String) args[2], (String) args[3], (String) args[4],
                        (String) args[5]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.nonConformityTypeMatchException"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NONCONFORMITY_STATUS_REQUIRED, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.nonConformityStatusNotFound"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(NONCONFORMITY_STATUS_INVALID, (String) args[1],
                        (String) args[2], (String) args[3]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.nonConformityStatusWithNameNotFound"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testNoNonconformitiesHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = mockUtil.createSurveillanceRequirement(
                1L, RequirementTypeEnum.K1.getName(),
                SurveillanceResultType.NON_CONFORMITY,
                SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        req.setNonconformities(null);
        surv.getRequirements().add(req);

        reviewer.review(surv);
        assertTrue(hasNonconformitiesRequiredErrorMessage(surv, req.getRequirement()));
    }

    @Test
    public void testMissingNonconformityTypeHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = mockUtil.createSurveillanceRequirement(
                1L, RequirementTypeEnum.K1.getName(),
                SurveillanceResultType.NON_CONFORMITY,
                SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        surv.getRequirements().add(req);
        SurveillanceNonconformity nc = mockUtil.createSurveillanceNonconformity(
                null, null, SurveillanceNonconformityStatus.OPEN);
        req.getNonconformities().add(nc);

        reviewer.review(surv);
        assertTrue(hasMissingNonconformityTypeErrorMessage(surv, req.getRequirement()));
    }

    @Test
    public void testUnattestedCriteriaAsNonconformityTypeHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = mockUtil.createSurveillanceRequirement(
                1L, RequirementTypeEnum.K1.getName(),
                SurveillanceResultType.NON_CONFORMITY,
                SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        surv.getRequirements().add(req);
        SurveillanceNonconformity nc = mockUtil.createSurveillanceNonconformity(
                null, "170.315 (a)(2)", SurveillanceNonconformityStatus.OPEN);
        req.getNonconformities().add(nc);

        reviewer.review(surv);
        assertTrue(hasInvalidNonconformityTypeErrorMessage(surv, nc.getNonconformityType()));
    }

    @Test
    public void testAttestedCriteriaAsNonconformityTypeNoError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = mockUtil.createSurveillanceRequirement(
                1L, RequirementTypeEnum.K1.getName(),
                SurveillanceResultType.NON_CONFORMITY,
                SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        surv.getRequirements().add(req);
        SurveillanceNonconformity nc = mockUtil.createSurveillanceNonconformity(
                null, "170.315 (a)(1)", SurveillanceNonconformityStatus.OPEN);
        req.getNonconformities().add(nc);

        reviewer.review(surv);
        assertFalse(hasInvalidNonconformityTypeErrorMessage(surv, nc.getNonconformityType()));
    }

    @Test
    public void testOtherAsNonconformityTypeNoError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = mockUtil.createSurveillanceRequirement(
                1L, RequirementTypeEnum.K1.getName(),
                SurveillanceResultType.NON_CONFORMITY,
                SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        surv.getRequirements().add(req);
        SurveillanceNonconformity nc = mockUtil.createSurveillanceNonconformity(
                null, NonconformityType.OTHER.getName(), SurveillanceNonconformityStatus.OPEN);
        req.getNonconformities().add(nc);

        reviewer.review(surv);
        assertFalse(hasInvalidNonconformityTypeErrorMessage(surv, nc.getNonconformityType()));
    }

    @Test
    public void testMissingNonconformityStatusHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = mockUtil.createSurveillanceRequirement(
                1L, RequirementTypeEnum.K1.getName(),
                SurveillanceResultType.NON_CONFORMITY,
                SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        surv.getRequirements().add(req);
        SurveillanceNonconformity nc = mockUtil.createSurveillanceNonconformity(
                null, NonconformityType.OTHER.getName(), null);
        nc.setStatus(null);
        req.getNonconformities().add(nc);

        reviewer.review(surv);
        assertTrue(hasMissingNonconformityStatusErrorMessage(
                surv, req.getRequirement(), nc.getNonconformityType()));
    }

    @Test
    public void testInvalidNonconformityStatusHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = mockUtil.createSurveillanceRequirement(
                1L, RequirementTypeEnum.K1.getName(),
                SurveillanceResultType.NON_CONFORMITY,
                SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        surv.getRequirements().add(req);
        SurveillanceNonconformity nc = mockUtil.createSurveillanceNonconformity(
                null, NonconformityType.OTHER.getName(), "BAD");
        nc.getStatus().setId(null);
        req.getNonconformities().add(nc);

        Mockito.when(
                survDao.findSurveillanceNonconformityStatusType(nc.getStatus().getName()))
                .thenReturn(null);

        reviewer.review(surv);
        assertFalse(hasMissingNonconformityStatusErrorMessage(
                surv, req.getRequirement(), nc.getNonconformityType()));
        assertTrue(hasInvalidNonconformityStatusErrorMessage(
                surv, nc.getStatus().getName(), req.getRequirement(), nc.getNonconformityType()));
    }

    @Test
    public void testValidNonconformityStatusNoError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = mockUtil.createSurveillanceRequirement(
                1L, RequirementTypeEnum.K1.getName(),
                SurveillanceResultType.NON_CONFORMITY,
                SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        surv.getRequirements().add(req);
        SurveillanceNonconformity nc = mockUtil.createSurveillanceNonconformity(
                null, NonconformityType.OTHER.getName(), SurveillanceNonconformityStatus.OPEN);
        nc.getStatus().setId(null);
        req.getNonconformities().add(nc);

        Mockito.when(
                survDao.findSurveillanceNonconformityStatusType(nc.getStatus().getName()))
                .thenReturn(nc.getStatus());

        reviewer.review(surv);
        assertFalse(hasMissingNonconformityStatusErrorMessage(
                surv, req.getRequirement(), nc.getNonconformityType()));
        assertFalse(hasInvalidNonconformityStatusErrorMessage(
                surv, nc.getStatus().getName(), req.getRequirement(), nc.getNonconformityType()));
    }

    private boolean hasNonconformitiesRequiredErrorMessage(Surveillance surv, String req) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NONCONFORMITY_REQUIRED, req))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMissingNonconformityTypeErrorMessage(Surveillance surv, String req) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NONCONFORMITY_TYPE_REQUIRED, req))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInvalidNonconformityTypeErrorMessage(Surveillance surv, String ncType) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NONCONFORMITY_TYPE_INVALID,
                    ncType, NonconformityType.K1.getName(),
                    NonconformityType.K2.getName(), NonconformityType.L.getName(),
                    NonconformityType.OTHER.getName()))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMissingNonconformityStatusErrorMessage(Surveillance surv, String req, String ncType) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(NONCONFORMITY_STATUS_REQUIRED, req, ncType))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInvalidNonconformityStatusErrorMessage(Surveillance surv,
            String statusName, String req, String ncType) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message,
                    formatMessage(NONCONFORMITY_STATUS_INVALID, statusName, req, ncType))) {
                return true;
            }
        }
        return false;
    }

    private String formatMessage(final String message, final Object... args) {
        return String.format(message, args);
    }
}
