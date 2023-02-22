package gov.healthit.chpl.scheduler.job.ics.reviewer;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.InheritedCertificationStatus;

public class IcsWithoutParentsReviewerTest {
    private static final String ERROR_MESSAGE = "Listing should have ICS Parents.";

    private IcsWithoutParentsReviewer reviewer;

    @Before
    @SuppressWarnings("checkstyle:magicnumber")
    public void setup() {
        reviewer = new IcsWithoutParentsReviewer(ERROR_MESSAGE);
    }

    @Test
    public void review_listingWithoutIcs_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(1L)
                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(false)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }

    @Test
    public void review_listingWithIcs_noParents_hasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNotNull(errorMessage);
        assertEquals(ERROR_MESSAGE, errorMessage);
    }

    @Test
    public void review_listingWithIcs_hasParents_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .id(2L)
                .chplProductNumber("15.02.05.1439.A111.01.01.1.200219")
                .ics(InheritedCertificationStatus.builder()
                        .inherits(true)
                        .parents(Stream.of(CertifiedProduct.builder()
                                .id(1L)
                                .chplProductNumber("15.02.05.1439.A111.01.00.1.200219")
                                .build()).toList())
                        .build())
                .build();
        String errorMessage = reviewer.getIcsError(listing);
        assertNull(errorMessage);
    }
}
