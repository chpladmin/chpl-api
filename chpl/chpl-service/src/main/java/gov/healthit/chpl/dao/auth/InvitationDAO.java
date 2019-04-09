package gov.healthit.chpl.dao.auth;

import gov.healthit.chpl.dto.auth.InvitationDTO;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface InvitationDAO {
	public InvitationDTO create(InvitationDTO toCreate) throws UserCreationException;
	public InvitationDTO update(InvitationDTO dto)  throws UserRetrievalException;
	public void delete(Long id) throws UserRetrievalException;
	public InvitationDTO getByInvitationToken(String token);
	public InvitationDTO getByConfirmationToken(String token);
	public InvitationDTO getById(Long id) throws UserRetrievalException;
}
