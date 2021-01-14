package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceReportGetQuarterlyReportActionPermissions")
public class GetQuarterlyReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof QuarterlyReportDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            QuarterlyReportDTO report = (QuarterlyReportDTO) obj;
            return isAcbValidForCurrentUser(report.getAcb().getId());
        } else {
            return false;
        }
    }

}
