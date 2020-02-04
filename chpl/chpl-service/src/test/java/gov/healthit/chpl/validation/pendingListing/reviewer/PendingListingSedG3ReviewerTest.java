package gov.healthit.chpl.validation.pendingListing.reviewer;

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

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ListingMockUtil;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.SedG32014Reviewer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
public class PendingListingSedG3ReviewerTest {
    private static final String G3_2014 = "170.314 (g)(3)";
    private static final String NO_G3_HAS_SED = "Listing has not attested to (g)(3), but at least one criteria was found attesting to SED.";
    private static final String HAS_G3_NO_SED = "Listing has attested to (g)(3), but no criteria were found attesting to SED.";

    @Autowired
    private ListingMockUtil mockUtil;

    @Autowired
    private MessageSource messageSource;

    @Spy
    private ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);

    private SedG32014Reviewer reviewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        reviewer = new SedG32014Reviewer(msgUtil);

        Mockito.doReturn(NO_G3_HAS_SED)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.criteria.foundSedCriteriaWithoutAttestingSed"));
        Mockito.doReturn(HAS_G3_NO_SED)
                .when(msgUtil).getMessage(
                        ArgumentMatchers.eq("listing.criteria.foundNoSedCriteriaButAttestingSed"));
    }

    @Test
    public void testNoG3WithSedNoIcsHasError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        // has sed = true for one criteria, no g3, and no ics

        reviewer.review(listing);
        assertTrue(hasNoG3WithSedErrorMessage(listing));
    }

    @Test
    public void testNoG3WithSedHasIcsHasError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        // set ics = true
        listing.setIcs(Boolean.TRUE);

        reviewer.review(listing);
        assertTrue(hasNoG3WithSedErrorMessage(listing));
    }

    @Test
    public void testHasG3NoSedNoIcsHasError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        for (PendingCertificationResultDTO criteria : listing.getCertificationCriterion()) {
            criteria.setSed(Boolean.FALSE);
        }
        PendingCertificationResultDTO g3 = mockUtil.create2014PendingCertResult(100L, G3_2014, true);
        listing.getCertificationCriterion().add(g3);

        reviewer.review(listing);
        assertTrue(hasG3NoSedErrorMessage(listing));
    }

    @Test
    public void testHasG3NoSedHasIcsNoError() {
        PendingCertifiedProductDTO listing = mockUtil.createPending2014Listing();
        listing.setIcs(Boolean.TRUE);
        for (PendingCertificationResultDTO criteria : listing.getCertificationCriterion()) {
            criteria.setSed(Boolean.FALSE);
        }
        PendingCertificationResultDTO g3 = mockUtil.create2014PendingCertResult(100L, G3_2014, true);
        listing.getCertificationCriterion().add(g3);

        reviewer.review(listing);
        assertFalse(hasG3NoSedErrorMessage(listing));
    }

    private Boolean hasNoG3WithSedErrorMessage(PendingCertifiedProductDTO listing) {
        for (String message : listing.getErrorMessages()) {
            if (message.equals(NO_G3_HAS_SED)) {
                return true;
            }
        }
        return false;
    }

    private Boolean hasG3NoSedErrorMessage(PendingCertifiedProductDTO listing) {
        for (String message : listing.getErrorMessages()) {
            if (message.equals(HAS_G3_NO_SED)) {
                return true;
            }
        }
        return false;
    }
}
