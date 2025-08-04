package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.conformanceMethod.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.conformanceMethod.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.conformanceMethod.UpdateActionPermissions;

@Component
public class ConformanceMethodDomainPermissions extends DomainPermissions {
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public ConformanceMethodDomainPermissions(
            @Qualifier("conformanceMethodDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("conformanceMethodUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("conformanceMethodCreateActionPermissions") CreateActionPermissions createActionPermissions) {
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }



}
