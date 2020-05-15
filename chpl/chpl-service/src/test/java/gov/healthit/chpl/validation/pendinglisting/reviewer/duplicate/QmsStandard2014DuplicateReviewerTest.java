package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.duplicate.QmsStandard2014DuplicateReviewer;

public class QmsStandard2014DuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate QMS Standard: '%s. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private QmsStandard2014DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateQmsStandard.2014"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        reviewer = new QmsStandard2014DuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductQmsStandardDTO qms1 = new PendingCertifiedProductQmsStandardDTO();
        qms1.setName("Qms1");

        PendingCertifiedProductQmsStandardDTO qms2 = new PendingCertifiedProductQmsStandardDTO();
        qms2.setName("Qms1");

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Qms1")))
                .count());
        assertEquals(1, listing.getQmsStandards().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductQmsStandardDTO qms1 = new PendingCertifiedProductQmsStandardDTO();
        qms1.setName("Qms1");

        PendingCertifiedProductQmsStandardDTO qms2 = new PendingCertifiedProductQmsStandardDTO();
        qms2.setName("Qms2");

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getQmsStandards().size());
    }

    @Test
    public void review_emptyQmsStandards_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.getQmsStandards().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getQmsStandards().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        PendingCertifiedProductQmsStandardDTO qms1 = new PendingCertifiedProductQmsStandardDTO();
        qms1.setName("Qms1");

        PendingCertifiedProductQmsStandardDTO qms2 = new PendingCertifiedProductQmsStandardDTO();
        qms2.setName("Qms2");

        PendingCertifiedProductQmsStandardDTO qms3 = new PendingCertifiedProductQmsStandardDTO();
        qms3.setName("Qms1");

        PendingCertifiedProductQmsStandardDTO qms4 = new PendingCertifiedProductQmsStandardDTO();
        qms4.setName("Qms3");

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);
        listing.getQmsStandards().add(qms3);
        listing.getQmsStandards().add(qms4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Qms1")))
                .count());
        assertEquals(3, listing.getQmsStandards().size());
    }
}