package old.gov.healthit.chpl.manager.rules.developer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.TransparencyAttestationDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.manager.rules.developer.DeveloperValidationContext;
import gov.healthit.chpl.manager.rules.developer.DeveloperValidationFactory;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import old.gov.healthit.chpl.CHPLTestConfig;
import old.gov.healthit.chpl.manager.impl.DeveloperStatusTest;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CHPLTestConfig.class, CHPLTestDeveloperValidationConfig.class, ErrorMessageUtil.class, DeveloperValidationFactory.class
})
public class DeveloperValidationFactoryTest {
    @Autowired
    private FF4j ff4j;

    @Mock
    private ResourcePermissions resourcePermissions;

    @Autowired
    @InjectMocks
    private DeveloperValidationFactory developerValidationFactory;

    @Autowired
    private ErrorMessageUtil msgUtil;

    public static final String DEFAULT_PENDING_ACB_NAME = "ACB_NAME";
    private static final String DEFAULT_ATTESTATION = "hasTransparencyAttestation";
    private static final String DEFAULT_DEVELOPER_STATUS_TYPE_LITERAL = "Under certification ban by ONC";
    private static final String RESULTS_SHOULD_HAVE = "The validation results should contain the error: ";
    private static final String RESULTS_SHOULD_NOT_HAVE = "The validation results should NOT contain the error: ";
    public static final String ERRORS_EXPECTED = "There are no validation error messages when there should be";
    public static final String NO_ERRORS_EXPECTED = "There are validation error messages when there should NOT be";

    public static final String NAME_REQUIRED, WEBSITE_REQUIRED, WEBSITE_WELL_FORMED, CONTACT_REQUIRED, CONTACT_NAME_REQUIRED,
            CONTACT_EMAIL_REQUIRED, CONTACT_PHONE_REQUIRED, ADDRESS_REQUIRED, ADDRESS_STREET_REQUIRED, ADDRESS_CITY_REQUIRED,
            ADDRESS_STATE_REQUIRED, ADDRESS_ZIP_REQUIRED, TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY,
            TRANSPARENCY_ATTESTATION_NOT_MATCHING, STATUS_EVENTS_NO_CURRENT, STATUS_EVENTS_DUPLICATE_STATUS,
            ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB;

