package gov.healthit.chpl.validation.developer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import gov.healthit.chpl.CHPLTestValidationConfig;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        CHPLTestValidationConfig.class, DeveloperInSystemIsSavedValidator.class, ErrorMessageUtil.class
})
public class DeveloperInSystemIsSavedValidatorTest {

    @Autowired
    DeveloperInSystemIsSavedValidator sysDevVal;

    public static final String DEFAULT_PENDING_ACB_NAME = "ACB_NAME";
    public static final String RESULTS_SHOULD_HAVE = "The validation results should contain the error: ";
    public static final String RESULTS_SHOULD_NOT_HAVE = "The validation results should NOT contain the error: ";

    private static final String NAME_REQUIRED, WEBSITE_REQUIRED, CONTACT_REQUIRED, CONTACT_NAME_REQUIRED,
            CONTACT_EMAIL_REQUIRED, CONTACT_PHONE_REQUIRED, ADDRESS_REQUIRED, ADDRESS_STREET_REQUIRED,
            ADDRESS_CITY_REQUIRED, ADDRESS_STATE_REQUIRED, ADDRESS_ZIP_REQUIRED,
            TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, TRANSPARENCY_ATTESTATION_NOT_MATCHING;

    static {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:/errors");
        messageSource.setDefaultEncoding("UTF-8");
        ErrorMessageUtil msgUtil = new ErrorMessageUtil(messageSource);
        NAME_REQUIRED = msgUtil.getMessage("system.developer.nameRequired");

        WEBSITE_REQUIRED = msgUtil.getMessage("system.developer.websiteRequired");

        CONTACT_REQUIRED = msgUtil.getMessage("system.developer.contactRequired");
        CONTACT_NAME_REQUIRED = msgUtil.getMessage("system.developer.contact.nameRequired");
        CONTACT_EMAIL_REQUIRED = msgUtil.getMessage("system.developer.contact.emailRequired");
        CONTACT_PHONE_REQUIRED = msgUtil.getMessage("system.developer.contact.phoneRequired");

        ADDRESS_REQUIRED = msgUtil.getMessage("system.developer.addressRequired");
        ADDRESS_STREET_REQUIRED = msgUtil.getMessage("system.developer.address.streetRequired");
        ADDRESS_CITY_REQUIRED = msgUtil.getMessage("system.developer.address.cityRequired");
        ADDRESS_STATE_REQUIRED = msgUtil.getMessage("system.developer.address.stateRequired");
        ADDRESS_ZIP_REQUIRED = msgUtil.getMessage("system.developer.address.zipRequired");

        TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY = msgUtil
                .getMessage("system.developer.transparencyAttestationIsNullOrEmpty");
        TRANSPARENCY_ATTESTATION_NOT_MATCHING = msgUtil
                .getMessage("system.developer.transparencyAttestationNotMatching");
    }

    @Test
    public void validateEmptySysDev() {
        DeveloperDTO sysDevDTO = new DeveloperDTO();
        Set<String> sysDevErrorMessages = sysDevVal.validate(sysDevDTO, DEFAULT_PENDING_ACB_NAME);

        assertFalse(
                "There are no validation error messages when there should be as the system developer has no data set",
                sysDevErrorMessages.isEmpty());

        assertTrueIfContainsErrorMessage(NAME_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_NAME_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, sysDevErrorMessages);
    }

