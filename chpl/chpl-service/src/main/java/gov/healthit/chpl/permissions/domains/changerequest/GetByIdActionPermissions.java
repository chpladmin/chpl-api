package gov.healthit.chpl.permissions.domains.changerequest;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestGetByIdActionPermissions")
public class GetByIdActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof ChangeRequest)) {
                return false;
            } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
                return true;
            } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
                ChangeRequest cr = (ChangeRequest) obj;
                List<CertificationBody> acbs = cr.getCertificationBodies().stream()
                        .filter(certBody -> getResourcePermissions().getAllAcbsForCurrentUser().stream()
                                .anyMatch(userAcb -> userAcb.getId().equals(certBody.getId())))
                        .collect(Collectors.toList());
                return acbs.size() > 0;
            } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
                ChangeRequest cr = (ChangeRequest) obj;
                return isDeveloperValidForCurrentUser(cr.getDeveloper().getDeveloperId());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
