package gov.healthit.chpl.auth.permission.dao;

import java.util.List;

import gov.healthit.chpl.auth.permission.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.UserPermissionUserMappingEntity;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;


public interface UserPermissionDAO {
	
	public void create(UserPermissionEntity permission);
	
	public void update(UserPermissionEntity permission);
	
	public void delete(String authority);
	
	public void delete(Long permissionId);
	
	public UserPermissionDTO getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException;
	
	public Long getIdFromAuthority(String authority) throws UserPermissionRetrievalException;
	
	public List<UserPermissionDTO> findAll();
	
	public void createMapping(UserPermissionUserMappingEntity mapping);
	
	public void createMapping(UserEntity user, UserPermissionEntity permission);
	
	public void createMapping(UserEntity user, String authority) throws UserPermissionRetrievalException;
	
	public void deleteMapping(UserPermissionUserMappingEntity mapping);
	
	public void deleteMapping(UserEntity user, UserPermissionEntity permission) throws UserPermissionRetrievalException;
	
	public void deleteMapping(String userName, String Authority) throws UserRetrievalException, UserPermissionRetrievalException;
	
	public void deleteMappingsForUser(String userName) throws UserRetrievalException;
	
	public void deleteMappingsForUser(Long userId);
	
	public void deleteMappingsForPermission(UserPermissionDTO userPermission) throws UserPermissionRetrievalException;
	
}