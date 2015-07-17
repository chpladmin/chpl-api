package gov.healthit.chpl.auth.permission.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.permission.AuthenticatedPermission;
import gov.healthit.chpl.auth.permission.UserPermission;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.UserPermissionUserMappingEntity;
import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.user.UserDTO;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.auth.user.dao.UserDAO;

@Repository(value="userPermissionDAO")
public class UserPermissionDAOImpl extends BaseDAOImpl implements UserPermissionDAO {
	
	
	@Autowired
	UserDAO userDAO;
	

	@Override
	public void create(UserPermissionEntity permission) {
		entityManager.persist(permission);
	}

	@Override
	public void update(UserPermissionEntity permission) {
		entityManager.merge(permission);
	}
	
	@Override
	public void delete(String authority) {

		Query query = entityManager.createQuery("UPDATE UserPermissionEntity SET deleted = true WHERE c.authority = :authority");
		query.setParameter("authority", authority);
		query.executeUpdate();
	}

	@Override
	public void delete(Long permissionId) {
		
		Query query = entityManager.createQuery("UPDATE UserPermissionEntity SET deleted = true WHERE c.user_permission_id = :user_permission_id");
		query.setParameter("user_permission_id", permissionId);
		query.executeUpdate();
	}

	@Override
	public List<UserPermission> findAll() {
		
		List<UserPermissionEntity> results = entityManager.createQuery( "from UserPermissionEntity where (NOT deleted = true) ", UserPermissionEntity.class ).getResultList();
		List<UserPermission> permissions = new ArrayList<UserPermission>();
		
		for (UserPermissionEntity entity : results){
			
			AuthenticatedPermission permission = new AuthenticatedPermission(entity.getAuthority());
			permission.setDescription(entity.getDescription());
			permission.setName(entity.getName());
			permissions.add(permission);
		}
		
		return permissions;
	}
	
	@Override
	public UserPermission getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException {			
			
		UserPermissionEntity permissionEntity = null;
		AuthenticatedPermission permission = new AuthenticatedPermission();
		
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
	
	private UserPermissionEntity getEntityFromAuthority(String authority) throws UserPermissionRetrievalException {
		
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
	
	
	@Override
	public void createMapping(UserPermissionUserMappingEntity mapping) {
		entityManager.persist(mapping);
	}

	@Override
	public void createMapping(UserEntity user, UserPermissionEntity permission) {
		
		UserPermissionUserMappingEntity permissionMapping = new UserPermissionUserMappingEntity();
		permissionMapping.setUser(user);
		permissionMapping.setPermission(permission);
		createMapping(permissionMapping);
	}

	@Override
	public void createMapping(UserEntity user, String authority) throws UserPermissionRetrievalException {
		
		UserPermissionEntity permissionEntity = getEntityFromAuthority(authority);
		createMapping(user, permissionEntity);
		
	}
	

	@Override
	public void deleteMapping(UserPermissionUserMappingEntity mapping) {
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE c.user_id = :userid AND c.user_permission_id_user_permission = :permissionid");
		query.setParameter("userid", mapping.getUser().getId());
		query.setParameter("permissionid", mapping.getPermission().getId());
		query.executeUpdate();
		
	}

	@Override
	public void deleteMapping(UserEntity user, UserPermissionEntity permission) throws UserPermissionRetrievalException {
		
		Long permissionId = getIdFromAuthority(permission.getAuthority());
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE c.user_id = :userid AND c.user_permission_id_user_permission = :permissionid");
		query.setParameter("userid", user.getId());
		query.setParameter("permissionid", permissionId);
		query.executeUpdate();
		
	}

	@Override
	public void deleteMapping(String userName, String authority) throws UserRetrievalException, UserPermissionRetrievalException {
		
		User user = this.userDAO.getByName(userName);
		Long permissionId = getIdFromAuthority(authority);
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE c.user_id = :userid AND c.user_permission_id_user_permission = :permissionid");
		query.setParameter("userid", user.getId());
		query.setParameter("permissionid", permissionId);
		query.executeUpdate();
		
	}
	
	@Override
	public void deleteMappingsForUser(String userName) throws UserRetrievalException{
		
		User user = this.userDAO.getByName(userName);
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE c.user_id = :userid");
		query.setParameter("userid", user.getId());
		query.executeUpdate();
		
	}
	
	@Override
	public void deleteMappingsForUser(Long userId){
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMappingEntity SET deleted = true WHERE c.user_id = :userid");
		query.setParameter("userid", userId);
		query.executeUpdate();
		
	}
	
	@Override
	public void deleteMappingsForPermission(UserPermission userPermission){
		//TODO: Implement this
	}	

	/*
	public void addPermissionMapping(String authority, UserPermissionUserMappingEntity mapping) throws UserPermissionRetrievalException {
		
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
		mapping.setPermission(permissionEntity);
	}
	*/
}
