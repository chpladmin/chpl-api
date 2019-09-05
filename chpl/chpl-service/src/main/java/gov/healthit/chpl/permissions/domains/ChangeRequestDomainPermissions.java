package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.changerequest.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.changerequest.UpdateActionPermissions;

@Component
public class ChangeRequestDomainPermissions extends DomainPermissions {
    public static final String GET_BY_ID = "GET_BY_ID";
    public static final String UPDATE = "UPDATE";

    @Autowired
    public ChangeRequestDomainPermissions(
            @Qualifier("changeRequestGetByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("changeRequestUpdateActionPermissions") UpdateActionPermissions updateActionPermissions) {
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
    }
}
