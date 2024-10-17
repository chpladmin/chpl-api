package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.apiKey.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.apiKey.GetAllActionPermissions;

@Component
public class ApiKeyDomainPermissions extends DomainPermissions {
    public static final String DELETE = "DELETE";
    public static final String GET_ALL = "GET_ALL";

    @Autowired
    public ApiKeyDomainPermissions(
            @Qualifier("apiKeyGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("apiKeyDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions) {

        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
    }

}
