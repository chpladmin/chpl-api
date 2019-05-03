package gov.healthit.chpl.permissions.domains.productversion;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ProductVersionDAO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productVersionMergeActionPermissions")
public class MergeActionPermissions extends ActionPermissions {

    @Autowired
    private ProductVersionDAO versionDAO;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            try {
                List<Long> versionIds = (List<Long>) obj;
                // All versions will have the same developer, so we only need to check the first one
                ProductVersionDTO versionDTO = versionDAO.getById(versionIds.get(0));
                return getResourcePermissions().isDeveloperActive(versionDTO.getDeveloperId());
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

}
