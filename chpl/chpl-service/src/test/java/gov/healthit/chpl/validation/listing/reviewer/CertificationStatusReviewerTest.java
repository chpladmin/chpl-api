package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class CertificationStatusReviewerTest {
    private static final String FIRST_STATUS_NOT_ACTIVE_ERROR = "The earliest certification status for any listing on the CHPL must be Active.";

    private ListingMockUtil mockUtil = new ListingMockUtil();

    @Autowired
    private MessageSource messageSource;

    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private CertificationStatusReviewer certStatusReviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        certStatusReviewer = new CertificationStatusReviewer(msgUtil);
    }

    // Case: A valid active certification status is first
    @Test
    public void testValidFirstStatus_DoesNotHaveError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        certStatusReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(FIRST_STATUS_NOT_ACTIVE_ERROR));
    }

    // Case: An invalid first certification status
    @Test
    public void testRetiredFirstStatus_HasError() {
        Mockito.doReturn(FIRST_STATUS_NOT_ACTIVE_ERROR)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.firstStatusNotActive"),
                        ArgumentMatchers.anyString());

        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        // make a status older than the oldest status that is not Active
        CertificationStatusEvent currOldestStatus = listing.getOldestStatus();
        CertificationStatusEvent oldestStatus = new CertificationStatusEvent();
        oldestStatus.setEventDate(currOldestStatus.getEventDate() - 1);
        oldestStatus.setId(2L);
        oldestStatus.setLastModifiedDate(currOldestStatus.getLastModifiedDate() - 1);
        oldestStatus.setLastModifiedUser(-2L);
        oldestStatus.setReason(null);
        CertificationStatus status = new CertificationStatus();
        status.setId(2L);
        status.setName("Retired");
        oldestStatus.setStatus(status);
        listing.getCertificationEvents().add(oldestStatus);

        certStatusReviewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(FIRST_STATUS_NOT_ACTIVE_ERROR));
    }
}
