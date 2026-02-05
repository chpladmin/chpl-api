package gov.healthit.chpl.permissions.domains.scheduler;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component(value = "schedulerGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {
    private static final String AUTHORITY_DELIMITER = ";";

    @Autowired
    public GetAllActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

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
                    || getResourcePermissions().isUserRoleOnc()) {
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
                    return AuthUtil.getCurrentUser().getAuthorities().stream()
                            .filter(userAuth -> authorities.contains(userAuth.getAuthority()))
                            .findAny()
                            .isPresent();
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
