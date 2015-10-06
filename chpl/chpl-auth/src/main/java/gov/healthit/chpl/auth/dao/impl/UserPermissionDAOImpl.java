package gov.healthit.chpl.auth.dao.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.entity.UserPermissionEntity;
import gov.healthit.chpl.auth.entity.UserPermissionUserMappingEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@Repository(value="userPermissionDAO")
public class UserPermissionDAOImpl extends BaseDAOImpl implements UserPermissionDAO {
	
	
	@Autowired
	UserDAO userDAO;
	
	@Override
	@Transactional
	public void create(UserPermissionDTO permission){
		
		UserPermissionEntity permissionEntity = new UserPermissionEntity();
		permissionEntity.setAuthority(permission.getAuthority());
		permissionEntity.setName(permission.getName());
		permissionEntity.setDescription(permission.getDescription());
		create(permissionEntity);
		
	}
	
	@Override
	@Transactional
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
	@Transactional
	public void delete(String authority) {

		Query query = entityManager.createQuery("UPDATE UserPermissionEntity SET deleted = true WHERE authority = :authority");
		query.setParameter("authority", authority);
		query.executeUpdate();
	}

	@Override
	@Transactional
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
	public UserPermissionDTO findById(Long id) {
		UserPermissionEntity result = this.getById(id);
		if(result != null) {
			return new UserPermissionDTO(result);
		}
		return null;
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
	@Transactional
	public void deleteMapping(String userName, String authority) throws UserRetrievalException, UserPermissionRetrievalException {
		
		UserDTO user = this.userDAO.getByName(userName);
		Long permissionId = getIdFromAuthority(authority);
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE user_id = :userid AND user_permission_id_user_permission = :permissionid");
		query.setParameter("userid", user.getId());
		query.setParameter("permissionid", permissionId);
		query.executeUpdate();
		
	}
	
	@Override
	@Transactional
	public void deleteMappingsForUser(String userName) throws UserRetrievalException{
		
		UserDTO user = this.userDAO.getByName(userName);
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE c.user_id = :userid");
		query.setParameter("userid", user.getId());
		query.executeUpdate();
		
	}
	
	@Override
	@Transactional
	public void deleteMappingsForUser(Long userId){
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE user_id = :userid");
		query.setParameter("userid", userId);
		query.executeUpdate();
		
	}

	@Override
	@Transactional
	public void deleteMappingsForPermission(UserPermissionDTO userPermission) throws UserPermissionRetrievalException {
		
		UserPermissionEntity permissionEntity = this.getPermissionEntityFromAuthority(userPermission.getAuthority());
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE user_permission_id_user_permission = :permissionid");
		query.setParameter("permissionid", permissionEntity.getId());
		query.executeUpdate();
	}
	
	@Override
	@Transactional
	public void createMapping(UserEntity user, String authority) throws UserPermissionRetrievalException {
		UserPermissionEntity permissionEntity = getPermissionEntityFromAuthority(authority);
		createMapping(user, permissionEntity);
	}
	
	
	@Override
	@Transactional
	public Set<UserPermissionDTO> findPermissionsForUser(Long userId) {
		
		Query query = entityManager.createQuery("FROM UserPermissionUserMappingEntity pme JOIN FETCH pme.permission WHERE pme.deleted <> true AND user_id = :userid");
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
		UserPermissionUserMappingEntity existingMap = getMapping(mapping.getUser().getId(), mapping.getPermission().getId());
		if (existingMap != null){
			//the mapping exists, make sure it's not deleted
			if(existingMap.isDeleted()) {
				existingMap.setDeleted(false);
				entityManager.merge(existingMap);
			}
		} else {
			entityManager.persist(mapping);
		}
	}
	
	private void createMapping(UserEntity user, UserPermissionEntity permission) throws UserPermissionRetrievalException {
		UserPermissionUserMappingEntity permissionMapping = new UserPermissionUserMappingEntity();
		permissionMapping.setUser(user);
		permissionMapping.setPermission(permission);
		permissionMapping.setDeleted(false);
		permissionMapping.setLastModifiedUser(Util.getCurrentUser().getId());
		createMapping(permissionMapping);
	}
	
	private void update(UserPermissionEntity permission) {
		entityManager.merge(permission);
	}
	
	private void create(UserPermissionEntity permission) {
		entityManager.persist(permission);
	}
	
	
	private UserPermissionEntity getById(Long permissionId){
		
		Query query = entityManager.createQuery("SELECT e FROM UserPermissionEntity e "
				+ "WHERE user_permission_id = :permissionId", UserPermissionEntity.class);
		query.setParameter("permissionId", permissionId);
		
		List<UserPermissionEntity> results = query.getResultList();
		if(results == null || results.size() == 0) {
			return null;
		}
		return results.get(0);
	}
	
	private UserPermissionUserMappingEntity getMapping(Long userId, Long permissionId){
		
		Query query = entityManager.createQuery("SELECT e FROM UserPermissionUserMappingEntity e "
				+ "WHERE user_id = :userid AND user_permission_id_user_permission = :permissionid", UserPermissionUserMappingEntity.class);
		query.setParameter("userid", userId);
		query.setParameter("permissionid", permissionId);
		
		List<UserPermissionUserMappingEntity> results = query.getResultList();
		if(results == null || results.size() == 0) {
			return null;
		}
		return results.get(0);
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
