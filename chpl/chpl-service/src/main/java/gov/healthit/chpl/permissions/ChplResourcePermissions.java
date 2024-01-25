package gov.healthit.chpl.permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.AuthenticatedUser;
import gov.healthit.chpl.auth.user.SystemUsers;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
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
    private UserDAO userDAO;
    private DeveloperDAO developerDAO;

    @SuppressWarnings({"checkstyle:parameternumber"})
    @Autowired
    public ChplResourcePermissions(UserCertificationBodyMapDAO userCertificationBodyMapDAO, UserDeveloperMapDAO userDeveloperMapDAO,
            CertificationBodyDAO acbDAO, ErrorMessageUtil errorMessageUtil, UserDAO userDAO, DeveloperDAO developerDAO) {

        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.acbDAO = acbDAO;
        this.errorMessageUtil = errorMessageUtil;
        this.userDAO = userDAO;
        this.developerDAO = developerDAO;
        this.userDeveloperMapDAO = userDeveloperMapDAO;
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
        AuthenticatedUser user = AuthUtil.getCurrentUser();
        List<CertificationBody> acbs = new ArrayList<CertificationBody>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                acbs = acbDAO.findAll();
            } else {
                List<UserCertificationBodyMapDTO> userAcbMaps = userCertificationBodyMapDAO.getByUserId(user.getId());
                acbs = userAcbMaps.stream()
                        .map(userAcbMap -> userAcbMap.getCertificationBody())
                        .collect(Collectors.toList());
            }
        }
        return acbs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificationBody> getAllAcbsForUser(User user) {
        List<CertificationBody> acbs = new ArrayList<CertificationBody>();
        List<UserCertificationBodyMapDTO> userAcbMaps = userCertificationBodyMapDAO.getByUserId(user.getUserId());
        return userAcbMaps.stream()
            .map(userAcbMap -> userAcbMap.getCertificationBody())
            .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<Developer> getAllDevelopersForCurrentUser() {
        AuthenticatedUser user = AuthUtil.getCurrentUser();
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
    public List<Developer> getAllDevelopersForUser(User user) {
        List<Developer> devs = new ArrayList<Developer>();
        List<UserDeveloperMapDTO> dtos = userDeveloperMapDAO.getByUserId(user.getUserId());
        for (UserDeveloperMapDTO dto : dtos) {
            devs.add(dto.getDeveloper());
        }
        return devs;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersForCurrentUser() {
        AuthenticatedUser user = AuthUtil.getCurrentUser();
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
    public CertificationBody getAcbIfPermissionById(Long certificationBodyId) throws EntityRetrievalException {
        try {
            acbDAO.getById(certificationBodyId);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("acb.notFound"));
        }

        List<CertificationBody> dtos = getAllAcbsForCurrentUser();
        CollectionUtils.filter(dtos, new Predicate<CertificationBody>() {
            @Override
            public boolean evaluate(final CertificationBody object) {
                return object.getId().equals(certificationBodyId);
            }

        });

        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public Developer getDeveloperIfPermissionById(Long developerId) throws EntityRetrievalException {
        try {
            developerDAO.getById(developerId);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("developer.notFound"));
        }

        List<Developer> developers = getAllDevelopersForCurrentUser();
        List<Developer> developersWithId = developers.stream()
            .filter(developer -> developer.getId().equals(developerId))
            .toList();

        if (developersWithId.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return developersWithId.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPermission getRoleByUser(User user) {
        try {
            UserDTO userDto = userDAO.getById(user.getUserId());
            return userDto.getPermission();
        } catch (UserRetrievalException ex) {
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermissionOnUser(User user) {
        if (getRoleByUser(user).getAuthority().equalsIgnoreCase(Authority.ROLE_STARTUP)) {
            return false;
        } else if (isUserRoleAdmin() || AuthUtil.getCurrentUser().getId().equals(user.getUserId())) {
            return true;
        } else if (isUserRoleOnc()) {
            return !getRoleByUser(user).getAuthority().equalsIgnoreCase(Authority.ROLE_ADMIN);
        } else if (isUserRoleAcbAdmin()) {
            if (getRoleByUser(user).getAuthority().equalsIgnoreCase(Authority.ROLE_DEVELOPER)) {
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

    @Override
    public boolean isUserRoleAdmin() {
        return doesUserHaveRole(Authority.ROLE_ADMIN);
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
        AuthenticatedUser user = AuthUtil.getCurrentUser();
        if (user == null) {
            return false;
        }

        UserPermission role = getRoleByUser(getUserById(user.getId()));
        if (role == null) {
            return false;
        }
        return role.getAuthority().equalsIgnoreCase(authority);
    }

    @Override
    public boolean doesAuditUserHaveRole(String authority) {
        Long auditUserId = AuthUtil.getAuditId();
        if (auditUserId == null || auditUserId.equals(SystemUsers.DEFAULT_USER_ID)) {
            return false;
        }

        UserPermission role = getRoleByUser(getUserById(auditUserId));
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

    private User getUserById(Long userId) {
        try {
            return userDAO.getById(userId).toDomain();
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not retrieve user with ID: {}", userId, e);
            return null;
        }
    }
}
