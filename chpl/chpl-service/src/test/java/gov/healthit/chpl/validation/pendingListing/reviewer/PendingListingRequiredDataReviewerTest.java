package gov.healthit.chpl.validation.pendingListing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

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

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.listing.ListingMockUtil;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class PendingListingRequiredDataReviewerTest {
    private static final String B_1 = "170.314 (b)(1)";
    private static final String Found_Sed_Criteria_Without_Attesting_Sed =
            "Listing has not attested to (g)(3), but at least one criteria was found attesting to SED.";
    
    
    @Autowired
    private ListingMockUtil mockUtil;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private CertificationResultRules certRules;

    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private RequiredDataReviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        reviewer = new RequiredDataReviewer(msgUtil, certRules);
        
        Mockito.doReturn(Found_Sed_Criteria_Without_Attesting_Sed)
        .when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.foundSedCriteriaWithoutAttestingSed"));
        
        //TODO - Can this be extracted as some sort of generic method, so it can be used all error messages??
        Mockito.doAnswer(new Answer<String> () {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                String invalidG1MacraMeasure = 
                        "Certification %s contains duplicate G1 Macra Measure: '%s'.";
                Object[] args = invocation.getArguments();
                return formatMessage(invalidG1MacraMeasure, (String)args[1], (String)args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.duplicateG1MacraMeasure"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());
        
        Mockito.doAnswer(new Answer<String> () {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                String invalidG2MacraMeasure = 
                        "Certification %s contains duplicate G2 Macra Measure: '%s'.";
                Object[] args = invocation.getArguments();
                return formatMessage(invalidG2MacraMeasure, (String)args[1], (String)args[2]);
            }
        }).when(msgUtil).getMessage(
                ArgumentMatchers.eq("listing.criteria.duplicateG2MacraMeasure"),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString());

    }
    
    @Test
    public void testDuplicateG1MacraMeasure() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        
        //Add Duplicate G1 Macra Measure
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getNumber().equals(B_1)) {
                cert.setG1MacraMeasures(new ArrayList<PendingCertificationResultMacraMeasureDTO>());
                
                PendingCertificationResultMacraMeasureDTO macra1 = new PendingCertificationResultMacraMeasureDTO();
                macra1.setEnteredValue("RT7 EP Stage 2");
                cert.getG1MacraMeasures().add(macra1);
                
                PendingCertificationResultMacraMeasureDTO macra2 = new PendingCertificationResultMacraMeasureDTO();
                macra2.setEnteredValue("RT7 EP Stage 2");
                cert.getG1MacraMeasures().add(macra2);
            }
        }
        
        reviewer.review(listing);
        
        assertTrue(hasDuplicateG1MacraMeasureErrorMessage(listing));
    }
    
    @Test
    public void testNotDuplicateG1MacraMeasure() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        
        //Add two different G1 Macra Measures
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getNumber().equals(B_1)) {
                cert.setG1MacraMeasures(new ArrayList<PendingCertificationResultMacraMeasureDTO>());
                
                PendingCertificationResultMacraMeasureDTO macra2 = new PendingCertificationResultMacraMeasureDTO();
                macra2.setEnteredValue("RT7 EP Stage 2");
                cert.getG1MacraMeasures().add(macra2);
                
                PendingCertificationResultMacraMeasureDTO macra1 = new PendingCertificationResultMacraMeasureDTO();
                macra1.setEnteredValue("RT7 EC Group");
                cert.getG1MacraMeasures().add(macra1);
            }
            
        }
        
        reviewer.review(listing);
        
        assertFalse(hasDuplicateG1MacraMeasureErrorMessage(listing));
    }
    
    @Test
    public void testSingleG1MacraMeasure() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        
        //Add Single G1 Macra Measure
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getNumber().equals(B_1)) {
                cert.setG1MacraMeasures(new ArrayList<PendingCertificationResultMacraMeasureDTO>());

                PendingCertificationResultMacraMeasureDTO macra1 = new PendingCertificationResultMacraMeasureDTO();
                macra1.setEnteredValue("RT7 EC Group");
                cert.getG1MacraMeasures().add(macra1);
            }
            
        }
        
        reviewer.review(listing);
        
        assertFalse(hasDuplicateG1MacraMeasureErrorMessage(listing));
    }
    

    @Test
    public void testDuplicateG2MacraMeasure() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        
        //Add Duplicate G2 Macra Measure
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getNumber().equals(B_1)) {
                cert.setG2MacraMeasures(new ArrayList<PendingCertificationResultMacraMeasureDTO>());
                
                PendingCertificationResultMacraMeasureDTO macra1 = new PendingCertificationResultMacraMeasureDTO();
                macra1.setEnteredValue("RT7 EP Stage 2");
                cert.getG2MacraMeasures().add(macra1);
                
                PendingCertificationResultMacraMeasureDTO macra2 = new PendingCertificationResultMacraMeasureDTO();
                macra2.setEnteredValue("RT7 EP Stage 2");
                cert.getG2MacraMeasures().add(macra2);
            }
        }
        
        reviewer.review(listing);
        
        assertTrue(hasDuplicateG2MacraMeasureErrorMessage(listing));
    }
    
    @Test
    public void testNotDuplicateG2MacraMeasure() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        
        //Add two different G2 Macra Measures
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getNumber().equals(B_1)) {
                cert.setG2MacraMeasures(new ArrayList<PendingCertificationResultMacraMeasureDTO>());
                
                PendingCertificationResultMacraMeasureDTO macra2 = new PendingCertificationResultMacraMeasureDTO();
                macra2.setEnteredValue("RT7 EP Stage 2");
                cert.getG2MacraMeasures().add(macra2);
                
                PendingCertificationResultMacraMeasureDTO macra1 = new PendingCertificationResultMacraMeasureDTO();
                macra1.setEnteredValue("RT7 EC Group");
                cert.getG2MacraMeasures().add(macra1);
            }
            
        }
        
        reviewer.review(listing);
        
        assertFalse(hasDuplicateG2MacraMeasureErrorMessage(listing));
    }
    
    @Test
    public void testSingleG2MacraMeasure() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        
        //Add Single G2 Macra Measure
        for (PendingCertificationResultDTO cert : listing.getCertificationCriterion()) {
            if (cert.getNumber().equals(B_1)) {
                cert.setG2MacraMeasures(new ArrayList<PendingCertificationResultMacraMeasureDTO>());

                PendingCertificationResultMacraMeasureDTO macra1 = new PendingCertificationResultMacraMeasureDTO();
                macra1.setEnteredValue("RT7 EC Group");
                cert.getG2MacraMeasures().add(macra1);
            }
            
        }
        
        reviewer.review(listing);
        
        assertFalse(hasDuplicateG2MacraMeasureErrorMessage(listing));
    }

    private Boolean hasDuplicateG1MacraMeasureErrorMessage(PendingCertifiedProductDTO listing) {
        for (String message : listing.getErrorMessages()) {
            if (StringUtils.contains(message, "contains duplicate G1 Macra Measure")) {
                return true;
            }
        }
        return false;
    }
    
    private Boolean hasDuplicateG2MacraMeasureErrorMessage(PendingCertifiedProductDTO listing) {
        for (String message : listing.getErrorMessages()) {
            if (StringUtils.contains(message, "contains duplicate G2 Macra Measure")) {
                return true;
            }
        }
        return false;
    }
    
    private String formatMessage(String message, String a, String b) {
        return String.format(message, a, b);
    }
}
