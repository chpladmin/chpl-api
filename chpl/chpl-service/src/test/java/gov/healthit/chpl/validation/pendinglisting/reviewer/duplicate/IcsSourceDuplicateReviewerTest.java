package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.IcsSourceDuplicateReviewer;

public class IcsSourceDuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate ICS Source: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private IcsSourceDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateIcsSource"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        reviewer = new IcsSourceDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        CertifiedProductDetailsDTO ics1 = new CertifiedProductDetailsDTO();
        ics1.setChplProductNumber("Chpl1");

        CertifiedProductDetailsDTO ics2 = new CertifiedProductDetailsDTO();
        ics2.setChplProductNumber("Chpl1");

        listing.getIcsParents().add(ics1);
        listing.getIcsParents().add(ics2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Chpl1")))
                .count());
        assertEquals(1, listing.getIcsParents().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        CertifiedProductDetailsDTO ics1 = new CertifiedProductDetailsDTO();
        ics1.setChplProductNumber("Chpl1");

        CertifiedProductDetailsDTO ics2 = new CertifiedProductDetailsDTO();
        ics2.setChplProductNumber("Chpl2");

        listing.getIcsParents().add(ics1);
        listing.getIcsParents().add(ics2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getIcsParents().size());
    }

    @Test
    public void review_emptyIcsSource_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.getIcsParents().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getIcsParents().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();

        CertifiedProductDetailsDTO ics1 = new CertifiedProductDetailsDTO();
        ics1.setChplProductNumber("Chpl1");

        CertifiedProductDetailsDTO ics2 = new CertifiedProductDetailsDTO();
        ics2.setChplProductNumber("Chpl2");

        CertifiedProductDetailsDTO ics3 = new CertifiedProductDetailsDTO();
        ics3.setChplProductNumber("Chpl1");

        CertifiedProductDetailsDTO ics4 = new CertifiedProductDetailsDTO();
        ics4.setChplProductNumber("Chpl4");

        listing.getIcsParents().add(ics1);
        listing.getIcsParents().add(ics2);
        listing.getIcsParents().add(ics3);
        listing.getIcsParents().add(ics4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Chpl1")))
                .count());
        assertEquals(3, listing.getIcsParents().size());
    }
}