package gov.healthit.chpl.auth.dao;

import gov.healthit.chpl.auth.dto.InvitationDTO;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

public interface InvitationDAO {
	public InvitationDTO create(InvitationDTO toCreate) throws UserCreationException;
	public void delete(Long id) throws UserRetrievalException;
	public InvitationDTO getByToken(String token);
}
