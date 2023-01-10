package gov.healthit.chpl.permissions.domains.scheduler;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.scheduler.job.DirectReviewCacheRefreshJob;

@Component(value = "schedulerCreateOneTimeTriggerActionPermissions")
public class CreateOneTimeTriggerActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
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
