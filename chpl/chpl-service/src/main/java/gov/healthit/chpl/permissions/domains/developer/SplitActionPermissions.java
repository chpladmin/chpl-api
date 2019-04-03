package gov.healthit.chpl.permissions.domains.developer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("developerSplitActionPermissions")
public class SplitActionPermissions extends ActionPermissions {

    @Autowired
    private DeveloperDAO developerDao;

    @Autowired
    private CertifiedProductDAO certifiedProductDao;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof DeveloperDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            DeveloperDTO developer = (DeveloperDTO) obj;
            if (isDeveloperActive(developer.getId())) {
                // ACB can only split developer if original developer is active and all listings owned by the developer
                // belong to the user's ACB
                return doesCurrentUserHaveAccessToAllOfDevelopersListings(developer.getId());
            } else {
                // ACB can never split developer if original developer is not active
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean doesCurrentUserHaveAccessToAllOfDevelopersListings(Long developerId) {
        List<CertifiedProductDetailsDTO> cpDtos = certifiedProductDao.findByDeveloperId(developerId);
        for (CertifiedProductDetailsDTO cpDto : cpDtos) {
            if (!isAcbValidForCurrentUser(cpDto.getCertificationBodyId())) {
                return false;
            }
        }
        return true;
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
