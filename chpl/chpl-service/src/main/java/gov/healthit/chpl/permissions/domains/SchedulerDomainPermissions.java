package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.scheduler.CreateOneTimeTriggerActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.GetAllActionPermissions;

@Component
public class SchedulerDomainPermissions extends DomainPermissions {
    public static final String GET_ALL = "GET_ALL";
    public static final String CREATE_ONE_TIME_TRIGGER = "CREATE_ONE_TIME_TRIGGER";

    @Autowired
    public SchedulerDomainPermissions(
            @Qualifier("schedulerGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("schedulerCreateOneTimeTriggerActionPermissions") CreateOneTimeTriggerActionPermissions createOneTimeTriggerActionPermissions) {
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(CREATE_ONE_TIME_TRIGGER, createOneTimeTriggerActionPermissions);
    }
}
