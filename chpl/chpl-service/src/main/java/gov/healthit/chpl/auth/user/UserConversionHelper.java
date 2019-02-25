package gov.healthit.chpl.auth.user;

import java.util.Date;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;

public abstract class UserConversionHelper {

    public static UserDTO createDTO(final UserEntity entity) {

        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setSubjectName(entity.getSubjectName());
        dto.setFullName(entity.getFullName());
        dto.setFriendlyName(entity.getFriendlyName());
        dto.setFailedLoginCount(entity.getFailedLoginCount());
        dto.setComplianceSignatureDate(entity.getComplianceSignature());
        dto.setEmail(entity.getContact().getEmail());
        dto.setPhoneNumber(entity.getContact().getPhoneNumber());
        dto.setTitle(entity.getContact().getTitle());
        dto.setAccountExpired(!entity.isAccountNonExpired());
        dto.setAccountLocked(!entity.isAccountNonLocked());
        dto.setAccountEnabled(entity.isEnabled());

        return dto;
    }

    public static UserDTO createDTO(final UserInfoJSONObject info) {

        UserDTO dto = new UserDTO();

        dto.setId(null);
        dto.setSubjectName(info.getUser().getSubjectName());
        dto.setFullName(info.getUser().getFullName());
        dto.setComplianceSignatureDate(Boolean.TRUE.equals(
                info.getUser().getComplianceTermsAccepted()) ? new Date() : null);
        dto.setFriendlyName(info.getUser().getFriendlyName());
        dto.setEmail(info.getUser().getEmail());
        dto.setPhoneNumber(info.getUser().getPhoneNumber());
        dto.setTitle(info.getUser().getTitle());
        dto.setAccountLocked(info.getUser().getAccountLocked());
        dto.setAccountEnabled(info.getUser().getAccountEnabled());

        return dto;
    }

    public static UserDTO createDTO(final UserCreationJSONObject info) {

        UserDTO dto = new UserDTO();
        dto.setId(null);
        dto.setSubjectName(info.getSubjectName());
        dto.setFullName(info.getFullName());
        dto.setComplianceSignatureDate(Boolean.TRUE.equals(info.getComplianceTermsAccepted()) ? new Date() : null);
        dto.setFriendlyName(info.getFriendlyName());
        dto.setEmail(info.getEmail());
        dto.setPhoneNumber(info.getPhoneNumber());
        dto.setTitle(info.getTitle());
        dto.setAccountLocked(false);
        dto.setAccountEnabled(true);

        return dto;
    }
}
