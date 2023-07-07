package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.svap.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.svap.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.svap.UpdateActionPermissions;

@Component
public class TestToolDomainPermissions extends DomainPermissions {
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public TestToolDomainPermissions(
            @Qualifier("testToolDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("testToolUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("testToolCreateActionPermissions") CreateActionPermissions createActionPermissions) {
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }

}
