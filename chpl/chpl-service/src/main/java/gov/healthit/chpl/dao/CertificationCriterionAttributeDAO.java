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
        return getCertificationCriteriaAttributeEntityForSvap().stream()
                .map(cca -> new CertificationCriterion(new CertificationCriterionDTO(cca.getCriterion())))
                .collect(Collectors.toList());
    }

    private List<CertificationCriterionAttributeEntity> getCertificationCriteriaAttributeEntityForSvap() {
        return entityManager
                .createQuery("SELECT cca "
                        + "FROM CertificationCriterionAttributeEntity cca "
                        + "JOIN FETCH cca.criterion "
                        + "WHERE cca.svap = true "
                        + "AND cca.deleted = false", CertificationCriterionAttributeEntity.class)
                .getResultList();
    }

    @Transactional
    public List<CertificationCriterion> getCriteriaForServiceBaseUrlList() {
        return getCertificationCriteriaAttributeEntityForServiceBaseUrlList().stream()
                .map(cca -> new CertificationCriterion(new CertificationCriterionDTO(cca.getCriterion())))
                .collect(Collectors.toList());
    }

    private List<CertificationCriterionAttributeEntity> getCertificationCriteriaAttributeEntityForServiceBaseUrlList() {
        return entityManager
                .createQuery("SELECT cca "
                        + "FROM CertificationCriterionAttributeEntity cca "
                        + "JOIN FETCH cca.criterion "
                        + "WHERE cca.serviceBaseUrlList = true "
                        + "AND cca.deleted = false", CertificationCriterionAttributeEntity.class)
                .getResultList();
    }
}
