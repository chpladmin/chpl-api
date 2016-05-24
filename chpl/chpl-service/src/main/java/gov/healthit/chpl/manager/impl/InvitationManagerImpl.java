package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.Util;
import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.dao.InvitationDAO;
import gov.healthit.chpl.auth.dao.InvitationPermissionDAO;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.dto.InvitationPermissionDTO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.InvitationManager;
import gov.healthit.chpl.manager.TestingLabManager;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;
import io.swagger.annotations.ApiOperation;

@Service
public class InvitationManagerImpl implements InvitationManager {
	
	@Autowired
	private UserPermissionDAO userPermissionDao;
	
	@Autowired
	private InvitationDAO invitationDao;
	
	@Autowired
	private UserDAO userDao;
	
	@Autowired
	private InvitationPermissionDAO invitationPermissionDao;
	
	@Autowired Authenticator userAuthenticator;
	@Autowired private UserManager userManager;
	@Autowired private CertificationBodyManager acbManager;
	@Autowired private TestingLabManager atlManager;
	
	private static final Logger logger = LogManager.getLogger(InvitationManagerImpl.class);

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public InvitationDTO inviteAdmin(String emailAddress, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException
	{
		InvitationDTO dto = new InvitationDTO();
		dto.setEmail(emailAddress);
		Date now = new Date();
		dto.setInviteToken(Util.md5(emailAddress + now.getTime()));
	
		return createInvitation(dto, permissions);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_ACB_ADMIN')")
	public InvitationDTO inviteWithRolesOnly(String emailAddress, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException
	{
		InvitationDTO dto = new InvitationDTO();
		dto.setEmail(emailAddress);
		Date now = new Date();
		dto.setInviteToken(Util.md5(emailAddress + now.getTime()));
		
		return createInvitation(dto, permissions);
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public InvitationDTO inviteWithAcbAccess(String emailAddress, Long acbId, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException
	{
		InvitationDTO dto = new InvitationDTO();
		dto.setEmail(emailAddress);
		dto.setAcbId(acbId);
		//could be multiple invitations for the same email so add the time to make it unique
		Date currTime = new Date();
		dto.setInviteToken(Util.md5(emailAddress + currTime.getTime()));
	
		return createInvitation(dto, permissions);
	}

	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ATL_ADMIN') and hasPermission(#atlId, 'gov.healthit.chpl.dto.TestingLabDTO', admin))")
	public InvitationDTO inviteWithAtlAccess(String emailAddress, Long atlId, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException
	{
		InvitationDTO dto = new InvitationDTO();
		dto.setEmail(emailAddress);
		dto.setTestingLabId(atlId);
		//could be multiple invitations for the same email so add the time to make it unique
		Date currTime = new Date();
		dto.setInviteToken(Util.md5(emailAddress + currTime.getTime()));
	
		return createInvitation(dto, permissions);
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "("
			+ "hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin) "
			+ " and "
			+ "hasRole('ROLE_ATL_ADMIN') and hasPermission(#atlId, 'gov.healthit.chpl.dto.TestingLabDTO', admin)"
			+ ")")
	public InvitationDTO inviteWithAcbAndAtlAccess(String emailAddress, Long acbId, Long atlId, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException
	{
		InvitationDTO dto = new InvitationDTO();
		dto.setEmail(emailAddress);
		dto.setTestingLabId(atlId);
		dto.setAcbId(acbId);
		//could be multiple invitations for the same email so add the time to make it unique
		Date currTime = new Date();
		dto.setInviteToken(Util.md5(emailAddress + currTime.getTime()));
	
		return createInvitation(dto, permissions);
	}
	
	private InvitationDTO createInvitation(InvitationDTO toCreate, List<String> permissions)  
			throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException
	{
		InvitationDTO createdInvitation = null;
		createdInvitation = invitationDao.create(toCreate);
		
		if(permissions != null && permissions.size() > 0) {
			for(String permission : permissions) {
				if(!permission.startsWith("ROLE_")) {
					permission = "ROLE_ " + permission.trim();
				}
				Long permissionId = userPermissionDao.getIdFromAuthority(permission);
				if(permissionId == null) {
					throw new UserPermissionRetrievalException("Cannot find permission " + permission + ".");
				}
				
				InvitationPermissionDTO permissionToCreate = new InvitationPermissionDTO();
				permissionToCreate.setPermissionId(permissionId);
				permissionToCreate.setPermissionName(permission);
				permissionToCreate.setUserId(createdInvitation.getId());
				InvitationPermissionDTO createdPermission = invitationPermissionDao.create(permissionToCreate);
				//the name does not get saved with the entity so we don't have it anymore
				createdPermission.setPermissionName(permission);
				
				createdInvitation.getPermissions().add(createdPermission);
			}
		}
		return createdInvitation;
	}

	@Override
	@Transactional
	public InvitationDTO getByInvitationHash(String hash) {
		return invitationDao.getByInvitationToken(hash);
	}
	
	@Override
	@Transactional
	public InvitationDTO getByConfirmationHash(String hash) {
		return invitationDao.getByConfirmationToken(hash);
	}
	
	@Override
	@Transactional
	public InvitationDTO getById(Long id) throws UserRetrievalException {
		return invitationDao.getById(id);
	}
	
	@Override
	@Transactional
	public UserDTO createUserFromInvitation(InvitationDTO invitation, UserCreationJSONObject user) 
		throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException, UserCreationException {
		Authentication authenticator = getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
		SecurityContextHolder.getContext().setAuthentication(authenticator);
		
		try {
			//create the user
			UserDTO newUser = userManager.getByName(user.getSubjectName());
			if(newUser == null) {
				newUser = userManager.create(user);
			} else {
				throw new InvalidArgumentsException("A user with the name " + user.getSubjectName() + " already exists.");
			}
			
			handleInvitation(invitation, newUser);
			
			//update invitation entity to change the hashes
			invitation.setCreatedUserId(newUser.getId());
			invitation.setInviteToken(null);
			Date now = new Date();
			invitation.setConfirmToken(Util.md5(invitation.getEmail() + now.getTime()));
			invitationDao.update(invitation);
			return newUser;
		} finally {
			SecurityContextHolder.getContext().setAuthentication(null);
		}
		
	}
	
	@Override
	@Transactional
	public UserDTO confirmAccountEmail(InvitationDTO invitation) throws UserRetrievalException {
		Authentication authenticator = getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
		SecurityContextHolder.getContext().setAuthentication(authenticator);
		
		try {
			//set the user signature date
			UserDTO user = userDao.getById(invitation.getCreatedUserId());
			if(user == null) {
				throw new UserRetrievalException("Could not find user created from invitation. Looking for user with id " + invitation.getCreatedUserId());
			}
			user.setSignatureDate(new Date());
			userDao.update(user);
			
			//delete the invitation and permissions now we are done with them
			for(InvitationPermissionDTO permission : invitation.getPermissions()) {
				invitationPermissionDao.delete(permission.getId());
			}
			invitationDao.delete(invitation.getId());
			return user;
		} finally {
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}
	
	@Override
	@Transactional
	public UserDTO updateUserFromInvitation(InvitationDTO invitation, UserDTO toUpdate) 
		throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException {
		User loggedInUser = gov.healthit.chpl.auth.Util.getCurrentUser();
		
		//have to give temporary permission to see all ACBs and ATLs 
		//because the logged in user wouldn't already have permission on them
		Authentication authenticator = getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
		SecurityContextHolder.getContext().setAuthentication(authenticator);
			
		handleInvitation(invitation, toUpdate);
			
		//delete invitation and permissions, we are done with it
		for(InvitationPermissionDTO permission : invitation.getPermissions()) {
			invitationPermissionDao.delete(permission.getId());
		}
		invitationDao.delete(invitation.getId());
			
		//put the permissions back how they were
		if(loggedInUser == null) {
			SecurityContextHolder.getContext().setAuthentication(null);
		} else {
			SecurityContextHolder.getContext().setAuthentication(loggedInUser);
		}
		
		return toUpdate;
	}
	
	/**
	 * gives the user the permissions listed in the invitation
	 * also adds the user to any ACBs in the invitation
	 * the securitycontext must have a valid authentication specified when this is called
	 * @param invitation
	 * @param user
	 * @throws EntityRetrievalException
	 * @throws InvalidArgumentsException
	 * @throws UserRetrievalException
	 */
	private void handleInvitation(InvitationDTO invitation, UserDTO user) 
			throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException {
		CertificationBodyDTO userAcb = null;
		if(invitation.getAcbId() != null) {
			userAcb = acbManager.getById(invitation.getAcbId());
			if(userAcb == null) {
				throw new InvalidArgumentsException("Could not find ACB with id " + invitation.getAcbId());
			}
		}
		TestingLabDTO userAtl = null;
		if(invitation.getTestingLabId() != null) {
			userAtl = atlManager.getById(invitation.getTestingLabId());
			if(userAtl == null) {
				throw new InvalidArgumentsException("Could not find the testing lab with id " + invitation.getTestingLabId());
			}
		}

		//give them permissions
		if(invitation.getPermissions() != null && invitation.getPermissions().size() > 0) {
			for(InvitationPermissionDTO permission : invitation.getPermissions()) {
				UserPermissionDTO userPermission = userPermissionDao.findById(permission.getPermissionId());
				try {
					if(userPermission.getAuthority().equals("ROLE_ADMIN")) {
						userManager.grantAdmin(user.getName());
					} else {
						userManager.grantRole(user.getName(), userPermission.getAuthority());
					}
				} catch(UserPermissionRetrievalException ex) {
					logger.error("Could not add role " + userPermission.getAuthority() + " for user " + user.getName(), ex);
				} catch(UserManagementException mex) {
					logger.error("Could not add role " + userPermission.getAuthority() + " for user " + user.getName(), mex);
				}
			}
		}
		
		//give them access to the invited acb
		if(userAcb != null) {
			acbManager.addPermission(userAcb, user.getId(), BasePermission.ADMINISTRATION);
		}
		//give them access to the invited atl
		if(userAtl != null) {
			atlManager.addPermission(userAtl, user.getId(), BasePermission.ADMINISTRATION);
		}
	}
	
	private Authentication getInvitedUserAuthenticator(Long id) {
		JWTAuthenticatedUser authenticator = new JWTAuthenticatedUser() {
			
			@Override
			public Long getId() {
				return id == null ? -2L : id;
			}
			
			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
				auths.add(new GrantedPermission("ROLE_INVITED_USER_CREATOR"));
				return auths;
			}

			@Override
			public Object getCredentials(){
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal(){
				return getName();
			}
			
			@Override 
			public String getSubjectName() {
				return this.getName();
			}
			
			@Override
			public boolean isAuthenticated(){
				return true;
			}

			@Override
			public void setAuthenticated(boolean arg0) throws IllegalArgumentException {}
			
			@Override
			public String getName(){
				return "admin";
			}
			
		};
		return authenticator;
	}
	
}
