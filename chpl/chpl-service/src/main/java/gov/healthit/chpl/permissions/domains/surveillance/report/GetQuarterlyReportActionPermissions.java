package gov.healthit.chpl.permissions.domains.surveillance.report;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;

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
        if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            if (obj instanceof List) {
                List<QuarterlyReport> reports = (List<QuarterlyReport>) obj;
                return reports.stream()
                    .filter(report -> !isAcbValidForCurrentUser(report.getAcb().getId()))
                    .findAny().isEmpty();
            } else if (obj instanceof QuarterlyReport) {
                QuarterlyReport report = (QuarterlyReport) obj;
                return isAcbValidForCurrentUser(report.getAcb().getId());
            } else if (obj instanceof Long) {
                Long acbId = (Long) obj;
                return isAcbValidForCurrentUser(acbId);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
