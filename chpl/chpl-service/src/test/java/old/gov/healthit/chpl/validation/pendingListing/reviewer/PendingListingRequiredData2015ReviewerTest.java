package old.gov.healthit.chpl.validation.pendingListing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.RequiredData2015Reviewer;
import old.gov.healthit.chpl.TestingUsers;
import old.gov.healthit.chpl.util.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
public class PendingListingRequiredData2015ReviewerTest extends TestingUsers {

    @Autowired
    private MacraMeasureDAO macraMeasureDAO;

    @Autowired
    private TestFunctionalityDAO testFuncDao;

    @Autowired
    private TestProcedureDAO testProcDao;

    @Autowired
    private TestDataDAO testDataDao;

    @Autowired
    private ListingMockUtil mockUtil;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private CertificationResultRules certRules;

    @Autowired
    private ValidationUtils validationUtils;

    @Autowired
    private CertificationCriterionDAO criterionDao;

    @Mock
    private ResourcePermissions resourcePermissions;

    @Mock
    private ErrorMessageUtil msgUtil;

    private RequiredData2015Reviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        setupForAcbUser(resourcePermissions);

        reviewer = new RequiredData2015Reviewer(macraMeasureDAO, testFuncDao, testProcDao,
                testDataDao, criterionDao, msgUtil, resourcePermissions, certRules, validationUtils);

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "An unrecognized character was found in Test Task \"%s\" \"%s\" \"%s\"."
                        + "The value must be only a numeric value. You can correct it within the field itself "
                        + "on the Edit Certified Product screen or modify it in the csv file and upload again.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2], (String) args[3]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskNumber"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "A non-integer numeric number was found in Test Task \"%s\" \"%s\" \"%s\". "
                        + "The number has been rounded to \"%s\".";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2], (String) args[3], (String) args[4]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.roundedTestTaskNumber"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Success Average value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskSuccessAverage"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Success Standard Deviation value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskSuccessStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Path Deviation Observed value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskPathDeviationObserved"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Path Deviation Optimal value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskPathDeviationOptimal"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Time Average value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskTimeAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Time Standard Deviation value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskTimeStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Time Deviation Observed Average value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskTimeDeviationObservedAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Time Deviation Optimal Average value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskTimeDeviationOptimalAvg"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Errors value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskErrors"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Errors Standard Deviation value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskErrorsStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Rating value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskRating"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber = "The test task %s for criteria %s requires a Task Rating Standard Deviation value.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskRatingStddev"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String g1MacraNotAllowed = "Certification %s may not reference the G1 Macra Measure: '%s' "
                        + "since this listing does not have ICS. The measure has been removed.";
                Object[] args = invocation.getArguments();
                return formatMessage(g1MacraNotAllowed, (String) args[1], (String) args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.removedG1MacraMeasureNoIcs"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testBadNumbersInTestTask() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2015Listing();

        PendingCertificationResultDTO cert = findPendingCertification(listing, "170.315 (a)(1)");
        PendingCertificationResultTestTaskDTO task = cert.getTestTasks().get(0);
        task.getPendingTestTask().setTaskErrors("1.6 e-1");
        task.getPendingTestTask().setTaskErrorsStddev("1.1ddds");
        task.getPendingTestTask().setTaskPathDeviationObserved("3.3dd");
        task.getPendingTestTask().setTaskPathDeviationOptimal("3.33dd");
        task.getPendingTestTask().setTaskRating("3l3.3d");
        task.getPendingTestTask().setTaskRatingStddev("3.s3");
        task.getPendingTestTask().setTaskSuccessAverage("3lk.");
        task.getPendingTestTask().setTaskSuccessStddev("3lks");
        task.getPendingTestTask().setTaskTimeAvg("3l3");
        task.getPendingTestTask().setTaskTimeDeviationOptimalAvg("3ldkl3");
        task.getPendingTestTask().setTaskTimeDeviationObservedAvg("3kl3");
        task.getPendingTestTask().setTaskTimeStddev("3la3");

        reviewer.review(listing);

        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Errors", "1.6 e-1"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Errors Standard Deviation", "1.1ddds"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Path Deviation Observed", "3.3dd"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Path Deviation Optimal", "3.33dd"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Rating", "3l3.3d"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Rating Standard Deviation", "3.s3"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Success Average", "3lk."));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Success Standard Deviation", "3lks"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Time Average", "3l3"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Time Deviation Optimal Average", "3ldkl3"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Time Deviation Observed Average", "3kl3"));
        assertTrue(hasTestTaskNumberErrorMessage(listing, "Task ID", "Task Time Standard Deviation", "3la3"));
    }

    @Test
    public void testGoodNumbersInTestTask() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2015Listing();

        reviewer.review(listing);

        assertFalse(hasGenericTestTaskErrorMessage(listing));
    }

    @Test
    public void testNullNumbersInTestTask() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2015Listing();

        PendingCertificationResultDTO cert = findPendingCertification(listing, "170.315 (a)(1)");
        PendingCertificationResultTestTaskDTO task = cert.getTestTasks().get(0);
        task.getPendingTestTask().setTaskErrors(null);
        task.getPendingTestTask().setTaskErrorsStddev(null);
        task.getPendingTestTask().setTaskPathDeviationObserved(null);
        task.getPendingTestTask().setTaskPathDeviationOptimal(null);
        task.getPendingTestTask().setTaskRating(null);
        task.getPendingTestTask().setTaskRatingStddev(null);
        task.getPendingTestTask().setTaskSuccessAverage(null);
        task.getPendingTestTask().setTaskSuccessStddev(null);
        task.getPendingTestTask().setTaskTimeAvg(null);
        task.getPendingTestTask().setTaskTimeDeviationOptimalAvg(null);
        task.getPendingTestTask().setTaskTimeDeviationObservedAvg(null);
        task.getPendingTestTask().setTaskTimeStddev(null);

        reviewer.review(listing);

        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Errors"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Errors Standard Deviation"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Path Deviation Observed"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Path Deviation Optimal"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Rating"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Rating Standard Deviation"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Success Average"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Success Standard Deviation"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Time Average"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Time Deviation Optimal Average"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Time Deviation Observed Average"));
        assertTrue(hasTestTaskNumberMissingMessage(listing, "Task ID", "170.315 (a)(1)", "Task Time Standard Deviation"));
    }

    @Test
    public void testRoundedNumbersInTestTask() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2015Listing();

        PendingCertificationResultDTO cert = findPendingCertification(listing, "170.315 (a)(1)");
        PendingCertificationResultTestTaskDTO task = cert.getTestTasks().get(0);
        task.getPendingTestTask().setTaskPathDeviationObserved("3.3");
        task.getPendingTestTask().setTaskPathDeviationOptimal("3.33");
        task.getPendingTestTask().setTaskTimeAvg("1.3");
        task.getPendingTestTask().setTaskTimeDeviationOptimalAvg("5.3");
        task.getPendingTestTask().setTaskTimeDeviationObservedAvg("9.9");
        task.getPendingTestTask().setTaskTimeStddev("8.0");

        reviewer.review(listing);

        assertTrue(hasRoundedTestTaskWarningMessage(listing, "Task ID", "Task Path Deviation Observed", "3.3", "3"));
        assertTrue(hasRoundedTestTaskWarningMessage(listing, "Task ID", "Task Path Deviation Optimal", "3.33", "3"));
        assertTrue(hasRoundedTestTaskWarningMessage(listing, "Task ID", "Task Time Average", "1.3", "1"));
        assertTrue(hasRoundedTestTaskWarningMessage(listing, "Task ID", "Task Time Deviation Optimal Average", "5.3", "5"));
        assertTrue(hasRoundedTestTaskWarningMessage(listing, "Task ID", "Task Time Deviation Observed Average", "9.9", "10"));
        assertTrue(hasRoundedTestTaskWarningMessage(listing, "Task ID", "Task Time Standard Deviation", "8.0", "8"));
    }

    @Test
    public void testRemovedMacraMeasureNotAllowedNoIcs() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2015Listing();
        listing.setIcs(Boolean.FALSE);

        PendingCertificationResultDTO cert = findPendingCertification(listing, "170.315 (a)(10)");
        PendingCertificationResultMacraMeasureDTO crMacra = new PendingCertificationResultMacraMeasureDTO();
        crMacra.setEnteredValue("My Macra");
        crMacra.setMacraMeasureId(-1L);
        MacraMeasureDTO mm = new MacraMeasureDTO();
        mm.setId(-1L);
        mm.setName("A long description of my macra measure");
        mm.setValue("My Macra");
        mm.setRemoved(Boolean.TRUE);
        crMacra.setMacraMeasure(mm);
        cert.getG1MacraMeasures().add(crMacra);

        reviewer.review(listing);

        assertTrue(hasRemovedG1MacraMeasureErrorMessage(listing, cert, mm));
    }

    @Test
    public void testRemovedMacraMeasureAllowedWithIcs() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2015Listing();
        listing.setIcs(Boolean.TRUE);

        PendingCertificationResultDTO cert = findPendingCertification(listing, "170.315 (a)(10)");
        PendingCertificationResultMacraMeasureDTO crMacra = new PendingCertificationResultMacraMeasureDTO();
        crMacra.setEnteredValue("My Macra");
        crMacra.setMacraMeasureId(-1L);
        MacraMeasureDTO mm = new MacraMeasureDTO();
        mm.setId(-1L);
        mm.setName("A long description of my macra measure");
        mm.setValue("My Macra");
        mm.setRemoved(Boolean.TRUE);
        crMacra.setMacraMeasure(mm);
        cert.getG1MacraMeasures().add(crMacra);

        reviewer.review(listing);

        assertFalse(hasRemovedG1MacraMeasureErrorMessage(listing, cert, mm));
    }

    private Boolean hasRemovedG1MacraMeasureErrorMessage(final PendingCertifiedProductDTO listing,
            final PendingCertificationResultDTO cert,
            final MacraMeasureDTO measure) {
        for (String message : listing.getErrorMessages()) {
            if (StringUtils.contains(message, "Certification "
                    + cert.getCriterion().getNumber() + " may not reference the G1 Macra Measure: '"
                    + measure.getValue() + "' since this listing does not have ICS. The measure has been removed.")) {
                return true;
            }
        }
        return false;
    }

    private Boolean hasTestTaskNumberErrorMessage(final PendingCertifiedProductDTO listing,
            final String taskId, final String valueName, final String badValue) {
        for (String message : listing.getErrorMessages()) {
            if (StringUtils.contains(message, "An unrecognized character was found in Test Task \""
                    + taskId + "\" \"" + valueName + "\" \"" + badValue + "\"")) {
                return true;
            }
        }
        return false;
    }

    private Boolean hasRoundedTestTaskWarningMessage(final PendingCertifiedProductDTO listing,
            final String taskId, final String valueName, final String badValue, final String roundedValue) {
        for (String message : listing.getWarningMessages()) {
            if (StringUtils.contains(message, "A non-integer numeric number was found in Test Task \""
                    + taskId + "\" \"" + valueName + "\" \"" + badValue
                    + "\". The number has been rounded to \"" + roundedValue)) {
                return true;
            }
        }
        return false;
    }

    private Boolean hasTestTaskNumberMissingMessage(final PendingCertifiedProductDTO listing,
            final String taskId, final String criteria, final String valueName) {
        for (String message : listing.getErrorMessages()) {
            if (StringUtils.contains(message, "The test task "
                    + taskId + " for criteria " + criteria + " requires a " + valueName + " value")) {
                return true;
            }
        }
        return false;
    }

    private Boolean hasGenericTestTaskErrorMessage(final PendingCertifiedProductDTO listing) {
        for (String message : listing.getErrorMessages()) {
            if (StringUtils.contains(message, "An unrecognized character was found in Test Task")) {
                return true;
            }
        }
        return false;
    }

    private String formatMessage(final String message, final Object... args) {
        return String.format(message, args);
    }

    private PendingCertificationResultDTO findPendingCertification(final PendingCertifiedProductDTO listing,
            final String certNumber) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getCriterion().getNumber().equals(certNumber)) {
                return cert;
            }
        }
        return null;
    }
}