    static {
        ErrorMessageUtil msgUtil = new ErrorMessageUtil(CHPLTestDeveloperValidationConfig.messageSource());
        NAME_REQUIRED = msgUtil.getMessage("developer.nameRequired");

        WEBSITE_REQUIRED = msgUtil.getMessage("developer.websiteRequired");
        WEBSITE_WELL_FORMED = msgUtil.getMessage("developer.websiteIsInvalid");

        CONTACT_REQUIRED = msgUtil.getMessage("developer.contactRequired");
        CONTACT_NAME_REQUIRED = msgUtil.getMessage("developer.contact.nameRequired");
        CONTACT_EMAIL_REQUIRED = msgUtil.getMessage("developer.contact.emailRequired");
        CONTACT_PHONE_REQUIRED = msgUtil.getMessage("developer.contact.phoneRequired");

        ADDRESS_REQUIRED = msgUtil.getMessage("developer.addressRequired");
        ADDRESS_STREET_REQUIRED = msgUtil.getMessage("developer.address.streetRequired");
        ADDRESS_CITY_REQUIRED = msgUtil.getMessage("developer.address.cityRequired");
        ADDRESS_STATE_REQUIRED = msgUtil.getMessage("developer.address.stateRequired");
        ADDRESS_ZIP_REQUIRED = msgUtil.getMessage("developer.address.zipRequired");

        TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY = msgUtil.getMessage("system.developer.transparencyAttestationIsNullOrEmpty");
        TRANSPARENCY_ATTESTATION_NOT_MATCHING = msgUtil.getMessage("system.developer.transparencyAttestationNotMatching");

        STATUS_EVENTS_NO_CURRENT = msgUtil.getMessage("developer.status.noCurrent");
        STATUS_EVENTS_DUPLICATE_STATUS = msgUtil.getMessage("developer.status.duplicateStatus",
                DEFAULT_DEVELOPER_STATUS_TYPE_LITERAL);

        ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB = msgUtil
                .getMessage("developer.transparencyAttestationEditNotAllowedForRoleACB");
    }

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Mockito.doReturn(false).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
    }

    @Test
    public void validateEmptyDev() {
        DeveloperDTO devDto = new DeveloperDTO();
        Set<String> errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertFalse(ERRORS_EXPECTED, errorMessages.isEmpty());

        assertTrueIfContainsErrorMessage(NAME_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);
        assertTrueIfContainsErrorMessage(STATUS_EVENTS_NO_CURRENT, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_NAME_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);
        assertFalseIfContainsErrorMessage(WEBSITE_WELL_FORMED, errorMessages);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_DUPLICATE_STATUS, errorMessages);
        assertFalseIfContainsErrorMessage(ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB, errorMessages);
    }

    @Test
    public void validateFullDev() {
        Set<String> errorMessages = testAllDeveloperValidations(getPopulatedDeveloperDTO(), DEFAULT_PENDING_ACB_NAME);
        assertTrue(NO_ERRORS_EXPECTED, errorMessages.isEmpty());
    }

    @Test
    public void validateSubContact() {
        DeveloperDTO devDto = new DeveloperDTO();
        devDto.setContact(new ContactDTO());
        Set<String> errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertFalse(ERRORS_EXPECTED, errorMessages.isEmpty());

        assertTrueIfContainsErrorMessage(NAME_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_NAME_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);
        assertTrueIfContainsErrorMessage(STATUS_EVENTS_NO_CURRENT, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);
        assertFalseIfContainsErrorMessage(WEBSITE_WELL_FORMED, errorMessages);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_DUPLICATE_STATUS, errorMessages);
        assertFalseIfContainsErrorMessage(ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB, errorMessages);
    }

    @Test
    public void validateSubAddress() {
        DeveloperDTO devDto = new DeveloperDTO();
        devDto.setAddress(new AddressDTO());
        Set<String> errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertFalse(ERRORS_EXPECTED, errorMessages.isEmpty());

        assertTrueIfContainsErrorMessage(NAME_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);
        assertTrueIfContainsErrorMessage(STATUS_EVENTS_NO_CURRENT, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_NAME_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);
        assertFalseIfContainsErrorMessage(WEBSITE_WELL_FORMED, errorMessages);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_DUPLICATE_STATUS, errorMessages);
        assertFalseIfContainsErrorMessage(ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB, errorMessages);
    }

    @Test
    public void validateWebsite() {
        // valid website
        DeveloperDTO devDto = getPopulatedDeveloperDTO();
        Set<String> errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrue(NO_ERRORS_EXPECTED, errorMessages.isEmpty());

        // null website
        devDto.setWebsite(null);
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(WEBSITE_WELL_FORMED, errorMessages);

        // empty website
        devDto.setWebsite("");
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(WEBSITE_WELL_FORMED, errorMessages);

        // invalid website
        devDto.setWebsite("xyz.abc");
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(WEBSITE_WELL_FORMED, errorMessages);
        assertFalseIfContainsErrorMessage(WEBSITE_REQUIRED, errorMessages);
    }

    @Test
    public void validateTransparencyAttestation() {
        DeveloperDTO devDto = new DeveloperDTO();
        devDto.setTransparencyAttestationMappings(getDefaultTransparencyAttestationMappings());
        Set<String> errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertFalse(ERRORS_EXPECTED, errorMessages.isEmpty());

        assertTrueIfContainsErrorMessage(NAME_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(STATUS_EVENTS_NO_CURRENT, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_NAME_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);
        assertFalseIfContainsErrorMessage(WEBSITE_WELL_FORMED, errorMessages);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_DUPLICATE_STATUS, errorMessages);
        assertFalseIfContainsErrorMessage(ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB, errorMessages);

        // null mapping
        devDto.setTransparencyAttestationMappings(null);
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);

        // empty mapping
        devDto.setTransparencyAttestationMappings(new ArrayList<DeveloperACBMapDTO>());
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);

        // null ACB name
        List<DeveloperACBMapDTO> mappings = new ArrayList<DeveloperACBMapDTO>();
        DeveloperACBMapDTO attestation = new DeveloperACBMapDTO();
        attestation.setAcbName(null);
        mappings.add(attestation);
        devDto.setTransparencyAttestationMappings(mappings);
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);

        // empty ACB name
        devDto.getTransparencyAttestationMappings().get(0).setAcbName("");
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);

        // has match but no actual attestation string
        devDto.getTransparencyAttestationMappings().get(0).setAcbName(DEFAULT_PENDING_ACB_NAME);
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);
    }

    @Test
    public void validateStatusEvents() {
        // valid status events
        DeveloperDTO devDto = getPopulatedDeveloperDTO();
        Set<String> errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrue(NO_ERRORS_EXPECTED, errorMessages.isEmpty());

        // null status events
        devDto.setStatusEvents(null);
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(STATUS_EVENTS_NO_CURRENT, errorMessages);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_DUPLICATE_STATUS, errorMessages);

        // empty list in status events
        devDto.setStatusEvents(Arrays.asList());
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(STATUS_EVENTS_NO_CURRENT, errorMessages);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_DUPLICATE_STATUS, errorMessages);

        // default constructor status events (technically error-free as
        // per current validation code) so testing as is
        // TODO: Determine if existing validation should be expanded to handle
        // rejecting an empty object - if so, update impl and this tests
        // expected result
        devDto.setStatusEvents(Arrays.asList(new DeveloperStatusEventDTO()));
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_NO_CURRENT, errorMessages);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_DUPLICATE_STATUS, errorMessages);

        // duplicate status events
        DeveloperStatusEventDTO devStatusEvent = DeveloperStatusTest.createStatusEvent(2L, 5L,
                DeveloperStatusType.UnderCertificationBanByOnc, new Date(), "A Reason");
        devDto.setStatusEvents(Arrays.asList(devStatusEvent, devStatusEvent));
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(STATUS_EVENTS_DUPLICATE_STATUS, errorMessages);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_NO_CURRENT, errorMessages);
    }

    @Test
    public void validateEditTransparencyAttestation() {
        DeveloperDTO devDto = new DeveloperDTO();
        DeveloperDTO beforeDev = new DeveloperDTO();
        devDto.setTransparencyAttestationMappings(getDefaultTransparencyAttestationMappings());
        Set<String> errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME, beforeDev);
        assertFalse(ERRORS_EXPECTED, errorMessages.isEmpty());

        assertTrueIfContainsErrorMessage(NAME_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_REQUIRED, errorMessages);
        assertTrueIfContainsErrorMessage(STATUS_EVENTS_NO_CURRENT, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_NAME_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, errorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, errorMessages);
        assertFalseIfContainsErrorMessage(WEBSITE_WELL_FORMED, errorMessages);
        assertFalseIfContainsErrorMessage(STATUS_EVENTS_DUPLICATE_STATUS, errorMessages);
        assertFalseIfContainsErrorMessage(ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB, errorMessages);

        // If both the flag is on and the role is AcbAdmin then all rules are
        // enforced/edit error is expected if there are changes
        // Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)).thenReturn(true);
        Mockito.doReturn(false).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        runValidateEditTransparencyAttestationTests(devDto, beforeDev, errorMessages, true);

        // If the flag EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK is off then no edit error is expected
        // Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)).thenReturn(false);
        Mockito.doReturn(false).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(true);
        runValidateEditTransparencyAttestationTests(devDto, beforeDev, errorMessages, false);

        // If the role is not AcbAdmin, then no edit error is expected
        // Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)).thenReturn(true);
        Mockito.doReturn(false).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
        runValidateEditTransparencyAttestationTests(devDto, beforeDev, errorMessages, false);

        // If both the flag is off and the role is not AcbAdmin then no edit error is expected
        // Mockito.when(ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)).thenReturn(false);
        Mockito.doReturn(false).when(ff4j).check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK);
        Mockito.when(resourcePermissions.isUserRoleAcbAdmin()).thenReturn(false);
        runValidateEditTransparencyAttestationTests(devDto, beforeDev, errorMessages, false);
    }

    private void runValidateEditTransparencyAttestationTests(DeveloperDTO devDto, DeveloperDTO beforeDev,
            Set<String> errorMessages, boolean isFlagOnAndRoleAcbAdmin) {
        // No changes as values are identical but not null
        resetDeveloperDTOs(devDto, beforeDev);
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME, beforeDev);
        assertFalseIfContainsErrorMessage(ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB, errorMessages);
        // Pending mapping null only
        resetDeveloperDTOs(devDto, beforeDev);
        devDto.setTransparencyAttestationMappings(null);
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME, beforeDev);
        assertEditTransparencyAttestation(errorMessages, isFlagOnAndRoleAcbAdmin);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, errorMessages);
        // Before mapping null only
        resetDeveloperDTOs(devDto, beforeDev);
        beforeDev.setTransparencyAttestationMappings(null);
        testAllValidationsAndAssertEditTransparencyAttestation(devDto, beforeDev, errorMessages, isFlagOnAndRoleAcbAdmin);
        // No null mappings, AcbName is different, expect edit error
        resetDeveloperDTOs(devDto, beforeDev);
        devDto.getTransparencyAttestationMappings().get(0).setAcbName("changedName");
        testAllValidationsAndAssertEditTransparencyAttestation(devDto, beforeDev, errorMessages, isFlagOnAndRoleAcbAdmin);
        // No null mappings, TransparencyAttestation is different, expect edit error
        resetDeveloperDTOs(devDto, beforeDev);
        devDto.getTransparencyAttestationMappings().get(0)
                .setTransparencyAttestation(new TransparencyAttestationDTO("changedAttestation"));
        testAllValidationsAndAssertEditTransparencyAttestation(devDto, beforeDev, errorMessages, isFlagOnAndRoleAcbAdmin);
        // No null mappings, AcbName is null (was not) - expect edit error
        resetDeveloperDTOs(devDto, beforeDev);
        devDto.getTransparencyAttestationMappings().get(0).setAcbName(null);
        testAllValidationsAndAssertEditTransparencyAttestation(devDto, beforeDev, errorMessages, isFlagOnAndRoleAcbAdmin);
        // No null mappings, TransparencyAttestation is null (was not) - expect edit error
        resetDeveloperDTOs(devDto, beforeDev);
        devDto.getTransparencyAttestationMappings().get(0).setTransparencyAttestation(null);
        testAllValidationsAndAssertEditTransparencyAttestation(devDto, beforeDev, errorMessages, isFlagOnAndRoleAcbAdmin);
        // No null mappings, AcbName is null and TransparencyAttestation is null - no change - so no edit error expected
        resetDeveloperDTOs(devDto, beforeDev);
        devDto.getTransparencyAttestationMappings().get(0).setTransparencyAttestation(null);
        beforeDev.getTransparencyAttestationMappings().get(0).setTransparencyAttestation(null);
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME, beforeDev);
        assertFalseIfContainsErrorMessage(ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB, errorMessages);
    }

    private void resetDeveloperDTOs(DeveloperDTO devDto, DeveloperDTO beforeDev) {
        devDto.setTransparencyAttestationMappings(getDefaultTransparencyAttestationMappings());
        beforeDev.setTransparencyAttestationMappings(getDefaultTransparencyAttestationMappings());
    }

    private void assertEditTransparencyAttestation(Set<String> errorMessages, boolean isFlagOnAndRoleAcbAdmin) {
        if (isFlagOnAndRoleAcbAdmin) {
            assertTrueIfContainsErrorMessage(ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB, errorMessages);
        } else {
            assertFalseIfContainsErrorMessage(ATTESTATION_EDIT_NOT_ALLOWED_FOR_ROLE_ACB, errorMessages);
        }
    }

    private void testAllValidationsAndAssertEditTransparencyAttestation(DeveloperDTO devDto, DeveloperDTO beforeDev,
            Set<String> errorMessages, boolean isFlagOnAndRoleAcbAdmin) {
        errorMessages = testAllDeveloperValidations(devDto, DEFAULT_PENDING_ACB_NAME, beforeDev);
        assertEditTransparencyAttestation(errorMessages, isFlagOnAndRoleAcbAdmin);
    }

    public static void assertTrueIfContainsErrorMessage(String errorMessage, Set<String> errorMessages) {
        assertTrue(RESULTS_SHOULD_HAVE + errorMessage, errorMessages.contains(errorMessage));
    }

    private static void assertFalseIfContainsErrorMessage(String errorMessage, Set<String> errorMessages) {
        assertFalse(RESULTS_SHOULD_NOT_HAVE + errorMessage, errorMessages.contains(errorMessage));
    }

    public static DeveloperDTO getPopulatedDeveloperDTO() {
        DeveloperDTO devDto = new DeveloperDTO();
        devDto.setName("hasName");
        devDto.setWebsite("http://www.hasWebsite.com");

        ContactDTO contact = new ContactDTO();
        contact.setFullName("Full Name");
        contact.setEmail("hasEmail@Server.com");
        contact.setPhoneNumber("5551112222");
        devDto.setContact(contact);

        AddressDTO address = new AddressDTO();
        address.setStreetLineOne("hasStreetLineOne");
        address.setCity("LA");
        address.setState("CA");
        address.setZipcode("55555");
        devDto.setAddress(address);

        devDto.setTransparencyAttestationMappings(getDefaultTransparencyAttestationMappings());

        DeveloperStatusEventDTO devStatusEvent = DeveloperStatusTest.createStatusEvent(2L, 5L,
                DeveloperStatusType.UnderCertificationBanByOnc, new Date(), "A Reason");
        devDto.setStatusEvents(Arrays.asList(devStatusEvent));

        return devDto;
    }

    private static List<DeveloperACBMapDTO> getDefaultTransparencyAttestationMappings() {
        List<DeveloperACBMapDTO> mappings = new ArrayList<DeveloperACBMapDTO>();
        DeveloperACBMapDTO attestation = new DeveloperACBMapDTO();
        attestation.setAcbName(DEFAULT_PENDING_ACB_NAME);
        attestation.setTransparencyAttestation(new TransparencyAttestationDTO(DEFAULT_ATTESTATION));
        mappings.add(attestation);
        return mappings;
    }

    private Set<String> testAllDeveloperValidations(final DeveloperDTO dto, final String pendingAcbName) {
        return testAllDeveloperValidations(dto, pendingAcbName, null);
    }

    private Set<String> testAllDeveloperValidations(final DeveloperDTO dto, final String pendingAcbName,
            final DeveloperDTO beforeDev) {
        List<ValidationRule<DeveloperValidationContext>> rules = new ArrayList<ValidationRule<DeveloperValidationContext>>();
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.NAME));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_REQUIRED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.WEBSITE_WELL_FORMED));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.CONTACT));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.ADDRESS));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.TRANSPARENCY_ATTESTATION));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.STATUS_EVENTS));
        rules.add(developerValidationFactory.getRule(DeveloperValidationFactory.EDIT_TRANSPARENCY_ATTESTATION));
        // TODO: Add remaining missing refactored validations from class
        return testValidations(rules, dto, pendingAcbName, beforeDev);
    }

    private Set<String> testValidations(final List<ValidationRule<DeveloperValidationContext>> rules, final DeveloperDTO dto,
            final String pendingAcbName, final DeveloperDTO beforeDev) {
        Set<String> errorMessages = new HashSet<String>();
        DeveloperValidationContext context = new DeveloperValidationContext(dto, msgUtil, pendingAcbName, beforeDev);

        for (ValidationRule<DeveloperValidationContext> rule : rules) {
            if (!rule.isValid(context)) {
                errorMessages.addAll(rule.getMessages());
            }
        }
        return errorMessages;
    }
}
