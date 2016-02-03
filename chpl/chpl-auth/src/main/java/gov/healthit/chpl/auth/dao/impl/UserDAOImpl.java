package gov.healthit.chpl.auth.dao.impl;


import gov.healthit.chpl.auth.BaseDAOImpl;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserContactDAO;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.entity.UserContactEntity;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.manager.impl.SecuredUserManagerImpl;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository(value="userDAO")
public class UserDAOImpl extends BaseDAOImpl implements UserDAO {
	private static final Logger logger = LogManager.getLogger(UserDAOImpl.class);
	
	@Autowired
	UserPermissionDAO userPermissionDAO;
	
	@Autowired
	UserContactDAO userContactDAO;
	
	
	@Override
	@Transactional
	public UserDTO create(UserDTO user, String encodedPassword) throws UserCreationException {
		
		UserEntity userEntity = null;
		try {
			userEntity = getEntityByName(user.getSubjectName());
		} catch (UserRetrievalException e) {
			throw new UserCreationException(e);
		}
		
		if (userEntity != null) {
			throw new UserCreationException("user name: "+user.getSubjectName() +" already exists.");
		} else {
			
			userEntity = new UserEntity(user.getSubjectName(), encodedPassword);
			
			userEntity.setFirstName(user.getFirstName());
			userEntity.setLastName(user.getLastName());
			userEntity.setComplianceSignature(user.getComplianceSignatureDate());
			userEntity.setFailedLoginCount(0);
			userEntity.setAccountEnabled(user.isAccountEnabled());
			userEntity.setAccountExpired(user.isAccountExpired());
			userEntity.setAccountLocked(user.isAccountLocked());
			userEntity.setCredentialsExpired(!user.isCredentialsNonExpired());
			userEntity.setLastModifiedUser(Util.getCurrentUser().getId());
			userEntity.setLastModifiedDate(new Date());
			userEntity.setDeleted(false);
			
			UserContactEntity contact = new UserContactEntity();
			contact.setEmail(user.getEmail());
			contact.setFirstName(user.getFirstName());
			contact.setLastName(user.getLastName());
			contact.setPhoneNumber(user.getPhoneNumber());
			contact.setTitle(user.getTitle());
			contact.setLastModifiedUser(Util.getCurrentUser().getId());
			contact.setLastModifiedDate(new Date());
			contact.setDeleted(false);
			contact.setSignatureDate(null); //null for new user, must confirm email to get it filled in
			
			userContactDAO.create(contact);
			userEntity.setContact(contact);
			create(userEntity);	
		}

		return new UserDTO(userEntity);
	}
	
	
	@Override
	@Transactional
	public UserDTO update(UserDTO user) throws UserRetrievalException {
		
		UserEntity userEntity = getEntityByName(user.getSubjectName());
		
		userEntity.setFirstName(user.getFirstName());
		userEntity.setLastName(user.getLastName());
		userEntity.setComplianceSignature(user.getComplianceSignatureDate());
		userEntity.setFailedLoginCount(user.getFailedLoginCount());
		userEntity.getContact().setEmail(user.getEmail());
		userEntity.getContact().setPhoneNumber(user.getPhoneNumber());
		userEntity.getContact().setSignatureDate(user.getSignatureDate());
		userEntity.getContact().setTitle(user.getTitle());
		userEntity.setAccountEnabled(user.isAccountEnabled());
		userEntity.setAccountExpired(user.isAccountExpired());
		userEntity.setAccountLocked(user.isAccountLocked());
		userEntity.setCredentialsExpired(!user.isCredentialsNonExpired());
		userEntity.setLastModifiedUser(Util.getCurrentUser().getId());
		userEntity.getContact().setLastModifiedUser(Util.getCurrentUser().getId());
		
		update(userEntity);
		return new UserDTO(userEntity);
	}
	
	
	@Override
	@Transactional
	public void delete(String uname) throws UserRetrievalException {
		
		// First delete the user / permission mappings for this user.
		userPermissionDAO.deleteMappingsForUser(uname);
		
		UserEntity toDelete = getEntityByName(uname);
		if(toDelete == null) {
			throw new UserRetrievalException("Could not find user with name " + uname);
		}
		
		delete(toDelete);
	}
	
	
	@Override
	@Transactional
	public void delete(Long userId) throws UserRetrievalException {
		
		// First delete the user / permission mappings for this user.
		userPermissionDAO.deleteMappingsForUser(userId);
		
		UserEntity toDelete = getEntityById(userId);
		if(toDelete == null) {
			throw new UserRetrievalException("Could not find user with id " + userId);
		}
		
		delete(toDelete);
	}
	
