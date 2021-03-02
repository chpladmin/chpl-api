package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.surveillance.report.dto.AnnualReportDTO;

@Component("surveillanceReportUpdateAnnualReportActionPermissions")
public class UpdateAnnualReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof AnnualReportDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            AnnualReportDTO toUpdate = (AnnualReportDTO) obj;
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
