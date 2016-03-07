package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDTO;

public interface CQMResultDAO {
	
	public void create(CQMResultDTO cqmResult) throws EntityCreationException;
	public CQMResultCriteriaDTO createCriteriaMapping(CQMResultCriteriaDTO criteria);
	public void delete(Long cqmResultId);
	public void deleteByCertifiedProductId(Long productId);
	public void deleteCriteriaMapping(Long mappingId);
	public void deleteMappingsForCqmResult(Long cqmResultId);
	public List<CQMResultDTO> findAll();
	public List<CQMResultDTO> findByCertifiedProductId(Long certifiedProductId);
	public CQMResultDTO getById(Long cqmResultId) throws EntityRetrievalException;
	public List<CQMResultCriteriaDTO> getCriteriaForCqmResult(Long cqmResultId);
	public void update(CQMResultDTO cqmResult) throws EntityRetrievalException;
	public CQMResultCriteriaDTO updateCriteriaMapping(CQMResultCriteriaDTO dto);

}
