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
import gov.healthit.chpl.permissions.domains.secureduser.LockedStatusActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.RemoveRoleActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.RemoveRoleAdminActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdateActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdateContactInfoActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdatePasswordActionPermissions;

@Component
public class SecuredUserDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String DELETE = "DELETE";
    public static final String FAILED_LOGIN_COUNT = "FAILED_LOGIN_COUNT";
    public static final String GET_ALL = "GET_ALL";
    public static final String GET_BY_ID = "GET_BY_ID";
    public static final String GET_BY_PERMISSION = "GET_BY_PERMISSION";
    public static final String GET_BY_USER_NAME = "GET_BY_USER_NAME";
    public static final String GET_PERMISSIONS = "GET_PERMISSIONS";
    public static final String GRANT_ROLE = "GRANT_ROLE";
    public static final String GRANT_ROLE_ADMIN = "GRANT_ROLE_ADMIN";
    public static final String LOCKED_STATUS = "LOCKED_STATUS";
    public static final String REMOVE_ROLE = "REMOVE_ROLE";
    public static final String REMOVE_ROLE_ADMIN = "REMOVE_ROLE_ADMIN";
    public static final String UPDATE = "UPDATE";
    public static final String UPDATE_CONTACT_INFO = "UPDATE_CONTACT_INFO";
    public static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";

    @Autowired
    public SecuredUserDomainPermissions(
            @Qualifier("securedUserCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("securedUserDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("securedUserFailedLoginCountActionPermissions") FailedLoginCountActionPermissions failedLoginCountActionPermissions,
            @Qualifier("securedUserGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("securedUserGetByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("securedUserGetByPermissionActionPermissions") GetByPermissionActionPermissions getByPermissionActionPermissions,
            @Qualifier("securedUserGetByUserNameActionPermisions") GetByUserNameActionPermissions getByUserNameActionPermissions,
            @Qualifier("securedUserGetPermissionsByUserActionPermissions") GetPermissionsByUserActionPermissions getPermissionsByUserActionPermissions,
            @Qualifier("securedUserGrantRoleActionPermissions") GrantRoleActionPermissions grantRoleActionPermissions,
            @Qualifier("securedUserGrantRoleAdminActionPermissions") GrantRoleAdminActionPermissions grantRoleAdminActionPermissions,
            @Qualifier("securedUserLockStatusActionPermissions") LockedStatusActionPermissions lockedStatusActionPermissions,
            @Qualifier("securedUserRemoveRoleActionPermissions") RemoveRoleActionPermissions removeRoleActionPermissions,
            @Qualifier("securedUserRemoveRoleAdminActionPermissions") RemoveRoleAdminActionPermissions removeRoleAdminActionPermissions,
            @Qualifier("securedUserUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("securedUserUpdateContactInfoActionPermissions") UpdateContactInfoActionPermissions updateContactInfoActionPermissions,
            @Qualifier("securedUserUpdatePasswordActionPermissions") UpdatePasswordActionPermissions updatePasswordActionPermissions) {

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
    }
}
