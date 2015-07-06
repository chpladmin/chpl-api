package gov.healthit.chpl.auth.permission.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@Repository(value="userPermissionDAO")
public class UserPermissionDAOImpl extends BaseDAOImpl implements UserPermissionDAO {
	

	@Override
	@Transactional
	public void create(UserPermissionEntity permission) {
		entityManager.persist(permission);
	}

	@Override
	@Transactional
	public void update(UserPermissionEntity permission) {
		entityManager.merge(permission);
	}
	
	@Override
	@Transactional
	public void deactivate(String authority) {

		Query query = entityManager.createQuery("UPDATE UserPermission SET deleted = true WHERE c.authority = :authority");
		query.setParameter("authority", authority);
		query.executeUpdate();
	}

	@Override
	@Transactional
	public void deactivate(Long permissionId) {
		
		Query query = entityManager.createQuery("UPDATE UserPermission SET deleted = true WHERE c.user_permission_id = :user_permission_id");
		query.setParameter("user_permission_id", permissionId);
		query.executeUpdate();
	}

	@Override
	@Transactional
	public List<UserPermissionEntity> findAll() {
		
		List<UserPermissionEntity> result = entityManager.createQuery( "from UserPermission  where (NOT deleted = true) ", UserPermissionEntity.class ).getResultList();
		return result;
	}
	
	@Override
	@Transactional
	public UserPermissionEntity getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException {			
			
		UserPermissionEntity permission = null;
			
		Query query = entityManager.createQuery( "from UserPermission where (NOT deleted = true) AND (authority = :authority) ", UserPermissionEntity.class );
		query.setParameter("authority", authority);
		List<UserPermissionEntity> result = query.getResultList();
			
		if (result.size() > 1){
			throw new UserPermissionRetrievalException("Data error. Duplicate authority in database.");
		}
			
		if (result.size() > 0){
			permission = result.get(0);
		}
		
		return permission;
	}

}
