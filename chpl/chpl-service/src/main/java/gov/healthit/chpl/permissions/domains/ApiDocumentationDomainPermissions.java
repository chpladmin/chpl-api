package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.apiDocumentation.CreateActionPermissions;

@Component
public class ApiDocumentationDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";

    @Autowired
    public ApiDocumentationDomainPermissions(
            @Qualifier("apiDocumentationCreateActionPermissions") CreateActionPermissions createActionPermissions) {
        getActionPermissions().put(CREATE, createActionPermissions);
    }

}
