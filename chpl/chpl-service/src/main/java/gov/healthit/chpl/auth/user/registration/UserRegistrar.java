package gov.healthit.chpl.auth.user.registration;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserImpl;
import gov.healthit.chpl.auth.user.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrar {
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER_CREATOR')")
	public boolean createUser(UserDTO userInfo) throws UserCreationException {
		
		User user = null;
		try {
			user = userManager.getByUserName(userInfo.getUserName());
		} catch (UserRetrievalException e) {
			throw new UserCreationException(e);
		}
		
		if (user != null) {
			throw new UserCreationException("user name: "+userInfo.getUserName() +" already exists.");
		} else {
			
			String encodedPassword = bCryptPasswordEncoder.encode(userInfo.getPassword());
			UserImpl userToCreate = new UserImpl(userInfo.getUserName(), encodedPassword);
			userManager.create(userToCreate);
			return true;
		}
	}
	
	public boolean updateUserPassword(String userName, String password) throws UserRetrievalException {
		
		User fetchedUser = userManager.getByUserName(userName);
		
		if (fetchedUser == null){
			throw new UserRetrievalException("User not found");
		} else {
			UserImpl user = (UserImpl) fetchedUser;
			String encodedPassword = getEncodedPassword(password);
			user.setPassword(encodedPassword);
			userManager.update(user);
		}
		return true;
	}
	
	public boolean deactivateUser(String userName) throws UserRetrievalException{
		
		User fetchedUser = userManager.getByUserName(userName);
		
		if (fetchedUser == null){
			throw new UserRetrievalException("User not found");
		} else {
			UserImpl user = (UserImpl) fetchedUser;
			userManager.delete(user);
		}
		return true;
	}
	
	
	public String getEncodedPassword(String password){
		String encodedPassword = bCryptPasswordEncoder.encode(password);
		return encodedPassword;
	}
	
	public void createAdminUser(){
		
		User adminUser = null;
		try {
			adminUser = userManager.getByUserName("admin");
		} catch (UserRetrievalException e) {
			//TODO: Add Logging here
			e.printStackTrace();
		}
		if (adminUser == null){
			
			UserImpl admin = new UserImpl("admin");
			admin.setPassword(getEncodedPassword("admin"));
			admin.addClaim("ROLE_ADMIN");
			admin.addClaim("ROLE_USER_CREATOR");
			
			admin.setAuthenticated(true);
			
			SecurityContextHolder.getContext().setAuthentication(admin);
			userManager.create(admin);
			SecurityContextHolder.getContext().setAuthentication(null);
			
		}
	}
	
}
