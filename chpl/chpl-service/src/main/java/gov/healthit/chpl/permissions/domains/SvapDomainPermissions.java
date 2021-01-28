package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.svap.DeleteActionPermissions;

@Component
public class SvapDomainPermissions extends DomainPermissions {

    public static final String DELETE = "DELETE";

    @Autowired
    public SvapDomainPermissions(
            @Qualifier("svapDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions) {

        getActionPermissions().put(DELETE, deleteActionPermissions);
    }

}
