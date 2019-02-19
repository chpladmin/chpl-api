package gov.healthit.chpl.validation.pendingListing.reviewer;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.dao.TestDataDAO;
import gov.healthit.chpl.dao.TestFunctionalityDAO;
import gov.healthit.chpl.dao.TestProcedureDAO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.dto.PendingTestTaskDTO;
import gov.healthit.chpl.listing.ListingMockUtil;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.RequiredData2015Reviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class PendingListingRequiredData2015ReviewerTest {

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

    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private RequiredData2015Reviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        reviewer = new RequiredData2015Reviewer(macraMeasureDAO, testFuncDao, testProcDao, testDataDao, msgUtil, certRules);

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(final InvocationOnMock invocation) throws Throwable {
                String badTestTaskNumber =
                        "An unrecognized character was found in Test Task \"%s\" \"%s\" \"%s\". The value must be only a numeric value. You can correct it within the field itself on the Edit Certified Product screen or modify it in the csv file and upload again.";
                Object[] args = invocation.getArguments();
                return formatMessage(badTestTaskNumber, (String) args[1], (String) args[2], (String) args[3]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.badTestTaskNumber"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
    }

    @Test
    public void testBadNumberInTestTask() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2015Listing();

        PendingCertificationResultDTO cert = findPendingCertification(listing, "170.315 (a)(1)");
        PendingCertificationResultTestTaskDTO test = cert.getTestTasks().get(0);
        test.getPendingTestTask().setTaskErrors("1.6 e-1");

        reviewer.review(listing);

        assertTrue(hasTestTaskNumberErrorMessage(listing));
    }

    private Boolean hasTestTaskNumberErrorMessage(final PendingCertifiedProductDTO listing) {
        for (String message : listing.getErrorMessages()) {
            if (StringUtils.contains(message, "An unrecognized character was found in Test Task")) {
                return true;
            }
        }
        return false;
    }

    private String formatMessage(final String message, final String a, final String b, final String c) {
        return String.format(message, a, b, c);
    }

    private PendingCertificationResultDTO findPendingCertification(final PendingCertifiedProductDTO listing,
            final String certNumber) {
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getNumber().equals(certNumber)) {
                return cert;
            }
        }
        return null;
    }
}
