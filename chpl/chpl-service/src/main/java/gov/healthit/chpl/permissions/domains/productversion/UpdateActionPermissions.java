package gov.healthit.chpl.permissions.domains.productversion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductVersionDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productVersionUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Autowired
    private DeveloperDAO developerDao;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ProductVersionDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            try {
                ProductVersionDTO dto = (ProductVersionDTO) obj;
                return isDeveloperActive(dto.getDeveloperId());
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDeveloperActive(Long developerId) {
        try {
            DeveloperDTO developerDto = developerDao.getById(developerId);
            if (developerDto != null && developerDto.getStatus() != null && developerDto.getStatus().getStatus()
                    .getStatusName().equals(DeveloperStatusType.Active.toString())) {
                return true;
            } else {
                return false;
            }
        } catch (EntityRetrievalException e) {
            return false;
        }
    }
}
