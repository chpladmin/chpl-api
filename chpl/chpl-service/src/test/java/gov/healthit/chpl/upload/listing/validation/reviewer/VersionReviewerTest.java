package gov.healthit.chpl.upload.listing.validation.reviewer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ProductVersion;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class VersionReviewerTest {
    private static final String MISSING_VERSION = "A product version is required.";
    private ErrorMessageUtil errorMessageUtil;
    private VersionReviewer reviewer;

    @Before
    public void setup() {
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);
        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.missingVersion")))
            .thenReturn(MISSING_VERSION);
        reviewer = new VersionReviewer(errorMessageUtil);
    }

    @Test
    public void review_versionMissing_HasError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(MISSING_VERSION));
    }

    @Test
    public void review_versionExists_NoError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .version(ProductVersion.builder()
                        .version("1.1.1")
                        .build())
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }
}