    @Test
    public void validateFullSysDev() {
        Set<String> sysDevErrorMessages = sysDevVal.validate(getPopulatedDeveloperDTO(), DEFAULT_PENDING_ACB_NAME);
        System.out.println(sysDevErrorMessages);
        assertTrue("There are validation error messages when there should NOT be as the system developer has "
                + "all required data populated, and, in a valid manner", sysDevErrorMessages.isEmpty());

        assertFalseIfContainsErrorMessage(NAME_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(WEBSITE_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_NAME_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, sysDevErrorMessages);
    }

    @Test
    public void validateSubContact() {
        DeveloperDTO sysDevDTO = new DeveloperDTO();
        sysDevDTO.setContact(new ContactDTO());
        Set<String> sysDevErrorMessages = sysDevVal.validate(sysDevDTO, DEFAULT_PENDING_ACB_NAME);

        assertFalse(
                "There are no validation error messages when there should be as the system developer has no data set",
                sysDevErrorMessages.isEmpty());

        assertTrueIfContainsErrorMessage(NAME_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_NAME_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, sysDevErrorMessages);
    }

    @Test
    public void validateSubAddress() {
        DeveloperDTO sysDevDTO = new DeveloperDTO();
        sysDevDTO.setAddress(new AddressDTO());
        Set<String> sysDevErrorMessages = sysDevVal.validate(sysDevDTO, DEFAULT_PENDING_ACB_NAME);

        assertFalse(
                "There are no validation error messages when there should be as the system developer has no data set",
                sysDevErrorMessages.isEmpty());

        assertTrueIfContainsErrorMessage(NAME_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_NAME_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, sysDevErrorMessages);
    }

    @Test
    public void validateTransparencyAttestation() {
        DeveloperDTO sysDevDTO = new DeveloperDTO();
        sysDevDTO.setTransparencyAttestationMappings(getDefaultTransparencyAttestationMappings());
        Set<String> sysDevErrorMessages = sysDevVal.validate(sysDevDTO, DEFAULT_PENDING_ACB_NAME);

        assertFalse(
                "There are no validation error messages when there should be as the system developer has no data set",
                sysDevErrorMessages.isEmpty());

        assertTrueIfContainsErrorMessage(NAME_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(WEBSITE_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(CONTACT_REQUIRED, sysDevErrorMessages);
        assertTrueIfContainsErrorMessage(ADDRESS_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_NAME_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_EMAIL_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(CONTACT_PHONE_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STREET_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_CITY_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_STATE_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(ADDRESS_ZIP_REQUIRED, sysDevErrorMessages);
        assertFalseIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, sysDevErrorMessages);

        // null mapping
        sysDevDTO.setTransparencyAttestationMappings(null);
        sysDevErrorMessages = sysDevVal.validate(sysDevDTO, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, sysDevErrorMessages);

        // empty mapping
        sysDevDTO.setTransparencyAttestationMappings(new ArrayList<DeveloperACBMapDTO>());
        sysDevErrorMessages = sysDevVal.validate(sysDevDTO, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_IS_NULL_OR_EMPTY, sysDevErrorMessages);

        // null ACB name
        List<DeveloperACBMapDTO> mappings = new ArrayList<DeveloperACBMapDTO>();
        DeveloperACBMapDTO attestation = new DeveloperACBMapDTO();
        attestation.setAcbName(null);
        mappings.add(attestation);
        sysDevDTO.setTransparencyAttestationMappings(mappings);
        sysDevErrorMessages = sysDevVal.validate(sysDevDTO, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, sysDevErrorMessages);

        // empty ACB name
        sysDevDTO.getTransparencyAttestationMappings().get(0).setAcbName("");
        sysDevErrorMessages = sysDevVal.validate(sysDevDTO, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, sysDevErrorMessages);

        // has match but no actual attestation string
        sysDevDTO.getTransparencyAttestationMappings().get(0).setAcbName(DEFAULT_PENDING_ACB_NAME);
        sysDevErrorMessages = sysDevVal.validate(sysDevDTO, DEFAULT_PENDING_ACB_NAME);
        assertTrueIfContainsErrorMessage(TRANSPARENCY_ATTESTATION_NOT_MATCHING, sysDevErrorMessages);
    }

    private static void assertTrueIfContainsErrorMessage(String errorMessage, Set<String> sysDevErrorMessages) {
        assertTrue(RESULTS_SHOULD_HAVE + errorMessage, sysDevErrorMessages.contains(errorMessage));
    }

    private static void assertFalseIfContainsErrorMessage(String errorMessage, Set<String> sysDevErrorMessages) {
        assertFalse(RESULTS_SHOULD_NOT_HAVE + errorMessage, sysDevErrorMessages.contains(errorMessage));
    }

    private static DeveloperDTO getPopulatedDeveloperDTO() {
        DeveloperDTO sysDevDTO = new DeveloperDTO();
        sysDevDTO.setName("hasName");
        sysDevDTO.setWebsite("www.hasWebsite.com");

        ContactDTO contact = new ContactDTO();
        contact.setFullName("Full Name");
        contact.setEmail("hasEmail@Server.com");
        contact.setPhoneNumber("5551112222");
        sysDevDTO.setContact(contact);

        AddressDTO address = new AddressDTO();
        address.setStreetLineOne("hasStreetLineOne");
        address.setCity("LA");
        address.setState("CA");
        address.setZipcode("55555");
        sysDevDTO.setAddress(address);

        sysDevDTO.setTransparencyAttestationMappings(getDefaultTransparencyAttestationMappings());

        return sysDevDTO;
    }

    private static List<DeveloperACBMapDTO> getDefaultTransparencyAttestationMappings() {
        List<DeveloperACBMapDTO> mappings = new ArrayList<DeveloperACBMapDTO>();
        DeveloperACBMapDTO attestation = new DeveloperACBMapDTO();
        attestation.setAcbName(DEFAULT_PENDING_ACB_NAME);
        attestation.setTransparencyAttestation("hasTransparencyAttestation");
        mappings.add(attestation);
        return mappings;
    }
}
