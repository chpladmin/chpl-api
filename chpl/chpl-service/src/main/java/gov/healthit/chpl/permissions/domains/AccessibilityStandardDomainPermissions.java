package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.accessibilityStandard.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.accessibilityStandard.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.accessibilityStandard.UpdateActionPermissions;

@Component
public class AccessibilityStandardDomainPermissions extends DomainPermissions {

    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public AccessibilityStandardDomainPermissions(
            @Qualifier("accessibilityStandardDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("accessibilityStandardUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("accessibilityStandardCreateActionPermissions") CreateActionPermissions createActionPermissions) {

        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }

}
