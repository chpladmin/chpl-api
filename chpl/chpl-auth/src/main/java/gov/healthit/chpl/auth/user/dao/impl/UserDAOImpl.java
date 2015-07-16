package gov.healthit.chpl.auth.user.dao.impl;


import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.permission.UserPermissionEntity;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.UserPermissionUserMappingEntity;
import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.user.DatabaseAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserContactEntity;
import gov.healthit.chpl.auth.user.UserUploadDTO;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.auth.user.dao.UserDAO;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository(value="userDAO")
public class UserDAOImpl extends BaseDAOImpl implements UserDAO {
	
	@Autowired
	UserPermissionDAO userPermissionDAO;
	
	@Override
	public void create(UserEntity user) {
		
		entityManager.persist(user);
		
	}
	
	private void update(UserEntity user) {
		
		entityManager.merge(user);	
	
	}
	
	@Override
	public void update(DatabaseAuthenticatedUser user){
		
		
	}
	
	
	
	
	@Override
	public void delete(String uname) {
		Query query = entityManager.createQuery("UPDATE UserEntity SET deleted = true WHERE c.user_id = :uname");
		query.setParameter("uname", uname);
		query.executeUpdate();
	}
	
	@Override
	public void delete(Long userId){
		Query query = entityManager.createQuery("UPDATE UserEntity SET deleted = true WHERE c.user_id = :userid");
		query.setParameter("userid", userId);
		query.executeUpdate();
	}
	
	@Override
	public List<UserEntity> findAll() {
		
		List<UserEntity> result = entityManager.createQuery( "from UserEntity where (NOT deleted = true) ", UserEntity.class ).getResultList();
		
		return result;
	}

	private UserEntity getEntityById(Long userId) throws UserRetrievalException {
		
		UserEntity user = null;
		
		Query query = entityManager.createQuery( "from UserEntity where (NOT deleted = true) AND (user_id = :userid) ", UserEntity.class );
		query.setParameter("userid", userId);
		List<UserEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserRetrievalException("Data error. Duplicate user id in database.");
		}
		
		if (result.size() < 0){
			user = result.get(0);
		}
		
		return user;
	}

	
	private UserEntity getEntityByName(String uname) throws UserRetrievalException {
		
		UserEntity user = null;
		
		Query query = entityManager.createQuery( "from UserEntity where ((NOT deleted = true) AND (user_name = (:uname))) ", UserEntity.class );
		query.setParameter("uname", uname);
		List<UserEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserRetrievalException("Data error. Duplicate user name in database.");
		}
		
		if (result.size() > 0){
			user = result.get(0);
		}
		
		return user;
	}

	@Override
	public void addPermission(String uname, String authority) throws UserPermissionRetrievalException, UserRetrievalException {
		
		UserEntity userEntity = this.getEntityByName(uname);
		userPermissionDAO.createMapping(userEntity, authority);
		
	}
}
