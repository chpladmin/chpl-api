package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.certificationbody.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.DeleteAllAcbPermissionForUserActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.DeleteAllPermissionsForUserActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.PermissionsByUserActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.RetireActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.UnretireActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.UpdateActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.UsersByAcbActionPermissions;

@Component
public class CertificationBodyDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String RETIRE = "RETIRE";
    public static final String UNRETIRE = "UNRETIRE";
    public static final String GET_BY_ID = "GET_BY_ID";
    public static final String USERS_BY_ACB = "USERS_BY_ACB";
    public static final String PERMISSIONS_BY_USER = "PERMISSIONS_BY_USER";
    public static final String DELETE_ALL_ACB_PERMISSIONS_FOR_USER = "DELETE_ALL_ACB_PERMISSIONS_FOR_USER";
    public static final String DELETE_ALL_PERMISSIONS_FOR_USER = "DELETE_ALL_PERMISSIONS_FOR_USER";

    public CertificationBodyDomainPermissions(
            @Qualifier("certificationBodyCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("certificationBodyUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("certificationBodyRetireActionPermissions") RetireActionPermissions retireActionPermissions,
            @Qualifier("certificationBodyUnretireActionPermissions") UnretireActionPermissions unretireActionPermissions,
            @Qualifier("certificationBodyGetByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("certificationBodyUsersByAcbActionPermissions") UsersByAcbActionPermissions usersByAcbActionPermissions,
            @Qualifier("certificationBodyPermissionsByUserActionPermissions") PermissionsByUserActionPermissions permissionsByUserActionPermissions,
            @Qualifier("certificationBodyDeleteAllAcbPermissionForUserActionPermissions") DeleteAllAcbPermissionForUserActionPermissions deleteAllAcbPermissionForUserActionPermissions,
            @Qualifier("certificationBodyDeleteAllPermissionsForUserActionPermissions") DeleteAllPermissionsForUserActionPermissions deleteAllPermissionsForUserActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(RETIRE, retireActionPermissions);
        getActionPermissions().put(UNRETIRE, unretireActionPermissions);
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
        getActionPermissions().put(USERS_BY_ACB, usersByAcbActionPermissions);
        getActionPermissions().put(PERMISSIONS_BY_USER, permissionsByUserActionPermissions);
        getActionPermissions().put(DELETE_ALL_ACB_PERMISSIONS_FOR_USER, deleteAllAcbPermissionForUserActionPermissions);
        getActionPermissions().put(DELETE_ALL_PERMISSIONS_FOR_USER, deleteAllPermissionsForUserActionPermissions);
    }
}
