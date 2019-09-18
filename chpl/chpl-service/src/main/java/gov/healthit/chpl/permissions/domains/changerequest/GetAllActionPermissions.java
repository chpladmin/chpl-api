package gov.healthit.chpl.permissions.domains.changerequest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    private DeveloperDAO developerDAO;

    @Autowired
    public GetAllActionPermissions(final DeveloperDAO developerDAO) {
        this.developerDAO = developerDAO;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ChangeRequest)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            ChangeRequest cr = (ChangeRequest) obj;
            List<DeveloperDTO> developers = developerDAO
                    .getByCertificationBodyId(
                            getResourcePermissions().getAllAcbsForCurrentUser().stream()
                                    .map(acb -> acb.getId())
                                    .collect(Collectors.<Long> toList()));

            return developers.stream()
                    .filter(dev -> dev.getId().equals(cr.getDeveloper().getDeveloperId()))
                    .collect(Collectors.<DeveloperDTO> toList())
                    .size() > 0;
        } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
            ChangeRequest cr = (ChangeRequest) obj;
            List<DeveloperDTO> developers = getResourcePermissions().getAllDevelopersForCurrentUser();

            return developers.stream()
                    .filter(dev -> dev.getId().equals(cr.getDeveloper().getDeveloperId()))
                    .collect(Collectors.<DeveloperDTO> toList())
                    .size() > 0;
        } else {
            return false;
        }
    }

}
