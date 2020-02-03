package gov.healthit.chpl.validation.surveillance.reviewer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

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
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;
import gov.healthit.chpl.util.SurveillanceMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class SurveillanceDetailsReviewerTest {
    private static final String CHPL_NUMBER_NOT_FOUND = "Could not find chpl product with unique id \"%s\".";
    private static final String MISSING_START_DATE = "Start date for surveillance is required.";
    private static final String SURV_TYPE_NOT_FOUND = "A surveillance type was not found matching \"%s\".";
    private static final String RANDOMIZED_SITES_REQUIRED = "Randomized surveillance must provide a nonzero value for number of randomized sites used.";
    private static final String RANDOMIZED_SITES_NA = "Number of randomized sites used is not applicable for surveillance type \"%s\".";
    private static final String END_DATE_REQ = "End date for surveillance is required when there are no open nonconformities.";

    private SurveillanceMockUtil mockUtil = new SurveillanceMockUtil();

    private ListingMockUtil listingMockUtil = new ListingMockUtil();

    @Autowired
    private FF4j ff4j;

    @Mock
    private CertifiedProductDAO cpDao;

    @Mock
    private SurveillanceDAO survDao;

    @Mock
    private ErrorMessageUtil msgUtil;

    @Mock
    private ChplProductNumberUtil chplProductNumberUtil;

    @InjectMocks
    private SurveillanceDetailsReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(CHPL_NUMBER_NOT_FOUND, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.productUniqueIdNotFound"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                return MISSING_START_DATE;
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.startDateRequired"));

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(SURV_TYPE_NOT_FOUND, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.typeMismatch"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                return RANDOMIZED_SITES_REQUIRED;
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.randomizedNonzeroValue"));

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                return formatMessage(RANDOMIZED_SITES_NA, (String) args[1]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.randomizedSitesNotApplicable"),
                ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                return END_DATE_REQ;
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("surveillance.endDateRequiredNoOpenNonConformities"));

        Mockito.doReturn(true).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK);
        Mockito.doReturn(true).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE);
    }

    @Test
    public void testInvalidOldStyleChplIdHasErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        CertifiedProduct listingWithOldStyleChplId = listingMockUtil.createSimpleCertifiedProduct(null, "CHP-12345", "2014",
                new Date());
        surv.setCertifiedProduct(listingWithOldStyleChplId);

        Mockito.when(
                chplProductNumberUtil.getListing(ArgumentMatchers.eq(listingWithOldStyleChplId.getChplProductNumber())))
                .thenReturn(null);

        reviewer.review(surv);

        assertTrue(hasChplProductNumberNotFoundErrorMessage(surv, listingWithOldStyleChplId.getChplProductNumber()));
    }

    @Test
    public void testInvalidNewStyleChplIdHasErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        CertifiedProduct listingWithNewStyleChplId = listingMockUtil.createSimpleCertifiedProduct(null,
                ListingMockUtil.CHPL_ID_2015, "2015", new Date());
        surv.setCertifiedProduct(listingWithNewStyleChplId);

        Mockito.when(
                chplProductNumberUtil.getListing(ArgumentMatchers.eq(listingWithNewStyleChplId.getChplProductNumber())))
                .thenReturn(null);

        reviewer.review(surv);

        assertTrue(hasChplProductNumberNotFoundErrorMessage(surv, listingWithNewStyleChplId.getChplProductNumber()));
    }

    @Test
    public void testValidNewStyleChplIdPopulatesData() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        CertifiedProduct listingWithNewStyleChplId = listingMockUtil.createSimpleCertifiedProduct(null,
                ListingMockUtil.CHPL_ID_2015, "2015", new Date());
        surv.setCertifiedProduct(listingWithNewStyleChplId);

        CertifiedProduct foundListing = listingMockUtil.createSimpleCertifiedProduct(1L, ListingMockUtil.CHPL_ID_2015, "2015",
                new Date());
        Mockito.when(
                chplProductNumberUtil.getListing(ArgumentMatchers.eq(listingWithNewStyleChplId.getChplProductNumber())))
                .thenReturn(foundListing);

        reviewer.review(surv);

        assertFalse(hasChplProductNumberNotFoundErrorMessage(surv, listingWithNewStyleChplId.getChplProductNumber()));
        assertNotNull(surv.getCertifiedProduct());
        assertEquals(foundListing.getId(), surv.getCertifiedProduct().getId());
    }

    @Test
    public void testMissingStartDateHasErrorMessage() throws EntityRetrievalException {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setCertifiedProduct(null);
        surv.setStartDate(null);
        reviewer.review(surv);
        assertTrue(hasMissingStartDateErrorMessage(surv));
    }

    @Test
    public void testHasStartDateNoErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setCertifiedProduct(null);
        reviewer.review(surv);
        assertFalse(hasMissingStartDateErrorMessage(surv));
    }

    @Test
    public void testBadSurveillanceTypeNameHasErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setCertifiedProduct(null);
        SurveillanceType type = new SurveillanceType();
        type.setId(null);
        type.setName("BAD");
        surv.setType(type);

        Mockito.when(
                survDao.findSurveillanceType(ArgumentMatchers.eq(type.getName())))
                .thenReturn(null);

        reviewer.review(surv);
        assertTrue(hasSurveillanceTypeNotFoundErrorMessage(surv, type.getName()));
    }

    @Test
    public void testValidSurveillanceTypeNameNoErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setCertifiedProduct(null);
        reviewer.review(surv);
        assertFalse(hasSurveillanceTypeNotFoundErrorMessage(surv, surv.getType().getName()));
    }

    @Test
    public void testRandomizedTypeWithoutSiteCountHasErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setCertifiedProduct(null);
        SurveillanceType type = new SurveillanceType();
        type.setId(null);
        type.setName(SurveillanceType.RANDOMIZED);
        surv.setType(type);
        surv.setRandomizedSitesUsed(null);
        reviewer.review(surv);
        assertTrue(hasRandomizedSitesRequiredErrorMessage(surv));
    }

    @Test
    public void testRandomizedTypeWithSiteCountNoErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setCertifiedProduct(null);
        SurveillanceType type = new SurveillanceType();
        type.setId(null);
        type.setName(SurveillanceType.RANDOMIZED);
        surv.setType(type);
        surv.setRandomizedSitesUsed(100);
        reviewer.review(surv);
        assertFalse(hasRandomizedSitesRequiredErrorMessage(surv));
    }

    @Test
    public void testReactiveTypeWithSiteCountHasErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setCertifiedProduct(null);
        SurveillanceType type = new SurveillanceType();
        type.setId(null);
        type.setName(SurveillanceType.REACTIVE);
        surv.setType(type);
        surv.setRandomizedSitesUsed(100);
        reviewer.review(surv);
        assertTrue(hasRandomizedSitesNaErrorMessage(surv, type.getName()));
    }

    @Test
    public void testReactiveTypeNoSiteCountNoErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setCertifiedProduct(null);
        SurveillanceType type = new SurveillanceType();
        type.setId(null);
        type.setName(SurveillanceType.REACTIVE);
        surv.setType(type);
        surv.setRandomizedSitesUsed(null);
        reviewer.review(surv);
        assertFalse(hasRandomizedSitesNaErrorMessage(surv, type.getName()));
    }

    @Test
    public void testNoEndDateNoNonconformitiesHasErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setCertifiedProduct(null);
        reviewer.review(surv);
        assertTrue(hasEndDateRequiredErrorMessage(surv));
    }

    @Test
    public void testHasEndDateNoNonconformitiesNoErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceNoNonconformity();
        surv.setEndDate(new Date());
        surv.setCertifiedProduct(null);
        reviewer.review(surv);
        assertFalse(hasEndDateRequiredErrorMessage(surv));
    }

    @Test
    public void testNoEndDateOpenNonconformitiesNoErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceWithOpenNonconformity();
        surv.setCertifiedProduct(null);
        reviewer.review(surv);
        assertFalse(hasEndDateRequiredErrorMessage(surv));
    }

    @Test
    public void testNoEndDateClosedNonconformitiesHasErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceWithClosedNonconformity();
        surv.setCertifiedProduct(null);
        reviewer.review(surv);
        assertTrue(hasEndDateRequiredErrorMessage(surv));
    }

    @Test
    public void testHasEndDateClosedNonconformitiesNoErrorMessage() {
        Surveillance surv = mockUtil.createOpenSurveillanceWithClosedNonconformity();
        surv.setEndDate(new Date());
        surv.setCertifiedProduct(null);
        reviewer.review(surv);
        assertFalse(hasEndDateRequiredErrorMessage(surv));
    }

    private boolean hasChplProductNumberNotFoundErrorMessage(Surveillance surv, String chplId) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(CHPL_NUMBER_NOT_FOUND, chplId))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMissingStartDateErrorMessage(Surveillance surv) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, MISSING_START_DATE)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSurveillanceTypeNotFoundErrorMessage(Surveillance surv, String typeName) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(SURV_TYPE_NOT_FOUND, typeName))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRandomizedSitesRequiredErrorMessage(Surveillance surv) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(RANDOMIZED_SITES_REQUIRED))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRandomizedSitesNaErrorMessage(Surveillance surv, String typeName) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(RANDOMIZED_SITES_NA, typeName))) {
                return true;
            }
        }
        return false;
    }

    private boolean hasEndDateRequiredErrorMessage(Surveillance surv) {
        for (String message : surv.getErrorMessages()) {
            if (StringUtils.equals(message, formatMessage(END_DATE_REQ))) {
                return true;
            }
        }
        return false;
    }

    private String formatMessage(final String message, final Object... args) {
        return String.format(message, args);
    }
}
