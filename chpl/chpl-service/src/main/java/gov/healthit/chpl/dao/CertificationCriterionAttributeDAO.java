package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.CertificationCriterionAttributeEntity;

@Repository
public class CertificationCriterionAttributeDAO extends BaseDAOImpl {
    public List<CertificationCriterion> getCriteriaForSvap() {
        return getAllCriteriaAttributeEntities().stream()
                .filter(att -> att.getSvap())
                .map(cca -> new CertificationCriterion(new CertificationCriterionDTO(cca.getCriterion())))
                .collect(Collectors.toList());
    }

    public List<CertificationCriterion> getCriteriaForTestTools() {
        return getAllCriteriaAttributeEntities().stream()
                .filter(att -> att.getTestTool())
                .map(cca -> new CertificationCriterion(new CertificationCriterionDTO(cca.getCriterion())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CertificationCriterionAttributeEntity> getAllCriteriaAttributes() {
        return getAllCriteriaAttributeEntities().stream()
                .collect(Collectors.toList());
    }

    private List<CertificationCriterionAttributeEntity> getAllCriteriaAttributeEntities() {
        return entityManager
                .createQuery("SELECT cca "
                        + "FROM CertificationCriterionAttributeEntity cca "
                        + "JOIN FETCH cca.criterion "
                        + "WHERE cca.deleted = false", CertificationCriterionAttributeEntity.class)
                .getResultList();
    }
}
