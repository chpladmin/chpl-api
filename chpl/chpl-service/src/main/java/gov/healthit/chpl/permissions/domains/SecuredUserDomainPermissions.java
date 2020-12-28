package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.secureduser.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetByPermissionActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetByUserNameActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.ImpersonateUserActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdateActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdateContactInfoActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdatePasswordActionPermissions;

@Component
public class SecuredUserDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String DELETE = "DELETE";
    public static final String GET_ALL = "GET_ALL";
    public static final String GET_BY_ID = "GET_BY_ID";
    public static final String GET_BY_PERMISSION = "GET_BY_PERMISSION";
    public static final String GET_BY_USER_NAME = "GET_BY_USER_NAME";
    public static final String UPDATE = "UPDATE";
    public static final String UPDATE_CONTACT_INFO = "UPDATE_CONTACT_INFO";
    public static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
    public static final String IMPERSONATE_USER = "IMPERSONATE_USER";

    @Autowired
    @SuppressWarnings({"checkstyle:linelength", "checkstyle:parameternumber"})
    public SecuredUserDomainPermissions(
            @Qualifier("securedUserCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("securedUserDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("securedUserGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("securedUserGetByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("securedUserGetByPermissionActionPermissions") GetByPermissionActionPermissions getByPermissionActionPermissions,
            @Qualifier("securedUserGetByUserNameActionPermisions") GetByUserNameActionPermissions getByUserNameActionPermissions,
            @Qualifier("securedUserUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("securedUserUpdateContactInfoActionPermissions") UpdateContactInfoActionPermissions updateContactInfoActionPermissions,
            @Qualifier("securedUserUpdatePasswordActionPermissions") UpdatePasswordActionPermissions updatePasswordActionPermissions,
            @Qualifier("userPermissionsImpersonateUserActionPermissions") ImpersonateUserActionPermissions impersonateUserActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
        getActionPermissions().put(GET_BY_PERMISSION, getByPermissionActionPermissions);
        getActionPermissions().put(GET_BY_USER_NAME, getByUserNameActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(UPDATE_CONTACT_INFO, updateContactInfoActionPermissions);
        getActionPermissions().put(UPDATE_PASSWORD, updatePasswordActionPermissions);
        getActionPermissions().put(IMPERSONATE_USER, impersonateUserActionPermissions);
    }
}
