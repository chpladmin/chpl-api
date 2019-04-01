package gov.healthit.chpl.auth.dao;

import gov.healthit.chpl.auth.dto.InvitationPermissionDTO;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

public interface InvitationPermissionDAO {
	public InvitationPermissionDTO create(InvitationPermissionDTO toCreate) throws UserCreationException;
	public void delete(Long id) throws UserRetrievalException;
}
