package gov.healthit.chpl.auth.permission.dao.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.permission.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.UserPermissionUserMappingEntity;
import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.user.UserDTO;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.auth.user.dao.UserDAO;

@Repository(value="userPermissionDAO")
public class UserPermissionDAOImpl extends BaseDAOImpl implements UserPermissionDAO {
	
	
	@Autowired
	UserDAO userDAO;
	
	public void create(UserPermissionDTO permission){
		
		UserPermissionEntity permissionEntity = new UserPermissionEntity();
		permissionEntity.setAuthority(permission.getAuthority());
		permissionEntity.setName(permission.getName());
		permissionEntity.setDescription(permission.getDescription());
		create(permissionEntity);
		
	}
	
	@Override
	public void update(UserPermissionDTO permission) throws UserPermissionRetrievalException{
		
		UserPermissionEntity permissionEntity = null;
		
		Query query = entityManager.createQuery( "from UserPermissionEntity where (NOT deleted = true) AND (authority = :authority) ", UserPermissionEntity.class );
		query.setParameter("authority", permission.getAuthority());
		List<UserPermissionEntity> result = query.getResultList();
			
		if (result.size() > 1){
			throw new UserPermissionRetrievalException("Data error. Duplicate authority in database.");
		}
		
		if (result.size() > 0){
			permissionEntity = result.get(0);
		} else {
			throw new UserPermissionRetrievalException("Permission does not exist.");
		}
		
		permissionEntity.setAuthority(permission.getAuthority());
		permissionEntity.setName(permission.getName());
		permissionEntity.setDescription(permission.getDescription());
		
		update(permissionEntity);
	}
	
	@Override
	public void delete(String authority) {

		Query query = entityManager.createQuery("UPDATE UserPermissionEntity SET deleted = true WHERE authority = :authority");
		query.setParameter("authority", authority);
		query.executeUpdate();
	}

	@Override
	public void delete(Long permissionId) {
		
		Query query = entityManager.createQuery("UPDATE UserPermissionEntity SET deleted = true WHERE user_permission_id = :user_permission_id");
		query.setParameter("user_permission_id", permissionId);
		query.executeUpdate();
	}

	@Override
	public List<UserPermissionDTO> findAll() {
		
		List<UserPermissionEntity> results = entityManager.createQuery( "from UserPermissionEntity where (NOT deleted = true) ", UserPermissionEntity.class ).getResultList();
		List<UserPermissionDTO> permissions = new ArrayList<UserPermissionDTO>();
		
		for (UserPermissionEntity entity : results){
			
			UserPermissionDTO permission = new UserPermissionDTO(entity);
			permission.setDescription(entity.getDescription());
			permission.setName(entity.getName());
			permissions.add(permission);
		}
		
		return permissions;
	}
	
	@Override
	public UserPermissionDTO getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException {			
			
		UserPermissionEntity permissionEntity = null;
		UserPermissionDTO permission = new UserPermissionDTO();
		
		Query query = entityManager.createQuery( "from UserPermissionEntity where (NOT deleted = true) AND (authority = :authority) ", UserPermissionEntity.class );
		query.setParameter("authority", authority);
		List<UserPermissionEntity> result = query.getResultList();
			
		if (result.size() > 1){
			throw new UserPermissionRetrievalException("Data error. Duplicate authority in database.");
		}
		
		if (result.size() > 0){
			permissionEntity = result.get(0);
		} else {
			throw new UserPermissionRetrievalException("Permission does not exist.");
		}
		
		permission.setAuthority(permissionEntity.getAuthority());
		permission.setName(permissionEntity.getName());
		permission.setDescription(permissionEntity.getDescription());
		
		return permission;
	}
	
	@Override
	public Long getIdFromAuthority(String authority) throws UserPermissionRetrievalException {
		
		UserPermissionEntity permissionEntity = null;
		
		Query query = entityManager.createQuery( "from UserPermissionEntity where (NOT deleted = true) AND (authority = :authority) ", UserPermissionEntity.class );
		query.setParameter("authority", authority);
		List<UserPermissionEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserPermissionRetrievalException("Data error. Duplicate authority in database.");
		}
		
		if (result.size() > 0){
			permissionEntity = result.get(0);
		} else {
			throw new UserPermissionRetrievalException("Permission does not exist.");
		}
		
