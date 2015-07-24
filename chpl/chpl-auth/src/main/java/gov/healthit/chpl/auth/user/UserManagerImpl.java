package gov.healthit.chpl.auth.user;

import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.permission.UserPermissionDTO;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.permission.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.user.dao.UserContactDAO;
import gov.healthit.chpl.auth.user.dao.UserDAO;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagerImpl implements UserManager {

	@Autowired
	private SecuredUserManager securedUserManager;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private UserContactDAO userContactDAO;
	
	@Autowired
	UserPermissionDAO userPermissionDAO;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	
	@Override
	@Transactional
	public void create(UserCreationJSONObject userInfo) throws UserCreationException, UserRetrievalException {
		
		UserDTO user = UserConversionHelper.createDTO(userInfo);
		String encodedPassword = encodePassword(userInfo.getPassword());
		securedUserManager.create(user, encodedPassword);
	}
	
	@Override
	@Transactional
	public void update(UserInfoJSONObject userInfo) throws UserRetrievalException{
		
		UserDTO userDTO = getByName(userInfo.getSubjectName());
		
		if (userInfo.getFirstName() != null){
			userDTO.setFirstName(userInfo.getFirstName());
		}
		
		if (userInfo.getLastName() != null){
			userDTO.setLastName(userInfo.getLastName());
		}
		
		if (userInfo.getEmail() != null){
			userDTO.setEmail(userInfo.getEmail());
		}
		
		if (userInfo.getPhoneNumber() != null){
			userDTO.setPhoneNumber(userInfo.getPhoneNumber());
		}
		
		if (userInfo.getTitle() != null){
			userDTO.setTitle(userInfo.getTitle());
		}
		
		userDTO.setAccountLocked(userInfo.isAccountLocked());
		userDTO.setAccountEnabled(userInfo.isAccountEnabled());
		securedUserManager.update(userDTO);
	}
	
	@Transactional
	private void update(UserDTO user) throws UserRetrievalException {	
		securedUserManager.update(user);
	}
	
	@Transactional
	private void updateContactInfo(UserEntity user){
		securedUserManager.updateContactInfo(user);
	}
	
	@Transactional
	public void delete(UserDTO user){
		securedUserManager.delete(user);
	}
	
	@Override
	@Transactional
	public void delete(String userName) throws UserRetrievalException{
		
		UserDTO user = securedUserManager.getBySubjectName(userName);
		if (user == null){
			throw new UserRetrievalException("User not found");
		} else {
			delete(user);
		}
	}
	
	
	@Transactional
	public List<UserDTO> getAll(){
		return securedUserManager.getAll();
	}
	
	
	@Transactional
	public UserDTO getById(Long id) throws UserRetrievalException{
		return securedUserManager.getById(id);
	}
	
	@Override
	@Transactional
	public void grantRole(String userName, String role) throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		securedUserManager.grantRole(userName, role);
	}
	
	@Override
	@Transactional
	public void grantAdmin(String userName) throws UserPermissionRetrievalException, UserRetrievalException, UserManagementException {
		securedUserManager.grantAdmin(userName);
	}
	
	@Override
	@Transactional
	public void removeRole(UserDTO user, String role) throws UserRetrievalException, UserPermissionRetrievalException {
		securedUserManager.removeRole(user, role);
	}
	
	@Override
	@Transactional
	public void removeRole(String userName, String role) throws UserRetrievalException, UserPermissionRetrievalException {
		securedUserManager.removeRole(userName, role);
	}
	
	@Override
	@Transactional
	public void updateUserPassword(String userName, String password) throws UserRetrievalException {
		
		String encodedPassword = encodePassword(password);
		UserDTO userToUpdate = securedUserManager.getBySubjectName(userName);
		securedUserManager.updatePassword(userToUpdate, encodedPassword);
		
	}
	
	@Override
	public String encodePassword(String password){
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		return encodedPassword;
	}

	@Override
	public String getEncodedPassword(UserDTO user) throws UserRetrievalException {
		return userDAO.getEncodedPassword(user);
	}
	
	
	@Override
	public Set<UserPermissionDTO> getGrantedPermissionsForUser(UserDTO user){
		return securedUserManager.getGrantedPermissionsForUser(user);
	}


	@Override
	public UserDTO getByName(String userName) throws UserRetrievalException {
		return securedUserManager.getBySubjectName(userName);
	}
	
	@Override
	public UserInfoJSONObject getUserInfo(String userName) throws UserRetrievalException {
		UserDTO user = securedUserManager.getBySubjectName(userName);
		UserInfoJSONObject userInfo = new UserInfoJSONObject(user);
		return userInfo;
	}
	
}
