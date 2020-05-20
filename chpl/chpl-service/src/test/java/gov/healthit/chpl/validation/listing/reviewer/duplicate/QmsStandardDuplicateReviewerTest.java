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

public class QmsStandardDuplicateReviewerTest {
    private static final String DUPQMS_MSG =
            "Listing contains duplicate QMS Standard: '%s', Criteria: '%s', Modification: '%s'. The duplicates have been removed.";

    private ErrorMessageUtil msgUtil;
    private QmsStandardDuplicateReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateQmsStandard"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(DUPQMS_MSG, i.getArgument(1), i.getArgument(2), i.getArgument(3)));
        reviewer = new QmsStandardDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertifiedProductQmsStandard qms1 = getQmsStandard("Qms1", "Crit1", "mod");
        CertifiedProductQmsStandard qms2 = getQmsStandard("Qms1", "Crit1", "mod");
        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPQMS_MSG, "Qms1", "Crit1", "mod")))
                .count());
        assertEquals(1, listing.getQmsStandards().size());
    }

    @Test
    public void review_duplicateExists_noMod_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertifiedProductQmsStandard qms1 = getQmsStandard("Qms1", "Crit1", null);
        CertifiedProductQmsStandard qms2 = getQmsStandard("Qms1", "Crit1", null);
        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPQMS_MSG, "Qms1", "Crit1", "")))
                .count());
        assertEquals(1, listing.getQmsStandards().size());
    }

    @Test
    public void review_duplicateExists_noCriteriaNoMod_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertifiedProductQmsStandard qms1 = getQmsStandard("Qms1", null, null);
        CertifiedProductQmsStandard qms2 = getQmsStandard("Qms1", null, null);
        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPQMS_MSG, "Qms1", "", "")))
                .count());
        assertEquals(1, listing.getQmsStandards().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        CertifiedProductQmsStandard qms1 = getQmsStandard("Qms1", "Crit1", null);
        CertifiedProductQmsStandard qms2 = getQmsStandard("Qms2", null, null);

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
        CertifiedProductQmsStandard qms1 = getQmsStandard("Qms1", "Crit1", null);
        CertifiedProductQmsStandard qms2 = getQmsStandard("Qms2", "Crit1", null);
        CertifiedProductQmsStandard qms3 = getQmsStandard("Qms1", "Crit1", null);
        CertifiedProductQmsStandard qms4 = getQmsStandard("Qms3", "Crit3", null);

        listing.getQmsStandards().add(qms1);
        listing.getQmsStandards().add(qms2);
        listing.getQmsStandards().add(qms3);
        listing.getQmsStandards().add(qms4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(DUPQMS_MSG, "Qms1", "Crit1", "")))
                .count());
        assertEquals(3, listing.getQmsStandards().size());
    }

    private CertifiedProductQmsStandard getQmsStandard(String name, String applicableCriteria, String modification) {
        CertifiedProductQmsStandard qms = new CertifiedProductQmsStandard();
        qms.setQmsStandardName(name);
        qms.setApplicableCriteria(applicableCriteria);
        qms.setQmsModification(modification);
        return qms;
    }
}