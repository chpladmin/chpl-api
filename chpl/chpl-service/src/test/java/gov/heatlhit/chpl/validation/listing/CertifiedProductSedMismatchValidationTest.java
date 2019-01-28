package gov.heatlhit.chpl.validation.listing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.impl.CertificationResultManagerImpl;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.PendingValidator;
import gov.healthit.chpl.validation.listing.Validator;

/**
 * Tests certified product sed+g3 mismatch rule
 * @author alarned
 *
 */

// This test class has a modified configuration to get the tests to work.  The method
// CertificationResultsManagerImpl.getCertifiedProductHasAdditionalSoftware() does not
// work in the test environment, so we are overriding that method.  Since we are not
// testing that particular method with these tests, this should be OK.  To do this, we
// did the following:
//   1. Create a new class (MyCertificationResultManager) that extends
//       CertificationResultManagerImpl and override the
//       getCertifiedProductHasAdditionalSoftware method with a constant value
//       of 'false'
//   2. Created a new Spring configuration class CertifiedProductSedMismatchValidationTestConfig,
//       based on the CHPLTestConfig class
//   3. In the new config class, specify that the CertificationResultManager bean should
//       use an instance of MyCertificationResultManager.
//   4. Modify this test class to use the new spring configuration that was just created:
//       @ContextConfiguration(classes = { CertifiedProductSedMismatchValidationTestConfig.class })
@Configuration
@Import(gov.healthit.chpl.CHPLTestConfig.class)
class CertifiedProductSedMismatchValidationTestConfig {
    @Bean
    @Primary
    public CertificationResultManager certificationResultManager() {
        return new TestCertificationResultManager();
    }
}

class TestCertificationResultManager extends CertificationResultManagerImpl  {
    @Override
    public boolean getCertifiedProductHasAdditionalSoftware(Long certifiedProductId) {
        return false;
    }
}
///////////////////////////////////////////////////////////////////////////////////////////////////

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CertifiedProductValidationTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class })
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductSedMismatchValidationTest {

    private static final String SED_FOUND_WITHOUT_SED_CRITERIA_ERROR = "Listing has attested to (g)(3), "
            + "but no criteria were found attesting to SED.";
    private static final String SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR = "Listing has not attested to (g)(3), "
            + "but at least one criteria was found attesting to SED.";

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Autowired
    ListingValidatorFactory validatorFactory;

    private static JWTAuthenticatedUser adminUser;
    private static final long ADMIN_ID = -2L;

    /**
     * Set up the default user as an admin.
     */
    @BeforeClass
    public static void setUpClass() {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ACB"));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingSedTrueAnd2015G3TrueHasNoError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper.createPendingCertResult("170.315 (a)(1)");
        pendingCertResult.setSed(Boolean.TRUE);
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO sedCertResult = CertifiedProductValidationTestHelper.createPendingCertResult("170.315 (g)(3)");
        pendingCertResults.add(sedCertResult);

        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingSedTrueAnd2014G3TrueHasNoError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2014");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper.createPendingCertResult("170.314 (a)(1)");
        pendingCertResult.setSed(Boolean.TRUE);
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO sedCertResult = CertifiedProductValidationTestHelper.createPendingCertResult("170.314 (g)(3)");
        pendingCertResults.add(sedCertResult);

        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingSedFalseAnd2015G3TrueHasError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO sedCertResult = CertifiedProductValidationTestHelper.createPendingCertResult("170.315 (g)(3)");
        pendingCertResults.add(sedCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }
        assertTrue(pendingListing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingSedFalseAnd2014G3TrueHasError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2014");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO sedCertResult = CertifiedProductValidationTestHelper.createPendingCertResult("170.314 (g)(3)");
        pendingCertResults.add(sedCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }
        assertTrue(pendingListing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingSedTrueAnd2014G3FalseHasError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2014");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper.createPendingCertResult("170.314 (a)(1)");
        pendingCertResult.setSed(Boolean.TRUE);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }
        assertTrue(pendingListing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingSedTrueAnd2015G3FalseHasError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper.createPendingCertResult("170.315 (a)(1)");
        pendingCertResult.setSed(Boolean.TRUE);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }
        assertTrue(pendingListing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validateSedTrueAnd2015G3TrueHasNoError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (a)(1)");
        certResult.setSed(Boolean.TRUE);
        certResults.add(certResult);
        CertificationResult sedCertResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(3)");
        certResults.add(sedCertResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }
        assertFalse(listing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
        assertFalse(listing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validateSedTrueAnd2014G3TrueHasNoError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2014");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.314 (a)(1)");
        certResult.setSed(Boolean.TRUE);
        certResults.add(certResult);
        CertificationResult sedCertResult = CertifiedProductValidationTestHelper.createCertResult("170.314 (g)(3)");
        certResults.add(sedCertResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }
        assertFalse(listing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
        assertFalse(listing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-2358: SED business rule does not apply for legacy CHPL listings
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validateSedTrueAnd2014LegacyG3TrueHasNoError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2014", true);
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.314 (a)(1)");
        certResult.setSed(Boolean.TRUE);
        certResults.add(certResult);
        CertificationResult sedCertResult = CertifiedProductValidationTestHelper.createCertResult("170.314 (g)(3)");
        certResults.add(sedCertResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }
        assertFalse(listing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
        assertFalse(listing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validateSedFalseAnd2015G3TrueHasError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult sedCertResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(3)");
        certResults.add(sedCertResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }
        assertTrue(listing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validateSedFalseAnd2014G3TrueHasError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2014");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult sedCertResult = CertifiedProductValidationTestHelper.createCertResult("170.314 (g)(3)");
        certResults.add(sedCertResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }
        assertTrue(listing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-2358: SED business rule does not apply for legacy CHPL listings
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validateSedFalseAnd2014LegacyG3TrueHasNoError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2014", true);
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult sedCertResult = CertifiedProductValidationTestHelper.createCertResult("170.314 (g)(3)");
        certResults.add(sedCertResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }
        assertFalse(listing.getErrorMessages().contains(SED_FOUND_WITHOUT_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validateSedTrueAnd2014G3FalseHasError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2014");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.314 (a)(1)");
        certResult.setSed(Boolean.TRUE);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }
        assertTrue(listing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-2358: SED business rule does not apply for legacy CHPL listings
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validateSedTrueAnd2014LegacyG3FalseHasNoError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2014", true);
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.314 (a)(1)");
        certResult.setSed(Boolean.TRUE);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }
        assertFalse(listing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }

    /**
     * OCD-1778: SED business rule.
     * Listing may attest to SED criteria (g3) iff it attests SED to at least one criteria.
     */
    @Transactional
    @Rollback(true)
    @Test
    public void validateSedTrueAnd2015G3FalseHasError() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (a)(1)");
        certResult.setSed(Boolean.TRUE);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }
        assertTrue(listing.getErrorMessages().contains(SED_NOT_FOUND_WITH_SED_CRITERIA_ERROR));
    }
}
