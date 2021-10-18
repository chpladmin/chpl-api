package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.surveillance.report.AnnualReportDAO;
import gov.healthit.chpl.surveillance.report.domain.AnnualReport;

@Component("surveillanceReportDeleteAnnualReportActionPermissions")
public class DeleteAnnualReportActionPermissions extends ActionPermissions {

    @Autowired
    private AnnualReportDAO annualReportDao;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long idToDelete = (Long) obj;
            AnnualReport toDelete = null;
            try {
                toDelete = annualReportDao.getById(idToDelete);
                return isAcbValidForCurrentUser(toDelete.getAcb().getId());
            } catch (EntityRetrievalException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

}
