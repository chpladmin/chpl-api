package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.job.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.job.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.job.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.job.GetByUserActionPermissions;
import gov.healthit.chpl.permissions.domains.job.StartActionPermissions;

@Component
public class JobDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String GET_ALL = "GET_ALL";
    public static final String GET_BY_ID = "GET_BY_ID";
    public static final String GET_BY_USER = "GET_BY_USER";
    public static final String START = "START";

    @Autowired
    public JobDomainPermissions(
            @Qualifier("jobCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("jobGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("jobGetByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("jobGetByUserActionPermissions") GetByUserActionPermissions getByUserActionPermissions,
            @Qualifier("jobStartActionPermissions") StartActionPermissions startActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
        getActionPermissions().put(GET_BY_USER, getByUserActionPermissions);
        getActionPermissions().put(START, startActionPermissions);
    }
}
