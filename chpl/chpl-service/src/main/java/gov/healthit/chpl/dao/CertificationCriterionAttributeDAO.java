package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.CertificationCriterionAttributeEntity;

@Repository
public class CertificationCriterionAttributeDAO extends BaseDAOImpl {
    public List<CertificationCriterion> getCriteriaForSvap() {
        return getAllCriteriaAttributeEntities().stream()
                .filter(att -> att.getSvap())
                .map(cca -> cca.getCriterion().toDomain())
                .collect(Collectors.toList());
    }

    public List<CertificationCriterion> getCriteriaForTestTools() {
        return getAllCriteriaAttributeEntities().stream()
                .filter(att -> att.getTestTool())
                .map(cca -> cca.getCriterion().toDomain())
                .collect(Collectors.toList());
    }

    public List<CertificationCriterion> getCriteriaForFunctionalitiesTested() {
        return getAllCriteriaAttributeEntities().stream()
                .filter(att -> att.getFunctionalityTested())
                .map(cca -> cca.getCriterion().toDomain())
                .collect(Collectors.toList());
    }

    public List<CertificationCriterion> getCriteriaForStandards() {
        return getAllCriteriaAttributeEntities().stream()
                .filter(att -> att.getStandard())
                .map(cca -> cca.getCriterion().toDomain())
                .collect(Collectors.toList());
    }

    public List<CertificationCriterion> getCriteriaForCodeSets() {
        return getAllCriteriaAttributeEntities().stream()
                .filter(att -> att.getCodeSet())
                .map(cca -> cca.getCriterion().toDomain())
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
                        + "JOIN FETCH cca.criterion c "
                        + "LEFT JOIN FETCH c.certificationEdition "
                        + "LEFT JOIN FETCH c.rule "
                        + "WHERE cca.deleted = false", CertificationCriterionAttributeEntity.class)
                .getResultList();
    }
}
