package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;

@Component("surveillanceReportUpdateQuarterlyReportActionPermissions")
public class UpdateQuarterlyReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (obj instanceof QuarterlyReport) {
            return hasAccess((QuarterlyReport) obj);
        }
        return false;
    }

    private boolean hasAccess(QuarterlyReport quarterlyReport) {
        if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            if (quarterlyReport.getAcb() == null || quarterlyReport.getAcb().getId() == null) {
                return false;
            }
            return isAcbValidForCurrentUser(quarterlyReport.getAcb().getId());
        } else {
            return false;
        }
    }
}
