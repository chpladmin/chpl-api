package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Qualifier;

import gov.healthit.chpl.permissions.domains.product.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.product.SplitActionPermissions;
import gov.healthit.chpl.permissions.domains.product.UpdateActionPermissions;

public class ProductDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String SPLIT = "SPLIT";

    public ProductDomainPermissions(
            @Qualifier("productCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("productUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("productSplitActionPermissions") SplitActionPermissions splitActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(SPLIT, splitActionPermissions);
    }
}
