package gov.heatlhit.chpl.validation.listing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
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
import gov.healthit.chpl.dao.MacraMeasureDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.InheritedCertificationStatus;
import gov.healthit.chpl.domain.MacraMeasure;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.dto.MacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CertificationResultManager;
import gov.healthit.chpl.manager.impl.CertificationResultManagerImpl;
import gov.healthit.chpl.validation.listing.ListingValidatorFactory;
import gov.healthit.chpl.validation.listing.PendingValidator;
import gov.healthit.chpl.validation.listing.Validator;

/**
 * Tests of the Certified Product validator.
 *
 * @author alarned
 *
 */

// This test class has a modified configuration to get the tests to work. The
// method
// CertificationResultsManagerImpl.getCertifiedProductHasAdditionalSoftware()
// does not
// work in the test environment, so we are overriding that method. Since we are
// not
// testing that particular method with these tests, this should be OK. To do
// this, we
// did the following:
// 1. Create a new class (MyCertificationResultManager) that extends
// CertificationResultManagerImpl and override the
// getCertifiedProductHasAdditionalSoftware method with a constant value
// of 'false'
// 2. Created a new Spring configuration class
// CertifiedProductValidationTestConfig,
// based on the CHPLTestConfig class
// 3. In the new config class, specify that the CertificationResultManager bean
// should
// use an instance of MyCertificationResultManager.
// 4. Modify this test class to use the new spring configuration that was just
// created:
// @ContextConfiguration(classes = { CertifiedProductValidationTestConfig.class
// })
@Configuration
@Import(gov.healthit.chpl.CHPLTestConfig.class)
class CertifiedProductValidationTestConfig {
    @Bean
    @Primary
    public CertificationResultManager certificationResultManager() {
        return new MyCertificationResultManager();
    }
}

