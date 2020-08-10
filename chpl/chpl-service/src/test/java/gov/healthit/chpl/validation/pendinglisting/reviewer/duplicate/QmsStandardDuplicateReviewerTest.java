package gov.healthit.chpl.validation.pendinglisting.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductQmsStandardDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.QmsStandardDuplicateReviewer;

public class QmsStandardDuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate QMS Standard: '%s', Criteria: '%s', Modification: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private QmsStandardDuplicateReviewer reviewer;

    @Before
    public void setup() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateQmsStandard"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new QmsStandardDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductQmsStandardDTO qms1 = getQmsStandard(1L, "Qms1", "Crit1", null);
        PendingCertifiedProductQmsStandardDTO qms2 = getQmsStandard(1L, "Qms1", "Crit1", null);
        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Qms1", "Crit1", "")))
                .count());
        assertEquals(1, listing.getQmsStandards().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductQmsStandardDTO qms1 = getQmsStandard(1L, "Qms1", "Crit1", null);
        PendingCertifiedProductQmsStandardDTO qms2 = getQmsStandard(2L, "Qms2", "Crit1", null);
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
    public void testDuplicateExistInLargerSet() {
        PendingCertifiedProductDTO listing = new PendingCertifiedProductDTO();
        PendingCertifiedProductQmsStandardDTO qms1 = getQmsStandard(1L, "Qms1", "Crit1", null);
        PendingCertifiedProductQmsStandardDTO qms2 = getQmsStandard(2L, "Qms2", "Crit1", null);
        PendingCertifiedProductQmsStandardDTO qms3 = getQmsStandard(1L, "Qms1", "Crit1", null);
        PendingCertifiedProductQmsStandardDTO qms4 = getQmsStandard(null, "new QMS", "Crit1", null);
        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);
        listing.getQmsStandards().add(qms3);
        listing.getQmsStandards().add(qms4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Qms1", "Crit1", "")))
                .count());
        assertEquals(3, listing.getQmsStandards().size());
    }

    private PendingCertifiedProductQmsStandardDTO getQmsStandard(Long id, String name, String criteria, String modification) {
        PendingCertifiedProductQmsStandardDTO qms = new PendingCertifiedProductQmsStandardDTO();
        qms.setQmsStandardId(id);
        qms.setName(name);
        qms.setModification(modification);
        qms.setApplicableCriteria(criteria);
        return qms;
    }
}