package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
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
import gov.healthit.chpl.dto.PendingCertifiedProductTargetedUserDTO;
import gov.healthit.chpl.listing.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class FieldLengthReviewerTest {

    private static final String BAD_LENGTH_PARTICIPANT_ID = "You have exceeded the max length, 20 characters, for the Participant Identifier ID This is more than twenty characters long..";
    private static final String BAD_LENGTH_TASK_ID = "You have exceeded the max length, 20 characters, for the Task Identifier with ID This is more than twenty characters long..";
    private static final String BAD_LENGTH_DEVELOPER_PHONE = "You have exceeded the max length, 100 characters, for the Developer Phone 00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000.";
    private static final String BAD_LENGTH_DEVELOPER_ZIP = "You have exceeded the max length, 25 characters, for the Developer Zip 20910209102091020910209102091020910209102091020910.";
    private static final String BAD_LENGTH_TARGETED_USER = "You have exceeded the max length, 300 characters, for the Targeted User This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long., which has been deleted.";

    @Autowired
    private FieldLengthReviewer fieldLengthReivewer;

    @Autowired
    private gov.healthit.chpl.validation.pendingListing.reviewer.FieldLengthReviewer fieldLengthReivewerPending;

    @Autowired
    private ListingMockUtil mockUtil;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testBadTestTask_HasErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        CertifiedProductSed sed = new CertifiedProductSed();
        TestTask tt = new TestTask();
        tt.setUniqueId("This is more than twenty characters long.");
        ArrayList<TestTask> tts = new ArrayList<TestTask>();
        tts.add(tt);
        sed.setTestTasks(tts);
        listing.setSed(sed);
        fieldLengthReivewer.review(listing);
        System.out.println(listing.getErrorMessages());
        assertTrue(listing.getErrorMessages().contains(BAD_LENGTH_TASK_ID));
    }

    @Test
    public void testBadTestParticipant_HasErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        CertifiedProductSed sed = new CertifiedProductSed();
        TestTask tt = new TestTask();
        tt.setUniqueId("participant");
        ArrayList<TestTask> tts = new ArrayList<TestTask>();
        tts.add(tt);
        sed.setTestTasks(tts);
        TestParticipant tp = new TestParticipant();
        tp.setUniqueId("This is more than twenty characters long.");
        Set<TestParticipant> tps = new HashSet<TestParticipant>();
        tps.add(tp);
        sed.getTestTasks().get(0).setTestParticipants(tps);
        listing.setSed(sed);
        fieldLengthReivewer.review(listing);
        System.out.println(listing.getErrorMessages());
        assertTrue(listing.getErrorMessages().contains(BAD_LENGTH_PARTICIPANT_ID));
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
    
    @Test
    public void testBadTargetedUserMaxLength_HasErrors_Pending() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        PendingCertifiedProductTargetedUserDTO tu = new PendingCertifiedProductTargetedUserDTO();
        tu.setName("This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.This name is way too long.");
        listing.getTargetedUsers().add(tu);
        fieldLengthReivewerPending.review(listing);
        assertTrue(listing.getWarningMessages().contains(BAD_LENGTH_TARGETED_USER));
        assertTrue(listing.getTargetedUsers().isEmpty());
    }
    
    @Test
    public void testTargetedUserMaxLength_HasNoErrors_Pending() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        PendingCertifiedProductTargetedUserDTO tu = new PendingCertifiedProductTargetedUserDTO();
        tu.setName("This name is not too long");
        listing.getTargetedUsers().add(tu);
        fieldLengthReivewerPending.review(listing);
        assertTrue(listing.getWarningMessages().isEmpty());
        assertTrue(listing.getTargetedUsers().contains(tu));
    }
}
