package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.PendingCertifiedProductSystemUpdateDTO;

public interface PendingCertifiedProductSystemUpdateDAO {
	PendingCertifiedProductSystemUpdateDTO create(PendingCertifiedProductSystemUpdateDTO acb) throws EntityRetrievalException, EntityCreationException;
}
