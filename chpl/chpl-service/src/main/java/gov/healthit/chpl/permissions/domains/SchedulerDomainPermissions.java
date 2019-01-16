package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.scheduler.GetAllActionPermissions;

@Component
public class SchedulerDomainPermissions extends DomainPermissions {
    public static final String GET_ALL = "GET_ALL";

    @Autowired
    public SchedulerDomainPermissions(
            @Qualifier("schedulerGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions) {
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
    }
}
