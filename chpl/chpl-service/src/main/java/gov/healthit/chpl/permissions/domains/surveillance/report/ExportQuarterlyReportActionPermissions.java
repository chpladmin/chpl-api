package gov.healthit.chpl.permissions.domains.surveillance.report;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.surveillance.report.QuarterlyReportDAO;
import gov.healthit.chpl.surveillance.report.domain.QuarterlyReport;

@Component("surveillanceReportExportQuarterlyReportActionPermissions")
public class ExportQuarterlyReportActionPermissions extends ActionPermissions {

    private QuarterlyReportDAO quarterlyReportDao;

    @Autowired
    public ExportQuarterlyReportActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao,
            QuarterlyReportDAO quarterlyReportDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        this.quarterlyReportDao = quarterlyReportDao;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long idToExport = (Long) obj;
            QuarterlyReport toExport = null;
            try {
                toExport = quarterlyReportDao.getById(idToExport);
                return isAcbValidForCurrentUser(toExport.getAcb().getId());
            } catch (EntityRetrievalException ex) {
                return false;
            }
        } else {
            return false;
        }
    }

}
