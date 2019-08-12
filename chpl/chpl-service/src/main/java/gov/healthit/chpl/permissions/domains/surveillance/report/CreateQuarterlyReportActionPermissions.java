package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceReportCreateQuarterlyReportActionPermissions")
public class CreateQuarterlyReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof QuarterlyReportDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            QuarterlyReportDTO toCreate = (QuarterlyReportDTO) obj;
            if (toCreate.getAcb() == null || toCreate.getAcb().getId() == null) {
                return false;
            }
            return isAcbValidForCurrentUser(toCreate.getAcb().getId());
        } else {
            return false;
        }
    }

}
