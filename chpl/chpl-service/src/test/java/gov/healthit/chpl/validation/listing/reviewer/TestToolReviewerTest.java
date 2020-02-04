package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.TestingUsers;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.TestTool2015Reviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class TestToolReviewerTest extends TestingUsers {
    private static final String C_3 = "170.315 (c)(3)";
    private static final String NO_TEST_TOOL_NAME_ERROR = "There was no test tool name found for certification " + C_3
            + ".";
    private static final String NO_TEST_TOOL_VERSION_ERROR = "There was no version found for test tool Bogus Test Tool and certification "
            + C_3 + ".";
    private static final String TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR = "Criteria " + C_3
            + " contains an invalid test tool 'Bogus Test Tool'.";
    private static final String RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR = "Test Tool 'Bogus Test Tool' can not be used for criteria '"
            + C_3 + "', as it is a retired tool, and this Certified Product does not carry ICS.";

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

    @Mock
    private ResourcePermissions resourcePermissions;

    private TestToolReviewer testToolReviewer;
    private TestTool2015Reviewer testTool2015Reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        setupForAcbUser(resourcePermissions);

        testToolReviewer = new TestToolReviewer(testToolDao, msgUtil, resourcePermissions);
        testTool2015Reviewer = new TestTool2015Reviewer(msgUtil, resourcePermissions);

        Mockito.doReturn(NO_TEST_TOOL_NAME_ERROR).when(msgUtil)
                .getMessage(ArgumentMatchers.eq("listing.criteria.missingTestToolName"), ArgumentMatchers.anyString());
        Mockito.doReturn(NO_TEST_TOOL_VERSION_ERROR).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.missingTestToolVersion"), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
        Mockito.doReturn(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.retiredTestToolNotAllowed"), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
        Mockito.doReturn(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.testToolNotFound"), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString());
        TestToolDTO cypressTestTool = new TestToolDTO();
        cypressTestTool.setId(1L);
        cypressTestTool.setName("Cypress");
        cypressTestTool.setRetired(false);
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Cypress"))).thenReturn(cypressTestTool);
    }

    @Test
    public void testNoTestTools_NoError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getTestToolsUsed() != null && certResult.getTestToolsUsed().size() > 0) {
                certResult.getTestToolsUsed().clear();
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
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals(C_3)) {
                certResult.getTestToolsUsed().clear();
                CertificationResultTestTool crtt = new CertificationResultTestTool();
                crtt.setId(1L);
                crtt.setRetired(false);
                crtt.setTestToolId(1L);
                crtt.setTestToolName("Bogus Test Tool");
                crtt.setTestToolVersion("1.0.0");
                certResult.getTestToolsUsed().add(crtt);
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
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals(C_3)) {
                certResult.getTestToolsUsed().clear();
                CertificationResultTestTool crtt = new CertificationResultTestTool();
                crtt.setId(1L);
                crtt.setRetired(false);
                crtt.setTestToolId(1L);
                crtt.setTestToolName("Bogus Test Tool");
                crtt.setTestToolVersion(null);
                certResult.getTestToolsUsed().add(crtt);
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
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals(C_3)) {
                CertificationResultTestTool crtt = new CertificationResultTestTool();
                crtt.setId(1L);
                crtt.setRetired(false);
                crtt.setTestToolId(1L);
                crtt.setTestToolName(null);
                crtt.setTestToolVersion("1.0.0");
                certResult.getTestToolsUsed().add(crtt);
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
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals(C_3)) {
                CertificationResultTestTool crtt = new CertificationResultTestTool();
                crtt.setId(1L);
                crtt.setRetired(false);
                crtt.setTestToolId(1L);
                crtt.setTestToolName("Bogus Test Tool");
                crtt.setTestToolVersion("1.0.0");
                certResult.getTestToolsUsed().add(crtt);
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
    public void testListingWithIcsConflictAndRetiredTestTool_HasWarning() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        listing.getIcs().setInherits(Boolean.TRUE);
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals(C_3)) {
                CertificationResultTestTool crtt = new CertificationResultTestTool();
                crtt.setId(1L);
                crtt.setRetired(true);
                crtt.setTestToolId(1L);
                crtt.setTestToolName("Bogus Test Tool");
                crtt.setTestToolVersion("1.0.0");
                certResult.getTestToolsUsed().add(crtt);
            }
        }

        TestToolDTO testTool = createBogusTestTool(true);
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Bogus Test Tool"))).thenReturn(testTool);
        testToolReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertFalse(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertFalse(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
        assertTrue(listing.getWarningMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
    }

    @Test
    public void testListingWithIcsAndRetiredTestTool_HasNoError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        String updatedListingId = mockUtil.getChangedListingId(listing.getChplProductNumber(),
                ChplProductNumberUtil.ICS_CODE_INDEX, "01");
        listing.setChplProductNumber(updatedListingId);
        listing.getIcs().setInherits(Boolean.TRUE);

        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals(C_3)) {
                CertificationResultTestTool crtt = new CertificationResultTestTool();
                crtt.setId(1L);
                crtt.setRetired(true);
                crtt.setTestToolId(1L);
                crtt.setTestToolName("Bogus Test Tool");
                crtt.setTestToolVersion("1.0.0");
                certResult.getTestToolsUsed().add(crtt);
            }
        }

        TestToolDTO testTool = createBogusTestTool(true);
        Mockito.when(testToolDao.getByName(ArgumentMatchers.eq("Bogus Test Tool"))).thenReturn(testTool);
        testToolReviewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(NO_TEST_TOOL_NAME_ERROR));
        assertFalse(listing.getErrorMessages().contains(TEST_TOOL_NOT_FOUND_AND_REMOVED_ERROR));
        assertFalse(listing.getErrorMessages().contains(RETIRED_TEST_TOOL_NOT_ALLOWED_ERROR));
    }

    private TestToolDTO createBogusTestTool(boolean retired) {
        TestToolDTO testTool = new TestToolDTO();
        testTool.setId(1L);
        testTool.setName("Bogus Test Tool");
        testTool.setRetired(retired);
        return testTool;
    }
}
