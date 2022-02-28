package gov.healthit.chpl.permissions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class ResourcePermissions {
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private UserTestingLabMapDAO userTestingLabMapDAO;
    private UserDeveloperMapDAO userDeveloperMapDAO;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationBodyDAO acbDAO;
    private TestingLabDAO atlDAO;
    private UserDAO userDAO;
    private DeveloperDAO developerDAO;

    @SuppressWarnings({"checkstyle:parameternumber"})
    @Autowired
    public ResourcePermissions(UserCertificationBodyMapDAO userCertificationBodyMapDAO,
            UserDeveloperMapDAO userDeveloperMapDAO, CertificationBodyDAO acbDAO,
            UserTestingLabMapDAO userTestingLabMapDAO, TestingLabDAO atlDAO,
            ErrorMessageUtil errorMessageUtil, UserDAO userDAO, DeveloperDAO developerDAO) {
        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.acbDAO = acbDAO;
        this.userTestingLabMapDAO = userTestingLabMapDAO;
        this.atlDAO = atlDAO;
        this.errorMessageUtil = errorMessageUtil;
        this.userDAO = userDAO;
        this.developerDAO = developerDAO;
        this.userDeveloperMapDAO = userDeveloperMapDAO;
    }

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

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) throws UserRetrievalException {
        return userDAO.getById(userId);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersOnAcb(CertificationBodyDTO acb) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByAcbId(acb.getId());

        for (UserCertificationBodyMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersOnAtl(TestingLabDTO atl) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByAtlId(atl.getId());

        for (UserTestingLabMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersOnDeveloper(Developer dev) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserDeveloperMapDTO> dtos = userDeveloperMapDAO.getByDeveloperId(dev.getId());

        for (UserDeveloperMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAllAcbsForCurrentUser() {
        User user = AuthUtil.getCurrentUser();
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc() || isUserRoleOncStaff()) {
                acbs = acbDAO.findAll();
            } else {
                List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(user.getId());
                for (UserCertificationBodyMapDTO dto : dtos) {
                    acbs.add(dto.getCertificationBody());
                }
            }
        }
        return acbs;
    }

    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAllAcbsForUser(Long userID) {
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(userID);
        for (UserCertificationBodyMapDTO dto : dtos) {
            acbs.add(dto.getCertificationBody());
        }
        return acbs;
    }

    @Transactional(readOnly = true)
    public List<TestingLabDTO> getAllAtlsForCurrentUser() {
        User user = AuthUtil.getCurrentUser();
        List<TestingLabDTO> atls = new ArrayList<TestingLabDTO>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                atls = atlDAO.findAll();
            } else {
                List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByUserId(user.getId());
                for (UserTestingLabMapDTO dto : dtos) {
                    atls.add(dto.getTestingLab());
                }
            }
        }
        return atls;
    }

    @Transactional(readOnly = true)
    public List<TestingLabDTO> getAllAtlsForUser(Long userId) {
        List<TestingLabDTO> atls = new ArrayList<TestingLabDTO>();
        List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByUserId(userId);
        for (UserTestingLabMapDTO dto : dtos) {
            atls.add(dto.getTestingLab());
        }
        return atls;
    }

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

    @Transactional(readOnly = true)
    public List<Developer> getAllDevelopersForUser(Long userId) {
        List<Developer> devs = new ArrayList<Developer>();
        List<UserDeveloperMapDTO> dtos = userDeveloperMapDAO.getByUserId(userId);
        for (UserDeveloperMapDTO dto : dtos) {
            devs.add(dto.getDeveloper());
        }
        return devs;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersForCurrentUser() {
        User user = AuthUtil.getCurrentUser();
        List<UserDTO> users = new ArrayList<UserDTO>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                users = userDAO.findAll();
            } else if (isUserRoleAcbAdmin()) {
                List<CertificationBodyDTO> acbs = getAllAcbsForCurrentUser();
                for (CertificationBodyDTO acb : acbs) {
                    users.addAll(getAllUsersOnAcb(acb));
                }
            } else if (isUserRoleAtlAdmin()) {
                List<TestingLabDTO> atls = getAllAtlsForCurrentUser();
                for (TestingLabDTO atl : atls) {
                    users.addAll(getAllUsersOnAtl(atl));
                }
            }  else if (isUserRoleDeveloperAdmin()) {
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

    @Transactional(readOnly = true)
    public CertificationBodyDTO getAcbIfPermissionById(Long id) throws EntityRetrievalException {
        try {
            acbDAO.getById(id);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("acb.notFound"));
        }

        List<CertificationBodyDTO> dtos = getAllAcbsForCurrentUser();
        CollectionUtils.filter(dtos, new Predicate<CertificationBodyDTO>() {
            @Override
            public boolean evaluate(final CertificationBodyDTO object) {
                return object.getId().equals(id);
            }

        });

        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

    @Transactional(readOnly = true)
    public TestingLabDTO getAtlIfPermissionById(Long id) throws EntityRetrievalException {
        try {
            atlDAO.getById(id);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("atl.notFound"));
        }

        List<TestingLabDTO> dtos = getAllAtlsForCurrentUser();
        CollectionUtils.filter(dtos, new Predicate<TestingLabDTO>() {
            @Override
            public boolean evaluate(final TestingLabDTO object) {
                return object.getId().equals(id);
            }

        });

        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

    @Transactional(readOnly = true)
    public Developer getDeveloperIfPermissionById(Long id) throws EntityRetrievalException {
        try {
            developerDAO.getById(id);
        } catch (final EntityRetrievalException ex) {
            throw new EntityRetrievalException(errorMessageUtil.getMessage("developer.notFound"));
        }

        List<Developer> dtos = getAllDevelopersForCurrentUser();
        CollectionUtils.filter(dtos, new Predicate<Developer>() {
            @Override
            public boolean evaluate(final Developer object) {
                return object.getId().equals(id);
            }

        });

        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

    @Transactional(readOnly = true)
    public UserPermission getRoleByUserId(Long userId) {
        try {
            UserDTO user = userDAO.getById(userId);
            return user.getPermission();
        } catch (UserRetrievalException ex) {
        }
        return null;
    }

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

    @Transactional(readOnly = true)
    public boolean hasPermissionOnUser(UserDTO user) {

        if (isUserRoleAdmin() || AuthUtil.getCurrentUser().getId().equals(user.getId())) {
            return true;
        } else if (isUserRoleOnc()) {
            return !getRoleByUserId(user.getId()).getAuthority().equalsIgnoreCase(Authority.ROLE_ADMIN);
        } else if (isUserRoleOncStaff()) {
            return getRoleByUserId(user.getId()).getAuthority().equalsIgnoreCase(Authority.ROLE_ONC_STAFF);
        } else if (isUserRoleAcbAdmin()) {
            if (getRoleByUserId(user.getId()).getAuthority().equalsIgnoreCase(Authority.ROLE_DEVELOPER)) {
                return true;
            }
            // is the user being checked on any of the same ACB(s) that the current user is on?
            List<CertificationBodyDTO> currUserAcbs = getAllAcbsForCurrentUser();
            List<CertificationBodyDTO> otherUserAcbs = getAllAcbsForUser(user.getId());
            for (CertificationBodyDTO currUserAcb : currUserAcbs) {
                for (CertificationBodyDTO otherUserAcb : otherUserAcbs) {
                    if (currUserAcb.getId().equals(otherUserAcb.getId())) {
                        return true;
                    }
                }
            }
        } else if (isUserRoleAtlAdmin()) {
            // is the user being checked on any of the same ATL(s) that the current user is on?
            List<TestingLabDTO> currUserAtls = getAllAtlsForCurrentUser();
            List<TestingLabDTO> otherUserAtls = getAllAtlsForUser(user.getId());
            for (TestingLabDTO currUserAtl : currUserAtls) {
                for (TestingLabDTO otherUserAtl : otherUserAtls) {
                    if (currUserAtl.getId().equals(otherUserAtl.getId())) {
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

    public boolean isUserRoleAdmin() {
        return doesUserHaveRole(Authority.ROLE_ADMIN);
    }

    public boolean isUserRoleOnc() {
        return doesUserHaveRole(Authority.ROLE_ONC);
    }

    public boolean isUserRoleOncStaff() {
        return doesUserHaveRole(Authority.ROLE_ONC_STAFF);
    }

    public boolean isUserRoleCmsStaff() {
        return doesUserHaveRole(Authority.ROLE_CMS_STAFF);
    }

    public boolean isUserRoleAcbAdmin() {
        return doesUserHaveRole(Authority.ROLE_ACB);
    }

    public boolean isUserRoleAtlAdmin() {
        return doesUserHaveRole(Authority.ROLE_ATL);
    }

    public boolean isUserRoleDeveloperAdmin() {
        return doesUserHaveRole(Authority.ROLE_DEVELOPER);
    }

    public boolean isUserRoleUserCreator() {
        return doesUserHaveRole(Authority.ROLE_USER_CREATOR);
    }

    public boolean isUserRoleUserAuthenticator() {
        return doesAuthenticationHaveRole(Authority.ROLE_USER_AUTHENTICATOR);
    }

    public boolean isUserRoleInvitedUserCreator() {
        return doesAuthenticationHaveRole(Authority.ROLE_INVITED_USER_CREATOR);
    }

    public boolean isUserAnonymous() {
        return AuthUtil.getCurrentUser() == null;
    }

    public boolean doesUserHaveRole(List<String> authorities) {
        for (String authority : authorities) {
            if (doesUserHaveRole(authority)) {
                return true;
            }
        }
        return false;
    }

    public boolean doesUserHaveRole(String authority) {
        User user = AuthUtil.getCurrentUser();
        if (user == null) {
            return false;
        }

        UserPermission role = getRoleByUserId(user.getId());
        if (role == null) {
            return false;
        }
        return role.getAuthority().equalsIgnoreCase(authority);
    }

    public boolean doesAuditUserHaveRole(String authority) {
        Long auditUserId = AuthUtil.getAuditId();
        if (auditUserId == null || auditUserId.equals(User.DEFAULT_USER_ID)) {
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
