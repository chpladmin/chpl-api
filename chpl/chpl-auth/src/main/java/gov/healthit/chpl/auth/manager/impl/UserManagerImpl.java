package gov.healthit.chpl.auth.manager.impl;

import gov.healthit.chpl.auth.dao.UserContactDAO;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.entity.UserEntity;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.manager.SecuredUserManager;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserConversionHelper;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManagerImpl implements UserManager {

    private final Random random = new SecureRandom();
	private static final char[] symbols;
    static {
      StringBuilder tmp = new StringBuilder();
      for (char ch = '0'; ch <= '9'; ++ch)
        tmp.append(ch);
      for (char ch = 'a'; ch <= 'z'; ++ch)
        tmp.append(ch);
      symbols = tmp.toString().toCharArray();
    } 
    
	@Autowired private Environment env;

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
	public UserDTO create(UserCreationJSONObject userInfo) throws UserCreationException, UserRetrievalException {
		
		UserDTO user = UserConversionHelper.createDTO(userInfo);
		String encodedPassword = encodePassword(userInfo.getPassword());
		user = securedUserManager.create(user, encodedPassword);
		return user;
	}
	
	@Override
	@Transactional
	public UserDTO update(User userInfo) throws UserRetrievalException{
		
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
		
		if (userInfo.getAccountEnabled() != null){
			userDTO.setAccountEnabled(userInfo.getAccountEnabled());
		} else {
			userDTO.setAccountEnabled(true);
		}
		
		if (userInfo.getAccountLocked() != null){
			userDTO.setAccountLocked(userInfo.getAccountLocked());
		} else {
			userDTO.setAccountLocked(true);
		}
		
		if(Boolean.TRUE.equals(userInfo.getComplianceTermsAccepted())) {
			if(userDTO.getComplianceSignatureDate() == null) {
				userDTO.setComplianceSignatureDate(new Date());
			}
		} else {
			userDTO.setComplianceSignatureDate(null);
		}
		
		userDTO.setTitle(userInfo.getTitle());
		return securedUserManager.update(userDTO);
	}
	
	@Override
	@Transactional
	public UserDTO update(UserDTO user) throws UserRetrievalException {	
		return securedUserManager.update(user);
	}
	
	@Transactional
	private void updateContactInfo(UserEntity user){
		securedUserManager.updateContactInfo(user);
	}
	
	@Transactional
	public void delete(UserDTO user) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
		securedUserManager.delete(user);
	}
	
	@Override
	@Transactional
	public void delete(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
		
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
	public void removeRole(UserDTO user, String role) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
		securedUserManager.removeRole(user, role);
	}
	
	@Override
	@Transactional
	public void removeRole(String userName, String role) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
		securedUserManager.removeRole(userName, role);
	}
	
	@Override
	@Transactional
	public void removeAdmin(String userName) throws UserRetrievalException, UserPermissionRetrievalException, UserManagementException {
		securedUserManager.removeAdmin(userName);
	}
	
	@Override
	@Transactional
	public void updateFailedLoginCount(UserDTO userToUpdate) throws UserRetrievalException { 
		securedUserManager.updateFailedLoginCount(userToUpdate);
		String maxLoginsStr = env.getProperty("authMaximumLoginAttempts");
		int maxLogins = Integer.parseInt(maxLoginsStr);
		
		if(userToUpdate.getFailedLoginCount() >= maxLogins) {
			userToUpdate.setAccountLocked(true);
			securedUserManager.updateAccountLockedStatus(userToUpdate);
		}
	}
	
	@Override
	@Transactional
	public void updateUserPassword(String userName, String password) throws UserRetrievalException {
		
		String encodedPassword = encodePassword(password);
		UserDTO userToUpdate = securedUserManager.getBySubjectName(userName);
		securedUserManager.updatePassword(userToUpdate, encodedPassword);
		
	}
	
	//no auth needed. create a random string and assign it to the user
	@Override
	@Transactional
	public String resetUserPassword(String username, String email) throws UserRetrievalException {
		UserDTO foundUser = userDAO.findUserByNameAndEmail(username, email);
		if(foundUser == null) {
			throw new UserRetrievalException("Cannot find user with name " + username + " and email address " + email);
		}
		
		//create new password
		char[] buf = new char[15];
    	
    	for (int idx = 0; idx < buf.length; ++idx) {
            buf[idx] = symbols[random.nextInt(symbols.length)];
    	}
    	String password = new String(buf);
    	
		//encode new password
		String encodedPassword = encodePassword(password);
			
		//update the userDTO with the new password
		userDAO.updatePassword(foundUser.getSubjectName(), encodedPassword);
		
		return password;
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
