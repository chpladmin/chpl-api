package gov.healthit.chpl.dao.statistics;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.compliance.surveillance.entity.NonconformityTypeEntity;
import gov.healthit.chpl.compliance.surveillance.entity.NonconformityTypeStatisticsEntity;
import gov.healthit.chpl.compliance.surveillance.entity.SurveillanceNonconformityEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.service.CertificationCriterionService;

@Repository("nonconformityTypeStatisticsDAO")
public class NonconformityTypeStatisticsDAO extends BaseDAOImpl {

    private CertificationCriterionService certificationCriterionService;

    @Autowired
    public NonconformityTypeStatisticsDAO(CertificationCriterionService certificationCriterionService) {
        this.certificationCriterionService = certificationCriterionService;
    }

    public List<NonconformityTypeStatisticsDTO> getAllNonconformityStatistics() {
        String hql = "SELECT data "
                + "FROM NonconformityTypeStatisticsEntity data "
                + "LEFT OUTER JOIN FETCH data.certificationCriterionEntity cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "LEFT JOIN FETCH cce.rule "
                + "WHERE data.deleted = false";
        Query query = entityManager.createQuery(hql, NonconformityTypeStatisticsEntity.class);

        List<NonconformityTypeStatisticsEntity> entities = query.getResultList();

        List<NonconformityTypeStatisticsDTO> dtos = new ArrayList<NonconformityTypeStatisticsDTO>();
        for (NonconformityTypeStatisticsEntity entity : entities) {
            NonconformityTypeStatisticsDTO dto = new NonconformityTypeStatisticsDTO(entity);
            dtos.add(dto);
        }

        return dtos;
    }


    @Transactional
    public void create(NonconformityTypeStatisticsDTO dto) {
        NonconformityTypeStatisticsEntity entity = new NonconformityTypeStatisticsEntity();
        entity.setNonconformityCount(dto.getNonconformityCount());
        entity.setNonconformityType(dto.getNonconformityType());
        if (dto.getCriterion() != null) {
            entity.setCertificationCriterionId(dto.getCriterion().getId());
        }

        if (dto.getLastModifiedUser() == null) {
            entity.setLastModifiedUser(-2L);
        } else {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        }

        if (dto.getDeleted() == null) {
            entity.setDeleted(false);
        } else {
            entity.setDeleted(dto.getDeleted());
        }
        entityManager.persist(entity);
        entityManager.flush();
    }


    /**
     * Examine nonconformities to get a count of how many of each type of NC there
     * are.
     *
     * @return a list of the DTOs that hold the counts
     */
    public List<NonconformityTypeStatisticsDTO> getAllNonconformitiesByCriterion() {
        List<SurveillanceNonconformityEntity> allNonconformities = entityManager.createQuery(
                "SELECT sne "
                + "FROM SurveillanceNonconformityEntity sne "
                + "JOIN FETCH sne.type ncType "
                + "WHERE sne.deleted = false ", SurveillanceNonconformityEntity.class)
                .getResultList();

        List<NonconformityTypeEntity> nonconformityTypes = entityManager.createQuery(
                "FROM NonconformityTypeEntity e ", NonconformityTypeEntity.class)
                .getResultList();

        return nonconformityTypes.stream()
            .map(ncType ->
                NonconformityTypeStatisticsDTO.builder()
                        .nonconformityCount(allNonconformities.stream()
                                .filter(nc -> nc.getType().getId().equals(ncType.getId()))
                                .count())
                        .nonconformityType(ncType.getClassification().equals(NonconformityClassification.REQUIREMENT.toString())
                                ? ncType.getTitle()
                                : null)
                        .criterion(ncType.getClassification().equals(NonconformityClassification.CRITERION.toString())
                                ?  certificationCriterionService.get(ncType.getId())
                                : null)
                        .build())
            .filter(dto -> !dto.getNonconformityCount().equals(0L))
            .toList();
    }

    @Transactional
    public void deleteAllOldNonConformityStatistics() throws EntityRetrievalException {
        String hql = "UPDATE NonconformityTypeStatisticsEntity SET deleted = true WHERE deleted = false";
        Query query = entityManager.createQuery(hql);
        query.executeUpdate();
    }
}
