package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Qualifier;

import gov.healthit.chpl.permissions.domains.productversion.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.productversion.UpdateActionPermissions;

public class ProductVersionDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";

    public ProductVersionDomainPermissions(
            @Qualifier("productVersionCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("productVersionUpdateActionPermissions") UpdateActionPermissions updateActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
    }
}