		return permissionEntity.getId();
	}
	

	@Override
	public void deleteMapping(String userName, String authority) throws UserRetrievalException, UserPermissionRetrievalException {
		
		UserDTO user = this.userDAO.getByName(userName);
		Long permissionId = getIdFromAuthority(authority);
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE user_id = :userid AND user_permission_id_user_permission = :permissionid");
		query.setParameter("userid", user.getId());
		query.setParameter("permissionid", permissionId);
		query.executeUpdate();
		
	}
	
	@Override
	public void deleteMappingsForUser(String userName) throws UserRetrievalException{
		
		UserDTO user = this.userDAO.getByName(userName);
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE c.user_id = :userid");
		query.setParameter("userid", user.getId());
		query.executeUpdate();
		
	}
	
	@Override
	public void deleteMappingsForUser(Long userId){
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE user_id = :userid");
		query.setParameter("userid", userId);
		query.executeUpdate();
		
	}

	@Override
	public void deleteMappingsForPermission(UserPermissionDTO userPermission) throws UserPermissionRetrievalException {
		
		UserPermissionEntity permissionEntity = this.getPermissionEntityFromAuthority(userPermission.getAuthority());
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE user_permission_id_user_permission = :permissionid");
		query.setParameter("permissionid", permissionEntity.getId());
		query.executeUpdate();
	}
	
	@Override
	public void createMapping(UserEntity user, String authority) throws UserPermissionRetrievalException {
		
		UserPermissionEntity permissionEntity = getPermissionEntityFromAuthority(authority);
		createMapping(user, permissionEntity);
		
	}
	
	
	@Override
	public Set<UserPermissionDTO> findPermissionsForUser(Long userId) {
		
		Query query = entityManager.createQuery("FROM UserPermissionUserMappingEntity WHERE user_id = :userid");
		query.setParameter("userid", userId);
		List<UserPermissionUserMappingEntity> results = query.getResultList();
		Set<UserPermissionDTO> userPermissions = new HashSet<UserPermissionDTO>();
		
		for (UserPermissionUserMappingEntity userMapping : results){
			
			UserPermissionEntity permission = userMapping.getPermission();
			userPermissions.add(new UserPermissionDTO(permission));
			
		}
		return userPermissions;
	}
	
		
	
	private void createMapping(UserPermissionUserMappingEntity mapping) throws UserPermissionRetrievalException {
		
		if (mappingExists(mapping.getUser().getId(), mapping.getPermission().getId())){
			throw new UserPermissionRetrievalException("User - Permission mapping already exists.");
		} else {
			entityManager.persist(mapping);
		}
	}
	
	private void createMapping(UserEntity user, UserPermissionEntity permission) throws UserPermissionRetrievalException {
		
		UserPermissionUserMappingEntity permissionMapping = new UserPermissionUserMappingEntity();
		permissionMapping.setUser(user);
		permissionMapping.setPermission(permission);
		createMapping(permissionMapping);
	}
	
	private void update(UserPermissionEntity permission) {
		entityManager.merge(permission);
	}
	
	private void create(UserPermissionEntity permission) {
		entityManager.persist(permission);
	}
	
	
	private boolean mappingExists(Long userId, Long permissionId){
		
		Query query = entityManager.createQuery("SELECT COUNT(global_user_permission_id) FROM UserPermissionUserMappingEntity WHERE user_id = :userid AND user_permission_id_user_permission = :permissionid");
		query.setParameter("userid", userId);
		query.setParameter("permissionid", permissionId);
		Long count = (Long) query.getSingleResult();
		return !(count < 1);
	}
	
	private UserPermissionEntity getPermissionEntityFromAuthority(String authority) throws UserPermissionRetrievalException {
		
		UserPermissionEntity permissionEntity = null;
		
		Query query = entityManager.createQuery( "from UserPermissionEntity where (NOT deleted = true) AND (authority = :authority) ", UserPermissionEntity.class );
		query.setParameter("authority", authority);
		List<UserPermissionEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserPermissionRetrievalException("Data error. Duplicate authority in database.");
		}
		
		if (result.size() > 0){
			permissionEntity = result.get(0);
		} else {
			throw new UserPermissionRetrievalException("Permission does not exist.");
		}
		
		return permissionEntity;
	}
	
}
