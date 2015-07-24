package gov.healthit.chpl.auth.user.dao.impl;


import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.user.UserDTO;
import gov.healthit.chpl.auth.user.UserContactEntity;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserEntity;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.auth.user.dao.UserContactDAO;
import gov.healthit.chpl.auth.user.dao.UserDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository(value="userDAO")
public class UserDAOImpl extends BaseDAOImpl implements UserDAO {
	
	@Autowired
	UserPermissionDAO userPermissionDAO;
	
	@Autowired
	UserContactDAO userContactDAO;
	
	
	@Override
	public void create(UserDTO user, String encodedPassword) throws UserCreationException {
		
		UserEntity userEntity = null;
		try {
			userEntity = getEntityByName(user.getSubjectName());
		} catch (UserRetrievalException e) {
			throw new UserCreationException(e);
		}
		
		if (userEntity != null) {
			throw new UserCreationException("user name: "+user.getSubjectName() +" already exists.");
		} else {
			
			UserEntity userToCreate = new UserEntity(user.getSubjectName(), encodedPassword);
			
			UserContactEntity contact = new UserContactEntity();
			contact.setEmail(user.getEmail());
			contact.setFirstName(user.getFirstName());
			contact.setLastName(user.getLastName());
			contact.setPhoneNumber(user.getPhoneNumber());
			contact.setTitle(user.getTitle());
			
			userContactDAO.create(contact);
			userToCreate.setContact(contact);
			create(userToCreate);			
		}
	}
	
	
	@Override
	public void update(UserDTO user) throws UserRetrievalException {
		
		UserEntity userEntity = getEntityByName(user.getSubjectName());
		
		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());
		userEntity.getContact().setEmail(user.getEmail());
		userEntity.getContact().setPhoneNumber(user.getPhoneNumber());
		userEntity.getContact().setTitle(user.getTitle());
		userEntity.setAccountEnabled(user.isAccountEnabled());
		userEntity.setAccountExpired(user.isAccountExpired());
		userEntity.setAccountLocked(user.isAccountLocked());
		userEntity.setCredentialsExpired(!user.isCredentialsNonExpired());
		
		update(userEntity);
	}
	
	
	@Override
	public void delete(String uname) throws UserRetrievalException {
		
		// First delete the user / permission mappings for this user.
		userPermissionDAO.deleteMappingsForUser(uname);
		
		Query query = entityManager.createQuery("UPDATE UserEntity SET deleted = true WHERE user_id = :uname");
		query.setParameter("uname", uname);
		query.executeUpdate();
	}
	
	
	@Override
	public void delete(Long userId){
		
		// First delete the user / permission mappings for this user.
		userPermissionDAO.deleteMappingsForUser(userId);
		
		Query query = entityManager.createQuery("UPDATE UserEntity SET deleted = true WHERE user_id = :userid");
		query.setParameter("userid", userId);
		query.executeUpdate();
	}
	
	public List<UserDTO> findAll(){
		
		List<UserEntity> entities = getAllEntities();
		List<UserDTO> users = new ArrayList<>();
		
		for (UserEntity entity : entities){
			UserDTO user = new UserDTO(entity);
			users.add(user);
		}
		return users;
	}
	
	private void create(UserEntity user) {
		
		entityManager.persist(user);
		
	}
	
	private void update(UserEntity user) {
		
		entityManager.merge(user);	
	
	}
	
	private List<UserEntity> getAllEntities() {
		
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

	@Override
	public UserDTO getById(Long userId) throws UserRetrievalException {
		
		UserEntity userEntity = this.getEntityById(userId);
		UserDTO user = new UserDTO(userEntity);
		return user;
	}

	@Override
	public UserDTO getByName(String uname) throws UserRetrievalException {
		
		UserDTO user = null;
		UserEntity userEntity = this.getEntityByName(uname);
		if (userEntity != null){
			user = new UserDTO(userEntity);
		} else {
			throw new UserRetrievalException("User does not exist");
		}
		
		return user;
	}

	@Override
	public void removePermission(String uname, String authority) throws UserRetrievalException, UserPermissionRetrievalException {
		userPermissionDAO.deleteMapping(uname, authority);
	}

	@Override
	public void updatePassword(String uname, String encodedPassword) throws UserRetrievalException {
		
		UserEntity userEntity = this.getEntityByName(uname);
		userEntity.setPassword(encodedPassword);
		update(userEntity);
		
	}
	
	public String getEncodedPassword(UserDTO user) throws UserRetrievalException {
		UserEntity userEntity = getEntityByName(user.getUsername());
		return userEntity.getPassword();
	}
	
}
