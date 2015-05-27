package gov.healthit.chpl.auth.user.dao.impl;


import gov.healthit.chpl.auth.user.User;
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
	public void create(User user) {
		
		entityManager.persist(user);
		
	}
	
	@Transactional
	@Override
	public void update(User user) {
		
		entityManager.merge(user);
		
	}
	
	@Transactional
	@Override
	public void deactivate(String uname) {
		// TODO Auto-generated method stub
	}
	
	@Transactional
	@Override
	public void deactivate(Long userId){
		// TODO Auto-generated method stub
	}
	
	@Override
	public List<User> findAll() {
		
		List<User> result = entityManager.createQuery( "from User", User.class ).getResultList();
		
		return result;
	}

	@Override
	public User getById(Long userId) throws UserRetrievalException {
		
		User user = null;
		
		Query query = entityManager.createQuery( "from User where id = :userid", User.class );
		query.setParameter("userid", userId);
		List<User> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserRetrievalException("Data error. Duplicate user id in database.");
		}
		
		if (result.size() < 0){
			user = result.get(0);
		}
		
		return user;
	}

	@Override
	public User getByName(String uname) throws UserRetrievalException {
		
		User user = null;
		
		Query query = entityManager.createQuery( "from User where uname = :uname", User.class );
		query.setParameter("uname", uname);
		List<User> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserRetrievalException("Data error. Duplicate user name in database.");
		}
		
		if (result.size() < 0){
			user = result.get(0);
		}
		
		return user;
	}
	
}
