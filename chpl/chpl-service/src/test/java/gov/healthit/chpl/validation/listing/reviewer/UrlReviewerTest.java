package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

public class UrlReviewerTest {
    private static final String BAD_URL = "The value for %s is not a valid URL.";

    private ErrorMessageUtil errorMessageUtil;
    private ResourcePermissions resourcePermissions;
    private ValidationUtils validationUtils;
    private UrlReviewer reviewer;

    @Before
    public void setup() {
        validationUtils = new ValidationUtils();
        resourcePermissions = Mockito.mock(ResourcePermissions.class);
        errorMessageUtil = Mockito.mock(ErrorMessageUtil.class);

        Mockito.when(errorMessageUtil.getMessage(ArgumentMatchers.eq("listing.invalidUrlFound"), ArgumentMatchers.any()))
        .thenAnswer(i -> String.format(BAD_URL, i.getArgument(1), ""));
        reviewer = new UrlReviewer(validationUtils, errorMessageUtil, resourcePermissions);
    }

    @Test
    public void review_nullSvapNoticeUrl_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .svapNoticeUrl(null)
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_emptySvapNoticeUrl_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .svapNoticeUrl("")
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validSvapNoticeUrl_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .svapNoticeUrl("http://www.test.com")
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_validEncodedSvapNoticeUrl_noError() {
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .svapNoticeUrl("http://www.test.com/%20documents~file.pdf")
                .build();

        reviewer.review(listing);
        assertEquals(0, listing.getErrorMessages().size());
    }

    @Test
    public void review_svapNoticeUrlWithNewlineAtEnd_noError() {
        String url = "http://www.test.com/documents.pdf\n";
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .svapNoticeUrl(url)
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_URL, "SVAP Notice URL '" + url + "'")));
    }

    @Test
    public void review_svapNoticeUrlWithNewlineInMiddle_noError() {
        String url = "http://www.test.com/docum\nents.pdf";
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .svapNoticeUrl(url)
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_URL, "SVAP Notice URL '" + url + "'")));
    }

    @Test
    public void review_svapNoticeUrlWithInvalidScheme_noError() {
        String url = "httr://www.test.com";
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .svapNoticeUrl(url)
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_URL, "SVAP Notice URL '" + url + "'")));
    }

    @Test
    public void review_svapNoticeUrlWithInvalidCharacters_noError() {
        String url = "httr://www.te@$^*st.com";
        CertifiedProductSearchDetails listing = CertifiedProductSearchDetails.builder()
                .svapNoticeUrl(url)
                .build();

        reviewer.review(listing);
        assertEquals(1, listing.getErrorMessages().size());
        assertTrue(listing.getErrorMessages().contains(String.format(BAD_URL, "SVAP Notice URL '" + url + "'")));
    }
}
