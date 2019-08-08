package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.auth.CreateUserRequest;
import gov.healthit.chpl.dto.auth.InvitationDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserInvitationDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface InvitationManager {

    InvitationDTO inviteAdmin(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteOnc(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteCms(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteWithAcbAccess(String emailAddress, Long acbId)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteWithAtlAccess(String emailAddress, Long atlId)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

    InvitationDTO inviteWithDeveloperAccess(String emailAddress, Long developerId)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException;

   InvitationDTO getByInvitationHash(String hash);

    InvitationDTO getById(Long id) throws UserRetrievalException;

    InvitationDTO getByConfirmationHash(String hash);

    UserDTO createUserFromInvitation(InvitationDTO invitation, CreateUserRequest user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException, UserCreationException;

    UserDTO confirmAccountEmail(InvitationDTO invitation) throws UserRetrievalException;

    UserDTO updateUserFromInvitation(UserInvitationDTO userInvitation)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException;
}
