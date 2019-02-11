package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.productversion.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.productversion.UpdateActionPermissions;

@Component
public class ProductVersionDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";

    @Autowired
    public ProductVersionDomainPermissions(
            @Qualifier("productVersionCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("productVersionUpdateActionPermissions") UpdateActionPermissions updateActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
    }
}
