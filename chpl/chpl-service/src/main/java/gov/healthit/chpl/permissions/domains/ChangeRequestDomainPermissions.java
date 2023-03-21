package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.changerequest.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.changerequest.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.changerequest.SearchActionPermissions;
import gov.healthit.chpl.permissions.domains.changerequest.UpdateActionPermissions;

@Component
public class ChangeRequestDomainPermissions extends DomainPermissions {
    public static final String GET_BY_ID = "GET_BY_ID";
    public static final String SEARCH = "SEARCH";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public ChangeRequestDomainPermissions(
            @Qualifier("changeRequestGetByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("changeRequestSearchActionPermissions") SearchActionPermissions searchActionPermissions,
            @Qualifier("changeRequestUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("changeRequestCreateActionPermissions") CreateActionPermissions createActionPermissions) {
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
        getActionPermissions().put(SEARCH, searchActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }
}
