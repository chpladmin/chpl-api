package gov.healthit.chpl.permissions;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserRoleMapDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
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

        if (Util.isUserRoleAdmin() || Util.isUserRoleOnc()) {
            acbs = acbDAO.findAllActive();
        } else {
            List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(user.getId());
            for (UserCertificationBodyMapDTO dto : dtos) {
                acbs.add(dto.getCertificationBody());
            }
        }
        return acbs;
    }

    @Transactional(readOnly = true)
    public CertificationBodyDTO getIfPermissionById(final Long id) {
        List<CertificationBodyDTO> dtos = getAllAcbsForCurrentUser();
        dtos.stream().filter(dto -> dto.getId().equals(id));
        if (dtos.size() == 0) {
            throw new AccessDeniedException(errorMessageUtil.getMessage("access.denied"));
        }
        return dtos.get(0);
    }

}
