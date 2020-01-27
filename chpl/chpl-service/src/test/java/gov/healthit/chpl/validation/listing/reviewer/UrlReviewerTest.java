package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;

/**
 * Tests URLs to ensure the URLs have no new lines and look like an URL.
 * @author alarned
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class UrlReviewerTest {
    private static final String BAD_REPORT_FILE_LOCATION_ERROR = "Fake error message";

    @Autowired private ListingMockUtil mockUtil;

    @Mock private ErrorMessageUtil msgUtil;

    @InjectMocks
    private UrlReviewer urlReviewer;

    private CertifiedProductSearchDetails listing;

    /**
     * Initialize mocks.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(BAD_REPORT_FILE_LOCATION_ERROR)
        .when(msgUtil).getMessage(eq("listing.invalidUrlFound"), anyString());
        listing = mockUtil.createValid2015Listing();
    }

    /**
     * Given the report file location has new lines
     * when the validator runs
     * then there should be an error.
     * OCD-744
     */
    @Test
    public void testWhenReportFileLocationUrlHasNewLine() {
        String url = "http://fake.example.com\nhttp://fake2.example.com";
        listing.setReportFileLocation(url);
        urlReviewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(BAD_REPORT_FILE_LOCATION_ERROR));
    }

    /**
     * Given the report file location has an invalid url
     * when the validator runs
     * then there should be an error.
     * OCD-744
     */
    @Test
    public void testWhenReportFileLocationUrlHasProperShape() {
        String url = "not a valid url";
        listing.setReportFileLocation(url);
        urlReviewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(BAD_REPORT_FILE_LOCATION_ERROR));
    }

    /**
     * Given the report file location has an empty URL
     * when the validator runs
     * then there should not be an error.
     *
     * A different validator is checking for required elements.
     * OCD-744
     */
    @Test
    public void testWhenReportFileLocationUrlIsEmpty() {
        String url = "";
        listing.setReportFileLocation(url);
        urlReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(BAD_REPORT_FILE_LOCATION_ERROR));
    }

    /**
     * Given the report file location has a null URL
     * when the validator runs
     * then there should not be an error.
     *
     * A different validator is checking for required elements.
     * OCD-744
     */
    @Test
    public void testWhenReportFileLocationUrlIsNull() {
        String url = null;
        listing.setReportFileLocation(url);
        urlReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(BAD_REPORT_FILE_LOCATION_ERROR));
    }
}
