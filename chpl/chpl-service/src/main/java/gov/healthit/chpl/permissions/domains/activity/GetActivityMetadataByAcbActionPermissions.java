package gov.healthit.chpl.permissions.domains.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetActivityMetadataByAcbActionPermissions")
public class GetActivityMetadataByAcbActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(GetActivityMetadataByAcbActionPermissions.class);

    private CertificationBodyDAO acbDao;

    @Autowired
    public GetActivityMetadataByAcbActionPermissions(CertificationBodyDAO acbDao) {
        this.acbDao = acbDao;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
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
                    LOGGER.warn("Non-admin user " + Util.getUsername()
                    + " tried to see activity for retired ACB " + acb.getName());
                    return false;
                }
            } catch (EntityRetrievalException ex) { }
            return isAcbValidForCurrentUser(acbId);
        }
    }
}
