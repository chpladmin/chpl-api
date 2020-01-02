package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class CertificationDateReviewerTest {
    private static final String FUTURE_CERT_DATE_ERROR = "Certification date occurs in the future.";
    @Autowired private ListingMockUtil mockUtil;
    @Autowired private MessageSource messageSource;

    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private CertificationDateReviewer certDateReviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        certDateReviewer = new CertificationDateReviewer(msgUtil);
    }

    //Case: A valid certification date
    @Test
    public void testValidCertificationDate_DoesNotHaveError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        certDateReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(FUTURE_CERT_DATE_ERROR));
    }

    //Case: An invalid/future certification date
    @Test
    public void testFutureCertificationDate_HasError() {
        Mockito.doReturn(FUTURE_CERT_DATE_ERROR)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.futureCertificationDate"));

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        listing.setCertificationDate(cal.getTimeInMillis());
        certDateReviewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(FUTURE_CERT_DATE_ERROR));
    }
}
