package gov.healthit.chpl.manager;


import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

import java.util.List;

public interface InvitationManager {
			
	
	public InvitationDTO inviteAdmin(String emailAddress, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;
	public InvitationDTO inviteWithRolesOnly(String emailAddress, List<String> permissions) 
			throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;
	public InvitationDTO inviteWithAcbAccess(String emailAddress, Long acbId, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;
	public InvitationDTO inviteWithAtlAccess(String emailAddress, Long atlId, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;
	public InvitationDTO inviteWithAcbAndAtlAccess(String emailAddress, Long acbId, Long atlId, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

	public InvitationDTO getByInvitationHash(String hash);
	public InvitationDTO getById(Long id)  throws UserRetrievalException;
	public InvitationDTO getByConfirmationHash(String hash);
	public UserDTO createUserFromInvitation(InvitationDTO invitation, UserCreationJSONObject user) 
			throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException,
			UserCreationException;
	public UserDTO confirmAccountEmail(InvitationDTO invitation) throws UserRetrievalException;
	public UserDTO updateUserFromInvitation(InvitationDTO invitation, UserDTO user) 
			throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException;
}
