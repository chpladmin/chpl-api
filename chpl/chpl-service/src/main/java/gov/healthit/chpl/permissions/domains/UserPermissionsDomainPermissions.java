package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.secureduser.ImpersonateUserActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.AddAtlActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAllAcbPermissionsForUserActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAllAtlPermissionsForUserActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAtlActionPermissions;

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
    public static final String ADD_ATL = "ADD_ATL";
    public static final String DELETE_ATL = "DELETE_ATL";
    public static final String DELETE_ALL_ATLS_FOR_USER = "DELETE_ALL_ATLS_FOR_USER";
    /** Permission to impersonate another user. */
    public static final String IMPERSONATE_USER = "IMPERSONATE_USER";

    @Autowired
    public UserPermissionsDomainPermissions(
            @Qualifier("userPermissionsAddAcbActionPermissions") final AddAcbActionPermissions addAcbActionPermissions,
            @Qualifier("userPermissionsDeleteAcbActionPermissions") final DeleteAcbActionPermissions deleteAcbActionPermissions,
            @Qualifier("userPermissionsDeleteAllAcbPermissionsForUserActionPermissions") final DeleteAllAcbPermissionsForUserActionPermissions deleteAllAcbPermissionsForUserActionPermissions,
            @Qualifier("userPermissionsImpersonateUserActionPermissions") final ImpersonateUserActionPermissions impersonateUserActionPermissions,
            @Qualifier("userPermissionsAddAtlActionPermissions") final AddAtlActionPermissions addAtlActionPermissions,
            @Qualifier("userPermissionsDeleteAtlActionPermissions") final DeleteAtlActionPermissions deleteAtlActionPermissions,
            @Qualifier("userPermissionsDeleteAllAtlPermissionsForUserActionPermissions") final DeleteAllAtlPermissionsForUserActionPermissions deleteAllAtlPermissionsForUserActionPermissions) {

        getActionPermissions().put(ADD_ACB, addAcbActionPermissions);
        getActionPermissions().put(DELETE_ACB, deleteAcbActionPermissions);
        getActionPermissions().put(DELETE_ALL_ACBS_FOR_USER, deleteAllAcbPermissionsForUserActionPermissions);
        getActionPermissions().put(ADD_ATL, addAtlActionPermissions);
        getActionPermissions().put(DELETE_ATL, deleteAtlActionPermissions);
        getActionPermissions().put(DELETE_ALL_ATLS_FOR_USER, deleteAllAtlPermissionsForUserActionPermissions);
        getActionPermissions().put(IMPERSONATE_USER, impersonateUserActionPermissions);
    }
}
