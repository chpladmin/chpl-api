package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.changerequest.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.changerequest.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.changerequest.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.changerequest.UpdateActionPermissions;

@Component
public class ChangeRequestDomainPermissions extends DomainPermissions {
    public static final String GET_BY_ID = "GET_BY_ID";
    public static final String GET_ALL = "GET_ALL";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public ChangeRequestDomainPermissions(
            @Qualifier("changeRequestGetByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("changeRequestGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("changeRequestUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("changeRequestCreateActionPermissions") CreateActionPermissions createActionPermissions) {
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }
}
