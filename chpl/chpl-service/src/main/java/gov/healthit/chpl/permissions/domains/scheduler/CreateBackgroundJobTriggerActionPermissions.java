package gov.healthit.chpl.permissions.domains.scheduler;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.scheduler.job.SplitDeveloperJob;

@Component(value = "schedulerCreateBackgroundJobTriggerActionPermissions")
public class CreateBackgroundJobTriggerActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            if (obj instanceof ChplOneTimeTrigger) {
                ChplOneTimeTrigger trigger = (ChplOneTimeTrigger) obj;
                if (trigger.getJob() != null && trigger.getJob().getName().equals(SplitDeveloperJob.JOB_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }

}
