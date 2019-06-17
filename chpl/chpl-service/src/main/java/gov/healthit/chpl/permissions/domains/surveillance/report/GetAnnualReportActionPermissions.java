package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceReportGetAnnualReportActionPermissions")
public class GetAnnualReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof AnnualReportDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            AnnualReportDTO report = (AnnualReportDTO) obj;
            return isAcbValidForCurrentUser(report.getAcb().getId());
        } else {
            return false;
        }
    }

}
