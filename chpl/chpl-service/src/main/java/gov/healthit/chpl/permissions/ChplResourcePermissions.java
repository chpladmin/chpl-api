package gov.healthit.chpl.permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.user.SystemUsers;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.CognitoUserService;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class ChplResourcePermissions implements ResourcePermissions {
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private UserDeveloperMapDAO userDeveloperMapDAO;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationBodyDAO acbDAO;
    private TestingLabDAO atlDAO;
    private UserDAO userDAO;
    private DeveloperDAO developerDAO;
    private CognitoUserService cognitoUserService;
    private FF4j ff4j;

    @SuppressWarnings({"checkstyle:parameternumber"})
    @Autowired
    public ChplResourcePermissions(UserCertificationBodyMapDAO userCertificationBodyMapDAO, UserDeveloperMapDAO userDeveloperMapDAO,
            CertificationBodyDAO acbDAO, TestingLabDAO atlDAO, ErrorMessageUtil errorMessageUtil, UserDAO userDAO,
            DeveloperDAO developerDAO, CognitoUserService cognitoUserService, FF4j ff4j) {

        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.acbDAO = acbDAO;
        this.atlDAO = atlDAO;
        this.errorMessageUtil = errorMessageUtil;
        this.userDAO = userDAO;
        this.developerDAO = developerDAO;
        this.userDeveloperMapDAO = userDeveloperMapDAO;
        this.cognitoUserService = cognitoUserService;
        this.ff4j = ff4j;
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
    @Deprecated
    @Transactional(readOnly = true)
    public UserDTO getUserByName(String userName) throws UserRetrievalException {
        UserDTO user = null;
        try {
            user = userDAO.getByNameOrEmail(userName);
        } catch (MultipleUserAccountsException ex) {
            throw new UserRetrievalException(ex.getMessage());
        }
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) throws UserRetrievalException {
        return userDAO.getById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersOnAcb(CertificationBody acb) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByAcbId(acb.getId());

        for (UserCertificationBodyMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersOnDeveloper(Developer dev) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserDeveloperMapDTO> dtos = userDeveloperMapDAO.getByDeveloperId(dev.getId());

        for (UserDeveloperMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationBody> getAllAcbsForCurrentUser() {
        User user = AuthUtil.getCurrentUser();
        List<CertificationBody> acbs = new ArrayList<CertificationBody>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                acbs = acbDAO.findAll();
            } else {
                if (ff4j.check(FeatureList.SSO)) {
                    
                } else {
                    List<UserCertificationBodyMapDTO> userAcbMaps = userCertificationBodyMapDAO.getByUserId(user.getId());
                    acbs = userAcbMaps.stream()
                            .map(userAcbMap -> userAcbMap.getCertificationBody())
                            .collect(Collectors.toList());
                }
            }
        }
        return acbs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationBody> getAllAcbsForUser(Long userID) {
        List<CertificationBody> acbs = new ArrayList<CertificationBody>();
        List<UserCertificationBodyMapDTO> userAcbMaps = userCertificationBodyMapDAO.getByUserId(userID);
        return userAcbMaps.stream()
            .map(userAcbMap -> userAcbMap.getCertificationBody())
            .collect(Collectors.toList());
    }

    
    // TODO - OCD-4379 - Is this still used??
    @Override
    @Transactional(readOnly = true)
    public List<TestingLab> getAllAtlsForCurrentUser() {
        User user = AuthUtil.getCurrentUser();
        List<TestingLab> atls = new ArrayList<TestingLab>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                atls = atlDAO.findAll();
            }
        }
        return atls;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Developer> getAllDevelopersForCurrentUser() {
        User user = AuthUtil.getCurrentUser();
        List<Developer> developers = new ArrayList<Developer>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc() || isUserRoleAcbAdmin()) {
                developers = developerDAO.findAll();
            } else {
                List<UserDeveloperMapDTO> dtos = userDeveloperMapDAO.getByUserId(user.getId());
                for (UserDeveloperMapDTO dto : dtos) {
                    developers.add(dto.getDeveloper());
                }
            }
        }
        return developers;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Developer> getAllDevelopersForUser(Long userId) {
        List<Developer> devs = new ArrayList<Developer>();
        List<UserDeveloperMapDTO> dtos = userDeveloperMapDAO.getByUserId(userId);
        for (UserDeveloperMapDTO dto : dtos) {
            devs.add(dto.getDeveloper());
        }
        return devs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersForCurrentUser() {
        User user = AuthUtil.getCurrentUser();
        List<UserDTO> users = new ArrayList<UserDTO>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                users = userDAO.findAll();
            } else if (isUserRoleAcbAdmin()) {
                List<CertificationBody> acbs = getAllAcbsForCurrentUser();
                for (CertificationBody acb : acbs) {
                    users.addAll(getAllUsersOnAcb(acb));
                }
            } else if (isUserRoleDeveloperAdmin()) {
                List<Developer> devs = getAllDevelopersForCurrentUser();
                for (Developer dev : devs) {
                    users.addAll(getAllUsersOnDeveloper(dev));
                }
            } else {
                //they just have permission on themselves
                UserDTO thisUser = null;
                try {
                    thisUser = userDAO.getById(user.getId());
                    users.add(thisUser);
                } catch (UserRetrievalException ex) { }
            }
        }
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public CertificationBody getAcbIfPermissionById(Long id) throws EntityRetrievalException {
        try {
            acbDAO.getById(id);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("acb.notFound"));
        }

        List<CertificationBody> dtos = getAllAcbsForCurrentUser();
        CollectionUtils.filter(dtos, new Predicate<CertificationBody>() {
            @Override
            public boolean evaluate(final CertificationBody object) {
                return object.getId().equals(id);
            }

        });

        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public Developer getDeveloperIfPermissionById(Long id) throws EntityRetrievalException {
        try {
            developerDAO.getById(id);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("developer.notFound"));
        }

        List<Developer> developers = getAllDevelopersForCurrentUser();
        List<Developer> developersWithId = developers.stream()
            .filter(developer -> developer.getId().equals(id))
            .toList();

        if (developersWithId.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return developersWithId.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPermission getRoleByUserId(Long userId) {
        try {
            UserDTO user = userDAO.getById(userId);
            return user.getPermission();
        } catch (UserRetrievalException ex) {
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermissionOnUser(Long userId) {
        UserDTO user = null;
        try {
            user = userDAO.getById(userId);
        } catch (UserRetrievalException ex) {
            return false;
        }
        return hasPermissionOnUser(user);
    }

    //TODO - This can be removed when SSO is implemented
    @Override
    @Transactional(readOnly = true)
    public boolean hasPermissionOnUser(UserDTO user) {
        if (getRoleByUserId(user.getId()).getAuthority().equalsIgnoreCase(Authority.ROLE_STARTUP)) {
            return false;
        } else if (isUserRoleAdmin() || AuthUtil.getCurrentUser().getId().equals(user.getId())) {
            return true;
        } else if (isUserRoleOnc()) {
            return !getRoleByUserId(user.getId()).getAuthority().equalsIgnoreCase(Authority.ROLE_ADMIN);
        } else if (isUserRoleAcbAdmin()) {
            if (getRoleByUserId(user.getId()).getAuthority().equalsIgnoreCase(Authority.ROLE_DEVELOPER)) {
                return true;
            }
            // is the user being checked on any of the same ACB(s) that the current user is on?
            List<CertificationBody> currUserAcbs = getAllAcbsForCurrentUser();
            List<CertificationBody> otherUserAcbs = getAllAcbsForUser(user.getId());
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
            List<Developer> otherUserDevs = getAllDevelopersForUser(user.getId());
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

    //TODO - This method will get built out to handle ONCs, ACBs, Developers as we continue to implemnt SSO
    @Override
    public boolean hasPermissionOnUser(UUID ssoId) {
        try {
            if (cognitoUserService.getUserInfo(ssoId).getRole().equalsIgnoreCase(CognitoGroups.CHPL_STARTUP)) {
                return false;
            } else if (isUserRoleAdmin() || AuthUtil.getCurrentUser().getSsoId().equals(ssoId)) {
                return true;
            }
            return false;
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not retrieve SSO user.", e);
            return false;
        }
    }

    @Override
    public boolean isUserRoleAdmin() {
        if (ff4j.check(FeatureList.SSO)) {
            return doesUserHaveRole(CognitoGroups.CHPL_ADMIN);
        } else {
            return doesUserHaveRole(Authority.ROLE_ADMIN);
        }
    }

    @Override
    public boolean isUserRoleOnc() {
        return doesUserHaveRole(Authority.ROLE_ONC);
    }

    @Override
    public boolean isUserRoleCmsStaff() {
        return doesUserHaveRole(Authority.ROLE_CMS_STAFF);
    }

    @Override
    public boolean isUserRoleAcbAdmin() {
        return doesUserHaveRole(Authority.ROLE_ACB);
    }

    @Override
    public boolean isUserRoleDeveloperAdmin() {
        return doesUserHaveRole(Authority.ROLE_DEVELOPER);
    }

    @Override
    public boolean isUserRoleUserCreator() {
        return doesUserHaveRole(Authority.ROLE_USER_CREATOR);
    }

    @Override
    public boolean isUserRoleUserAuthenticator() {
        return doesAuthenticationHaveRole(Authority.ROLE_USER_AUTHENTICATOR);
    }

    @Override
    public boolean isUserRoleInvitedUserCreator() {
        return doesAuthenticationHaveRole(Authority.ROLE_INVITED_USER_CREATOR);
    }

    @Override
    public boolean isUserRoleStartup() {
        return doesAuthenticationHaveRole(Authority.ROLE_STARTUP);
    }

    @Override
    public boolean isUserAnonymous() {
        return AuthUtil.getCurrentUser() == null;
    }

    @Override
    public boolean doesUserHaveRole(List<String> authorities) {
        for (String authority : authorities) {
            if (doesUserHaveRole(authority)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean doesUserHaveRole(String authority) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return false;
        }

        if (ff4j.check(FeatureList.SSO)) {
            return user.getPermissions().stream()
                    .filter(permission -> permission.getAuthority().equals(authority))
                    .findAny()
                    .isPresent();
        } else {
            UserPermission role = getRoleByUserId(user.getId());
            if (role == null) {
                return false;
            }
            return role.getAuthority().equalsIgnoreCase(authority);
        }
    }

    @Override
    public boolean doesAuditUserHaveRole(String authority) {
        Long auditUserId = AuthUtil.getAuditId();
        if (auditUserId == null || auditUserId.equals(SystemUsers.DEFAULT_USER_ID)) {
            return false;
        }

        UserPermission role = getRoleByUserId(auditUserId);
        if (role == null) {
            return false;
        }
        return role.getAuthority().equalsIgnoreCase(authority);
    }

    private boolean doesAuthenticationHaveRole(String authority) {
        Authentication auth = AuthUtil.getCurrentAuthentication();
        if (auth == null) {
            return false;
        }

        for (GrantedAuthority role : auth.getAuthorities()) {
            if (role.getAuthority().contentEquals(authority)) {
                return true;
            }
        }
        return false;
    }
}
