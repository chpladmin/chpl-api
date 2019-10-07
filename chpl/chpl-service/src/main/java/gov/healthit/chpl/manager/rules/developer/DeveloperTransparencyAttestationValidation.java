package gov.healthit.chpl.manager.rules.developer;

import java.util.List;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperTransparencyAttestationValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        // transparency attestation is now required to exist in the system
        if (context.getDeveloperDTO().getTransparencyAttestationMappings() == null
                || context.getDeveloperDTO().getTransparencyAttestationMappings().isEmpty()) {
            getMessages().add(
                    context.getErrorMessageUtil().getMessage("system.developer.transparencyAttestationIsNullOrEmpty"));
            return false;
        }
        if (!isMatchingTransparencyAttestation(context.getDeveloperDTO().getTransparencyAttestationMappings(),
                context.getPendingAcbName())) {
            getMessages().add(
                    context.getErrorMessageUtil().getMessage("system.developer.transparencyAttestationNotMatching"));
            return false;
        }
        return true;
    }

    private static boolean isMatchingTransparencyAttestation(final List<DeveloperACBMapDTO> mappings,
            final String pendingAcbName) {
        for (DeveloperACBMapDTO mapping : mappings) {
            if (!StringUtils.isEmpty(mapping.getAcbName())) {
                // check for ACB name match between system and pending upload
                if (mapping.getAcbName().equals(pendingAcbName)) {
                    // enforce matching object has an attestation defined
                    return !StringUtils.isEmpty(mapping.getTransparencyAttestation());
                }
            }
        }
        return false;
    }
}
