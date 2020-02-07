package old.gov.healthit.chpl.validation.pendingListing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.TestToolReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.AmbulatoryRequiredTestToolReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.TestTool2015Reviewer;
import old.gov.healthit.chpl.util.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class PendingListingTestToolReviewerTest {
    private static final String B_2 = "170.314 (b)(2)";
    private static final String G_1 = "170.314 (g)(1)";
    private static final String G_2 = "170.314 (g)(2)";
    private static final String F_3 = "170.314 (f)(3)";
    private static final String NO_TEST_TOOL_ERROR = "Test tools are required for certification criteria " + B_2 + ".";
    private static final String NO_TEST_TOOL_NAME_ERROR = "There was no test tool name found for certification " + B_2
            + ".";
    private static final String NO_TEST_TOOL_VERSION_ERROR = "There was no version found for test tool Bogus Test Tool and certification "
            + B_2 + ".";
    private static final String TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR = "Criteria " + B_2
            + " contains an invalid test tool 'Bogus Test Tool'. It has been removed from the pending listing.";
    private static final String RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR = "Test Tool 'Bogus Test Tool' can not be used for criteria '"
            + B_2 + "', as it is a retired tool.";

    @Autowired
    private ListingMockUtil mockUtil;

    @Autowired
    private MessageSource messageSource;

    @Spy
    private TestToolDAO testToolDao;
    @Spy
    private ChplProductNumberUtil productNumberUtil;
    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);
    @Spy
    private CertificationResultRules certRules;

    private TestToolReviewer testToolReviewer;
    private TestTool2015Reviewer testTool2015Reviewer;
    private AmbulatoryRequiredTestToolReviewer ambulatoryTestToolReviewier;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        testToolReviewer = new TestToolReviewer(testToolDao, msgUtil, productNumberUtil);
        testTool2015Reviewer = new TestTool2015Reviewer(msgUtil);
        ambulatoryTestToolReviewier = new AmbulatoryRequiredTestToolReviewer(msgUtil, certRules);

        Mockito.doReturn(NO_TEST_TOOL_ERROR).when(msgUtil)
                .getMessage(ArgumentMatchers.eq("listing.criteria.missingTestTool"), ArgumentMatchers.anyString());
        Mockito.doReturn(NO_TEST_TOOL_NAME_ERROR).when(msgUtil)
                .getMessage(ArgumentMatchers.eq("listing.criteria.missingTestToolName"), ArgumentMatchers.anyString());
        Mockito.doReturn(NO_TEST_TOOL_VERSION_ERROR).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.missingTestToolVersion"), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
        Mockito.doReturn(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.retiredTestToolNoIcsNotAllowed"), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
        Mockito.doReturn(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.testToolNotFoundAndRemoved"), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
        Mockito.doReturn(false).when(certRules).hasCertOption(ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(CertificationResultRules.GAP));
        Mockito.doReturn(false).when(certRules).hasCertOption(ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED));
        Mockito.doReturn(true).when(certRules).hasCertOption(ArgumentMatchers.eq(G_1),
                ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED));
        Mockito.doReturn(true).when(certRules).hasCertOption(ArgumentMatchers.eq(G_2),
                ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED));
        Mockito.doReturn(true).when(certRules).hasCertOption(ArgumentMatchers.eq(F_3),
                ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED));
        Mockito.doReturn(true).when(certRules).hasCertOption(ArgumentMatchers.eq(B_2),
                ArgumentMatchers.eq(CertificationResultRules.TEST_TOOLS_USED));
        TestToolDTO testTool = new TestToolDTO();
        testTool.setId(1L);
        testTool.setName("Direct Certificate Discovery Tool");
        testTool.setRetired(false);
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Direct Certificate Discovery Tool")))
                .thenReturn(testTool);
    }

    @Test
    public void testNoTestTools_NoError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getTestTools() != null && certResult.getTestTools().size() > 0) {
                certResult.getTestTools().clear();
            }
        }
        testToolReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertFalse(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertFalse(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_VERSION_ERROR));
    }

    @Test
    public void testValidTestTool_NoError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getCriterion().getNumber().equals(B_2)) {
                certResult.getTestTools().clear();
                PendingCertificationResultTestToolDTO crtt = new PendingCertificationResultTestToolDTO();
                crtt.setId(1L);
                crtt.setTestToolId(1L);
                crtt.setName("Bogus Test Tool");
                crtt.setVersion("1.0.0");
                certResult.getTestTools().add(crtt);
            }
        }

        TestToolDTO testTool = createBogusTestTool(false);
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Bogus Test Tool"))).thenReturn(testTool);
        testToolReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertFalse(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertFalse(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_VERSION_ERROR));
    }

    @Test
    public void testMissingTestToolVersion_HasError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getCriterion().getNumber().equals(B_2)) {
                certResult.getTestTools().clear();
                PendingCertificationResultTestToolDTO crtt = new PendingCertificationResultTestToolDTO();
                crtt.setId(1L);
                crtt.setTestToolId(1L);
                crtt.setName("Bogus Test Tool");
                crtt.setVersion(null);
                certResult.getTestTools().add(crtt);
            }
        }

        TestToolDTO testTool = createBogusTestTool(false);
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Bogus Test Tool"))).thenReturn(testTool);
        testTool2015Reviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertFalse(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertFalse(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
        assertTrue(listing.getErrorMessages().contains(NO_TEST_TOOL_VERSION_ERROR));
    }

    @Test
    public void testMissingTestToolName_HasError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getCriterion().getNumber().equals(B_2)) {
                certResult.getTestTools().clear();
                PendingCertificationResultTestToolDTO crtt = new PendingCertificationResultTestToolDTO();
                crtt.setId(1L);
                crtt.setTestToolId(1L);
                crtt.setName(null);
                crtt.setVersion("1.0.0");
                certResult.getTestTools().add(crtt);
            }
        }
        testToolReviewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertFalse(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertFalse(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_VERSION_ERROR));
    }

    @Test
    public void testBadTestToolName_HasError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getCriterion().getNumber().equals(B_2)) {
                certResult.getTestTools().clear();
                PendingCertificationResultTestToolDTO crtt = new PendingCertificationResultTestToolDTO();
                crtt.setId(1L);
                crtt.setTestToolId(1L);
                crtt.setName("Bogus Test Tool");
                crtt.setVersion("1.0.0");
                certResult.getTestTools().add(crtt);
            }
        }
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Bogus Test Tool"))).thenReturn(null);
        testToolReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertTrue(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertFalse(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_VERSION_ERROR));
    }

    @Test
    public void testListingWithoutIcsAndRetiredTestTool_HasError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getCriterion().getNumber().equals(B_2)) {
                certResult.getTestTools().clear();
                PendingCertificationResultTestToolDTO crtt = new PendingCertificationResultTestToolDTO();
                crtt.setId(1L);
                crtt.setTestToolId(1L);
                crtt.setName("Bogus Test Tool");
                crtt.setVersion("1.0.0");
                certResult.getTestTools().add(crtt);
            }
        }

        TestToolDTO testTool = createBogusTestTool(true);
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Bogus Test Tool"))).thenReturn(testTool);
        testToolReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertFalse(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertTrue(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_VERSION_ERROR));
    }

    @Test
    public void testListingWithIcsConflictAndRetiredTestTool_HasWarning() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        listing.setIcs(Boolean.TRUE);
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getCriterion().getNumber().equals(B_2)) {
                certResult.getTestTools().clear();
                PendingCertificationResultTestToolDTO crtt = new PendingCertificationResultTestToolDTO();
                crtt.setId(1L);
                crtt.setTestToolId(1L);
                crtt.setName("Bogus Test Tool");
                crtt.setVersion("1.0.0");
                certResult.getTestTools().add(crtt);
            }
        }

        TestToolDTO testTool = createBogusTestTool(true);
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Bogus Test Tool"))).thenReturn(testTool);
        testToolReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertFalse(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertTrue(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
        assertFalse(listing.getWarningMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
    }

    @Test
    public void testListingWithIcsAndRetiredTestTool_HasNoError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        listing.setIcs(Boolean.TRUE);
        String updatedListingId = mockUtil.getChangedListingId(listing.getUniqueId(),
                ChplProductNumberUtil.ICS_CODE_INDEX, "01");
        listing.setUniqueId(updatedListingId);
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getCriterion().getNumber().equals(B_2)) {
                certResult.getTestTools().clear();
                PendingCertificationResultTestToolDTO crtt = new PendingCertificationResultTestToolDTO();
                crtt.setId(1L);
                crtt.setTestToolId(1L);
                crtt.setName("Bogus Test Tool");
                crtt.setVersion("1.0.0");
                certResult.getTestTools().add(crtt);
            }
        }

        TestToolDTO testTool = createBogusTestTool(true);
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Bogus Test Tool"))).thenReturn(testTool);
        testToolReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertFalse(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertFalse(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
    }

    @Test
    public void testMissingRequiredTestTool_HasError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getCriterion().getNumber().equals(B_2)) {
                certResult.getTestTools().clear();
            }
        }
        ambulatoryTestToolReviewier.review(listing);
        assertTrue(listing.getErrorMessages().contains(NO_TEST_TOOL_ERROR));
    }

    @Test
    public void testMissingOptionalTestTools_NoError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        for (PendingCertificationResultDTO certResult : listing.getCertificationCriterion()) {
            if (certResult.getCriterion().getNumber().equals(G_1)
                    || certResult.getCriterion().getNumber().equals(G_2)
                    || certResult.getCriterion().getNumber().equals(F_3)) {
                certResult.getTestTools().clear();
            }
        }
        ambulatoryTestToolReviewier.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_ERROR));
    }

    private TestToolDTO createBogusTestTool(boolean retired) {
        TestToolDTO testTool = new TestToolDTO();
        testTool.setId(1L);
        testTool.setName("Bogus Test Tool");
        testTool.setRetired(retired);
        return testTool;
    }
}
