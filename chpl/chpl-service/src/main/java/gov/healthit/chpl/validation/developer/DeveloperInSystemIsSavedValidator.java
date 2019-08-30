package gov.healthit.chpl.validation.developer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Validate fields used when creating a new developer.
 */
@Component("developerInSystemIsSavedValidator")
public class DeveloperInSystemIsSavedValidator {

    private ErrorMessageUtil msgUtil;

    public DeveloperInSystemIsSavedValidator() {}

    @Autowired
    public DeveloperInSystemIsSavedValidator(final ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    /**
     * Looks for missing fields in the persisted developer object. If none are
     * missing then it is known that they were saved
     *
     * @param systemDev
     *            the persisted developer to validate
     * @return a list of error messages generated to notify the user that
     *         required developer data is missing in the system and thus a save
     *         is required
     */
    public Set<String> validate(final DeveloperDTO systemDev, final String pendingAcbName) {
        Set<String> errorMessages = new HashSet<String>();

        if (StringUtils.isEmpty(systemDev.getName())) {
            errorMessages.add(msgUtil.getMessage("system.developer.nameRequired"));
        }

        // website is now required in the system whether valid or not and we
        // don't check URL validity because that is already checked before a
        // save
        if (StringUtils.isEmpty(systemDev.getWebsite())) {
            errorMessages.add(msgUtil.getMessage("system.developer.websiteRequired"));
        }

        if (systemDev.getContact() == null) {
            errorMessages.add(msgUtil.getMessage("system.developer.contactRequired"));
        } else {
            if (StringUtils.isEmpty(systemDev.getContact().getFullName())) {
                errorMessages.add(msgUtil.getMessage("system.developer.contact.nameRequired"));
            }
            if (StringUtils.isEmpty(systemDev.getContact().getEmail())) {
                errorMessages.add(msgUtil.getMessage("system.developer.contact.emailRequired"));
            }
            if (StringUtils.isEmpty(systemDev.getContact().getPhoneNumber())) {
                errorMessages.add(msgUtil.getMessage("system.developer.contact.phoneRequired"));
            }
        }

        if (systemDev.getAddress() == null) {
            errorMessages.add(msgUtil.getMessage("system.developer.addressRequired"));
        } else {
            if (StringUtils.isEmpty(systemDev.getAddress().getStreetLineOne())) {
                errorMessages.add(msgUtil.getMessage("system.developer.address.streetRequired"));
            }
            if (StringUtils.isEmpty(systemDev.getAddress().getCity())) {
                errorMessages.add(msgUtil.getMessage("system.developer.address.cityRequired"));
            }
            if (StringUtils.isEmpty(systemDev.getAddress().getState())) {
                errorMessages.add(msgUtil.getMessage("system.developer.address.stateRequired"));
            }
            if (StringUtils.isEmpty(systemDev.getAddress().getZipcode())) {
                errorMessages.add(msgUtil.getMessage("system.developer.address.zipRequired"));
            }
        }

        // transparency attestation is now required to exist in the system
        if (systemDev.getTransparencyAttestationMappings() == null
                || systemDev.getTransparencyAttestationMappings().isEmpty()) {
            errorMessages.add(msgUtil.getMessage("system.developer.transparencyAttestationIsNullOrEmpty"));
        } else {
            if (!isMatchingTransparencyAttestationStringIn(systemDev.getTransparencyAttestationMappings(),
                    pendingAcbName)) {
                errorMessages.add(msgUtil.getMessage("system.developer.transparencyAttestationNotMatching"));
            }
        }

        return errorMessages;
    }

    private static boolean isMatchingTransparencyAttestationStringIn(final List<DeveloperACBMapDTO> mappings,
            final String pendingAcbName) {
        for (DeveloperACBMapDTO mapping : mappings) {
            if (!StringUtils.isEmpty(mapping.getAcbName())) {
                // check for ACB name match between the system and the pending
                // upload
                if (mapping.getAcbName().equals(pendingAcbName)) {
                    // enforce the matching object actually has an attestation
                    // defined
                    return !StringUtils.isEmpty(mapping.getTransparencyAttestation());
                }
            }
        }
        return false;
    }
}
