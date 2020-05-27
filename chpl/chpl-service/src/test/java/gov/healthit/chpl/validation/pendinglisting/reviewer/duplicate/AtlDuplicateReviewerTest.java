package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductTestingLabDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.AtlDuplicateReviewer;

public class AtlDuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate Testing Lab: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private AtlDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateTestingLab"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        reviewer = new AtlDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductTestingLabDTO atl1 = getTestingLab(1L, "Atl1");
        PendingCertifiedProductTestingLabDTO atl2 = getTestingLab(1L, "Atl1");
        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Atl1")))
                .count());
        assertEquals(1, listing.getTestingLabs().size());
    }

    @Test
    public void review_duplicateExistsNullId_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductTestingLabDTO atl1 = getTestingLab(null, "Atl1");
        PendingCertifiedProductTestingLabDTO atl2 = getTestingLab(null, "Atl1");
        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Atl1")))
                .count());
        assertEquals(1, listing.getTestingLabs().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductTestingLabDTO atl1 = getTestingLab(1L, "Atl1");
        PendingCertifiedProductTestingLabDTO atl2 = getTestingLab(2L, "Atl1");

        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getTestingLabs().size());
    }

    @Test
    public void review_emptyTestingLabs_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        listing.getTestingLabs().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getTestingLabs().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductTestingLabDTO atl1 = getTestingLab(1L, "Atl1");
        PendingCertifiedProductTestingLabDTO atl2 = getTestingLab(2L, "Atl2");
        PendingCertifiedProductTestingLabDTO atl3 = getTestingLab(1L, "Atl1");
        PendingCertifiedProductTestingLabDTO atl4 = getTestingLab(3L, "Atl3");

        listing.getTestingLabs().add(atl1);
        listing.getTestingLabs().add(atl2);
        listing.getTestingLabs().add(atl3);
        listing.getTestingLabs().add(atl4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Atl1")))
                .count());
        assertEquals(3, listing.getTestingLabs().size());
    }

    private PendingCertifiedProductTestingLabDTO getTestingLab(Long id, String name) {
        PendingCertifiedProductTestingLabDTO atl = new PendingCertifiedProductTestingLabDTO();
        atl.setTestingLabId(id);
        atl.setTestingLabName(name);
        return atl;
    }
}