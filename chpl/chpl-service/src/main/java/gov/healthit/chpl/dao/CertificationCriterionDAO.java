package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationCriterionDAO {

    CertificationCriterionDTO create(CertificationCriterionDTO result)
            throws EntityCreationException, EntityRetrievalException;

    CertificationCriterionDTO update(CertificationCriterionDTO result)
            throws EntityRetrievalException, EntityCreationException;

    void delete(Long criterionId);

    List<CertificationCriterionDTO> findAll();

    List<CertificationCriterionDTO> findByCertificationEditionYear(String year);

    CertificationCriterionDTO getById(Long criterionId) throws EntityRetrievalException;

    CertificationCriterionDTO getByName(String criterionName);

    CertificationCriterionDTO getByNameAndYear(String criterionName, String year);

    CertificationCriterionDTO getByNumberAndTitle(String number, String title);

    CertificationCriterionEntity getEntityByName(String name);

    CertificationCriterionEntity getEntityById(Long id) throws EntityRetrievalException;

}
