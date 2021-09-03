package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;

@Component("surveillanceReportUpdateAnnualReportActionPermissions")
public class UpdateAnnualReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof AnnualReport)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            AnnualReport toUpdate = (AnnualReport) obj;
            if (toUpdate.getAcb() == null
                    || toUpdate.getAcb().getId() == null) {
                return false;
            }
            return isAcbValidForCurrentUser(toUpdate.getAcb().getId());
        } else {
            return false;
        }
    }

}
