package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CQMCriterionDTO;
import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CQMCriterionDAO {

    CQMCriterionDTO create(CQMCriterionDTO criterion) throws EntityCreationException, EntityRetrievalException;

    void update(CQMCriterionDTO criterion) throws EntityRetrievalException, EntityCreationException;

    void delete(Long criterionId);

    List<CQMCriterionDTO> findAll();

    CQMCriterionDTO getById(Long criterionId) throws EntityRetrievalException;

    CQMCriterionDTO getCMSByNumber(String number);

    CQMCriterionDTO getNQFByNumber(String number);

    CQMCriterionDTO getCMSByNumberAndVersion(String number, String version);

    CQMCriterionEntity getCMSEntityByNumberAndVersion(String number, String version);

    CQMCriterionEntity getCMSEntityByNumber(String number);

    CQMCriterionEntity getNQFEntityByNumber(String number);
}
