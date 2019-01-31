package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.userpermissions.AddActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteActionPermissions;

@Component
public class UserPermissionDomainPermissions extends DomainPermissions {
    public static final String ADD = "ADD";
    public static final String DELETE = "DELETE";

    @Autowired
    public UserPermissionDomainPermissions(
            @Qualifier("userPermissionsAddActionPermissions") AddActionPermissions addActionPermissions,
            @Qualifier("userPermissionsDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions) {

        getActionPermissions().put(ADD, addActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
    }
}
