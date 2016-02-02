package gov.healthit.chpl.auth.user;

import java.util.Date;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;

public abstract class UserConversionHelper {
	
	public static UserDTO createDTO(UserEntity entity){
		
			UserDTO dto = new UserDTO();
			dto.setId(entity.getId());
			dto.setSubjectName(entity.getSubjectName());
			dto.setFirstName(entity.getFirstName());
			dto.setLastName(entity.getLastName());
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
	
	public static UserDTO createDTO(UserInfoJSONObject info){
		
		UserDTO dto = new UserDTO();
		
		dto.setId(null);
		dto.setSubjectName(info.getUser().getSubjectName());
		dto.setFirstName(info.getUser().getFirstName());
		dto.setComplianceSignatureDate(Boolean.TRUE.equals(info.getUser().getComplianceTermsAccepted()) ? new Date() : null);
		dto.setLastName(info.getUser().getLastName());
		dto.setEmail(info.getUser().getEmail());
		dto.setPhoneNumber(info.getUser().getPhoneNumber());
		dto.setTitle(info.getUser().getTitle());
		dto.setAccountLocked(info.getUser().getAccountLocked());
		dto.setAccountEnabled(info.getUser().getAccountEnabled());
		
		return dto;
	}
	
	public static UserDTO createDTO(UserCreationJSONObject info){
		
		UserDTO dto = new UserDTO();
		dto.setId(null);
		dto.setSubjectName(info.getSubjectName());
		dto.setFirstName(info.getFirstName());
		dto.setComplianceSignatureDate(Boolean.TRUE.equals(info.getComplianceTermsAccepted()) ? new Date() : null);
		dto.setLastName(info.getLastName());
		dto.setEmail(info.getEmail());
		dto.setPhoneNumber(info.getPhoneNumber());
		dto.setTitle(info.getTitle());
		dto.setAccountLocked(false);
		dto.setAccountEnabled(true);
		
		return dto;
	}

}
