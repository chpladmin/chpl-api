package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CQMResultCriteriaDTO;
import gov.healthit.chpl.dto.CQMResultDTO;

public interface CQMResultDAO {

    CQMResultDTO create(CQMResultDTO cqmResult) throws EntityCreationException;

    CQMResultCriteriaDTO createCriteriaMapping(CQMResultCriteriaDTO criteria);

    void delete(Long cqmResultId);

    void deleteByCertifiedProductId(Long productId);

    void deleteCriteriaMapping(Long mappingId);

    void deleteMappingsForCqmResult(Long cqmResultId);

    List<CQMResultDTO> findAll();

    List<CQMResultDTO> findByCertifiedProductId(Long certifiedProductId);

    CQMResultDTO getById(Long cqmResultId) throws EntityRetrievalException;

    List<CQMResultCriteriaDTO> getCriteriaForCqmResult(Long cqmResultId);

    void update(CQMResultDTO cqmResult) throws EntityRetrievalException;

    CQMResultCriteriaDTO updateCriteriaMapping(CQMResultCriteriaDTO dto);

}
