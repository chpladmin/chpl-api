package gov.healthit.chpl.criteriaattribute;

import java.util.List;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CriteriaAttributeDAO {

    List<CriteriaAttributeCriteriaMap> getAllAssociatedCriteriaMaps() throws EntityRetrievalException;
    CriteriaAttribute getCriteriaAttributeById(Long id);
    List<CertifiedProductDetailsDTO> getCertifiedProductsByCriteriaAttributeAndCriteria(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion) throws EntityRetrievalException;
    void update(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException;
    void addCriteriaAttributeCriteriaMap(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion);
    void removeTestToolCriteriaMap(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion);
}
