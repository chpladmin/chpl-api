package gov.healthit.chpl.auth.user.dao.impl;


import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.auth.user.dao.UserDAO;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository(value="userDAO")
public class UserDAOImpl extends BaseDAOImpl implements UserDAO {
	
	@Transactional
	@Override
	public void create(UserEntity user) {
		
		entityManager.persist(user);
		
	}
	
	@Transactional
	@Override
	public void update(UserEntity user) {
		
		entityManager.merge(user);
		
	}
	
	@Transactional
	@Override
	public void deactivate(String uname) {
		Query query = entityManager.createQuery("UPDATE UserEntity SET deleted = true WHERE c.user_id = :uname");
		query.setParameter("uname", uname);
		query.executeUpdate();
	}
	
	@Transactional
	@Override
	public void deactivate(Long userId){
		Query query = entityManager.createQuery("UPDATE UserEntity SET deleted = true WHERE c.user_id = :userid");
		query.setParameter("userid", userId);
		query.executeUpdate();
	}
	
	@Transactional
	@Override
	public List<UserEntity> findAll() {
		
		List<UserEntity> result = entityManager.createQuery( "from UserEntity where (NOT deleted = true) ", UserEntity.class ).getResultList();
		
		return result;
	}

	@Transactional
	@Override
	public UserEntity getById(Long userId) throws UserRetrievalException {
		
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

	@Transactional
	@Override
	public UserEntity getByName(String uname) throws UserRetrievalException {
		
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
	
}
