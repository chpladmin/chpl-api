package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.domain.auth.CreateUserRequest;
import gov.healthit.chpl.dto.auth.InvitationDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

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

    UserDTO createUserFromInvitation(InvitationDTO invitation, CreateUserRequest user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException, UserCreationException;

    UserDTO confirmAccountEmail(InvitationDTO invitation) throws UserRetrievalException;

    UserDTO updateUserFromInvitation(InvitationDTO invitation, UserDTO user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException;
}
