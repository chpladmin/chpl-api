package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.surveillance.report.dto.QuarterlyReportDTO;

@Component("surveillanceReportGetQuarterlyReportActionPermissions")
public class GetQuarterlyReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof QuarterlyReportDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            QuarterlyReportDTO report = (QuarterlyReportDTO) obj;
            return isAcbValidForCurrentUser(report.getAcb().getId());
        } else {
            return false;
        }
    }

}
