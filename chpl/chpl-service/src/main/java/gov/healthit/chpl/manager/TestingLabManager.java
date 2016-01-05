package gov.healthit.chpl.manager;


import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.TestingLabDTO;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import com.fasterxml.jackson.core.JsonProcessingException;


public interface TestingLabManager {
	
	public void addPermission(TestingLabDTO atl, Long userId, Permission permission) throws UserRetrievalException;
	
	public void deletePermission(TestingLabDTO atl, Sid recipient, Permission permission);
	public void deleteAllPermissionsOnAtl(TestingLabDTO atl, Sid recipient);
	public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException;
	public TestingLabDTO create(TestingLabDTO atl) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public TestingLabDTO update(TestingLabDTO atl) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public void delete(TestingLabDTO atl) throws JsonProcessingException, EntityCreationException, EntityRetrievalException;
	public List<TestingLabDTO> getAllForUser();
	public List<TestingLabDTO> getAll();
	public TestingLabDTO getById(Long id) throws EntityRetrievalException;
	public List<UserDTO> getAllUsersOnAtl(TestingLabDTO atl);
	public List<Permission> getPermissionsForUser(TestingLabDTO atl, Sid recipient);
}
