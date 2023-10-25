package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.functionalitytested.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.functionalitytested.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.functionalitytested.UpdateActionPermissions;

@Component
public class StandardDomainPermissions extends DomainPermissions {
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public StandardDomainPermissions(
            @Qualifier("standardDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("standardUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("standardCreateActionPermissions") CreateActionPermissions createActionPermissions) {
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }

}
