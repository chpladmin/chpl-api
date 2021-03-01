package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.surveillance.report.AnnualReportDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceReportCreateAnnualReportActionPermissions")
public class CreateAnnualReportActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof AnnualReportDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            AnnualReportDTO toCreate = (AnnualReportDTO) obj;
            if (toCreate.getAcb() == null
                    || toCreate.getAcb().getId() == null) {
                return false;
            }
            return isAcbValidForCurrentUser(toCreate.getAcb().getId());
        } else {
            return false;
        }
    }

}
