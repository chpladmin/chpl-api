package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class InheritanceComparisonReviewerTest {
    private static final String REMOVED_CHILD = "Removing the ICS child %s is not allowed.";
    private ErrorMessageUtil msgUtil;

    private InheritanceComparisonReviewer reviewer;

    @Before
    public void before() {
        msgUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(msgUtil.getMessage(ArgumentMatchers.eq("listing.icsChildRemoved"), ArgumentMatchers.anyString()))
                .thenAnswer(i -> String.format(REMOVED_CHILD, i.getArgument(1), ""));
        reviewer = new InheritanceComparisonReviewer(msgUtil);
    }

    @Test
    public void review_nullIcsInCurrentOrUpdated_noErrors() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .ics(null)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .ics(null)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_nullIcsChildrenInCurrentOrUpdated_noErrors() {
        InheritedCertificationStatus ics = InheritedCertificationStatus.builder()
                .build();
        ics.setChildren(null);

        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .ics(ics)
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .ics(ics)
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_emptyIcsChildrenInCurrentOrUpdated_noErrors() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(new ArrayList<CertifiedProduct>())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(new ArrayList<CertifiedProduct>())
                        .build())
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_currentIcsEmptyChildrenUpdatedIcsOneChild_noErrors() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(new ArrayList<CertifiedProduct>())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .certificationDate(System.currentTimeMillis())
                                .certificationStatus(CertificationStatusType.Active.getName())
                                .curesUpdate(false)
                                .chplProductNumber("CHP-12345")
                                .build()).toList())
                        .build())
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_currentIcsMatchesUpdatedIcsOneChild_noErrors() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .certificationDate(System.currentTimeMillis())
                                .certificationStatus(CertificationStatusType.Active.getName())
                                .curesUpdate(false)
                                .chplProductNumber("CHP-12345")
                                .build()).toList())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .certificationDate(System.currentTimeMillis())
                                .certificationStatus(CertificationStatusType.Active.getName())
                                .curesUpdate(false)
                                .chplProductNumber("CHP-12345")
                                .build()).toList())
                        .build())
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(0, updatedListing.getErrorMessages().size());
    }

    @Test
    public void review_currentIcsOneChildUpdatedIcsEmptyChildren_hasError() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .certificationDate(System.currentTimeMillis())
                                .certificationStatus(CertificationStatusType.Active.getName())
                                .curesUpdate(false)
                                .chplProductNumber("CHP-12345")
                                .build()).toList())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(new ArrayList<CertifiedProduct>())
                        .build())
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertTrue(updatedListing.getErrorMessages().contains(String.format(REMOVED_CHILD, "CHP-12345")));
    }

    @Test
    public void review_currentIcsOneChildUpdatedIcsDifferentChild_hasError() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .certificationDate(System.currentTimeMillis())
                                .certificationStatus(CertificationStatusType.Active.getName())
                                .curesUpdate(false)
                                .chplProductNumber("CHP-12345")
                                .build()).toList())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(Stream.of(CertifiedProduct.builder()
                                .id(2L)
                                .certificationDate(System.currentTimeMillis())
                                .certificationStatus(CertificationStatusType.Active.getName())
                                .curesUpdate(false)
                                .chplProductNumber("CHP-12346")
                                .build()).toList())
                        .build())
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertTrue(updatedListing.getErrorMessages().contains(String.format(REMOVED_CHILD, "CHP-12345")));
    }

    @Test
    public void review_currentIcsTwoChildrenUpdatedIcsOneChild_hasError() {
        CertifiedProductSearchDetails existingListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(Stream.of(
                                CertifiedProduct.builder()
                                    .id(1L)
                                    .certificationDate(System.currentTimeMillis())
                                    .certificationStatus(CertificationStatusType.Active.getName())
                                    .curesUpdate(false)
                                    .chplProductNumber("CHP-12345")
                                    .build(),
                                CertifiedProduct.builder()
                                    .id(2L)
                                    .certificationDate(System.currentTimeMillis())
                                    .certificationStatus(CertificationStatusType.Active.getName())
                                    .curesUpdate(false)
                                    .chplProductNumber("CHP-12346")
                                    .build()).toList())
                        .build())
                .build();
        CertifiedProductSearchDetails updatedListing = CertifiedProductSearchDetails.builder()
                .ics(InheritedCertificationStatus.builder()
                        .children(Stream.of(CertifiedProduct.builder()
                                .id(2L)
                                .certificationDate(System.currentTimeMillis())
                                .certificationStatus(CertificationStatusType.Active.getName())
                                .curesUpdate(false)
                                .chplProductNumber("CHP-12346")
                                .build()).toList())
                        .build())
                .build();

        reviewer.review(existingListing, updatedListing);
        assertEquals(1, updatedListing.getErrorMessages().size());
        assertTrue(updatedListing.getErrorMessages().contains(String.format(REMOVED_CHILD, "CHP-12345")));
    }
}
