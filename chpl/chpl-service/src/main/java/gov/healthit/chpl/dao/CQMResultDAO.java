package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CQMResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.dto.CQMResultDTO;

public interface CQMResultDAO {
	
	public void create(CQMResultDTO cqmResult) throws EntityCreationException;
	public void delete(Long cqmResultId);
	public void deleteByCertifiedProductId(Long productId);
	public List<CQMResultDTO> findAll();
	public List<CQMResultDTO> findByCertifiedProductId(Long certifiedProductId);
	public CQMResultDTO getById(Long cqmResultId) throws EntityRetrievalException;
	public void update(CQMResultDTO cqmResult) throws EntityRetrievalException;

}
