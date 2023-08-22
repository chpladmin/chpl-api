package gov.healthit.chpl.validation.listing.reviewer.duplicate;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

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
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.duplicateIcsSource"),
                ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(ERR_MSG, i.getArgument(1), ""));
        reviewer = new IcsSourceDuplicateReviewer(msgUtil);
    }

    @Test
    public void review_duplicateExists_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .parents(Stream.of(
                                CertifiedProduct.builder()
                                    .chplProductNumber("Chpl1")
                                    .build(),
                                CertifiedProduct.builder()
                                    .chplProductNumber("Chpl1")
                                    .build()).toList())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Chpl1")))
                .count());
        assertEquals(1, listing.getIcs().getParents().size());
    }

    @Test
    public void review_noDuplicates_noWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .parents(Stream.of(
                                CertifiedProduct.builder()
                                    .chplProductNumber("Chpl1")
                                    .build(),
                                CertifiedProduct.builder()
                                    .chplProductNumber("Chpl2")
                                    .build()).toList())
                        .build())
                .build();
        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(2, listing.getIcs().getParents().size());
    }

    @Test
    public void review_emptyIcsSource_noWarning() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .parents(new ArrayList<CertifiedProduct>())
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(0, listing.getWarningMessages().size());
        assertEquals(0, listing.getIcs().getParents().size());
    }

    @Test
    public void review_duplicateExistsInLargeSet_warningFoundAndDuplicateRemoved() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .parents(Stream.of(
                                CertifiedProduct.builder()
                                    .chplProductNumber("Chpl1")
                                    .build(),
                                CertifiedProduct.builder()
                                    .chplProductNumber("Chpl2")
                                    .build(),
                                CertifiedProduct.builder()
                                    .chplProductNumber("Chpl1")
                                    .build(),
                                CertifiedProduct.builder()
                                    .chplProductNumber("Chpl4")
                                    .build()).toList())
                        .build())
                .build();

        reviewer.review(listing);

        assertEquals(1, listing.getWarningMessages().size());
        assertEquals(1, listing.getWarningMessages().stream()
                .filter(warning -> warning.equals(String.format(ERR_MSG, "Chpl1")))
                .count());
        assertEquals(3, listing.getIcs().getParents().size());
    }
}