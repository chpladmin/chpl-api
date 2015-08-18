package gov.healthit.chpl.auth.user;

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
		dto.setSubjectName(info.getSubjectName());
		dto.setFirstName(info.getFirstName());
		dto.setLastName(info.getLastName());
		dto.setEmail(info.getEmail());
		dto.setPhoneNumber(info.getPhoneNumber());
		dto.setTitle(info.getTitle());
		dto.setAccountLocked(info.isAccountLocked());
		dto.setAccountEnabled(info.isAccountEnabled());
		
		return dto;
	}
	
	public static UserDTO createDTO(UserCreationJSONObject info){
		
		UserDTO dto = new UserDTO();
		
		
		dto.setId(null);
		dto.setSubjectName(info.getSubjectName());
		dto.setFirstName(info.getFirstName());
		dto.setLastName(info.getLastName());
		dto.setEmail(info.getEmail());
		dto.setPhoneNumber(info.getPhoneNumber());
		dto.setTitle(info.getTitle());
		dto.setAccountLocked(false);
		dto.setAccountEnabled(true);
		
		return dto;
	}

}
