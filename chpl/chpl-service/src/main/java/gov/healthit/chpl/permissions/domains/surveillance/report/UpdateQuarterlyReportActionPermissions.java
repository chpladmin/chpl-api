package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceReportUpdateQuarterlyReportActionPermissions")
public class UpdateQuarterlyReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof QuarterlyReportDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            QuarterlyReportDTO toUpdate = (QuarterlyReportDTO) obj;
            if (toUpdate.getAcb() == null || toUpdate.getAcb().getId() == null) {
                return false;
            }
            return isAcbValidForCurrentUser(toUpdate.getAcb().getId());
        } else {
            return false;
        }
    }

}
