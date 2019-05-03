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
                for (Long versionId : versionIds) {
                    ProductVersionDTO versionDTO = versionDAO.getById(versionId);
                    if (!getResourcePermissions().isDeveloperActive(versionDTO.getDeveloperId())) {
                        return false;
                    }
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

}
