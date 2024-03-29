package gov.healthit.chpl.util;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.dto.OrganizationDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.entity.auth.UserEntity;

@Component
public class UserMapper {

    private CertificationBodyDAO acbDao;
    private DeveloperDAO developerDao;

    @Autowired
    public UserMapper(CertificationBodyDAO abcDao,
            DeveloperDAO developerDao) {
        this.acbDao = abcDao;
        this.developerDao = developerDao;
    }

    @Transactional(readOnly = true)
    public UserDTO from(UserEntity entity) {
        UserDTO dto = new UserDTO();
        if (entity != null) {
            dto = populateBasicUserInfo(entity);
            if (dto != null && dto.getPermission() != null
                    && dto.getPermission().getAuthority() != null) {
                populateOrganizations(dto);
            }
        }
        return dto;
    }

    private UserDTO populateBasicUserInfo(UserEntity entity) {
        UserDTO dto = new UserDTO();
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
            dto.setPermission(entity.getPermission().toDomain());
        }
        return dto;
    }

    private void populateOrganizations(UserDTO user) {
        if (user.getPermission().getAuthority().equals(Authority.ROLE_ACB)) {
            List<CertificationBody> acbs = getAllAcbsForUser(user.getId());
            for (CertificationBody acb : acbs) {
                user.getOrganizations().add(new OrganizationDTO(acb.getId(), acb.getName()));
            }
        }
        if (user.getPermission().getAuthority().equals(Authority.ROLE_DEVELOPER)) {
            List<Developer> devs = getAllDevelopersForUser(user.getId());
            for (Developer dev : devs) {
                user.getOrganizations().add(new OrganizationDTO(dev.getId(), dev.getName()));
            }
        }
    }

    private List<CertificationBody> getAllAcbsForUser(Long userID) {
        return acbDao.getCertificationBodiesByUserId(userID);
    }

    private List<Developer> getAllDevelopersForUser(Long userId) {
        return developerDao.getDevelopersByUserId(userId);
    }

}
