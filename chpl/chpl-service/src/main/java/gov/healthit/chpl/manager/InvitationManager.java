package gov.healthit.chpl.manager;


import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.json.UserInfoJSONObject;
import gov.healthit.chpl.auth.json.UserInvitation;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

import java.util.List;
import java.util.Set;

public interface InvitationManager {
			
	
	public InvitationDTO inviteAdmin(String emailAddress, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;
	
	public InvitationDTO inviteWithAcbAccess(String emailAddress, Long acbId, List<String> permissions) throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

	public boolean isHashValid(String hash);
	public InvitationDTO getByHash(String hash);
	public UserDTO createUserFromInvitation(InvitationDTO invitation, UserCreationJSONObject user) 
			throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException,
			UserCreationException;
	public UserDTO updateUserFromInvitation(InvitationDTO invitation, UserDTO user) 
			throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException;
}
