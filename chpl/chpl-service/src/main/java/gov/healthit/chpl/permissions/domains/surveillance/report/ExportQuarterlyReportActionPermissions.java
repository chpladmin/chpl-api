package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceReportExportQuarterlyReportActionPermissions")
public class ExportQuarterlyReportActionPermissions extends ActionPermissions {

    @Autowired
    private QuarterlyReportDAO quarterlyReportDao;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long idToDelete = (Long) obj;
            QuarterlyReportDTO toDelete = null;
            try {
                toDelete = quarterlyReportDao.getById(idToDelete);
                return isAcbValidForCurrentUser(toDelete.getAnnualReport().getAcb().getId());
            } catch (EntityRetrievalException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

}
