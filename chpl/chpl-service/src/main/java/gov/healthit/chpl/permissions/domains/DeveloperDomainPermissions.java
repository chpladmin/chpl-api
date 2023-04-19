package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.developer.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.developer.GetAllUsersActionPermissions;
import gov.healthit.chpl.permissions.domains.developer.GetAllWithDeletedActionPermissions;
import gov.healthit.chpl.permissions.domains.developer.JoinActionPermissions;
import gov.healthit.chpl.permissions.domains.developer.SplitActionPermissions;
import gov.healthit.chpl.permissions.domains.developer.UpdateActionPermissions;

@Component
public class DeveloperDomainPermissions extends DomainPermissions {
    public static final String GET_ALL_WITH_DELETED = "GET_ALL_WITH_DELETED";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";
    public static final String JOIN = "JOIN";
    public static final String SPLIT = "SPLIT";
    public static final String GET_ALL_USERS = "GET_ALL_USERS";

    @Autowired
    public DeveloperDomainPermissions(
            @Qualifier("developerGetAllWithDeletedActionPermissions") GetAllWithDeletedActionPermissions getAllWithDeletedActionPermissions,
            @Qualifier("developerUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("developerCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("developerJoinActionPermissions") JoinActionPermissions joinActionPermissions,
            @Qualifier("developerSplitActionPermissions") SplitActionPermissions splitActionPermissions,
            @Qualifier("developerGetAllUsersActionPermissions") GetAllUsersActionPermissions getUsersActionPermissions) {

        getActionPermissions().put(GET_ALL_WITH_DELETED, getAllWithDeletedActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(JOIN, joinActionPermissions);
        getActionPermissions().put(SPLIT, splitActionPermissions);
        getActionPermissions().put(GET_ALL_USERS, getUsersActionPermissions);
    }
}
