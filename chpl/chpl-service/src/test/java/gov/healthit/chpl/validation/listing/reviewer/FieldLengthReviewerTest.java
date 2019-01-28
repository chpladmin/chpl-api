package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.listing.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class FieldLengthReviewerTest {

    private static final String BAD_LENGTH_DEVELOPER_PHONE = "You have exceeded the max length, 100 characters, for the Developer Phone 00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.";
    private static final String BAD_LENGTH_DEVELOPER_ZIP = "You have exceeded the max length, 25 characters, for the Developer Zip 20910209102091020910209102091020910209102091020910.";

    @Autowired
    private gov.healthit.chpl.validation.pendingListing.reviewer.FieldLengthReviewer fieldLengthReivewerPending;

    @Autowired
    private ListingMockUtil mockUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBadDeveloperMaxLength_HasErrors_Pending() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        listing.setDeveloperPhoneNumber(
                "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
        listing.setDeveloperZipCode("20910209102091020910209102091020910209102091020910");
        fieldLengthReivewerPending.review(listing);
        assertTrue(listing.getErrorMessages().contains(BAD_LENGTH_DEVELOPER_PHONE));
        assertTrue(listing.getErrorMessages().contains(BAD_LENGTH_DEVELOPER_ZIP));
    }
}
