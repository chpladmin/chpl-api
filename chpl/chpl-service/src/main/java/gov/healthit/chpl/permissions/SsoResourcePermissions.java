package gov.healthit.chpl.permissions;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.CognitoAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SsoResourcePermissions implements ResourcePermissions {
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    public SsoResourcePermissions(CertificationBodyDAO certificationBodyDAO) {
        this.certificationBodyDAO = certificationBodyDAO;
    }

    @Override
    public boolean isDeveloperActive(Long developerId) {
        throw new NotImplementedException("Not implemented: 1");
    }

    @Override
    public UserDTO getUserByName(String userName) throws UserRetrievalException {
        throw new NotImplementedException("Not implemented: 2");
    }

    @Override
    public UserDTO getUserById(Long userId) throws UserRetrievalException {
        throw new NotImplementedException("Not implemented: 3");
    }

    @Override
    public List<UserDTO> getAllUsersOnAcb(CertificationBody acb) {
        throw new NotImplementedException("Not implemented: 5");
    }

    @Override
    public List<UserDTO> getAllUsersOnDeveloper(Developer dev) {
        throw new NotImplementedException("Not implemented: 6");
    }

    @Override
    public List<CertificationBody> getAllAcbsForCurrentUser() {
        CognitoAuthenticatedUser user = AuthUtil.getCurrentSsoUser();
        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                return certificationBodyDAO.findAll();
            } else {
                return user.getOrganizationIds().stream()
                        .map(orgId -> getCertifcationBody(orgId))
                        .collect(Collectors.toList());
            }
        } else {
            return null;
        }
    }

    @Override
    public List<CertificationBody> getAllAcbsForUser(Long userID) {
        throw new NotImplementedException("Not implemented: 7");
    }

    @Override
    public List<TestingLab> getAllAtlsForCurrentUser() {
        throw new NotImplementedException("Not implemented: 8");
    }

    @Override
    public List<Developer> getAllDevelopersForCurrentUser() {
        throw new NotImplementedException("Not implemented: 9");
    }

    @Override
    public List<Developer> getAllDevelopersForUser(Long userId) {
        throw new NotImplementedException("Not implemented: 10");
    }

    @Override
    public List<UserDTO> getAllUsersForCurrentUser() {
        throw new NotImplementedException("Not implemented: 11");
    }

    @Override
    public CertificationBody getAcbIfPermissionById(Long id) throws EntityRetrievalException {
        throw new NotImplementedException("Not implemented: 12");
    }

    @Override
    public Developer getDeveloperIfPermissionById(Long id) throws EntityRetrievalException {
        throw new NotImplementedException("Not implemented: 13");
    }

    @Override
    public UserPermission getRoleByUserId(Long userId) {
        throw new NotImplementedException("Not implemented: 14");
    }

    @Override
    public boolean hasPermissionOnUser(Long userId) {
        throw new NotImplementedException("Not implemented: 15");
    }

    @Override
    public boolean hasPermissionOnUser(UserDTO user) {
        throw new NotImplementedException("Not implemented: 16");
    }

    @Override
    public boolean hasPermissionOnUser(UUID ssoId) {
        throw new NotImplementedException("Not implemented: 17");
    }

    @Override
    public boolean isUserRoleAdmin() {
        return doesAuditUserHaveRole(CognitoGroups.CHPL_ADMIN);
    }

    @Override
    public boolean isUserRoleOnc() {
        return doesAuditUserHaveRole(CognitoGroups.CHPL_ONC);
    }

    //TODO OCD-4379 - Is this still used?
    @Override
    public boolean isUserRoleCmsStaff() {
        return doesAuditUserHaveRole(CognitoGroups.CHPL_CMS_STAFF);
    }

    @Override
    public boolean isUserRoleAcbAdmin() {
        return doesAuditUserHaveRole(CognitoGroups.CHPL_ACB);
    }

    @Override
    public boolean isUserRoleDeveloperAdmin() {
        return doesAuditUserHaveRole(CognitoGroups.CHPL_DEVELOPER);
    }

    @Override
    public boolean isUserRoleUserCreator() {
        return doesAuditUserHaveRole(CognitoGroups.CHPL_USER_CREATOR);
    }

    @Override
    public boolean isUserRoleUserAuthenticator() {
        return doesAuditUserHaveRole(CognitoGroups.CHPL_USER_AUTHENTICATOR);
    }

    @Override
    public boolean isUserRoleInvitedUserCreator() {
        return doesAuditUserHaveRole(CognitoGroups.CHPL_INVITED_USER_CREATOR);
    }

    @Override
    public boolean isUserRoleStartup() {
        return doesAuditUserHaveRole(CognitoGroups.CHPL_STARTUP);
    }

    @Override
    public boolean isUserAnonymous() {
        return AuthUtil.getCurrentUser() == null;
    }

    @Override
    public boolean doesUserHaveRole(List<String> authorities) {
        throw new NotImplementedException("Not implemented: 25");
    }

    @Override
    public boolean doesUserHaveRole(String authority) {
        CognitoAuthenticatedUser user = AuthUtil.getCurrentSsoUser();
        if (user == null) {
            return false;
        }

        return user.getPermissions().stream()
                .filter(permission -> permission.getAuthority().equals(authority))
                .findAny()
                .isPresent();
    }

    @Override
    public boolean doesAuditUserHaveRole(String authority) {
        //TODO This will need to be changed when we figure out Impersonation
        return doesUserHaveRole(authority);
    }

    private CertificationBody getCertifcationBody(Long certificationBodyId) {
        try {
            return certificationBodyDAO.getById(certificationBodyId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve Certification Body: {}", certificationBodyId);
            return null;
        }
    }

}
