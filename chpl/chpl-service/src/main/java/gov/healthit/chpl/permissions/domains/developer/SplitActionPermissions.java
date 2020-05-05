package gov.healthit.chpl.permissions.domains.developer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("developerSplitActionPermissions")
public class SplitActionPermissions extends ActionPermissions {

    private List<CertificationStatusType> allowedCertStatuses;

    @Autowired
    public SplitActionPermissions() {
        allowedCertStatuses = new ArrayList<CertificationStatusType>();
        allowedCertStatuses.add(CertificationStatusType.Active);
        allowedCertStatuses.add(CertificationStatusType.SuspendedByAcb);
        allowedCertStatuses.add(CertificationStatusType.SuspendedByOnc);
        allowedCertStatuses.add(CertificationStatusType.TerminatedByOnc);
        allowedCertStatuses.add(CertificationStatusType.WithdrawnByAcb);
        allowedCertStatuses.add(CertificationStatusType.WithdrawnByDeveloper);
        allowedCertStatuses.add(CertificationStatusType.WithdrawnByDeveloperUnderReview);
    }

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
            if (getResourcePermissions().isDeveloperActive(developer.getId())) {
                // ACB can only split developer if original developer is active and all non-retired
                //listings owned by the developer belong to the user's ACB
                return doesCurrentUserHaveAccessToAllOfDevelopersListings(developer.getId(), allowedCertStatuses);
            } else {
                // ACB can never split developer if original developer is not active
                return false;
            }
        } else {
            return false;
        }
    }
}
