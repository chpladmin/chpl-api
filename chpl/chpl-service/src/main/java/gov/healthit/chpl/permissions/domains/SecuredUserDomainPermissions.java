package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.secureduser.AddOrganizationToUserActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetByUserNameActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.UpdateActionPermissions;

@Component
public class SecuredUserDomainPermissions extends DomainPermissions {
    public static final String GET_ALL = "GET_ALL";
    public static final String GET_BY_USER_NAME = "GET_BY_USER_NAME";
    public static final String UPDATE = "UPDATE";
    public static final String ADD_ORG_TO_USER = "ADD_ORG_TO_USER";

    @Autowired
    public SecuredUserDomainPermissions(
            @Qualifier("securedUserGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("securedUserGetByUserNameActionPermisions") GetByUserNameActionPermissions getByUserNameActionPermissions,
            @Qualifier("securedUserUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("securedUserAddOrganizationToUserActionPermissions") AddOrganizationToUserActionPermissions addOrganizationToUserActionPermissions) {

        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(GET_BY_USER_NAME, getByUserNameActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(ADD_ORG_TO_USER, addOrganizationToUserActionPermissions);
    }
}
