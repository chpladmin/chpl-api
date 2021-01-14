package gov.healthit.chpl.permissions.domains.scheduler;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;

@Component(value = "schedulerGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(GetAllActionPermissions.class);
    private static final String AUTHORITY_DELIMITER = ";";

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof ChplJob)) {
                return false;
            } else if (getResourcePermissions().isUserRoleAcbAdmin()
                    || getResourcePermissions().isUserRoleOnc()
                    || getResourcePermissions().isUserRoleOncStaff()) {
                ChplJob job = (ChplJob) obj;
                return doesUserHavePermissionToJob(job);
            } else {
                return getResourcePermissions().isUserRoleAdmin();
            }
        } catch (Exception e) {
            LOGGER.error(e);
            return false;
        }
    }

    private Boolean doesUserHavePermissionToJob(final ChplJob job) {
        // Get the authorities from the job
        if (job.getGroup() != null && job.getGroup().equals(SchedulerManager.CHPL_JOBS_KEY)) {
            if (job.getJobDataMap().containsKey("authorities")) {
                List<String> authorities = Arrays
                        .asList(job.getJobDataMap().get("authorities").toString().split(AUTHORITY_DELIMITER));
                if (authorities.size() > 0) {
                    Set<GrantedPermission> userRoles = AuthUtil.getCurrentUser().getPermissions();
                    for (GrantedPermission permission : userRoles) {
                        for (String authority : authorities) {
                            if (permission.getAuthority().equalsIgnoreCase(authority)) {
                                return true;
                            }
                        }
                    }
                } else {
                    // If no authorities are present, we assume there are no
                    // permissions on the job
                    // and everyone has access
                    return true;
                }
            }
        }
        // At this point we have fallen through all of the logic, and the user
        // does not have the appropriate rights
        return false;
    }

}
