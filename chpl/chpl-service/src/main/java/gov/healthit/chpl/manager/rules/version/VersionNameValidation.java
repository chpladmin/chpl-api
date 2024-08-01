package gov.healthit.chpl.manager.rules.version;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class VersionNameValidation extends ValidationRule<VersionValidationContext> {

    @Override
    public boolean isValid(VersionValidationContext context) {
        String updatedVersionName = context.getVersion().getVersion();
        if (StringUtils.isBlank(updatedVersionName)) {
            getMessages().add(context.getErrorMessageUtil().getMessage("version.nameRequired"));
            return false;
        }
        List<ProductVersionDTO> currentVersionsForProudct = context.getVersionDao().getByProductId(context.getProductId());
        boolean currentVersionWithSameName = currentVersionsForProudct.stream()
            .filter(currVer -> currVer.getVersion().equalsIgnoreCase(updatedVersionName)
                    && !currVer.getId().equals(context.getVersion().getId()))
            .findAny().isPresent();
        if (currentVersionWithSameName) {
            getMessages().add(context.getErrorMessageUtil().getMessage("version.duplicateName", updatedVersionName));
            return false;
        }
        return true;
    }
}
