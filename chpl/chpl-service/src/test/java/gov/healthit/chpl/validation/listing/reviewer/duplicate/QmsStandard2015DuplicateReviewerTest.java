package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertifiedProductQmsStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate.QmsStandard2015DuplicateReviewer;

public class QmsStandard2015DuplicateReviewerTest {
    private static final String ERR_MSG =
            "Listing contains duplicate QMS Standard: '%s, Criteria: %s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private QmsStandard2015DuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateQmsStandard.2015"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), i.getArgument(2)));
        reviewer = new QmsStandard2015DuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductQmsStandard qms1 = new CertifiedProductQmsStandard();
        qms1.setQmsStandardName("Qms1");
        qms1.setApplicableCriteria("Crit1");

        CertifiedProductQmsStandard qms2 = new CertifiedProductQmsStandard();
        qms2.setQmsStandardName("Qms1");
        qms2.setApplicableCriteria("Crit1");

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Qms1", "Crit1")))
                .count());
        assertEquals(1, listing.getQmsStandards().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductQmsStandard qms1 = new CertifiedProductQmsStandard();
        qms1.setQmsStandardName("Qms1");
        qms1.setApplicableCriteria("Crit1");

        CertifiedProductQmsStandard qms2 = new CertifiedProductQmsStandard();
        qms2.setQmsStandardName("Qms2");
        qms2.setApplicableCriteria("Crit1");

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getQmsStandards().size());
    }

    @Test
    public void review_emptyQmsStandards_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.getQmsStandards().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getQmsStandards().size());
    }

    @Test
    public void testDuplicateExistInLargerSet() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();

        CertifiedProductQmsStandard qms1 = new CertifiedProductQmsStandard();
        qms1.setQmsStandardName("Qms1");
        qms1.setApplicableCriteria("Crit1");

        CertifiedProductQmsStandard qms2 = new CertifiedProductQmsStandard();
        qms2.setQmsStandardName("Qms2");
        qms2.setApplicableCriteria("Crit1");

        CertifiedProductQmsStandard qms3 = new CertifiedProductQmsStandard();
        qms3.setQmsStandardName("Qms1");
        qms3.setApplicableCriteria("Crit1");

        CertifiedProductQmsStandard qms4 = new CertifiedProductQmsStandard();
        qms4.setQmsStandardName("Qms3");
        qms4.setApplicableCriteria("Crit3");

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);
        listing.getQmsStandards().add(qms3);
        listing.getQmsStandards().add(qms4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Qms1", "Crit1")))
                .count());
        assertEquals(3, listing.getQmsStandards().size());
    }
}