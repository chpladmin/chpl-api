package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.secureduser.AddOrganizationToUserActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.CognitoUpdateActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.secureduser.GetByUserNameActionPermissions;

@Component
public class SecuredUserDomainPermissions extends DomainPermissions {
    public static final String GET_ALL = "GET_ALL";
    public static final String GET_BY_USER_NAME = "GET_BY_USER_NAME";
    public static final String UPDATE_COGNITO = "UPDATE_COGNITO";
    public static final String ADD_ORG_TO_USER = "ADD_ORG_TO_USER";

    @Autowired
    public SecuredUserDomainPermissions(
            @Qualifier("securedUserGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("securedUserGetByUserNameActionPermisions") GetByUserNameActionPermissions getByUserNameActionPermissions,
            @Qualifier("securedCognitoUserUpdateActionPermissions") CognitoUpdateActionPermissions cognitoUpdateActionPermissions,
            @Qualifier("securedCognitoUserAddOrganizationToUserActionPermissions") AddOrganizationToUserActionPermissions addOrganizationToUserActionPermissions) {

        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(GET_BY_USER_NAME, getByUserNameActionPermissions);
        getActionPermissions().put(UPDATE_COGNITO, cognitoUpdateActionPermissions);
        getActionPermissions().put(ADD_ORG_TO_USER, addOrganizationToUserActionPermissions);
    }
}
