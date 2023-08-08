package gov.healthit.chpl.criteriaattribute;

import java.util.List;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CriteriaAttributeDAO {

    CriteriaAttribute getCriteriaAttributeById(Long id);

    void remove(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException;
    CriteriaAttribute add(CriteriaAttribute criteriaAttribute);
    void update(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException;

    List<CriteriaAttributeCriteriaMap> getAllAssociatedCriteriaMaps() throws EntityRetrievalException;
    void addCriteriaAttributeCriteriaMap(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion);
    void removeCriteriaAttributeCriteriaMap(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion);

    List<CertifiedProductDetailsDTO> getCertifiedProductsByCriteriaAttribute(CriteriaAttribute criteriaAttribute) throws EntityRetrievalException;
    List<CertifiedProductDetailsDTO> getCertifiedProductsByCriteriaAttributeAndCriteria(CriteriaAttribute criteriaAttribute, CertificationCriterion criterion) throws EntityRetrievalException;
}
