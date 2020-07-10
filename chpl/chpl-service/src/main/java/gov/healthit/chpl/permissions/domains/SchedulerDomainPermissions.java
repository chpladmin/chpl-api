package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.scheduler.CreateBackgroundJobTriggerActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.CreateOneTimeTriggerActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.CreateTriggerActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.DeleteTriggerActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.GetAllSystemTriggersActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.GetAllTriggersActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.UpdateAcbNameActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.UpdateJobActionPermissions;
import gov.healthit.chpl.permissions.domains.scheduler.UpdateTriggerActionPermissions;

@Component
public class SchedulerDomainPermissions extends DomainPermissions {
    public static final String GET_ALL = "GET_ALL";
    public static final String CREATE_ONE_TIME_TRIGGER = "CREATE_ONE_TIME_TRIGGER";
    public static final String CREATE_BACKGROUND_JOB_TRIGGER = "CREATE_BACKGROUND_JOB_TRIGGER";
    public static final String CREATE_TRIGGER = "CREATE_TRIGGER";
    public static final String DELETE_TRIGGER = "DELETE_TRIGGER";
    public static final String GET_ALL_SYSTEM_TRIGGERS = "GET_ALL_SYSTEM_TRIGGERS";
    public static final String GET_ALL_TRIGGERS = "GET_ALL_TRIGGERS";
    public static final String UPDATE_TRIGGER = "UPDATE_TRIGGER";
    public static final String UPDATE_JOB = "UPDATE_JOB";
    public static final String UPDATE_ACB_NAME = "UPDATE_ACB_NAME";

    @Autowired
    public SchedulerDomainPermissions(
            @Qualifier("schedulerGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("schedulerCreateOneTimeTriggerActionPermissions") CreateOneTimeTriggerActionPermissions createOneTimeTriggerActionPermissions,
            @Qualifier("schedulerCreateBackgroundJobTriggerActionPermissions") CreateBackgroundJobTriggerActionPermissions createBackgroundJobTriggerActionPermissions,
            @Qualifier("schedulerCreateTriggerActionPermissions") CreateTriggerActionPermissions createTriggerActionPermissions,
            @Qualifier("schedulerDeleteTriggerActionPermissions") DeleteTriggerActionPermissions deleteTriggerActionPermissions,
            @Qualifier("schedulerGetAllSystemTriggersActionPermissions") GetAllSystemTriggersActionPermissions getAllSystemTriggersActionPermissions,
            @Qualifier("schedulerGetAllTriggersActionPermissions") GetAllTriggersActionPermissions getAllTriggersActionPermissions,
            @Qualifier("schedulerUpdateTriggerActionPermissions") UpdateTriggerActionPermissions updateTriggerActionPermissions,
            @Qualifier("schedulerUpdateJobActionPermissions") UpdateJobActionPermissions updateJobActionPermissions,
            @Qualifier("schedulerUpdateAcbNameActionPermissions") UpdateAcbNameActionPermissions updateAcbNameActionPermissions) {

        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(CREATE_ONE_TIME_TRIGGER, createOneTimeTriggerActionPermissions);
        getActionPermissions().put(CREATE_BACKGROUND_JOB_TRIGGER, createBackgroundJobTriggerActionPermissions);
        getActionPermissions().put(CREATE_TRIGGER, createTriggerActionPermissions);
        getActionPermissions().put(DELETE_TRIGGER, deleteTriggerActionPermissions);
        getActionPermissions().put(GET_ALL_SYSTEM_TRIGGERS, getAllSystemTriggersActionPermissions);
        getActionPermissions().put(GET_ALL_TRIGGERS, getAllTriggersActionPermissions);
        getActionPermissions().put(UPDATE_TRIGGER, updateTriggerActionPermissions);
        getActionPermissions().put(UPDATE_JOB, updateJobActionPermissions);
        getActionPermissions().put(UPDATE_ACB_NAME, updateAcbNameActionPermissions);
    }
}
