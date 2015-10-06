package gov.healthit.chpl.auth.dao;

import java.util.List;
import java.util.Set;

import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserRetrievalException;


public interface UserPermissionDAO {
	
	public void create(UserPermissionDTO permission);
	
	public void update(UserPermissionDTO permission) throws UserPermissionRetrievalException;
	
	public void delete(String authority);
	
	public void delete(Long permissionId);
	
	public UserPermissionDTO getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException;
	
	public Long getIdFromAuthority(String authority) throws UserPermissionRetrievalException;
	
	public List<UserPermissionDTO> findAll();
	public UserPermissionDTO findById(Long id);
	
	public void createMapping(UserEntity user, String authority) throws UserPermissionRetrievalException;
	
	public void deleteMapping(String userName, String Authority) throws UserRetrievalException, UserPermissionRetrievalException;
	
	public void deleteMappingsForUser(String userName) throws UserRetrievalException;
	
	public void deleteMappingsForUser(Long userId);
	
	public void deleteMappingsForPermission(UserPermissionDTO userPermission) throws UserPermissionRetrievalException;

	public Set<UserPermissionDTO> findPermissionsForUser(Long userId);
	
}