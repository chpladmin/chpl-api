package gov.healthit.chpl.auth.permission.dao.impl;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.permission.UserPermissionUserMapping;
import gov.healthit.chpl.auth.permission.dao.UserPermissionUserMappingDAO;


@Repository(value="userPermissionUserMappingDAO")
public class UserPermissionUserMappingDAOImpl extends BaseDAOImpl implements
		UserPermissionUserMappingDAO {
	
	@Override
	public void create(UserPermissionUserMapping permissionMapping) {
		entityManager.persist(permissionMapping);
	}

	@Override
	public void update(UserPermissionUserMapping permissionMapping) {
		entityManager.merge(permissionMapping);
	}

	@Override
	public void deactivate(UserPermissionUserMapping permissionMapping) {
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMapping SET deleted = true WHERE c.user_id = :user_id AND c.user_permission_id_user_permission = :permission_id ");
		query.setParameter("user_id", permissionMapping.getUser().getId());
		query.setParameter("permission_id", permissionMapping.getPermission().getId());
		query.executeUpdate();
		
	}

	@Override
	public void deactivate(Long userId, Long permissionId) {
		
		Query query = entityManager.createQuery("UPDATE UserPermissionUserMapping SET deleted = true WHERE c.user_id = :user_id AND c.user_permission_id_user_permission = :permission_id ");
		query.setParameter("user_id", userId);
		query.setParameter("permission_id", permissionId);
		query.executeUpdate();
		
	}

}