class MyCertificationResultManager extends CertificationResultManagerImpl {
    @Override
    public boolean getCertifiedProductHasAdditionalSoftware(Long certifiedProductId) {
        return false;
    }
}
///////////////////////////////////////////////////////////////////////////////////////////////////

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CertifiedProductValidationTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductValidationTest {

    private static final String B4_INVALID_TEST_TOOL_NAME_ERROR =
            "Criteria 170.315 (b)(4) contains an invalid test tool 'DOES NOT EXIST'. It has been removed from the pending listing.";
    private static final String B4_RETIRED_TEST_TOOL_NOT_ALLOWED = "Test Tool 'Transport Testing Tool' can not "
            + "be used for criteria '170.315 (b)(4)', as it is a retired tool, and this "
            + "Certified Product does not carry ICS.";
    private static final String B4_MISSING_TEST_TOOL_VERSION_ERROR = "There was no version found for test tool "
            + "Cypress and certification 170.315 (b)(4).";
    private static final String E2E3_D1_MISSING_ERROR = "Certification criterion 170.315 (e)(2) or 170.315 (e)(3) "
            + "was found so 170.315 (d)(1) is required but was not found.";
    private static final String E2E3_D2_MISSING_ERROR = "Certification criterion 170.315 (e)(2) or 170.315 (e)(3) "
            + "was found so 170.315 (d)(2) is required but was not found.";
    private static final String E2E3_D3_MISSING_ERROR = "Certification criterion 170.315 (e)(2) or 170.315 (e)(3) "
            + "was found so 170.315 (d)(3) is required but was not found.";
    private static final String E2E3_D5_MISSING_ERROR = "Certification criterion 170.315 (e)(2) or 170.315 (e)(3) "
            + "was found so 170.315 (d)(5) is required but was not found.";
    private static final String E2E3_D9_MISSING_ERROR = "Certification criterion 170.315 (e)(2) or 170.315 (e)(3) "
            + "was found so 170.315 (d)(9) is required but was not found.";
    private static final String G7G8G9_D1_MISSING_ERROR = "Certification criterion 170.315 (g)(7) or 170.315 (g)(8) "
            + "or 170.315 (g)(9) was found so 170.315 (d)(1) is required but was not found.";
    private static final String G7G8G9_D9_MISSING_ERROR = "Certification criterion 170.315 (g)(7) or 170.315 (g)(8) "
            + "or 170.315 (g)(9) was found so 170.315 (d)(9) is required but was not found.";
    private static final String G7G8G9_D2D10_MISSING_ERROR = "Certification criterion 170.315 (g)(7) or 170.315 (g)(8) "
            + "or 170.315 (g)(9) was found so 170.315 (d)(2) or 170.315 (d)(10) is required but was not found.";
    private static final String MISSING_G1_MACRA_ERROR = "Listing has attested to (g)(1), "
            + "but no measures have been successfully tested for (g)(1).";
    private static final String MISSING_G2_MACRA_ERROR = "Listing has attested to (g)(2), "
            + "but no measures have been successfully tested for (g)(2).";
    private static final String SED_UCD_MISMATCH_ERROR = "We changed your pending listing to set the SED boolean to "
            + "be true for criteria 170.314 (a)(1) because UCD processes were included for that criteria.";
    private static final String B_3 = "170.315 (b)(3)";
    private static final String MISSING_TEST_TASK = "Certification 170.315 (b)(3) requires at least one test task.";
    private static final String MISSING_UCD_PROCESS = "Certification 170.315 (b)(3) requires at least one UCD process.";

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Autowired
    MacraMeasureDAO mmDao;

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

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingMissingTestToolVersionHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (b)(4)");
        PendingCertificationResultTestToolDTO existingTestTool = new PendingCertificationResultTestToolDTO();
        existingTestTool.setName("Cypress");
        pendingCertResult.getTestTools().add(existingTestTool);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getErrorMessages().contains(B4_MISSING_TEST_TOOL_VERSION_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingWithTestToolVersionNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (b)(4)");
        PendingCertificationResultTestToolDTO existingTestTool = new PendingCertificationResultTestToolDTO();
        existingTestTool.setName("Cypress");
        existingTestTool.setVersion("1.0.0");
        pendingCertResult.getTestTools().add(existingTestTool);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(B4_MISSING_TEST_TOOL_VERSION_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingBadTestToolNameHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (b)(4)");
        PendingCertificationResultTestToolDTO nonexistentTestTool = new PendingCertificationResultTestToolDTO();
        nonexistentTestTool.setName("DOES NOT EXIST");
        pendingCertResult.getTestTools().add(nonexistentTestTool);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getErrorMessages().contains(B4_INVALID_TEST_TOOL_NAME_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingTestToolNameNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (b)(4)");
        PendingCertificationResultTestToolDTO existingTestTool = new PendingCertificationResultTestToolDTO();
        existingTestTool.setName("Cypress");
        pendingCertResult.getTestTools().add(existingTestTool);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(B4_INVALID_TEST_TOOL_NAME_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingRetiredTestToolNoIcsHasError()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        pendingListing.setIcs(Boolean.FALSE);
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (b)(4)");
        PendingCertificationResultTestToolDTO existingTestTool = new PendingCertificationResultTestToolDTO();
        existingTestTool.setName("Transport Testing Tool");
        pendingCertResult.getTestTools().add(existingTestTool);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getWarningMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
        assertTrue(pendingListing.getErrorMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingRetiredTestToolIcsConflictHasWarning()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        pendingListing.setIcs(Boolean.TRUE);
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (b)(4)");
        PendingCertificationResultTestToolDTO existingTestTool = new PendingCertificationResultTestToolDTO();
        existingTestTool.setName("Transport Testing Tool");
        pendingCertResult.getTestTools().add(existingTestTool);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getWarningMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
        assertTrue(pendingListing.getErrorMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingRetiredTestToolHasIcsNoError()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        pendingListing.setUniqueId("15.07.07.2642.IC04.36.01.1.160402");
        pendingListing.setIcs(Boolean.TRUE);
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (b)(4)");
        PendingCertificationResultTestToolDTO existingTestTool = new PendingCertificationResultTestToolDTO();
        existingTestTool.setName("Transport Testing Tool");
        pendingCertResult.getTestTools().add(existingTestTool);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getWarningMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
        assertFalse(pendingListing.getErrorMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingE2ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (e)(2)");
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D1_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D2_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D3_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D5_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D9_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingE3ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (e)(3)");
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D1_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D2_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D3_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D5_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(E2E3_D9_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingE2ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (e)(2)");
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO pendingCertResultD1 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(1)");
        pendingCertResults.add(pendingCertResultD1);
        PendingCertificationResultDTO pendingCertResultD2 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(2)");
        pendingCertResults.add(pendingCertResultD2);
        PendingCertificationResultDTO pendingCertResultD3 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(3)");
        pendingCertResults.add(pendingCertResultD3);
        PendingCertificationResultDTO pendingCertResultD5 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(5)");
        pendingCertResults.add(pendingCertResultD5);
        PendingCertificationResultDTO pendingCertResultD9 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(9)");
        pendingCertResults.add(pendingCertResultD9);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D1_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D2_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D3_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D5_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D9_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingE3ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (e)(3)");
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO pendingCertResultD1 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(1)");
        pendingCertResults.add(pendingCertResultD1);
        PendingCertificationResultDTO pendingCertResultD2 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(2)");
        pendingCertResults.add(pendingCertResultD2);
        PendingCertificationResultDTO pendingCertResultD3 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(3)");
        pendingCertResults.add(pendingCertResultD3);
        PendingCertificationResultDTO pendingCertResultD5 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(5)");
        pendingCertResults.add(pendingCertResultD5);
        PendingCertificationResultDTO pendingCertResultD9 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(9)");
        pendingCertResults.add(pendingCertResultD9);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D1_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D2_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D3_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D5_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(E2E3_D9_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingG7ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(7)");
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingG8ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(8)");
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingG9ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(9)");
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertTrue(pendingListing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingG7ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(7)");
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO pendingCertResultD1 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(1)");
        pendingCertResults.add(pendingCertResultD1);
        PendingCertificationResultDTO pendingCertResultD9 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(9)");
        pendingCertResults.add(pendingCertResultD9);
        PendingCertificationResultDTO pendingCertResultD2 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(2)");
        pendingCertResults.add(pendingCertResultD2);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingG8ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(8)");
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO pendingCertResultD1 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(1)");
        pendingCertResults.add(pendingCertResultD1);
        PendingCertificationResultDTO pendingCertResultD9 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(9)");
        pendingCertResults.add(pendingCertResultD9);
        PendingCertificationResultDTO pendingCertResultD10 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(10)");
        pendingCertResults.add(pendingCertResultD10);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingG9ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(9)");
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO pendingCertResultD1 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(1)");
        pendingCertResults.add(pendingCertResultD1);
        PendingCertificationResultDTO pendingCertResultD9 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(9)");
        pendingCertResults.add(pendingCertResultD9);
        PendingCertificationResultDTO pendingCertResultD10 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (d)(10)");
        pendingCertResults.add(pendingCertResultD10);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertFalse(pendingListing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingSedUcdMismatchHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2014");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.314 (a)(1)");
        pendingCertResult.setSed(Boolean.FALSE);
        PendingCertificationResultUcdProcessDTO pendingUcd = new PendingCertificationResultUcdProcessDTO();
        pendingUcd.setUcdProcessDetails("UCD Process Details");
        pendingUcd.setUcdProcessName("UCD Process Name");
        pendingCertResult.getUcdProcesses().add(pendingUcd);
        pendingCertResults.add(pendingCertResult);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getWarningMessages().contains(SED_UCD_MISMATCH_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateSedUcdMismatchHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2014");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.314 (a)(1)");
        certResult.setSed(Boolean.FALSE);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);
        CertifiedProductSed sed = new CertifiedProductSed();
        UcdProcess ucdProcess = new UcdProcess();
        ucdProcess.setDetails("UCD Process Details");
        ucdProcess.setName("UCD Process Name");
        CertificationCriterion criteria = new CertificationCriterion();
        criteria.setCertificationEdition("2014");
        criteria.setCertificationEditionId(CertifiedProductValidationTestHelper.EDITION_2014_ID);
        criteria.setNumber("170.314 (a)(1)");
        ucdProcess.getCriteria().add(criteria);
        sed.getUcdProcesses().add(ucdProcess);
        listing.setSed(sed);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getWarningMessages().contains(SED_UCD_MISMATCH_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateMissingTestToolVersionHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (b)(4)");
        CertificationResultTestTool existingTestTool = new CertificationResultTestTool();
        existingTestTool.setTestToolName("Cypress");
        certResult.getTestToolsUsed().add(existingTestTool);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getErrorMessages().contains(B4_MISSING_TEST_TOOL_VERSION_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateWithTestToolVersionNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (b)(4)");
        CertificationResultTestTool existingTestTool = new CertificationResultTestTool();
        existingTestTool.setTestToolName("Cypress");
        existingTestTool.setTestToolVersion("1.0.0");
        certResult.getTestToolsUsed().add(existingTestTool);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getErrorMessages().contains(B4_MISSING_TEST_TOOL_VERSION_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateBadTestToolNameHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (b)(4)");
        CertificationResultTestTool nonexistentTestTool = new CertificationResultTestTool();
        nonexistentTestTool.setTestToolName("DOES NOT EXIST");
        certResult.getTestToolsUsed().add(nonexistentTestTool);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getErrorMessages().contains(B4_INVALID_TEST_TOOL_NAME_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateTestToolNameNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (b)(4)");
        CertificationResultTestTool existingTestTool = new CertificationResultTestTool();
        existingTestTool.setTestToolName("Cypress");
        certResult.getTestToolsUsed().add(existingTestTool);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getErrorMessages().contains(B4_INVALID_TEST_TOOL_NAME_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateRetiredTestToolNoIcsHasError()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (b)(4)");
        CertificationResultTestTool existingTestTool = new CertificationResultTestTool();
        existingTestTool.setTestToolName("Transport Testing Tool");
        certResult.getTestToolsUsed().add(existingTestTool);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getWarningMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
        assertTrue(listing.getErrorMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateRetiredTestToolIcsConflictHasWarning()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.TRUE);
        listing.setIcs(ics);
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (b)(4)");
        CertificationResultTestTool existingTestTool = new CertificationResultTestTool();
        existingTestTool.setTestToolName("Transport Testing Tool");
        certResult.getTestToolsUsed().add(existingTestTool);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getWarningMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
        assertTrue(listing.getErrorMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateRetiredTestToolHasIcsNoError()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        listing.setChplProductNumber("15.07.07.2642.IC04.36.01.1.160402");
        InheritedCertificationStatus ics = new InheritedCertificationStatus();
        ics.setInherits(Boolean.TRUE);
        listing.setIcs(ics);
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (b)(4)");
        CertificationResultTestTool existingTestTool = new CertificationResultTestTool();
        existingTestTool.setTestToolName("Transport Testing Tool");
        certResult.getTestToolsUsed().add(existingTestTool);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getWarningMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
        assertFalse(listing.getErrorMessages().contains(B4_RETIRED_TEST_TOOL_NOT_ALLOWED));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateE2ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (e)(2)");
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getErrorMessages().contains(E2E3_D1_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(E2E3_D2_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(E2E3_D3_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(E2E3_D5_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(E2E3_D9_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateE3ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (e)(3)");
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getErrorMessages().contains(E2E3_D1_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(E2E3_D2_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(E2E3_D3_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(E2E3_D5_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(E2E3_D9_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateE2ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (e)(2)");
        certResults.add(certResult);
        CertificationResult certResultD1 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(1)");
        certResults.add(certResultD1);
        CertificationResult certResultD2 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(2)");
        certResults.add(certResultD2);
        CertificationResult certResultD3 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(3)");
        certResults.add(certResultD3);
        CertificationResult certResultD5 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(5)");
        certResults.add(certResultD5);
        CertificationResult certResultD9 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(9)");
        certResults.add(certResultD9);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getErrorMessages().contains(E2E3_D1_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(E2E3_D2_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(E2E3_D3_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(E2E3_D5_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(E2E3_D9_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateE3ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (e)(3)");
        certResults.add(certResult);
        CertificationResult certResultD1 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(1)");
        certResults.add(certResultD1);
        CertificationResult certResultD2 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(2)");
        certResults.add(certResultD2);
        CertificationResult certResultD3 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(3)");
        certResults.add(certResultD3);
        CertificationResult certResultD5 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(5)");
        certResults.add(certResultD5);
        CertificationResult certResultD9 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(9)");
        certResults.add(certResultD9);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getErrorMessages().contains(E2E3_D1_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(E2E3_D2_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(E2E3_D3_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(E2E3_D5_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(E2E3_D9_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateG7ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(7)");
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateG8ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(8)");
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateG9ComplimentaryCertsHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(9)");
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertTrue(listing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateG7ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(7)");
        certResults.add(certResult);
        CertificationResult certResultD1 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(1)");
        certResults.add(certResultD1);
        CertificationResult certResultD9 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(9)");
        certResults.add(certResultD9);
        CertificationResult certResultD2 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(2)");
        certResults.add(certResultD2);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateG8ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(8)");
        certResults.add(certResult);
        CertificationResult certResultD1 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(1)");
        certResults.add(certResultD1);
        CertificationResult certResultD9 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(9)");
        certResults.add(certResultD9);
        CertificationResult certResultD10 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(10)");
        certResults.add(certResultD10);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateG9ComplimentaryCertsNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(9)");
        certResults.add(certResult);
        CertificationResult certResultD1 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(1)");
        certResults.add(certResultD1);
        CertificationResult certResultD9 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(9)");
        certResults.add(certResultD9);
        CertificationResult certResultD10 = CertifiedProductValidationTestHelper.createCertResult("170.315 (d)(10)");
        certResults.add(certResultD10);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getErrorMessages().contains(G7G8G9_D1_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(G7G8G9_D9_MISSING_ERROR));
        assertFalse(listing.getErrorMessages().contains(G7G8G9_D2D10_MISSING_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingHasG1MissingG1MacrasHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(1)");
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO pendingCertResult2 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (a)(3)");
        pendingCertResults.add(pendingCertResult2);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getErrorMessages().contains(MISSING_G1_MACRA_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingHasG2MissingG2MacrasHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(2)");
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO pendingCertResult2 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (a)(3)");
        pendingCertResults.add(pendingCertResult2);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertTrue(pendingListing.getErrorMessages().contains(MISSING_G2_MACRA_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingHasG1HasG1MacrasNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        final Long macraMeasureId = 98L;
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(1)");
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO pendingCertResult2 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (a)(3)");
        PendingCertificationResultMacraMeasureDTO crmm = new PendingCertificationResultMacraMeasureDTO();
        crmm.setEnteredValue("EH/CAH Stage 3");
        crmm.setMacraMeasureId(macraMeasureId);
        MacraMeasureDTO mmDto = mmDao.getByCriteriaNumberAndValue(pendingCertResult2.getNumber(),
                crmm.getEnteredValue());
        crmm.setMacraMeasure(mmDto);
        pendingCertResult2.getG1MacraMeasures().add(crmm);
        pendingCertResults.add(pendingCertResult2);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(MISSING_G1_MACRA_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validatePendingHasG2HasG2MacrasNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        final Long macraMeasureId = 98L;
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        PendingCertifiedProductDTO pendingListing = CertifiedProductValidationTestHelper.createPendingListing("2015");
        List<PendingCertificationResultDTO> pendingCertResults = new ArrayList<PendingCertificationResultDTO>();
        PendingCertificationResultDTO pendingCertResult = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (g)(2)");
        pendingCertResults.add(pendingCertResult);
        PendingCertificationResultDTO pendingCertResult2 = CertifiedProductValidationTestHelper
                .createPendingCertResult("170.315 (a)(3)");
        PendingCertificationResultMacraMeasureDTO crmm = new PendingCertificationResultMacraMeasureDTO();
        crmm.setEnteredValue("EH/CAH Stage 3");
        crmm.setMacraMeasureId(macraMeasureId);
        MacraMeasureDTO mmDto = mmDao.getByCriteriaNumberAndValue(pendingCertResult2.getNumber(),
                crmm.getEnteredValue());
        crmm.setMacraMeasure(mmDto);
        pendingCertResult2.getG2MacraMeasures().add(crmm);
        pendingCertResults.add(pendingCertResult2);
        pendingListing.setCertificationCriterion(pendingCertResults);

        PendingValidator validator = validatorFactory.getValidator(pendingListing);
        if (validator != null) {
            validator.validate(pendingListing);
        }

        assertFalse(pendingListing.getErrorMessages().contains(MISSING_G2_MACRA_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateHasG1MissingG1MacrasHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(1)");
        certResults.add(certResult);
        CertificationResult certResult2 = CertifiedProductValidationTestHelper.createCertResult("170.315 (a)(3)");
        certResults.add(certResult2);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getErrorMessages().contains(MISSING_G1_MACRA_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateHasG2MissingG2MacrasHasExpectedErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(2)");
        certResults.add(certResult);
        CertificationResult certResult2 = CertifiedProductValidationTestHelper.createCertResult("170.315 (a)(3)");
        certResults.add(certResult2);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertTrue(listing.getErrorMessages().contains(MISSING_G2_MACRA_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateHasG1HasG1MacrasNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        final Long macraMeasureId = 98L;
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(1)");
        certResults.add(certResult);
        CertificationResult certResult2 = CertifiedProductValidationTestHelper.createCertResult("170.315 (a)(3)");
        PendingCertificationResultMacraMeasureDTO crmm = new PendingCertificationResultMacraMeasureDTO();
        crmm.setEnteredValue("EH/CAH Stage 3");
        crmm.setMacraMeasureId(macraMeasureId);
        MacraMeasureDTO mmDto = mmDao.getByCriteriaNumberAndValue(certResult2.getNumber(), crmm.getEnteredValue());
        crmm.setMacraMeasure(mmDto);
        certResult2.getG1MacraMeasures().add(new MacraMeasure(mmDto));
        certResults.add(certResult2);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getErrorMessages().contains(MISSING_G1_MACRA_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateHasG2HasG2MacrasNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        final Long macraMeasureId = 98L;
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult("170.315 (g)(2)");
        certResults.add(certResult);
        CertificationResult certResult2 = CertifiedProductValidationTestHelper.createCertResult("170.315 (a)(3)");
        PendingCertificationResultMacraMeasureDTO crmm = new PendingCertificationResultMacraMeasureDTO();
        crmm.setEnteredValue("EH/CAH Stage 3");
        crmm.setMacraMeasureId(macraMeasureId);
        MacraMeasureDTO mmDto = mmDao.getByCriteriaNumberAndValue(certResult2.getNumber(), crmm.getEnteredValue());
        crmm.setMacraMeasure(mmDto);
        certResult2.getG2MacraMeasures().add(new MacraMeasure(mmDto));
        certResults.add(certResult2);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        assertFalse(listing.getErrorMessages().contains(MISSING_G2_MACRA_ERROR));
    }

    @Transactional
    @Rollback(true)
    @Test
    public void validateSEDFalseNoErrors()
            throws EntityRetrievalException, EntityCreationException, IOException, ParseException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);
        CertifiedProductSearchDetails listing = CertifiedProductValidationTestHelper.createListing("2015");
        List<CertificationResult> certResults = new ArrayList<CertificationResult>();
        CertificationResult certResult = CertifiedProductValidationTestHelper.createCertResult(B_3);
        certResult.setSed(false);
        certResults.add(certResult);
        listing.setCertificationResults(certResults);

        Validator validator = validatorFactory.getValidator(listing);
        if (validator != null) {
            validator.validate(listing);
        }

        System.out.println(listing.getErrorMessages());
        assertFalse(listing.getErrorMessages().contains(MISSING_UCD_PROCESS));
        assertFalse(listing.getErrorMessages().contains(MISSING_TEST_TASK));
    }
}