	private void delete(UserEntity toDelete) {
		//delete the contact
		if(toDelete.getContact() != null) {
			userContactDAO.delete(toDelete.getContact());
		}
		
		//delete the user
		toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
		toDelete.setLastModifiedDate(new Date());
		toDelete.setDeleted(true);
		update(toDelete);
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
	
	public List<UserDTO> findByNames(List<String> names) {
		List<UserDTO> result = new ArrayList<UserDTO>();

		if(names == null || names.size() == 0) {
			return result;
		}
		
		Query query = entityManager.createQuery( "from UserEntity where deleted <> true AND user_name in (:names)", UserEntity.class );
		query.setParameter("names", names);
		List<UserEntity> queryResult = query.getResultList();

		if(queryResult != null) {
			for(UserEntity entity : queryResult) {
				result.add(new UserDTO(entity));
			}
		}
		return result;
	}
	
	public UserDTO findUserByNameAndEmail(String username, String email) {
		UserDTO foundUser = null;
		
		String userQuery = "from UserEntity u" 
				+ " where (NOT u.deleted = true) "
				+ " AND (u.subjectName = :subjectName) "
				+ " AND (u.contact.email = :email)";

		Query query = entityManager.createQuery(userQuery, UserEntity.class );
		query.setParameter("subjectName", username);
		query.setParameter("email", email);
		List<UserEntity> result = query.getResultList();
		if (result.size() >= 1){
			UserEntity entity = result.get(0);
			foundUser = new UserDTO(entity);
		} 
		
		return foundUser;
	}
	
	public UserDTO findUser(UserDTO toSearch) {
		UserDTO foundUser = null;
		
		String userQuery = "from UserEntity u" 
				+ " where (NOT u.deleted = true) "
				+ " AND (u.subjectName = :subjectName) "
				+ " AND (u.contact.firstName = :firstName)"
				+ " AND (u.contact.lastName = :lastName)"
				+ " AND (u.contact.email = :email)"
				+ " AND (u.contact.phoneNumber = :phoneNumber)";
		if(toSearch.getTitle() != null) {
			userQuery += " AND (u.contact.title = :title)";
		} else {
			userQuery += " AND (u.contact.title IS NULL)";
		}
		Query query = entityManager.createQuery(userQuery, UserEntity.class );
		query.setParameter("subjectName", toSearch.getSubjectName());
		query.setParameter("firstName", toSearch.getFirstName());
		query.setParameter("lastName", toSearch.getLastName());
		query.setParameter("email", toSearch.getEmail());
		query.setParameter("phoneNumber", toSearch.getPhoneNumber());
		if(toSearch.getTitle() != null) {
			query.setParameter("title", toSearch.getTitle());
		}
		
		List<UserEntity> result = query.getResultList();
		if (result.size() >= 1){
			UserEntity entity = result.get(0);
			foundUser = new UserDTO(entity);
		} 
		
		return foundUser;
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
		
		if(result.size() == 0) {
			return null;
		}
		return result.get(0);
	}

	
	private UserEntity getEntityByName(String uname) throws UserRetrievalException {
		
		UserEntity user = null;
		
		Query query = entityManager.createQuery( "from UserEntity where ((NOT deleted = true) AND (user_name = (:uname))) ", UserEntity.class );
		query.setParameter("uname", uname);
		List<UserEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new UserRetrievalException("Data error. Duplicate user name in database.");
		} 
		
		if(result.size() == 0) {
			return null;
		}
		return result.get(0);
	}

	@Override
	@Transactional
	public void addPermission(String uname, String authority) throws UserPermissionRetrievalException, UserRetrievalException {
		
		UserEntity userEntity = this.getEntityByName(uname);
		if(userEntity != null) {
			Set<UserPermissionDTO> permissions = userPermissionDAO.findPermissionsForUser(userEntity.getId());
			boolean permissionExists = false;
			for(UserPermissionDTO permission : permissions) {
				if(permission.getAuthority().equals(authority)) {
					permissionExists = true;
				}
			}
			
			if(!permissionExists) {
				userPermissionDAO.createMapping(userEntity, authority);
			} else {
				logger.error("Permission " + authority + " already exists for " + uname + ". Not adding anything.");
			}
		}		
	}

	@Override
	public UserDTO getById(Long userId) throws UserRetrievalException {
		
		UserEntity userEntity = this.getEntityById(userId);
		if(userEntity == null) {
			return null;
		}
		return new UserDTO(userEntity);
	}

	@Override
	public UserDTO getByName(String uname) throws UserRetrievalException {
		UserEntity userEntity = this.getEntityByName(uname);
		if(userEntity == null) {
			return null;
		}
		return new UserDTO(userEntity);
	}

	@Override
	@Transactional
	public void removePermission(String uname, String authority) throws UserRetrievalException, UserPermissionRetrievalException {
		userPermissionDAO.deleteMapping(uname, authority);
	}

	@Override
	@Transactional
	public void updatePassword(String uname, String encodedPassword) throws UserRetrievalException {
		
		UserEntity userEntity = this.getEntityByName(uname);
		userEntity.setPassword(encodedPassword);
		update(userEntity);
		
	}
	
	@Override
	@Transactional
	public void updateFailedLoginCount(String uname, int failedLoginCount) throws UserRetrievalException {
		
		UserEntity userEntity = this.getEntityByName(uname);
		userEntity.setFailedLoginCount(failedLoginCount);
		update(userEntity);
		
	}
	
	@Override
	@Transactional
	public void updateAccountLockedStatus(String uname, boolean locked) throws UserRetrievalException {
		
		UserEntity userEntity = this.getEntityByName(uname);
		userEntity.setAccountLocked(locked);
		update(userEntity);
		
	}
	
	public String getEncodedPassword(UserDTO user) throws UserRetrievalException {
		UserEntity userEntity = getEntityByName(user.getUsername());
		return userEntity.getPassword();
	}
	
}
