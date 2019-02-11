package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Qualifier;

import gov.healthit.chpl.permissions.domains.developer.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.developer.GetAllWithDeletedActionPermissions;
import gov.healthit.chpl.permissions.domains.developer.UpdateActionPermissions;

public class DeveloperDomainPermissions extends DomainPermissions {
    public static final String GET_ALL_WITH_DELETED = "GET_ALL_WITH_DELETED";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";
    
    public DeveloperDomainPermissions(
            @Qualifier("developerGetAllWithDeletedActionPermissions") GetAllWithDeletedActionPermissions getAllWithDeletedActionPermissions,
            @Qualifier("developerUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("developerCreateActionPermissions") CreateActionPermissions createActionPermissions) {
        
        getActionPermissions().put(GET_ALL_WITH_DELETED, getAllWithDeletedActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }
}
