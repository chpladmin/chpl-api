package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.HashMap;

import org.ff4j.FF4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.rules.developer.CHPLTestDeveloperValidationConfig;
import gov.healthit.chpl.manager.rules.developer.DeveloperValidationFactory;
import gov.healthit.chpl.manager.rules.developer.DeveloperValidationFactoryTest;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CHPLTestDeveloperValidationConfig.class
})
public class DeveloperManagerMockitoTest {
    private static final String DEFAULT_TEST_PRODUCT_NUMBER = "15.04.04.1234.tes1.03.00.1.170102";
    private static final String NEW_DEV_CODE_IN_PRODUCT_NUMBER = "15.04.04." + DeveloperManager.NEW_DEVELOPER_CODE
            + ".tes1.03.00.1.170102";
    private static final Long DEFAULT_TEST_DEVELOPER_ID = 5L;
    private static final String VALIDATION_EXCEPTION_NOT_EXPECTED = "ValidationException was thrown but was NOT EXPECTED.";

    @Spy
    ChplProductNumberUtil chplProductNumberUtil;

    @Mock
    private DeveloperDAO developerDao;

    @Mock
    private ResourcePermissions resourcePermissions;

    @Autowired
    private FF4j ff4j;

    @Mock
    private CertificationBodyManager acbManager;

    @Spy
    private DeveloperValidationFactory developerValidationFactory;

    @Spy
    private ErrorMessageUtil msgUtil;

    @Spy
    @InjectMocks
    private DeveloperManager developerManager;

    private DeveloperDTO sysDev;
    private PendingCertifiedProductDetails pendingCp;

    public static final String PENDING_ACB_NAME_NULL_OR_EMPTY;

    static {
        ErrorMessageUtil msgUtil = new ErrorMessageUtil(CHPLTestDeveloperValidationConfig.messageSource());
        PENDING_ACB_NAME_NULL_OR_EMPTY = msgUtil.getMessage("system.developer.pendingACBNameNullOrEmpty");
    }

    @Before
    public void setup() {
        msgUtil = Mockito.spy((new ErrorMessageUtil(CHPLTestDeveloperValidationConfig.messageSource())));
        developerValidationFactory = Mockito.spy(new DeveloperValidationFactory(ff4j, resourcePermissions));
        MockitoAnnotations.initMocks(this);

        sysDev = getPopulatedDeveloperDTO();
        pendingCp = getPopulatedPendingCertifiedProductDetails();
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_fullyValid() throws EntityRetrievalException {
        Mockito.doReturn(getPopulatedDeveloperDTO()).when(developerManager).getById(ArgumentMatchers.anyLong());
        systemValidationExpectNoExceptionOrErrorsTester();
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_isNewDeveloperCode() throws EntityRetrievalException {
        pendingCp.setChplProductNumber(NEW_DEV_CODE_IN_PRODUCT_NUMBER);
        systemValidationExpectNoExceptionOrErrorsTester();
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_nullPendingCpDeveloper() throws EntityRetrievalException {
        pendingCp.setDeveloper(null);
        systemValidationExpectNoExceptionOrErrorsTester();
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_nullPendingCpDeveloperId() throws EntityRetrievalException {
        pendingCp.getDeveloper().setDeveloperId(null);
        systemValidationExpectNoExceptionOrErrorsTester();
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_nullPendingAcbNameObj() throws EntityRetrievalException {
        pendingCp.setCertifyingBody(new HashMap<String, Object>());
        Mockito.doReturn(sysDev).when(developerManager).getById(ArgumentMatchers.anyLong());
        systemValidationPropertyTester(PENDING_ACB_NAME_NULL_OR_EMPTY);
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_contact() throws EntityRetrievalException {
        sysDev.setContact(null);
        Mockito.doReturn(sysDev).when(developerManager).getById(ArgumentMatchers.anyLong());
        systemValidationPropertyTester(DeveloperValidationFactoryTest.CONTACT_REQUIRED);
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_address() throws EntityRetrievalException {
        sysDev.setAddress(null);
        systemValidationPropertyTester(DeveloperValidationFactoryTest.ADDRESS_REQUIRED);
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_website() throws EntityRetrievalException {
        sysDev.setWebsite(null);
        systemValidationPropertyTester(DeveloperValidationFactoryTest.WEBSITE_REQUIRED);
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_transparencyAttestationNotMatching()
            throws EntityRetrievalException {
        sysDev.getTransparencyAttestationMappings().get(0).setAcbName("hasNoMatch");
        systemValidationPropertyTester(DeveloperValidationFactoryTest.TRANSPARENCY_ATTESTATION_NOT_MATCHING);
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_transparencyAttestationEmptyMapping()
            throws EntityRetrievalException {
        sysDev.setTransparencyAttestationMappings(new ArrayList<DeveloperACBMapDTO>());
        systemValidationPropertyTester(DeveloperValidationFactoryTest.TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY);
    }

    @Test
    public void testValidateDeveloperInSystemIfExists_transparencyAttestationNullMapping()
            throws EntityRetrievalException {
        sysDev.setTransparencyAttestationMappings(null);
        systemValidationPropertyTester(DeveloperValidationFactoryTest.TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY);
    }

    private void systemValidationExpectNoExceptionOrErrorsTester() throws EntityRetrievalException {
        Mockito.doReturn(getPopulatedDeveloperDTO()).when(developerManager).getById(ArgumentMatchers.anyLong());
        try {
            developerManager.validateDeveloperInSystemIfExists(pendingCp);
        } catch (ValidationException e) {
            Assert.assertTrue(DeveloperValidationFactoryTest.NO_ERRORS_EXPECTED,
                    e.getErrorMessages() != null && e.getErrorMessages().isEmpty());
            Assert.fail(VALIDATION_EXCEPTION_NOT_EXPECTED);
        }
    }

    private void systemValidationPropertyTester(final String expectedErrorMessage) throws EntityRetrievalException {
        Mockito.doReturn(sysDev).when(developerManager).getById(ArgumentMatchers.anyLong());
        try {
            developerManager.validateDeveloperInSystemIfExists(pendingCp);
        } catch (ValidationException e) {
            DeveloperValidationFactoryTest.assertTrueIfContainsErrorMessage(expectedErrorMessage, e.getErrorMessages());
        }
    }

    public static PendingCertifiedProductDetails getPopulatedPendingCertifiedProductDetails() {
        PendingCertifiedProductDetails pcpd = new PendingCertifiedProductDetails();
        pcpd.setChplProductNumber(DEFAULT_TEST_PRODUCT_NUMBER);
        pcpd.setDeveloper(new Developer(getPopulatedDeveloperDTO()));
        pcpd.getCertifyingBody().put(CertifiedProductSearchDetails.ACB_NAME_KEY,
                DeveloperValidationFactoryTest.DEFAULT_PENDING_ACB_NAME);
        return pcpd;
    }

    private static DeveloperDTO getPopulatedDeveloperDTO() {
        DeveloperDTO devDto = DeveloperValidationFactoryTest.getPopulatedDeveloperDTO();
        devDto.setId(DEFAULT_TEST_DEVELOPER_ID);
        return devDto;
    }
}
