package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;

public interface PendingCertifiedProductDao {
	
	public PendingCertifiedProductDTO create(PendingCertifiedProductEntity product) throws EntityCreationException;
	public List<PendingCertifiedProductDTO> findAll();
	public PendingCertifiedProductDTO findById(Long pcpId) throws EntityRetrievalException;
	public List<PendingCertifiedProductDTO> findByAcbId(Long acbId);
}
