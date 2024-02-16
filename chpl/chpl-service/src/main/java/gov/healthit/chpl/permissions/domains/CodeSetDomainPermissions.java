package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.codessetdate.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.codessetdate.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.codessetdate.UpdateActionPermissions;

@Component
public class CodeSetDomainPermissions extends DomainPermissions {
    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public CodeSetDomainPermissions(
            @Qualifier("codeSetDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("codeSetUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("codeSetCreateActionPermissions") CreateActionPermissions createActionPermissions) {
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }



}
