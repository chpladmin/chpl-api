package gov.healthit.chpl.web.controller;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.authentication.LoginCredentials;
import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.GrantRoleJSONObject;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserCreationWithRolesJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.json.UserInvitation;
import gov.healthit.chpl.auth.json.UserListJSONObject;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.AuthorizeCredentials;
import gov.healthit.chpl.domain.CertificationBodyPermission;
import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.InvitationManager;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserManagementController {
	
	@Autowired UserManager userManager;
	@Autowired CertificationBodyManager acbManager;
	@Autowired InvitationManager invitationManager;
	@Autowired private Authenticator authenticator;
	
	private static final Logger logger = LogManager.getLogger(UserManagementController.class);
	
	@RequestMapping(value="/create", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public User createUser(@RequestBody CreateUserFromInvitationRequest userInfo) 
			throws InvalidArgumentsException, UserCreationException, UserRetrievalException, EntityRetrievalException {
		boolean validHash = invitationManager.isHashValid(userInfo.getHash());
		if(!validHash) {
			throw new InvalidArgumentsException("Provided hash is not valid in the database. The hash is valid for up to 3 days from when it is assigned.");
		}
		
		if(userInfo.getUser() == null || userInfo.getUser().getSubjectName() == null) {
			throw new InvalidArgumentsException("Username ('subject name') is required.");
		}
		
		InvitationDTO invitation = invitationManager.getByHash(userInfo.getHash());
		UserDTO createdUser = invitationManager.createUserFromInvitation(invitation, userInfo.getUser());
		return new User(createdUser);
	}
	
	/*
	 * update a user's permissions with new ones issued in an invitation
	 */
	@RequestMapping(value="/authorize", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String authorizeUser(@RequestBody AuthorizeCredentials credentials) 
			throws InvalidArgumentsException, JWTCreationException, UserRetrievalException, EntityRetrievalException {
		if(StringUtils.isEmpty(credentials.getHash()) || StringUtils.isEmpty(credentials.getUserName()) ||
				StringUtils.isEmpty(credentials.getPassword())) {
			throw new InvalidArgumentsException("Username, Password, and Token are all required.");
		}
		
		boolean validHash = invitationManager.isHashValid(credentials.getHash());
		if(!validHash) {
			throw new InvalidArgumentsException("Provided hash is not valid in the database. The hash is valid for up to 3 days from when it is assigned.");
		}
		
		UserDTO userToUpdate = authenticator.getUser(credentials);		
		if(userToUpdate == null) {
			throw new UserRetrievalException("The user " + credentials.getUserName() + " could not be authenticated.");
		}
		
		InvitationDTO invitation = invitationManager.getByHash(credentials.getHash());
		invitationManager.updateUserFromInvitation(invitation, userToUpdate);
		
		String jwt = authenticator.getJWT(credentials);
		String jwtJSON = "{\"token\": \""+jwt+"\"}";
		return jwtJSON;
	}
	
	@RequestMapping(value="/invite", method=RequestMethod.POST,
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public UserInvitation inviteUser(@RequestBody UserInvitation invitation) 
			throws InvalidArgumentsException, UserCreationException, UserRetrievalException, 
			UserPermissionRetrievalException, AddressException, MessagingException {
		boolean isChplAdmin = false;
		for(String permission : invitation.getPermissions()) {
			if(permission.equals("ADMIN") || permission.equals("ROLE_ADMIN")) {
				isChplAdmin = true;
			}
		}
		
		InvitationDTO createdInvite = null;
		if(isChplAdmin) {
			createdInvite = invitationManager.inviteAdmin(invitation.getEmailAddress(), invitation.getPermissions());
		} else {
			if(invitation.getAcbId() == null || invitation.getAcbId() < 0) {
				throw new InvalidArgumentsException("An ACB ID is required.");
			}
			createdInvite = invitationManager.inviteWithAcbAccess(invitation.getEmailAddress(), invitation.getAcbId(), invitation.getPermissions());
		}
		
		//send email
		//sendEmail(createdInvite);
		
		UserInvitation result = new UserInvitation(createdInvite);
		return result;
	}
	
	@RequestMapping(value="/update", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public User updateUserDetails(@RequestBody User userInfo) throws UserRetrievalException, UserPermissionRetrievalException {
		if(userInfo.getUserId() <= 0) {
			throw new UserRetrievalException("Cannot update user with ID less than 0");
		}
		UserDTO updated = userManager.update(userInfo);
		return new User(updated);
	}
	
	
	@RequestMapping(value="/{userId}/delete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteUser(@PathVariable("userId") Long userId) 
			throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		if(userId <= 0) {
			throw new UserRetrievalException("Cannot delete user with ID less than 0");
		}
		UserDTO toDelete = userManager.getById(userId);
		
		//delete the acb permissions for that user
		acbManager.deletePermissionsForUser(toDelete);
		
		//delete the user
		userManager.delete(toDelete);
		
		return "{\"deletedUser\" : true }";
	}
	
	
	@RequestMapping(value="/reset_password", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String resetPassword(@RequestBody LoginCredentials newCredentials) throws UserRetrievalException {
		
		userManager.updateUserPassword(newCredentials.getUserName(), newCredentials.getPassword());
		return "{\"passwordUpdated\" : true }";
	
	}	
	
	@RequestMapping(value="/grant_role", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String grantUserRole(@RequestBody GrantRoleJSONObject grantRoleObj) throws InvalidArgumentsException, UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		String isSuccess = String.valueOf(false);
		UserDTO user = userManager.getByName(grantRoleObj.getSubjectName());
		if(user == null) {
			throw new InvalidArgumentsException("No user with name " + grantRoleObj.getSubjectName() + " exists in the system.");
		}
		
		if(grantRoleObj.getRole().equals("ROLE_ADMIN")) {
			try {
				userManager.grantAdmin(user.getSubjectName());

				List<CertificationBodyDTO> acbs = acbManager.getAll();
				for(CertificationBodyDTO acb : acbs) {
					acbManager.addPermission(acb, user.getId(), BasePermission.ADMINISTRATION);
				}
			} catch(AccessDeniedException adEx) {
				logger.error("User " + Util.getUsername() + " does not have access to grant ROLE_ADMIN");
			}
		} else {
			userManager.grantRole(user.getSubjectName(), grantRoleObj.getRole());
		}

		isSuccess = String.valueOf(true);
		return "{\"roleAdded\" : "+isSuccess+" }";
		
	}
	
	@RequestMapping(value="/revoke_role", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String revokeUserRole(@RequestBody GrantRoleJSONObject grantRoleObj) throws InvalidArgumentsException, UserRetrievalException, UserManagementException, UserPermissionRetrievalException {
		
		String isSuccess = String.valueOf(false);
		UserDTO user = userManager.getByName(grantRoleObj.getSubjectName());
		if(user == null) {
			throw new InvalidArgumentsException("No user with name " + grantRoleObj.getSubjectName() + " exists in the system.");
		}
		
		if(grantRoleObj.getRole().equals("ROLE_ADMIN")) {
			try {
				userManager.removeAdmin(user.getSubjectName());
				
				//if they were a chpladmin then they need to have all ACB access removed
				List<CertificationBodyDTO> acbs = acbManager.getAll();
				for(CertificationBodyDTO acb : acbs) {
					acbManager.deletePermission(acb, new PrincipalSid(user.getSubjectName()), BasePermission.ADMINISTRATION);
				}
			} catch(AccessDeniedException adEx) {
				logger.error("User " + Util.getUsername() + " does not have access to revoke ROLE_ADMIN");
			}
		} else if(grantRoleObj.getRole().equals("ROLE_ACB_ADMIN")) {
			try {
				userManager.removeRole(grantRoleObj.getSubjectName(), grantRoleObj.getRole());
				
				//if they were an acb admin then they need to have all ACB access removed
				List<CertificationBodyDTO> acbs = acbManager.getAll();
				for(CertificationBodyDTO acb : acbs) {
					acbManager.deletePermission(acb, new PrincipalSid(user.getSubjectName()), BasePermission.ADMINISTRATION);
				}
			} catch(AccessDeniedException adEx) {
				logger.error("User " + Util.getUsername() + " does not have access to revoke ROLE_ADMIN");
			}
		} else {
			userManager.removeRole(grantRoleObj.getSubjectName(), grantRoleObj.getRole());
		}
		

		
		isSuccess = String.valueOf(true);
		return "{\"roleRemoved\" : "+isSuccess+" }";
		
	}
	
	@RequestMapping(value="/", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody UserListJSONObject getUsers(){
		
		List<UserDTO> userList = userManager.getAll();
		List<UserInfoJSONObject> userInfos = new ArrayList<UserInfoJSONObject>();
		
		for (UserDTO user : userList){
			Set<UserPermissionDTO> permissions = userManager.getGrantedPermissionsForUser(user);
			
			UserInfoJSONObject userInfo = new UserInfoJSONObject(user);
			List<String> permissionList = new ArrayList<String>(permissions.size());
			for(UserPermissionDTO permission : permissions) {
				permissionList.add(permission.getAuthority());
			}
			userInfo.setRoles(permissionList);
			userInfos.add(userInfo);
		}
		
		UserListJSONObject ulist = new UserListJSONObject();
		ulist.setUsers(userInfos);
		return ulist;
	}
	
	@RequestMapping(value="/{userName}/details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody UserInfoJSONObject getUser(@PathVariable("userName") String userName) throws UserRetrievalException {
		
		return userManager.getUserInfo(userName);
		
	}
	
	/**
	 * create and send the email to invite the user
	 * @param invitation
	 */
	private void sendEmail(InvitationDTO invitation) throws AddressException, MessagingException {
		 // sets SMTP server properties
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "144.202.233.67");
        properties.put("mail.smtp.port", "25");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
 
        // creates a new session with an authenticator
        javax.mail.Authenticator auth = new javax.mail.Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("chpl-etl", "Audac1ous");
            }
        };
 
        Session session = Session.getInstance(properties, auth);
 
        // creates a new e-mail message
        Message msg = new MimeMessage(session);
 
        msg.setFrom(new InternetAddress("chpl-etl@ainq.com"));
        InternetAddress[] toAddresses = { new InternetAddress(invitation.getEmail()) };
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject("OpenCHPL Invitation");
        msg.setSentDate(new Date());
        // set plain text message
        msg.setContent(
        			"<h3>Join OpenCHPL</h3>"
        			+ "<p>You've been invited to access the CHPL.</p>"
        			+ "<p>Click the link below to create your account."
        			+ "<br/>http://localhost:8000/app?hash=" + invitation.getToken() + 
        			"</p>", "text/html");

        // sends the e-mail
        Transport.send(msg);
	}
}
