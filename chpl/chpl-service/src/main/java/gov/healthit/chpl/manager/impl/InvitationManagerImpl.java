package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.dao.InvitationDAO;
import gov.healthit.chpl.auth.dao.InvitationPermissionDAO;
import gov.healthit.chpl.auth.dao.UserPermissionDAO;
import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.dto.InvitationPermissionDTO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.entity.InvitationPermissionEntity;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInvitation;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationBodyPermission;
import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.InvitationManager;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;
import gov.healthit.chpl.web.controller.UserManagementController;

@Service
public class InvitationManagerImpl implements InvitationManager {
	
	@Autowired
	private UserPermissionDAO userPermissionDao;
	
	@Autowired
	private InvitationDAO invitationDao;
	
	@Autowired
	private InvitationPermissionDAO invitationPermissionDao;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired private UserManager userManager;
	@Autowired private CertificationBodyManager acbManager;
	
	private static final Logger logger = LogManager.getLogger(InvitationManagerImpl.class);

	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public InvitationDTO inviteAdmin(String emailAddress, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException
	{
		InvitationDTO createdInvitation = null;
		
		InvitationDTO dto = new InvitationDTO();
		dto.setEmail(emailAddress);
		dto.setToken(bCryptPasswordEncoder.encode(emailAddress));
	
		createdInvitation = invitationDao.create(dto);
		
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
	@PreAuthorize("hasRole('ROLE_ADMIN') or "
			+ "(hasRole('ROLE_ACB_ADMIN') and hasPermission(#acbId, 'gov.healthit.chpl.dto.CertificationBodyDTO', admin))")
	public InvitationDTO inviteWithAcbAccess(String emailAddress, Long acbId, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException
	{
		InvitationDTO createdInvitation = null;
		
		InvitationDTO dto = new InvitationDTO();
		dto.setEmail(emailAddress);
		dto.setAcbId(acbId);
		//could be multiple invitations for the same email so add the time to make it unique
		Date currTime = new Date();
		dto.setToken(bCryptPasswordEncoder.encode(emailAddress + currTime.getTime()));
	
		createdInvitation = invitationDao.create(dto);
		
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
	public boolean isHashValid(String hash) {
		InvitationDTO invitation = invitationDao.getByToken(hash);
		if(invitation == null) { 
			return false;
		}
		
		if(invitation.isExpired()) {
			return false;
		}
		return true;
	}
	
	@Override
	@Transactional
	public InvitationDTO getByHash(String hash) {
		return invitationDao.getByToken(hash);
	}
	
	@Override
	@Transactional
	public UserDTO createUserFromInvitation(InvitationDTO invitation, UserCreationJSONObject user) 
		throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException, UserCreationException {
		Authentication authenticator = getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
		SecurityContextHolder.getContext().setAuthentication(authenticator);
		
		//create the user
		UserDTO newUser = userManager.getByName(user.getSubjectName());
		if(newUser == null) {
			newUser = userManager.create(user);
		} else {
			throw new InvalidArgumentsException("A user with the name " + user.getSubjectName() + " already exists.");
		}
		
		handleInvitation(invitation, newUser);

		return newUser;
	}
	
	@Override
	@Transactional
	public UserDTO updateUserFromInvitation(InvitationDTO invitation, UserDTO toUpdate) 
		throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException {
		Authentication authenticator = getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
		SecurityContextHolder.getContext().setAuthentication(authenticator);
		
		handleInvitation(invitation, toUpdate);
		
		SecurityContextHolder.getContext().setAuthentication(null);
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
		
		//give them permissions
		boolean isChplAdmin = false;
		if(invitation.getPermissions() != null && invitation.getPermissions().size() > 0) {
			for(InvitationPermissionDTO permission : invitation.getPermissions()) {
				UserPermissionDTO userPermission = userPermissionDao.findById(permission.getPermissionId());
				try {
					if(userPermission.getAuthority().equals("ROLE_ADMIN")) {
						userManager.grantAdmin(user.getName());
						isChplAdmin = true;
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
			
		//give them roles on the appropriate ACBs
		//if they are a chpladmin then they need to be given access to all of the ACBs
		if(isChplAdmin) {
			List<CertificationBodyDTO> acbs = acbManager.getAll();
			for(CertificationBodyDTO acb : acbs) {
				acbManager.addPermission(acb, user.getId(), BasePermission.ADMINISTRATION);
			}
		} else if(userAcb != null) {
			acbManager.addPermission(userAcb, user.getId(), BasePermission.ADMINISTRATION);
		}
		
		//delete the permissions
		for(InvitationPermissionDTO permission : invitation.getPermissions()) {
			invitationPermissionDao.delete(permission.getId());
		}
		//delete the invitation
		invitationDao.delete(invitation.getId());
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
