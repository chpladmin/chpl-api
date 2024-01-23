package gov.healthit.chpl.permissions;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface ResourcePermissions {

    boolean isDeveloperActive(Long developerId);

    UserDTO getUserByName(String userName) throws UserRetrievalException;

    UserDTO getUserById(Long userId) throws UserRetrievalException;

    List<UserDTO> getAllUsersOnAcb(CertificationBody acb);

    List<UserDTO> getAllUsersOnDeveloper(Developer dev);

    List<CertificationBody> getAllAcbsForCurrentUser();

    List<CertificationBody> getAllAcbsForUser(Long userID);

    // TODO - OCD-4379 - Is this still used??
    List<TestingLab> getAllAtlsForCurrentUser();

    List<Developer> getAllDevelopersForCurrentUser();

    List<Developer> getAllDevelopersForUser(Long userId);

    List<UserDTO> getAllUsersForCurrentUser();

    CertificationBody getAcbIfPermissionById(Long id) throws EntityRetrievalException;

    Developer getDeveloperIfPermissionById(Long id) throws EntityRetrievalException;

    UserPermission getRoleByUserId(Long userId);

    boolean hasPermissionOnUser(Long userId);

    //TODO - This can be removed when SSO is implemented
    boolean hasPermissionOnUser(UserDTO user);

    //TODO - This method will get built out to handle ONCs, ACBs, Developers as we continue to implemnt SSO
    boolean hasPermissionOnUser(UUID ssoId);

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
