package gov.healthit.chpl.validation.listing.reviewer;

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
import gov.healthit.chpl.listing.ListingMockUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Tests URLs to ensure the URLs have no new lines and look like an URL.
 * @author alarned
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class UrlReviewerTest {
    private static final String URL_WITH_NEWLINE = "http://fake.example.com\nhttp://fake2.example.com";
    private static final String BAD_REPORT_FILE_LOCATION_ERROR =
            "The value for Report File Location, '" + URL_WITH_NEWLINE + "', is not a valid URL";

    @Autowired private ListingMockUtil mockUtil;

//    @Spy private CertifiedProductManager cpManager;
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
        listing = mockUtil.createValid2015Listing();
    }

    /**
     * Given the report file location has new lines
     * when the validator runs
     * then there should be an error.
     * OCD-744
     */
    @Test
    public void testValidReportFileLocationUrlHasNewLine() {
        Mockito.doReturn(BAD_REPORT_FILE_LOCATION_ERROR)
        .when(msgUtil).getMessage(eq("listing.invalidUrlFound"), anyString());
        listing.setReportFileLocation(URL_WITH_NEWLINE);
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
    public void testValidReportFileLocationUrlHasProperShape() {
        Mockito.doReturn("The value for Report File Location, 'not an url', is not a valid URL.")
        .when(msgUtil).getMessage(eq("listing.invalidUrlFound"), anyString());
        listing.setReportFileLocation("not an url");
        urlReviewer.review(listing);
        assertTrue(listing.getErrorMessages()
                .contains("The value for Report File Location, 'not an url', is not a valid URL."));
    }
}
