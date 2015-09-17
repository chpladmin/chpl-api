package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;

public interface PendingCertifiedProductDao {
	
	public PendingCertifiedProductDTO create(PendingCertifiedProductEntity product) throws EntityCreationException;
}
