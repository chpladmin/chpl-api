package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.productversion.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.productversion.MergeActionPermissions;
import gov.healthit.chpl.permissions.domains.productversion.SplitActionPermissions;
import gov.healthit.chpl.permissions.domains.productversion.UpdateActionPermissions;

@Component
public class ProductVersionDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String MERGE = "MERGE";
    public static final String SPLIT = "SPLIT";

    @Autowired
    public ProductVersionDomainPermissions(
            @Qualifier("productVersionCreateActionPermissions") final CreateActionPermissions createActionPermissions,
            @Qualifier("productVersionUpdateActionPermissions") final UpdateActionPermissions updateActionPermissions,
            @Qualifier("productVersionMergeActionPermissions") final MergeActionPermissions mergeActionPermissions,
            @Qualifier("productVersionSplitActionPermissions") final SplitActionPermissions splitActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(MERGE, mergeActionPermissions);
        getActionPermissions().put(SPLIT, splitActionPermissions);
    }
}
