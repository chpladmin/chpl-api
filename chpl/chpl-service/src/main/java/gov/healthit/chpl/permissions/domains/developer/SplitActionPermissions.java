package gov.healthit.chpl.permissions.domains.developer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("developerSplitActionPermissions")
public class SplitActionPermissions extends ActionPermissions {
    private ErrorMessageUtil msgUtil;
    private List<CertificationStatusType> allowedCertStatuses;

    @Autowired
    public SplitActionPermissions(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;

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
        if (!(obj instanceof Developer)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Developer developer = (Developer) obj;
            if (!getResourcePermissions().isDeveloperNotBannedOrSuspended(developer.getId())) {
                //ACB can never split developer if original developer is not active
                return false;
            } else if (!doesCurrentUserHaveAccessToAllOfDevelopersListings(developer.getId(), allowedCertStatuses)) {
                //ACB can only split developer if original developer is active and all non-retired
                //listings owned by the developer belong to the user's ACB
                throw new AccessDeniedException(msgUtil.getMessage("developer.split.notAllowedMultipleAcbs"));
            }
            return true;
        } else {
            return false;
        }
    }
}
