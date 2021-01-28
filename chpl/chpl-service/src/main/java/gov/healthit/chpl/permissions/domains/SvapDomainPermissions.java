package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.svap.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.svap.UpdateActionPermssions;

@Component
public class SvapDomainPermissions extends DomainPermissions {

    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";

    @Autowired
    public SvapDomainPermissions(
            @Qualifier("svapDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("svapUpdateActionPermissions") UpdateActionPermssions updateActionPermissions) {

        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
    }

}
