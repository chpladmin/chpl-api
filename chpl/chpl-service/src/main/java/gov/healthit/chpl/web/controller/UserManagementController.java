package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.GrantRoleJSONObject;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.json.UserInvitation;
import gov.healthit.chpl.auth.json.UserListJSONObject;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ActivityConcept;
import gov.healthit.chpl.domain.AuthorizeCredentials;
import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.InvitationManager;
import gov.healthit.chpl.manager.TestingLabManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "users")
@RestController
@RequestMapping("/users")
public class UserManagementController {
	
	@Autowired UserManager userManager;
	@Autowired CertificationBodyManager acbManager;
	@Autowired TestingLabManager atlManager;
	@Autowired InvitationManager invitationManager;
	@Autowired private Authenticator authenticator;
	@Autowired private ActivityManager activityManager;
	@Autowired private SendMailUtil sendMailService;
	@Autowired private Environment env;
	
	private static final Logger logger = LogManager.getLogger(UserManagementController.class);
	private static final long VALID_INVITATION_LENGTH = 3L*24L*60L*60L*1000L;
	private static final long VALID_CONFIRMATION_LENGTH = 30L*24L*60L*60L*1000L;
	
	@ApiOperation(value="Create a new user account from an invitation.", 
			notes="An individual who has been invited to the CHPL has a special user key in their invitation email. "
					+ "That user key along with all the information needed to create a new user's account "
					+ "can be passed in here. The account is created but cannot be used until that user "
					+ "confirms that their email address is valid. The correct order to call invitation requests is "
					+ "the following: 1) /invite 2) /create or /authorize 3) /confirm ")
	@RequestMapping(value="/create", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public User createUser(@RequestBody CreateUserFromInvitationRequest userInfo) 
			throws InvalidArgumentsException, UserCreationException, UserRetrievalException, 
			EntityRetrievalException, MessagingException, JsonProcessingException, EntityCreationException {
		
		if(userInfo.getUser() == null || userInfo.getUser().getSubjectName() == null) {
			throw new InvalidArgumentsException("Username ('subject name') is required.");
		}
		
		InvitationDTO invitation = invitationManager.getByInvitationHash(userInfo.getHash());
		if(invitation == null || invitation.isOlderThan(VALID_INVITATION_LENGTH)) {
			throw new InvalidArgumentsException("Provided user key is not valid in the database. The user key is valid for up to 3 days from when it is assigned.");
		}
		
		UserDTO createdUser = invitationManager.createUserFromInvitation(invitation, userInfo.getUser());
		
		//get the invitation again to get the new hash
		invitation = invitationManager.getById(invitation.getId());
	
		//send email for user to confirm email address	
		String htmlMessage = "<p>Thank you for setting up your administrator account on ONC's Open Data CHPL. " +
					"Please click the link below to activate your account: <br/>" +
					env.getProperty("chplUrlBegin") + "/#/registration/confirm-user/" + invitation.getConfirmToken() +
				"</p>" +
				"<p>If you have any questions, please contact Scott Purnell-Saunders at Scott.Purnell-Saunders@hhs.gov.</p>" +
				"<p>The Open Data CHPL Team</p>";

		sendMailService.sendEmail(createdUser.getEmail(), "Confirm CHPL Administrator Account", htmlMessage);
		
		String activityDescription = "User "+createdUser.getSubjectName()+" was created.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_USER, createdUser.getId(), activityDescription, null, createdUser, createdUser.getId());
		
		User result = new User(createdUser);
		result.setHash(invitation.getConfirmToken());
		return result;
	}
	
	@ApiOperation(value="Confirm that a user's email address is valid.", 
			notes="When a new user accepts their invitation to the CHPL they have to provide "
					+ "an email address. They then receive an email prompting them to confirm "
					+ "that this email address is valid. Confirming the email address must be done "
					+ "via this request before the user will be allowed to log in with "
					+ "the credentials they selected. "
					+ "The correct order to call invitation requests is "
					+ "the following: 1) /invite 2) /create or /authorize 3) /confirm ")	
	@RequestMapping(value="/confirm", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public User confirmUser(@RequestBody String hash) 
			throws InvalidArgumentsException, UserRetrievalException, EntityRetrievalException, MessagingException, JsonProcessingException, EntityCreationException {
		InvitationDTO invitation = invitationManager.getByConfirmationHash(hash);

		if(invitation == null || invitation.isOlderThan(VALID_INVITATION_LENGTH))
		{
			throw new InvalidArgumentsException("Provided user key is not valid in the database. The user key is valid for up to 3 days from when it is assigned.");
		}
		
		UserDTO createdUser = invitationManager.confirmAccountEmail(invitation);
		
		String activityDescription = "User "+createdUser.getSubjectName()+" was confirmed.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_USER, createdUser.getId(), activityDescription, createdUser, createdUser, createdUser.getId());
		
		
		return new User(createdUser);
	}
	
	@ApiOperation(value="Update an existing user account with new permissions.", 
			notes="Adds all permissions from the invitation identified by the user key "
					+ "to the appropriate existing user account."
					+ "The correct order to call invitation requests is "
					+ "the following: 1) /invite 2) /create or /authorize 3) /confirm ")
	@RequestMapping(value="/authorize", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String authorizeUser(@RequestBody AuthorizeCredentials credentials) 
			throws InvalidArgumentsException, JWTCreationException, UserRetrievalException, EntityRetrievalException {
		if(StringUtils.isEmpty(credentials.getHash())) {
			throw new InvalidArgumentsException("User key is required.");
		}
		
		gov.healthit.chpl.auth.user.User loggedInUser = Util.getCurrentUser();
		if(loggedInUser == null && (StringUtils.isEmpty(credentials.getUserName()) ||
				StringUtils.isEmpty(credentials.getPassword()))) {
			throw new InvalidArgumentsException("Username and Password are required since no user is currently logged in.");
		}
		
		InvitationDTO invitation = invitationManager.getByInvitationHash(credentials.getHash());
		if(invitation == null || invitation.isOlderThan(VALID_CONFIRMATION_LENGTH)) {
			throw new InvalidArgumentsException("Provided user key is not valid in the database. The user key is valid for up to 3 days from when it is assigned.");
		}
		
		String jwtToken = null;
		if(loggedInUser == null) {
			UserDTO userToUpdate = authenticator.getUser(credentials);		
			if(userToUpdate == null) {
				throw new UserRetrievalException("The user " + credentials.getUserName() + " could not be authenticated.");
			}
			invitationManager.updateUserFromInvitation(invitation, userToUpdate);
			jwtToken = authenticator.getJWT(credentials);
		} else {
			//add authorization to the currently logged in user
			UserDTO userToUpdate = userManager.getById(loggedInUser.getId());
			invitationManager.updateUserFromInvitation(invitation, userToUpdate);
			UserDTO updatedUser = userManager.getById(loggedInUser.getId());
			jwtToken = authenticator.getJWT(updatedUser);
		}
		
		String jwtJSON = "{\"token\": \""+jwtToken+"\"}";
		return jwtJSON;
	}
	
	@ApiOperation(value="Invite a user to the CHPL.", 
			notes="This request creates an invitation that is sent to the email address provided. "
					+ "The recipient of this invitation can then choose to create a new account "
					+ "or add the permissions contained within the invitation to an exisitng account "
					+ "if they have one. Said another way, an invitation can be used to create or "
					+ "modify CHPL user accounts."
					+ "The correct order to call invitation requests is "
					+ "the following: 1) /invite 2) /create or /authorize 3) /confirm ")
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
			if(invitation.getAcbId() == null && invitation.getTestingLabId() == null) {
				createdInvite = invitationManager.inviteWithRolesOnly(invitation.getEmailAddress(), invitation.getPermissions());
			} else if(invitation.getAcbId() != null && invitation.getTestingLabId() == null) {
				createdInvite = invitationManager.inviteWithAcbAccess(invitation.getEmailAddress(), invitation.getAcbId(), invitation.getPermissions());
			} else if(invitation.getAcbId() == null && invitation.getTestingLabId() != null) {
				createdInvite = invitationManager.inviteWithAtlAccess(invitation.getEmailAddress(), invitation.getTestingLabId(), invitation.getPermissions());
			} else {
				createdInvite = invitationManager.inviteWithAcbAndAtlAccess(invitation.getEmailAddress(), invitation.getAcbId(), invitation.getTestingLabId(), invitation.getPermissions());
			}
		}
		
		//send email		
		String htmlMessage = "<p>Hi,</p>" +
				"<p>You have been granted a new role on ONC's Open Data CHPL " +
					"which will allow you to manage certified product listings on the CHPL. " +
					"Please click the link below to create or update your account: <br/>" +
					env.getProperty("chplUrlBegin") + "/#/registration/create-user/"+ createdInvite.getInviteToken() +
				"</p>" +
				"<p>If you have any questions, please contact Scott Purnell-Saunders at Scott.Purnell-Saunders@hhs.gov.</p>" +
				"<p>Take care,<br/> " +
				 "The Open Data CHPL Team</p>";

		sendMailService.sendEmail(createdInvite.getEmail(), "Open Data CHPL Administrator Invitation", htmlMessage);
		
		UserInvitation result = new UserInvitation(createdInvite);
		return result;
	}	
	
	@ApiOperation(value="Modify user information.", 
			notes="")
	@RequestMapping(value="/update", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public User updateUserDetails(@RequestBody User userInfo) throws UserRetrievalException, UserPermissionRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException {
		
		if(userInfo.getUserId() <= 0) {
			throw new UserRetrievalException("Cannot update user with ID less than 0");
		}
		
		UserDTO before = userManager.getById(userInfo.getUserId());
		UserDTO updated = userManager.update(userInfo);
		
		String activityDescription = "User "+userInfo.getSubjectName()+" was updated.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_USER, before.getId(), activityDescription, before, updated);
		
		return new User(updated);
	}
	
	@ApiOperation(value="Delete a user.", 
			notes="Deletes a user account and all associated authorities on ACBs and ATLs. "
					+ "The logged in user must have ROLE_ADMIN.")
	@RequestMapping(value="/{userId}/delete", method= RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public String deleteUser(@PathVariable("userId") Long userId) 
			throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException {
		if(userId <= 0) {
			throw new UserRetrievalException("Cannot delete user with ID less than 0");
		}
		UserDTO toDelete = userManager.getById(userId);
		
		if(toDelete == null) {
			throw new UserRetrievalException("Could not find user with id " + userId);
		}
		
		//delete the acb permissions for that user
		acbManager.deletePermissionsForUser(toDelete);
		atlManager.deletePermissionsForUser(toDelete);
		
		//delete the user
		userManager.delete(toDelete);
		
		String activityDescription = "User "+toDelete.getSubjectName()+" was deleted.";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_USER, toDelete.getId(), activityDescription, toDelete, null);
		
		
		return "{\"deletedUser\" : true }";
	}
	
	@ApiOperation(value="Give additional roles to a user.", 
			notes="Users may be given ROLE_ADMIN, ROLE_ACB_ADMIN, ROLE_ACB_STAFF, "
					+ "ROLE_ATL_ADMIN, or ROLE_ATL_STAFF roles within the system.")
	@RequestMapping(value="/grant_role", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String grantUserRole(@RequestBody GrantRoleJSONObject grantRoleObj) throws InvalidArgumentsException, UserRetrievalException, UserManagementException, UserPermissionRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException {
		
		UserDTO user = userManager.getByName(grantRoleObj.getSubjectName());
		if(user == null) {
			throw new InvalidArgumentsException("No user with name " + grantRoleObj.getSubjectName() + " exists in the system.");
		}
		
		if(grantRoleObj.getRole().equals("ROLE_ADMIN")) {
			try {
				userManager.grantAdmin(user.getSubjectName());
			} catch(AccessDeniedException adEx) {
				logger.error("User " + Util.getUsername() + " does not have access to grant ROLE_ADMIN");
				throw adEx;
			}
		} else {
			userManager.grantRole(user.getSubjectName(), grantRoleObj.getRole());
		}

		UserDTO updated = userManager.getByName(grantRoleObj.getSubjectName());
		
		String activityDescription = "User "+user.getSubjectName()+" was granted role "+grantRoleObj.getRole()+".";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_USER, user.getId(), activityDescription, user, updated);
		
		return "{\"roleAdded\" : true }";
		
	}
	
	@ApiOperation(value="Remove roles previously granted to a user.", 
			notes="Users may be given ROLE_ADMIN, ROLE_ACB_ADMIN, ROLE_ACB_STAFF, "
					+ "ROLE_ATL_ADMIN, or ROLE_ATL_STAFF roles within the system.")
	@RequestMapping(value="/revoke_role", method= RequestMethod.POST, 
			consumes= MediaType.APPLICATION_JSON_VALUE,
			produces="application/json; charset=utf-8")
	public String revokeUserRole(@RequestBody GrantRoleJSONObject grantRoleObj) throws InvalidArgumentsException, UserRetrievalException, UserManagementException, UserPermissionRetrievalException, JsonProcessingException, EntityCreationException, EntityRetrievalException {
		
		String isSuccess = String.valueOf(false);
		UserDTO user = userManager.getByName(grantRoleObj.getSubjectName());
		if(user == null) {
			throw new InvalidArgumentsException("No user with name " + grantRoleObj.getSubjectName() + " exists in the system.");
		}
		
		if(grantRoleObj.getRole().equals("ROLE_ADMIN")) {
			try {
				userManager.removeAdmin(user.getSubjectName());
			} catch(AccessDeniedException adEx) {
				logger.error("User " + Util.getUsername() + " does not have access to revoke ROLE_ADMIN");
			}
		} else if(grantRoleObj.getRole().equals("ROLE_ACB_ADMIN")) {
			try {
				userManager.removeRole(grantRoleObj.getSubjectName(), grantRoleObj.getRole());
				
				//if they were an acb admin then they need to have all ACB access removed
				List<CertificationBodyDTO> acbs = acbManager.getAllForUser(false);
				for(CertificationBodyDTO acb : acbs) {
					acbManager.deletePermission(acb, new PrincipalSid(user.getSubjectName()), BasePermission.ADMINISTRATION);
				}
			} catch(AccessDeniedException adEx) {
				logger.error("User " + Util.getUsername() + " does not have access to revoke ROLE_ADMIN");
			}
		} else {
			userManager.removeRole(grantRoleObj.getSubjectName(), grantRoleObj.getRole());
		}
		
		UserDTO updated = userManager.getByName(grantRoleObj.getSubjectName());
		
		String activityDescription = "User "+user.getSubjectName()+" was removed from role "+grantRoleObj.getRole()+".";
		activityManager.addActivity(ActivityConcept.ACTIVITY_CONCEPT_USER, user.getId(), activityDescription, user, updated);
		
		
		isSuccess = String.valueOf(true);
		return "{\"roleRemoved\" : "+isSuccess+" }";
		
	}
	
	@ApiOperation(value="View users of the system.", 
			notes="Only ROLE_ADMIN will be able to see all users.")
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
	
	@ApiOperation(value="View a specific user's details.", 
			notes="The logged in user must either be the user in the parameters, have ROLE_ADMIN, or "
					+ "have ROLE_ACB_ADMIN.")
	@RequestMapping(value="/{userName}/details", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody UserInfoJSONObject getUser(@PathVariable("userName") String userName) throws UserRetrievalException {
		
		return userManager.getUserInfo(userName);
		
	}
}
