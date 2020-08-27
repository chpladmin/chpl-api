package gov.healthit.chpl.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.dao.TestingLabDAO;
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

    private CertificationBodyDAO acbDao;
    private TestingLabDAO atlDao;
    private DeveloperDAO developerDao;

    @Autowired
    public UserMapper(CertificationBodyDAO abcDao,
            TestingLabDAO atlDao, DeveloperDAO developerDao) {
        this.acbDao = abcDao;
        this.atlDao = atlDao;
        this.developerDao = developerDao;
    }

    @Transactional(readOnly = true)
    public UserDTO from(UserEntity entity) {
        UserDTO dto = populateBasicUserInfo(entity);
        if (dto != null) {
            populateOrganizations(dto);
        }
        return dto;
    }

    private UserDTO populateBasicUserInfo(UserEntity entity) {
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
        }
        return dto;
    }

    private void populateOrganizations(UserDTO user) {
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

    private List<CertificationBodyDTO> getAllAcbsForUser(Long userID) {
        return acbDao.getCertificationBodiesByUserId(userID);
    }

    private List<TestingLabDTO> getAllAtlsForUser(Long userId) {
        return atlDao.getTestingLabsByUserId(userId);
    }

    private List<DeveloperDTO> getAllDevelopersForUser(Long userId) {
        return developerDao.getDevelopersByUserId(userId);
    }

}
