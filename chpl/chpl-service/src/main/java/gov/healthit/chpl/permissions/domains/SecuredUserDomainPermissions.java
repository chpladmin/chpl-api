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
import gov.healthit.chpl.permissions.domains.secureduser.ImpersonateUserActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.LockedStatusActionPermissions;
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
    /** Permission to update locked status. */
    public static final String LOCKED_STATUS = "LOCKED_STATUS";
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
            @Qualifier("securedUserLockStatusActionPermissions")
            final LockedStatusActionPermissions lockedStatusActionPermissions,
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
        getActionPermissions().put(LOCKED_STATUS, lockedStatusActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(UPDATE_CONTACT_INFO, updateContactInfoActionPermissions);
        getActionPermissions().put(UPDATE_PASSWORD, updatePasswordActionPermissions);
        getActionPermissions().put(IMPERSONATE_USER, impersonateUserActionPermissions);
    }
}
