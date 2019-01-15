package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.json.UserCreationJSONObject;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;

public interface InvitationManager {

    InvitationDTO inviteAdmin(String emailAddress, List<String> permissions)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteOnc(String emailAddress, List<String> permissions)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteWithRolesOnly(String emailAddress, List<String> permissions)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteWithAcbAccess(String emailAddress, Long acbId, List<String> permissions)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteWithAtlAccess(String emailAddress, Long atlId, List<String> permissions)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteWithAcbAndAtlAccess(String emailAddress, Long acbId, Long atlId,
            List<String> permissions)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO getByInvitationHash(String hash);

    InvitationDTO getById(Long id) throws UserRetrievalException;

    InvitationDTO getByConfirmationHash(String hash);

    UserDTO createUserFromInvitation(InvitationDTO invitation, UserCreationJSONObject user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException, UserCreationException;

    UserDTO confirmAccountEmail(InvitationDTO invitation) throws UserRetrievalException;

    UserDTO updateUserFromInvitation(InvitationDTO invitation, UserDTO user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException;
}
