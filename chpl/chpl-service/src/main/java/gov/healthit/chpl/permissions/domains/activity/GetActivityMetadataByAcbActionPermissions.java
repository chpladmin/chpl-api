package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("actionGetActivityMetadataByAcbActionPermissions")
public class GetActivityMetadataByAcbActionPermissions extends ActionPermissions {
    private CertificationBodyDAO acbDao;

    @Autowired
    public GetActivityMetadataByAcbActionPermissions(final CertificationBodyDAO acbDao) {
        this.acbDao = acbDao;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            Long acbId = (Long) obj;
            CertificationBodyDTO acb = null;
            try {
                acb = acbDao.getById(acbId);
                if (acb != null && acb.isRetired()) {
                    LOGGER.warn("Non-admin user " + AuthUtil.getUsername()
                    + " tried to see activity for retired ACB " + acb.getName());
                    return false;
                }
            } catch (Exception ex) {
                return false;
            }
            return isAcbValidForCurrentUser(acbId);
        }
    }
}
