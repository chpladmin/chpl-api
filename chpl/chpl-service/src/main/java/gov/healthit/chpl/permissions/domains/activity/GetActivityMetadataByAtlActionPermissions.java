package gov.healthit.chpl.permissions.domains.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetActivityMetadataByAtlActionPermissions")
public class GetActivityMetadataByAtlActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(GetActivityMetadataByAtlActionPermissions.class);

    private TestingLabDAO atlDao;

    @Autowired
    public GetActivityMetadataByAtlActionPermissions(final TestingLabDAO atlDao) {
        this.atlDao = atlDao;
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
            Long atlId = (Long) obj;
            TestingLabDTO atl = null;
            try {
                atl = atlDao.getById(atlId);
                if (atl != null && atl.isRetired()) {
                    LOGGER.warn("Non-admin user " + Util.getUsername()
                    + " tried to see activity for retired ATL " + atl.getName());
                    return false;
                }
            } catch (EntityRetrievalException ex) { }
            return isAtlValidForCurrentUser(atlId);
        }
    }
}
