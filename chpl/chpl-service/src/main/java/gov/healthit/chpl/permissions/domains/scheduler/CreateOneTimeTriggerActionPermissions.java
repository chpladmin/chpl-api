package gov.healthit.chpl.permissions.domains.scheduler;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.scheduler.job.DirectReviewCacheRefreshJob;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component(value = "schedulerCreateOneTimeTriggerActionPermissions")
public class CreateOneTimeTriggerActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (AuthUtil.getCurrentUser() != null) {
        }
        if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleStartup()
                && obj instanceof ChplOneTimeTrigger) {
            ChplOneTimeTrigger trigger = (ChplOneTimeTrigger) obj;
            if (trigger.getJob() != null
                    && DirectReviewCacheRefreshJob.JOB_NAME.equals(trigger.getJob().getName())
                    && DirectReviewCacheRefreshJob.JOB_GROUP.equals(trigger.getJob().getGroup())) {
                return true;
            }
        }
        return false;
    }

}
