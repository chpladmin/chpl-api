package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.PendingCertifiedProductEntity;

public interface PendingCertifiedProductDAO {
	
	public PendingCertifiedProductDTO create(PendingCertifiedProductEntity product) throws EntityCreationException;
	public void delete(Long pendingProductId) throws EntityRetrievalException;
	public List<PendingCertifiedProductDTO> findAll();
	public List<PendingCertifiedProductDTO> findByStatus(Long statusId);
	public Long findIdByOncId(String id) throws EntityRetrievalException;
	public PendingCertifiedProductDTO findById(Long pcpId, boolean includeDeleted) throws EntityRetrievalException;
	public List<PendingCertifiedProductDTO> findByAcbId(Long acbId);
}
