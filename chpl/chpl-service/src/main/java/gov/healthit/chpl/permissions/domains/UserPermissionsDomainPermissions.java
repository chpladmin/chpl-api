package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.userpermissions.AddAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAcbActionPermissions;
import gov.healthit.chpl.permissions.domains.userpermissions.DeleteAllAcbPermissionsForUserActionPermissions;

@Component
public class UserPermissionsDomainPermissions extends DomainPermissions {
    public static final String ADD_ACB = "ADD_ACB";
    public static final String DELETE_ACB = "DELETE_ACB";
    public static final String DELETE_ALL_ACBS_FOR_USER = "DELETE_ALL_ACBS_FOR_USER";

    @Autowired
    public UserPermissionsDomainPermissions(
            @Qualifier("userPermissionsAddAcbActionPermissions") AddAcbActionPermissions addAcbActionPermissions,
            @Qualifier("userPermissionsDeleteAcbActionPermissions") DeleteAcbActionPermissions deleteAcbActionPermissions,
            @Qualifier("userPermissionsDeleteAllAcbPermissionsForUserActionPermissions") DeleteAllAcbPermissionsForUserActionPermissions deleteAllAcbPermissionsForUserActionPermissions) {

        getActionPermissions().put(ADD_ACB, addAcbActionPermissions);
        getActionPermissions().put(DELETE_ACB, deleteAcbActionPermissions);
        getActionPermissions().put(DELETE_ALL_ACBS_FOR_USER, deleteAllAcbPermissionsForUserActionPermissions);
    }
}
