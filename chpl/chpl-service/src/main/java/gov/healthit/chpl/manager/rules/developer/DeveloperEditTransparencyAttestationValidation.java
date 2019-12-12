package gov.healthit.chpl.manager.rules.developer;

import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.ff4j.FF4j;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class DeveloperEditTransparencyAttestationValidation extends ValidationRule<DeveloperValidationContext> {
    private FF4j ff4j;
    private ResourcePermissions resourcePermissions;

    public DeveloperEditTransparencyAttestationValidation(final FF4j ff4j,
            final ResourcePermissions resourcePermissions) {
        this.ff4j = ff4j;
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public boolean isValid(DeveloperValidationContext context) {
        ErrorMessageUtil msgUtil = context.getErrorMessageUtil();
        DeveloperDTO updatedDev = context.getDeveloperDTO();
        if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            if (resourcePermissions.isUserRoleAcbAdmin()) {
                if (isTransparencyAttestationUpdated(context.getBeforeDev(), updatedDev)) {
                    getMessages().add(msgUtil.getMessage("developer.transparencyAttestationEditNotAllowedForRoleACB"));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isTransparencyAttestationUpdated(final DeveloperDTO original, final DeveloperDTO changed) {
        if ((original.getTransparencyAttestationMappings() != null
                && changed.getTransparencyAttestationMappings() == null)
                || (original.getTransparencyAttestationMappings() == null
                        && changed.getTransparencyAttestationMappings() != null)
                || (original.getTransparencyAttestationMappings().size() != changed.getTransparencyAttestationMappings()
                        .size())) {
            return true;
        } else {
            for (DeveloperACBMapDTO originalMapping : original.getTransparencyAttestationMappings()) {
                Optional<DeveloperACBMapDTO> matchingMapping =
                        changed.getTransparencyAttestationMappings().stream()
                    .filter(changedMapping -> StringUtils.equals(originalMapping.getAcbName(), changedMapping.getAcbName()))
                    .findFirst();
                if (!matchingMapping.isPresent()
                        || !StringUtils.equals(originalMapping.getTransparencyAttestation(),
                                matchingMapping.get().getTransparencyAttestation())) {
                    return true;
                }
            }
        }
        return false;
    }
}
