package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.secureduser.ImpersonateUserActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddDeveloperActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteDeveloperActionPermissions;

/**
 * Permissions related to User & User/ACB management.
 *
 */
@Component
public class UserPermissionsDomainPermissions extends DomainPermissions {
    /** Permission to add a new ACB. */
    public static final String ADD_ACB = "ADD_ACB";
    /** Permission to delete an ACB. */
    public static final String DELETE_ACB = "DELETE_ACB";
    /** Permission to impersonate another user. */
    public static final String IMPERSONATE_USER = "IMPERSONATE_USER";
    /** Developer permissions. */
    public static final String ADD_DEVELOPER = "ADD_DEVELOPER";
    public static final String DELETE_DEVELOPER = "DELETE_DEVELOPER";
    public static final String DELETE_ALL_DEVELOPERS_FOR_USER = "DELETE_ALL_DEVELOPERS_FOR_USER";

    @Autowired
    public UserPermissionsDomainPermissions(
            @Qualifier("userPermissionsAddAcbActionPermissions") final AddAcbActionPermissions addAcbActionPermissions,
            @Qualifier("userPermissionsDeleteAcbActionPermissions") final DeleteAcbActionPermissions deleteAcbActionPermissions,
            @Qualifier("userPermissionsImpersonateUserActionPermissions") final ImpersonateUserActionPermissions impersonateUserActionPermissions,
            @Qualifier("userPermissionsAddDeveloperActionPermissions") final AddDeveloperActionPermissions addDeveloperActionPermissions,
            @Qualifier("userPermissionsDeleteDeveloperActionPermissions") final DeleteDeveloperActionPermissions deleteDeveloperActionPermissions) {

        getActionPermissions().put(ADD_ACB, addAcbActionPermissions);
        getActionPermissions().put(DELETE_ACB, deleteAcbActionPermissions);
        getActionPermissions().put(IMPERSONATE_USER, impersonateUserActionPermissions);
        getActionPermissions().put(ADD_DEVELOPER, addDeveloperActionPermissions);
        getActionPermissions().put(DELETE_DEVELOPER, deleteDeveloperActionPermissions);
    }
}
