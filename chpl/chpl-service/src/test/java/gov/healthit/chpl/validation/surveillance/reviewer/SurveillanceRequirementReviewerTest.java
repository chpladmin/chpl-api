package gov.healthit.chpl.validation.surveillance.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
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
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;
import gov.healthit.chpl.util.SurveillanceMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class SurveillanceRequirementReviewerTest {
    private static final String REQUIREMENTS_REQUIRED =
            "At least one surveillance requirement is required for CHPL product \"%s\".";
    private static final String REQ_NAME_MISSING =
            "A surveillance requirement (reg text number or other value) is required.";
    private static final String REQUIREMENT_TYPE_NAME_INVALID =
            "No type with name \"%s\" was found for surveillance requirement \"%s\".";
    private static final String UNATTESTED_CRITERIA_NOT_ALLOWED =
            "The requirement \"%s\" is not valid for requirement type \"%s\". Valid values are any criterion this product has attested to.";
    private static final String TRANSPARENCY_REQ_INVALID =
            "The requirement \"%s\" is not valid for requirement type \"%s\". Valid values are \"%s\" or \"%s\".";
    private static final String RESULT_MISSING =
            "Result was not found for surveillance requirement \"%s\".";
    private static final String REQUIREMENT_STATUS_NAME_INVALID =
            "No result with name '%s' was found for surveillance requirement \"%s\".";

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
    private SurveillanceRequirementReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(REQUIREMENTS_REQUIRED, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.requirementIsRequiredForProduct"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                return formatMessage(REQ_NAME_MISSING);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.requirementIsRequired"));

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(REQUIREMENT_TYPE_NAME_INVALID, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.typeNameMissingForRequirement"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(UNATTESTED_CRITERIA_NOT_ALLOWED,
                        (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.requirementInvalidForRequirementType"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(TRANSPARENCY_REQ_INVALID,
                        (String) args[1], (String) args[2], (String) args[3], (String) args[4]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.requirementInvalidForTransparencyType"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(RESULT_MISSING, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.resultNotFound"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(REQUIREMENT_STATUS_NAME_INVALID, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.resultWithNameNotFound"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testNoRequirementsHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        reviewer.review(surv);
        assertTrue(hasRequirementsRequiredErrorMessage(surv, surv.getCertifiedProduct().getChplProductNumber()));
    }

    @Test
    public void testNullRequirementsHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setRequirements(null);
        reviewer.review(surv);
        assertTrue(hasRequirementsRequiredErrorMessage(surv, surv.getCertifiedProduct().getChplProductNumber()));
    }

    @Test
    public void testRequirementNameMissingHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().iterator().next().setRequirement(null);
        reviewer.review(surv);
        assertTrue(hasRequirementsNameMissingErrorMessage(surv));
    }

    @Test
    public void testRequirementBadTypeNameHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setId(null);
        req.setRequirement("170.315 (a)(1)");
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(null);
        type.setName("BAD");
        req.setType(type);
        SurveillanceResultType result = new SurveillanceResultType();
        result.setId(1L);
        result.setName(SurveillanceResultType.NO_NON_CONFORMITY);
        req.setResult(result);
        surv.getRequirements().add(req);

        Mockito.when(
                survDao.findSurveillanceRequirementType(ArgumentMatchers.eq(type.getName())))
                .thenReturn(null);

        reviewer.review(surv);
        assertTrue(hasBadRequirementTypeErrorMessage(surv, type.getName(), req.getRequirement()));
    }

    @Test
    public void testRequirementValidTypeNameNoError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setId(null);
        req.setRequirement("170.315 (a)(1)");
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(null);
        type.setName(SurveillanceRequirementType.CERTIFIED_CAPABILITY);
        req.setType(type);
        SurveillanceResultType result = new SurveillanceResultType();
        result.setId(1L);
        result.setName(SurveillanceResultType.NO_NON_CONFORMITY);
        req.setResult(result);
        surv.getRequirements().add(req);

        Mockito.when(
                survDao.findSurveillanceRequirementType(ArgumentMatchers.eq(type.getName())))
                .thenReturn(type);

        reviewer.review(surv);
        assertFalse(hasBadRequirementTypeErrorMessage(surv, type.getName(), req.getRequirement()));
    }

    @Test
    public void testRequirementForUnattestedCriteriaHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        List<CertificationResultDetailsDTO> certResults =
                new ArrayList<CertificationResultDetailsDTO>();
        CertificationResultDetailsDTO certResult = listingMockUtil.create2015CertResultDetails(1L, "170.315 (a)(1)", true);
        certResults.add(certResult);
        Mockito.when(
                certResultDetailsDao.getCertificationResultsForSurveillanceListing(ArgumentMatchers.any()))
                .thenReturn(certResults);
        reviewer.review(surv);
        SurveillanceRequirement req = surv.getRequirements().iterator().next();
        assertTrue(hasUnattestedCriteriaNotAllowedErrorMessage(surv,
                req.getRequirement(),
                req.getType().getName()));
    }

    @Test
    public void testRequirementForAttestedCriteriaNoError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        SurveillanceRequirement req = surv.getRequirements().iterator().next();

        List<CertificationResultDetailsDTO> certResults =
                new ArrayList<CertificationResultDetailsDTO>();
        CertificationResultDetailsDTO certResult =
                listingMockUtil.create2015CertResultDetails(1L, req.getRequirement(), true);
        certResults.add(certResult);
        Mockito.when(
                certResultDetailsDao.getCertificationResultsForSurveillanceListing(ArgumentMatchers.any()))
                .thenReturn(certResults);
        reviewer.review(surv);
        assertFalse(hasUnattestedCriteriaNotAllowedErrorMessage(surv,
                req.getRequirement(),
                req.getType().getName()));
    }

    @Test
    public void testTransparencyRequirementInvalidHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setId(null);
        req.setRequirement("170.315 (a)(1)");
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(null);
        type.setName(SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        req.setType(type);
        SurveillanceResultType result = new SurveillanceResultType();
        result.setId(1L);
        result.setName(SurveillanceResultType.NO_NON_CONFORMITY);
        req.setResult(result);
        surv.getRequirements().add(req);

        reviewer.review(surv);
        assertTrue(hasTransparencyRequirementInvalidErrorMessage(surv,
                req.getRequirement(),
                req.getType().getName()));
    }

    @Test
    public void testTransparencyRequirementValidNoError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.getRequirements().clear();
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setId(null);
        req.setRequirement(RequirementTypeEnum.K1.getName());
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(null);
        type.setName(SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        req.setType(type);
        SurveillanceResultType result = new SurveillanceResultType();
        result.setId(1L);
        result.setName(SurveillanceResultType.NO_NON_CONFORMITY);
        req.setResult(result);
        surv.getRequirements().add(req);

        reviewer.review(surv);
        assertFalse(hasTransparencyRequirementInvalidErrorMessage(surv,
                req.getRequirement(),
                req.getType().getName()));
    }

    @Test
    public void testClosedSurveillanceUnresolvedRequirementHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setEndDate(new Date());
        surv.getRequirements().clear();
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setId(null);
        req.setRequirement(RequirementTypeEnum.K1.getName());
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(null);
        type.setName(SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        req.setType(type);
        req.setResult(null);
        surv.getRequirements().add(req);

        reviewer.review(surv);
        assertTrue(hasMissingResultErrorMessage(surv, req.getRequirement()));
    }

    @Test
    public void testClosedSurveillanceResolvedRequirementNoError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setEndDate(new Date());
        surv.getRequirements().clear();
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setId(null);
        req.setRequirement(RequirementTypeEnum.K1.getName());
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(null);
        type.setName(SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        req.setType(type);
        SurveillanceResultType result = new SurveillanceResultType();
        result.setId(1L);
        result.setName(SurveillanceResultType.NO_NON_CONFORMITY);
        req.setResult(result);
        surv.getRequirements().add(req);

        reviewer.review(surv);
        assertFalse(hasMissingResultErrorMessage(surv, req.getRequirement()));
    }

    @Test
    public void testRequirementStatusInvalidNameHasError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setEndDate(new Date());
        surv.getRequirements().clear();
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setId(null);
        req.setRequirement(RequirementTypeEnum.K1.getName());
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(null);
        type.setName(SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        req.setType(type);
        SurveillanceResultType result = new SurveillanceResultType();
        result.setId(null);
        result.setName("BAD");
        req.setResult(result);
        surv.getRequirements().add(req);

        Mockito.when(
                survDao.findSurveillanceResultType(ArgumentMatchers.eq(result.getName())))
                .thenReturn(null);

        reviewer.review(surv);
        assertTrue(hasInvalidResultNameErrorMessage(surv, result.getName(), req.getRequirement()));
    }

    @Test
    public void testRequirementStatusValidNameNoError() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setEndDate(new Date());
        surv.getRequirements().clear();
        SurveillanceRequirement req = new SurveillanceRequirement();
        req.setId(null);
        req.setRequirement(RequirementTypeEnum.K1.getName());
        SurveillanceRequirementType type = new SurveillanceRequirementType();
        type.setId(null);
        type.setName(SurveillanceRequirementType.TRANS_DISCLOSURE_REQ);
        req.setType(type);
        SurveillanceResultType result = new SurveillanceResultType();
        result.setId(null);
        result.setName(SurveillanceResultType.NO_NON_CONFORMITY);
        req.setResult(result);
        surv.getRequirements().add(req);

        Mockito.when(
                survDao.findSurveillanceResultType(ArgumentMatchers.eq(result.getName())))
                .thenReturn(result);

        reviewer.review(surv);
        assertFalse(hasInvalidResultNameErrorMessage(surv, result.getName(), req.getRequirement()));
    }

    private boolean hasRequirementsRequiredErrorMessage(Surveillance surv, String chplNumber) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(REQUIREMENTS_REQUIRED, chplNumber))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRequirementsNameMissingErrorMessage(Surveillance surv) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(REQ_NAME_MISSING))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBadRequirementTypeErrorMessage(Surveillance surv, String typeName, String reqName) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(REQUIREMENT_TYPE_NAME_INVALID, typeName, reqName))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasUnattestedCriteriaNotAllowedErrorMessage(Surveillance surv, String req, String reqType) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(UNATTESTED_CRITERIA_NOT_ALLOWED, req, reqType))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTransparencyRequirementInvalidErrorMessage(Surveillance surv, String req, String reqType) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(TRANSPARENCY_REQ_INVALID,
                    req, reqType, RequirementTypeEnum.K1.getName(), RequirementTypeEnum.K2.getName()))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMissingResultErrorMessage(Surveillance surv, String req) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(RESULT_MISSING, req))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasInvalidResultNameErrorMessage(Surveillance surv, String statusName, String req) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(REQUIREMENT_STATUS_NAME_INVALID, statusName, req))) {
                return true;
            }
        }
        return false;
    }

    private String formatMessage(final String message, final Object... args) {
        return String.format(message, args);
    }
}
