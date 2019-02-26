package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.secureduser.ImpersonateUserActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAllAcbPermissionsForUserActionPermissions;

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
    /** Permission to delete all ACBs for a given user. */
    public static final String DELETE_ALL_ACBS_FOR_USER = "DELETE_ALL_ACBS_FOR_USER";
    /** Permission to impersonate another user. */
    public static final String IMPERSONATE_USER = "IMPERSONATE_USER";

    @Autowired
    public UserPermissionsDomainPermissions(
            @Qualifier("userPermissionsAddAcbActionPermissions")
            final AddAcbActionPermissions addAcbActionPermissions,
            @Qualifier("userPermissionsDeleteAcbActionPermissions")
            final DeleteAcbActionPermissions deleteAcbActionPermissions,
            @Qualifier("userPermissionsDeleteAllAcbPermissionsForUserActionPermissions")
            final DeleteAllAcbPermissionsForUserActionPermissions deleteAllAcbPermissionsForUserActionPermissions,
            @Qualifier("userPermissionsImpersonateUserActionPermissions")
            final ImpersonateUserActionPermissions impersonateUserActionPermissions) {

        getActionPermissions().put(ADD_ACB, addAcbActionPermissions);
        getActionPermissions().put(DELETE_ACB, deleteAcbActionPermissions);
        getActionPermissions().put(DELETE_ALL_ACBS_FOR_USER, deleteAllAcbPermissionsForUserActionPermissions);
        getActionPermissions().put(IMPERSONATE_USER, impersonateUserActionPermissions);
    }
}
