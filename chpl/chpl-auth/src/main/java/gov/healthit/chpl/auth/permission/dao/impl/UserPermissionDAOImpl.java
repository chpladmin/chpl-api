package gov.healthit.chpl.auth.permission.dao.impl;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.permission.UserPermission;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.user.UserImpl;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@Repository(value="userPermissionDAO")
public class UserPermissionDAOImpl extends BaseDAOImpl implements UserPermissionDAO {
	

	@Override
	public void create(UserPermission permission) {
		entityManager.persist(permission);
	}

	@Override
	public void update(UserPermission permission) {
		entityManager.merge(permission);
	}

	@Override
	public void deactivate(String authority) {

		Query query = entityManager.createQuery("UPDATE UserPermission SET deleted = true WHERE c.authority = :authority");
		query.setParameter("authority", authority);
		query.executeUpdate();
		// TODO: Make sure result is removed from mapping table as well 
	}

	@Override
	public void deactivate(Long permissionId) {
		
		Query query = entityManager.createQuery("UPDATE UserPermission SET deleted = true WHERE c.user_permission_id = :user_permission_id");
		query.setParameter("user_permission_id", permissionId);
		query.executeUpdate();
		// TODO: Make sure result is removed from mapping table as well 
	}

	@Override
	public List<UserPermission> findAll() {
		
		List<UserPermission> result = entityManager.createQuery( "from UserPermission  where (NOT deleted = true) ", UserPermission.class ).getResultList();
		return result;
	}
	
	@Override
	public UserPermission getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException {			
			
		UserPermission permission = null;
			
		Query query = entityManager.createQuery( "from UserPermission where (NOT deleted = true) AND (authority = :authority) ", UserPermission.class );
		query.setParameter("authority", authority);
		List<UserPermission> result = query.getResultList();
			
		if (result.size() > 1){
			throw new UserPermissionRetrievalException("Data error. Duplicate authority in database.");
		}
			
		if (result.size() > 0){
			permission = result.get(0);
		}
		
		return permission;
	}

}
