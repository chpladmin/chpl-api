package gov.healthit.chpl.permissions.domains.scheduler;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.scheduler.job.CognitoUserCacheRefreshJob;
import gov.healthit.chpl.scheduler.job.DirectReviewCacheRefreshJob;
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
        if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleStartup()
                && obj instanceof ChplOneTimeTrigger) {
            ChplOneTimeTrigger trigger = (ChplOneTimeTrigger) obj;
            if (trigger.getJob() != null) {
                return isDirectReviewCacheRefreshJob(trigger) || isCognitoUserCacheRefreshJob(trigger);
            }
        }
        return false;
    }

    private boolean isDirectReviewCacheRefreshJob(ChplOneTimeTrigger trigger) {
        return trigger.getJob().getName().equals(DirectReviewCacheRefreshJob.JOB_NAME)
                && trigger.getJob().getGroup().equals(DirectReviewCacheRefreshJob.JOB_GROUP);
    }

    private boolean isCognitoUserCacheRefreshJob(ChplOneTimeTrigger trigger) {
        return trigger.getJob().getName().equals(CognitoUserCacheRefreshJob.JOB_NAME)
                && trigger.getJob().getGroup().equals(CognitoUserCacheRefreshJob.JOB_GROUP);
    }
}
