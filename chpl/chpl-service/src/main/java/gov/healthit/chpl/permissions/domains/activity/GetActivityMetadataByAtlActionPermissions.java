package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("actionGetActivityMetadataByAtlActionPermissions")
public class GetActivityMetadataByAtlActionPermissions extends ActionPermissions {
    private TestingLabDAO atlDao;

    @Autowired
    public GetActivityMetadataByAtlActionPermissions(TestingLabDAO atlDao) {
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
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff()) {
            return true;
        } else {
            Long atlId = (Long) obj;
            TestingLabDTO atl = null;
            try {
                atl = atlDao.getById(atlId);
                if (atl != null && atl.isRetired()) {
                    LOGGER.warn("Non-admin user " + AuthUtil.getUsername()
                    + " tried to see activity for retired ATL " + atl.getName());
                    return false;
                }
            } catch (Exception ex) {
                return false;
            }
            return isAtlValidForCurrentUser(atlId);
        }
    }
}
