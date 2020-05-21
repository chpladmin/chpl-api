package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.util.ErrorMessageUtil;

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
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setIcs(new InheritedCertificationStatus());

        CertifiedProduct ics1 = new CertifiedProduct();
        ics1.setChplProductNumber("Chpl1");

        CertifiedProduct ics2 = new CertifiedProduct();
        ics2.setChplProductNumber("Chpl1");

        listing.getIcs().getParents().add(ics1);
        listing.getIcs().getParents().add(ics2);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Chpl1")))
                .count());
        assertEquals(1, listing.getIcs().getParents().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setIcs(new InheritedCertificationStatus());

        CertifiedProduct ics1 = new CertifiedProduct();
        ics1.setChplProductNumber("Chpl1");

        CertifiedProduct ics2 = new CertifiedProduct();
        ics2.setChplProductNumber("Chpl2");

        listing.getIcs().getParents().add(ics1);
        listing.getIcs().getParents().add(ics2);

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getIcs().getParents().size());
    }

    @Test
    public void review_emptyIcsSource_noWarning() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setIcs(new InheritedCertificationStatus());
        listing.getIcs().getParents().clear();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getIcs().getParents().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = new CertifiedProductSearchDetails();
        listing.setIcs(new InheritedCertificationStatus());

        CertifiedProduct ics1 = new CertifiedProduct();
        ics1.setChplProductNumber("Chpl1");

        CertifiedProduct ics2 = new CertifiedProduct();
        ics2.setChplProductNumber("Chpl2");

        CertifiedProduct ics3 = new CertifiedProduct();
        ics3.setChplProductNumber("Chpl1");

        CertifiedProduct ics4 = new CertifiedProduct();
        ics4.setChplProductNumber("Chpl4");

        listing.getIcs().getParents().add(ics1);
        listing.getIcs().getParents().add(ics2);
        listing.getIcs().getParents().add(ics3);
        listing.getIcs().getParents().add(ics4);

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Chpl1")))
                .count());
        assertEquals(3, listing.getIcs().getParents().size());
    }
}