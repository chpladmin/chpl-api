package gov.healthit.chpl.validation.listing.reviewer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultAdditionalSoftware;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.PrivacyAndSecurityFrameworkConcept;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ListingMockUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
public class ValidDataReviewerTest {
    private static final String D_1 = "170.315 (d)(1)";
    private static final String BAD_PRIVACY_SECURITY_ERROR =
            "Certification " + D_1
            + " contains Privacy and Security Framework value 'Approach 12' which must match one of "
            + PrivacyAndSecurityFrameworkConcept.getFormattedValues();
    private static final String BAD_ADDL_SOFTWARE_ERROR =
            "No CHPL product was found matching additional software CHP-12345 for " + D_1;

    @Spy private ChplProductNumberUtil chplNumberUtil;
    @Autowired private ListingMockUtil mockUtil;

    @InjectMocks
    private ValidDataReviewer validDataReivewer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testValidListing_EmptyValidFields_DoesNotHaveErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for(CertificationResult certResult : listing.getCertificationResults()) {
            if(certResult.getNumber().equals(D_1)) {
                certResult.setPrivacySecurityFramework(null);
                certResult.setAdditionalSoftware(null);
            }
        }
        validDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(BAD_PRIVACY_SECURITY_ERROR));
        assertFalse(listing.getErrorMessages().contains(BAD_ADDL_SOFTWARE_ERROR));
    }

    @Test
    public void testValidListing_ValidFields_DoesNotHaveErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals(D_1)) {
                certResult.setPrivacySecurityFramework("Approach 1");
                CertificationResultAdditionalSoftware addSoft = new CertificationResultAdditionalSoftware();
                addSoft.setCertifiedProductId(2L);
                addSoft.setCertifiedProductNumber("CHP-12345");
                addSoft.setCertificationResultId(certResult.getId());
                certResult.getAdditionalSoftware().add(addSoft);
            }
        }
        try {
            Mockito.when(chplNumberUtil.chplIdExists(ArgumentMatchers.anyString()))
        .thenReturn(true);
        } catch (EntityRetrievalException ex) { }
        validDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(BAD_PRIVACY_SECURITY_ERROR));
        assertFalse(listing.getErrorMessages().contains(BAD_ADDL_SOFTWARE_ERROR));
    }

    @Test
    public void testBadPrivacySecurity_HasError() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals(D_1)) {
                certResult.setPrivacySecurityFramework("Approach 12");
            }
        }
        try {
            Mockito.when(chplNumberUtil.chplIdExists(ArgumentMatchers.anyString()))
        .thenReturn(true);
        } catch (EntityRetrievalException ex) { }
        validDataReivewer.review(listing);
        assertTrue(listing.getErrorMessages().contains(BAD_PRIVACY_SECURITY_ERROR));
        assertFalse(listing.getErrorMessages().contains(BAD_ADDL_SOFTWARE_ERROR));
    }

    @Test
    public void testBadAdditionalSoftware_HasErrors() {
        CertifiedProductSearchDetails listing = mockUtil.createValid2015Listing();
        for (CertificationResult certResult : listing.getCertificationResults()) {
            if (certResult.getNumber().equals(D_1)) {
                CertificationResultAdditionalSoftware addSoft = new CertificationResultAdditionalSoftware();
                addSoft.setCertifiedProductNumber("CHP-12345");
                addSoft.setCertificationResultId(certResult.getId());
                certResult.getAdditionalSoftware().add(addSoft);
            }
        }
        try {
            Mockito.when(chplNumberUtil.chplIdExists(ArgumentMatchers.anyString()))
        .thenReturn(false);
        } catch (EntityRetrievalException ex) { }
        validDataReivewer.review(listing);
        assertFalse(listing.getErrorMessages().contains(BAD_PRIVACY_SECURITY_ERROR));
        assertTrue(listing.getErrorMessages().contains(BAD_ADDL_SOFTWARE_ERROR));
    }
}
