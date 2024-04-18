package gov.healthit.chpl.permissions;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CognitoResourcePermissions implements ResourcePermissions {
    private CertificationBodyDAO certificationBodyDAO;
    private DeveloperDAO developerDAO;
    private CognitoApiWrapper cognitoApiWrapper;
    private ErrorMessageUtil errorMessageUtil;


    public CognitoResourcePermissions(CertificationBodyDAO certificationBodyDAO, DeveloperDAO developerDAO, CognitoApiWrapper cognitoApiWrapper,
            ErrorMessageUtil errorMessageUtil) {

        this.certificationBodyDAO = certificationBodyDAO;
        this.developerDAO = developerDAO;
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.errorMessageUtil = errorMessageUtil;
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
    public List<User> getAllUsersOnAcb(CertificationBody acb) {
        return cognitoApiWrapper.getAllUsers().stream()
                .filter(user -> user.getRole() != null
                        && user.getRole().equals(CognitoGroups.CHPL_ACB)
                        && user.getOrganizations().stream()
                                .filter(org -> org.getId().equals(acb.getId()))
                                .findAny()
                                .isPresent())
                .toList();
    }

    @Override
    public List<User> getAllUsersOnDeveloper(Developer dev) {
        return cognitoApiWrapper.getAllUsers().stream()
                .filter(user -> user.getRole() != null
                        && user.getRole().equals(CognitoGroups.CHPL_DEVELOPER)
                        && user.getOrganizations().stream()
                                .filter(org -> org.getId().equals(dev.getId()))
                                .findAny()
                                .isPresent())
                .toList();
    }

    @Override
    public List<CertificationBody> getAllAcbsForCurrentUser() {
        try {
            User user = cognitoApiWrapper.getUserInfo(AuthUtil.getCurrentUser().getCognitoId());
            if (user != null) {
                if (isUserRoleAdmin() || isUserRoleOnc()) {
                    return certificationBodyDAO.findAll();
                } else if (isUserRoleAcbAdmin()) {
                    return getAllAcbsForUser(user);
                } else {
                    return null;
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
            User user = cognitoApiWrapper.getUserInfo(AuthUtil.getCurrentUser().getCognitoId());
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
    public List<User> getAllUsersForCurrentUser() {
        LOGGER.error("Not implemented: getAllUsersForCurrentUser");
        throw new NotImplementedException("Not implemented: getAllUsersForCurrentUser");
    }

    @Override
    public CertificationBody getAcbIfPermissionById(Long certificationBodyId) throws EntityRetrievalException {
        try {
            certificationBodyDAO.getById(certificationBodyId);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("acb.notFound"));
        }

        List<CertificationBody> acbs = getAllAcbsForCurrentUser();
        CollectionUtils.filter(acbs, new Predicate<CertificationBody>() {
            @Override
            public boolean evaluate(final CertificationBody object) {
                return object.getId().equals(certificationBodyId);
            }

        });

        if (acbs.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return acbs.get(0);
    }

    @Override
    public Developer getDeveloperIfPermissionById(Long developerId) throws EntityRetrievalException {
        LOGGER.error("Not implemented: getDeveloperIfPermissionById");
        throw new NotImplementedException("Not implemented: getDeveloperIfPermissionById");
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
        LOGGER.error("Not implemented: doesUserHaveRole");
        throw new NotImplementedException("Not implemented: doesUserHaveRole");
    }

    @Override
    public boolean doesUserHaveRole(String authority) {
        JWTAuthenticatedUser user = AuthUtil.getCurrentUser();
        if (user == null) {
            return false;
        }

        return user.getAuthorities().stream()
                .filter(auth -> auth.getAuthority().equals(authority))
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
        LOGGER.error("Not implemented: getRoleByUser");
        throw new NotImplementedException("Not implemented: getRoleByUser");
    }

    @Override
    public boolean hasPermissionOnUser(User user) {
        if (user.getRole() == null) {
            return false;
        } else if (user.getRole().equalsIgnoreCase(CognitoGroups.CHPL_STARTUP)) {
            return false;
        } else if (isUserRoleAdmin() || (AuthUtil.getCurrentUser().getCognitoId()).equals(user.getCognitoId())) {
            return true;
        } else if (isUserRoleOnc()) {
            return !user.getRole().equalsIgnoreCase(CognitoGroups.CHPL_ADMIN);
        } else if (isUserRoleAcbAdmin()) {
            if (user.getRole().equalsIgnoreCase(CognitoGroups.CHPL_DEVELOPER)) {
                return true;
            }
            // is the user being checked on any of the same ACB(s) that the current user is on?
            List<CertificationBody> currUserAcbs = getAllAcbsForCurrentUser();
            List<CertificationBody> otherUserAcbs = getAllAcbsForUser(user);
            for (CertificationBody currUserAcb : currUserAcbs) {
                for (CertificationBody otherUserAcb : otherUserAcbs) {
                    if (currUserAcb.getId().equals(otherUserAcb.getId())) {
                        return true;
                    }
                }
            }
        } else if (isUserRoleDeveloperAdmin()) {
            // is the user being checked on any of the same Developer(s) that the current user is on?
            List<Developer> currUserDevs = getAllDevelopersForCurrentUser();
            List<Developer> otherUserDevs = getAllDevelopersForUser(user);
            for (Developer currUserDev : currUserDevs) {
                for (Developer otherUserDev : otherUserDevs) {
                    if (currUserDev.getId().equals(otherUserDev.getId())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
