package gov.healthit.chpl.auth.user.dao.impl;


import gov.healthit.chpl.auth.user.UserImpl;
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
	public void create(UserImpl user) {
		
		entityManager.persist(user);
		
	}
	
	@Transactional
	@Override
	public void update(UserImpl user) {
		
		entityManager.merge(user);
		
	}
	
	@Transactional
	@Override
	public void deactivate(String uname) {
		Query query = entityManager.createQuery("UPDATE UserImpl SET deleted = true WHERE c.user_id = :uname");
		query.setParameter("uname", uname);
		query.executeUpdate();
	}
	
	@Transactional
	@Override
	public void deactivate(Long userId){
		Query query = entityManager.createQuery("UPDATE UserImpl SET deleted = true WHERE c.user_id = :userid");
		query.setParameter("userid", userId);
		query.executeUpdate();
	}
	
	@Override
	public List<UserImpl> findAll() {
		//TODO: Where not deleted
		//List<UserImpl> result = entityManager.createQuery( "from UserImpl WHERE deleted is not true ", UserImpl.class ).getResultList();
		//List<UserImpl> result = entityManager.createQuery( "from UserImpl WHERE NOT (deleted = true) ", UserImpl.class ).getResultList();
		List<UserImpl> result = entityManager.createQuery( "from UserImpl ", UserImpl.class ).getResultList();
		
		return result;
	}

	@Override
	public UserImpl getById(Long userId) throws UserRetrievalException {
		//TODO: Where not deleted
		UserImpl user = null;
		
		//Query query = entityManager.createQuery( "from user where user_id = :userid and deleted is not true", UserImpl.class );
		Query query = entityManager.createQuery( "from user where user_id = :userid", UserImpl.class );
		query.setParameter("userid", userId);
		List<UserImpl> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserRetrievalException("Data error. Duplicate user id in database.");
		}
		
		if (result.size() < 0){
			user = result.get(0);
		}
		
		return user;
	}

	@Override
	public UserImpl getByName(String uname) throws UserRetrievalException {
		//TODO: Where not deleted
		UserImpl user = null;
		
		//Query query = entityManager.createQuery( "from UserImpl where user_name = :uname and deleted is not true", UserImpl.class );
		//Query query = entityManager.createQuery( "from UserImpl where user_name = :uname AND NOT (deleted = true) ", UserImpl.class );
		Query query = entityManager.createQuery( "from UserImpl where user_name = :uname ", UserImpl.class );
		query.setParameter("uname", uname);
		List<UserImpl> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserRetrievalException("Data error. Duplicate user name in database.");
		}
		
		if (result.size() > 0){
			user = result.get(0);
		}
		
		return user;
	}
	
}
