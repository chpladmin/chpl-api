package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.secureduser.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.FailedLoginCountActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetByPermissionActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetByUserNameActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetPermissionsByUserActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GrantRoleActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GrantRoleAdminActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.ImpersonateUserActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.LockedStatusActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.RemoveRoleActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.RemoveRoleAdminActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdateActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdateContactInfoActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdatePasswordActionPermissions;

@Component
public class SecuredUserDomainPermissions extends DomainPermissions {
    /** Permission to create. */
    public static final String CREATE = "CREATE";
    /** Permission to delete. */
    public static final String DELETE = "DELETE";
    /** Permission to update failed login count. */
    public static final String FAILED_LOGIN_COUNT = "FAILED_LOGIN_COUNT";
    /** Permission to get all. */
    public static final String GET_ALL = "GET_ALL";
    /** Permission to get by id. */
    public static final String GET_BY_ID = "GET_BY_ID";
    /** Permission to get by permission. */
    public static final String GET_BY_PERMISSION = "GET_BY_PERMISSION";
    /** Permission to get by user name. */
    public static final String GET_BY_USER_NAME = "GET_BY_USER_NAME";
    /** Permission to get permissions. */
    public static final String GET_PERMISSIONS = "GET_PERMISSIONS";
    /** Permission to grant role. */
    public static final String GRANT_ROLE = "GRANT_ROLE";
    /** Permission to grant role admin. */
    public static final String GRANT_ROLE_ADMIN = "GRANT_ROLE_ADMIN";
    /** Permission to update locked status. */
    public static final String LOCKED_STATUS = "LOCKED_STATUS";
    /** Permission to remove role. */
    public static final String REMOVE_ROLE = "REMOVE_ROLE";
    /** Permission to remove role admin. */
    public static final String REMOVE_ROLE_ADMIN = "REMOVE_ROLE_ADMIN";
    /** Permission to update. */
    public static final String UPDATE = "UPDATE";
    /** Permission to update contact info. */
    public static final String UPDATE_CONTACT_INFO = "UPDATE_CONTACT_INFO";
    /** Permission to update password. */
    public static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
    /** Permission to impersonate another user. */
    public static final String IMPERSONATE_USER = "IMPERSONATE_USER";

    @Autowired
    public SecuredUserDomainPermissions(
            @Qualifier("securedUserCreateActionPermissions")
            final CreateActionPermissions createActionPermissions,
            @Qualifier("securedUserDeleteActionPermissions")
            final DeleteActionPermissions deleteActionPermissions,
            @Qualifier("securedUserFailedLoginCountActionPermissions")
            final FailedLoginCountActionPermissions failedLoginCountActionPermissions,
            @Qualifier("securedUserGetAllActionPermissions")
            final GetAllActionPermissions getAllActionPermissions,
            @Qualifier("securedUserGetByIdActionPermissions")
            final GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("securedUserGetByPermissionActionPermissions")
            final GetByPermissionActionPermissions getByPermissionActionPermissions,
            @Qualifier("securedUserGetByUserNameActionPermisions")
            final GetByUserNameActionPermissions getByUserNameActionPermissions,
            @Qualifier("securedUserGetPermissionsByUserActionPermissions")
            final GetPermissionsByUserActionPermissions getPermissionsByUserActionPermissions,
            @Qualifier("securedUserGrantRoleActionPermissions")
            final GrantRoleActionPermissions grantRoleActionPermissions,
            @Qualifier("securedUserGrantRoleAdminActionPermissions")
            final GrantRoleAdminActionPermissions grantRoleAdminActionPermissions,
            @Qualifier("securedUserLockStatusActionPermissions")
            final LockedStatusActionPermissions lockedStatusActionPermissions,
            @Qualifier("securedUserRemoveRoleActionPermissions")
            final RemoveRoleActionPermissions removeRoleActionPermissions,
            @Qualifier("securedUserRemoveRoleAdminActionPermissions")
            final RemoveRoleAdminActionPermissions removeRoleAdminActionPermissions,
            @Qualifier("securedUserUpdateActionPermissions")
            final UpdateActionPermissions updateActionPermissions,
            @Qualifier("securedUserUpdateContactInfoActionPermissions")
            final UpdateContactInfoActionPermissions updateContactInfoActionPermissions,
            @Qualifier("securedUserUpdatePasswordActionPermissions")
            final UpdatePasswordActionPermissions updatePasswordActionPermissions,
            @Qualifier("userPermissionsImpersonateUserActionPermissions")
            final ImpersonateUserActionPermissions impersonateUserActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(FAILED_LOGIN_COUNT, failedLoginCountActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
        getActionPermissions().put(GET_BY_PERMISSION, getByPermissionActionPermissions);
        getActionPermissions().put(GET_BY_USER_NAME, getByUserNameActionPermissions);
        getActionPermissions().put(GET_PERMISSIONS, getPermissionsByUserActionPermissions);
        getActionPermissions().put(GRANT_ROLE, grantRoleActionPermissions);
        getActionPermissions().put(GRANT_ROLE_ADMIN, grantRoleAdminActionPermissions);
        getActionPermissions().put(LOCKED_STATUS, lockedStatusActionPermissions);
        getActionPermissions().put(REMOVE_ROLE, removeRoleActionPermissions);
        getActionPermissions().put(REMOVE_ROLE_ADMIN, removeRoleAdminActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(UPDATE_CONTACT_INFO, updateContactInfoActionPermissions);
        getActionPermissions().put(UPDATE_PASSWORD, updatePasswordActionPermissions);
        getActionPermissions().put(IMPERSONATE_USER, impersonateUserActionPermissions);
    }
}
