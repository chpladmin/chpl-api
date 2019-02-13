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

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.domain.Authority;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserRoleMapDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.RoleDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserRoleMapDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class ResourcePermissions {
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private UserRoleMapDAO userRoleMapDAO;
    private ErrorMessageUtil errorMessageUtil;
    private CertificationBodyDAO acbDAO;

    @Autowired
    public ResourcePermissions(final UserCertificationBodyMapDAO userCertificationBodyMapDAO,
            final UserRoleMapDAO userRoleMapDAO, final CertificationBodyDAO acbDAO,
            final ErrorMessageUtil errorMessageUtil) {

        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.userRoleMapDAO = userRoleMapDAO;
        this.acbDAO = acbDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersOnAcb(final CertificationBodyDTO acb) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByAcbId(acb.getId());

        for (UserCertificationBodyMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAllAcbsForCurrentUser() {
        User user = Util.getCurrentUser();
        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();

        if (user != null) {
            if (isUserRoleAdmin() || isUserRoleOnc()) {
                acbs = acbDAO.findAllActive();
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
    public CertificationBodyDTO getAcbIfPermissionById(final Long id) {
        List<CertificationBodyDTO> dtos = getAllAcbsForCurrentUser();

        CollectionUtils.filter(dtos, new Predicate<CertificationBodyDTO>() {
            @Override
            public boolean evaluate(CertificationBodyDTO object) {
                return object.getId().equals(id);
            }

        });

        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

    @Transactional(readOnly = true)
    public List<RoleDTO> getRolesByUserId(final Long userId) {
        List<RoleDTO> roles = new ArrayList<RoleDTO>();
        List<UserRoleMapDTO> userRoleMaps = userRoleMapDAO.getByUserId(userId);
        if (userRoleMaps != null) {
            for (UserRoleMapDTO dto : userRoleMaps) {
                roles.add(dto.getRole());
            }
        }
        return roles;
    }

    public boolean isUserRoleAdmin() {
        return doesUserHaveRole(Authority.ROLE_ADMIN);
    }

    public boolean isUserRoleOnc() {
        return doesUserHaveRole(Authority.ROLE_ONC);
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

    public boolean isUserRoleUserCreator() {
        return doesUserHaveRole(Authority.ROLE_USER_CREATOR);
    }

    public boolean isUserRoleUserAuthenticator() {
        return doesAuthenticationHaveRole(Authority.ROLE_USER_AUTHENTICATOR);
    }

    public boolean isUserRoleInvitedUserCreator() {
        return doesAuthenticationHaveRole(Authority.ROLE_INVITED_USER_CREATOR);
    }

    private boolean doesUserHaveRole(final String authority) {
        User user = Util.getCurrentUser();
        if (user == null) {
            return false;
        }

        List<RoleDTO> roles = getRolesByUserId(user.getId());
        for (RoleDTO role : roles) {
            if (role.getAuthority().equals(authority)) {
                return true;
            }
        }
        return false;
    }

    private boolean doesAuthenticationHaveRole(final String authority) {
        Authentication auth = Util.getCurrentAuthentication();
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
