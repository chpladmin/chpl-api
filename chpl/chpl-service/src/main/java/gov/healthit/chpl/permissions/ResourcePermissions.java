package gov.healthit.chpl.permissions;

import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ResourcePermissions {

    boolean isDeveloperActive(Long developerId);

    List<User> getAllUsersOnAcb(CertificationBody acb);

    List<User> getAllUsersOnDeveloper(Developer dev);

    List<User> getAllUsersOnDeveloper(Long developerId);

    List<CertificationBody> getAllAcbsForCurrentUser();

    List<CertificationBody> getAllAcbsForUser(User user);

    List<Developer> getAllDevelopersForCurrentUser();

    List<Developer> getAllDevelopersForUser(User user);

    List<User> getAllUsersForCurrentUser();

    CertificationBody getAcbIfPermissionById(Long certificationBodyId) throws EntityRetrievalException;

    Developer getDeveloperIfPermissionById(Long developerId) throws EntityRetrievalException;

    UserPermission getRoleByUser(User user);

    boolean hasPermissionOnUser(User user);

    boolean isUserRoleAdmin();

    boolean isUserRoleOnc();

    boolean isUserRoleCmsStaff();

    boolean isUserRoleAcbAdmin();

    boolean isUserRoleDeveloperAdmin();

    boolean isUserRoleUserCreator();

    boolean isUserRoleUserAuthenticator();

    boolean isUserRoleInvitedUserCreator();

    boolean isUserRoleStartup();

    boolean isUserAnonymous();

    boolean doesUserHaveRole(List<String> authorities);

    boolean doesUserHaveRole(String authority);

    boolean doesAuditUserHaveRole(String authority);

}
