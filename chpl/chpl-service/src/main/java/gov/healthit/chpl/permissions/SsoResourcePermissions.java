package gov.healthit.chpl.permissions;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.CognitoAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SsoResourcePermissions implements ResourcePermissions {
    private CertificationBodyDAO certificationBodyDAO;
    private DeveloperDAO developerDAO;
    private CognitoUserService cognitoUserService;

    @Autowired
    public SsoResourcePermissions(CertificationBodyDAO certificationBodyDAO, DeveloperDAO developerDAO, CognitoUserService cognitoUserService) {
        this.certificationBodyDAO = certificationBodyDAO;
        this.developerDAO = developerDAO;
        this.cognitoUserService = cognitoUserService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDeveloperActive(Long developerId) {
        try {
            Developer developer = developerDAO.getById(developerId);
            return developer != null && developer.getStatus() != null
                    && developer.getStatus().getStatus().equals(DeveloperStatusType.Active.toString());
        } catch (EntityRetrievalException e) {
            return false;
        }
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
        try {
            User user = cognitoUserService.getUserInfo(AuthUtil.getCurrentUser().getSsoId());
            if (user != null) {
                if (isUserRoleAdmin() || isUserRoleOnc()) {
                    return certificationBodyDAO.findAll();
                } else {
                    return getAllAcbsForUser(user);
                }
            } else {
                return null;
            }
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not retrieve all ONC-ACBs for current user.", e);
            return null;
        }
    }

    @Override
    public List<CertificationBody> getAllAcbsForUser(User user) {
        return user.getOrganizations().stream()
                .map(org -> getCertifcationBody(org.getId()))
                .toList();
    }

    @Override
    public List<Developer> getAllDevelopersForCurrentUser() {
        try {
            User user = cognitoUserService.getUserInfo(AuthUtil.getCurrentUser().getSsoId());
            return getAllDevelopersForUser(user);
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not retrieve all developers for current user.", e);
            return null;
        }
    }

    @Override
    public List<Developer> getAllDevelopersForUser(User user) {
        return user.getOrganizations().stream()
                .map(org -> getDeveloper(org.getId()))
                .toList();

    }

    @Override
    public List<UserDTO> getAllUsersForCurrentUser() {
        throw new NotImplementedException("Not implemented: 11");
    }

    @Override
    public CertificationBody getAcbIfPermissionById(Long certificationBodyId) throws EntityRetrievalException {
        throw new NotImplementedException("Not implemented: 12");
    }

    @Override
    public Developer getDeveloperIfPermissionById(Long developerId) throws EntityRetrievalException {
        throw new NotImplementedException("Not implemented: 13");
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

    private Developer getDeveloper(Long developerId) {
        try {
            return developerDAO.getById(developerId);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve Developer: {}", developerId);
            return null;
        }
    }

    @Override
    public UserPermission getRoleByUser(User user) {
        throw new NotImplementedException("Not implemented: 35");
    }

    @Override
    public boolean hasPermissionOnUser(User user) {
        throw new NotImplementedException("Not implemented: 45");
    }

}
