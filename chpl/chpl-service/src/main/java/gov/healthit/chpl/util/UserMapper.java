package gov.healthit.chpl.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.OrganizationDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserPermissionDTO;
import gov.healthit.chpl.entity.auth.UserEntity;

@Component
public class UserMapper {

    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private UserTestingLabMapDAO userTestingLabMapDAO;
    private UserDeveloperMapDAO userDeveloperMapDAO;

    @Autowired
    public UserMapper(final UserCertificationBodyMapDAO userCertificationBodyMapDAO,
            final UserTestingLabMapDAO userTestingLabMapDAO, final UserDeveloperMapDAO userDeveloperMapDAO) {
        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.userTestingLabMapDAO = userTestingLabMapDAO;
        this.userDeveloperMapDAO = userDeveloperMapDAO;
    }

    @Transactional(readOnly = true)
    public UserDTO from(final UserEntity entity) {
        UserDTO dto = new UserDTO();
        if (entity != null) {
            dto.setId(entity.getId());
            dto.setSubjectName(entity.getSubjectName());
            dto.setFailedLoginCount(entity.getFailedLoginCount());
            dto.setAccountExpired(entity.isAccountExpired());
            dto.setAccountLocked(entity.isAccountLocked());
            dto.setAccountEnabled(entity.isAccountEnabled());
            dto.setCredentialsExpired(entity.isCredentialsExpired());
            dto.setPasswordResetRequired(entity.isPasswordResetRequired());
            dto.setLastLoggedInDate(entity.getLastLoggedInDate());
            if (entity.getContact() != null) {
                dto.setFullName(entity.getContact().getFullName());
                dto.setFriendlyName(entity.getContact().getFriendlyName());
                dto.setEmail(entity.getContact().getEmail());
                dto.setPhoneNumber(entity.getContact().getPhoneNumber());
                dto.setTitle(entity.getContact().getTitle());
                dto.setSignatureDate(entity.getContact().getSignatureDate());
            }
            if (entity.getPermission() != null) {
                dto.setPermission(new UserPermissionDTO(entity.getPermission()));
            }
            populateOrganizations(dto);
        }
        return dto;
    }

    private void populateOrganizations(final UserDTO user) {
        if (user.getPermission().getAuthority().equals(Authority.ROLE_ACB)) {
            List<CertificationBodyDTO> acbs = getAllAcbsForUser(user.getId());
            for (CertificationBodyDTO acb : acbs) {
                user.getOrganizations().add(new OrganizationDTO(acb.getId(), acb.getName()));
            }
        }
        if (user.getPermission().getAuthority().equals(Authority.ROLE_ATL)) {
            List<TestingLabDTO> atls = getAllAtlsForUser(user.getId());
            for (TestingLabDTO atl : atls) {
                user.getOrganizations().add(new OrganizationDTO(atl.getId(), atl.getName()));
            }
        }
        if (user.getPermission().getAuthority().equals(Authority.ROLE_DEVELOPER)) {
            List<DeveloperDTO> devs = getAllDevelopersForUser(user.getId());
            for (DeveloperDTO dev : devs) {
                user.getOrganizations().add(new OrganizationDTO(dev.getId(), dev.getName()));
            }
        }
    }

    private List<CertificationBodyDTO> getAllAcbsForUser(final Long userID) {
        return userCertificationBodyMapDAO.getCertificationBodyByUserId(userID);
    }

    private List<TestingLabDTO> getAllAtlsForUser(final Long userId) {
        return userTestingLabMapDAO.getTestingLabsByUserId(userId);
    }

    private List<DeveloperDTO> getAllDevelopersForUser(final Long userId) {
        return userDeveloperMapDAO.getDevelopersByUserId(userId);
    }

}
