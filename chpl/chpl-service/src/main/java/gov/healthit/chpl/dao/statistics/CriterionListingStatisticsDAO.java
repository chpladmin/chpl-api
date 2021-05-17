package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.statistics.CriterionListingCountStatisticDTO;
import gov.healthit.chpl.entity.statistics.CriterionListingCountStatisticEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;


@Repository("criterionListingStatisticsDAO")
public class CriterionListingStatisticsDAO extends BaseDAOImpl {

    public Integer getListingCountForCriterion(Long certificationCriterionId) {
        String hql = "SELECT count(*) "
                + "FROM CertificationResultEntity cre "
                + "WHERE cre.certification_criterion_id = :criterionId "
                + "AND cre.deleted = false "
                + "AND cre.success = true "
                + "GROUP BY cre.certifiedProductId";
        Query query = entityManager.createQuery(hql);
        query.setParameter("criterionId", certificationCriterionId);
        return (Integer) query.getSingleResult();
    }

    public List<CriterionListingCountStatisticDTO> findAll() {
        List<CriterionListingCountStatisticEntity> entities = this.findAllEntities();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public List<CriterionListingCountStatisticDTO> getStatisticsForDate(LocalDate statisticDate) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CriterionListingCountStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (stats.deleted = false) "
                + "AND stats.statisticDate = :statisticDate ",
                CriterionListingCountStatisticEntity.class);
        query.setParameter("statisticDate", statisticDate);
        List<CriterionListingCountStatisticEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public void delete(Long id) throws EntityRetrievalException {
        CriterionListingCountStatisticEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            update(toDelete);
        }
    }

    public void create(CriterionListingCountStatisticDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        CriterionListingCountStatisticEntity entity = new CriterionListingCountStatisticEntity();
        entity.setListingCount(dto.getListingsCertifyingToCriterionCount());
        entity.setCertificationCriterionId(dto.getCriterion().getId());
        entity.setStatisticDate(dto.getStatisticDate());
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);

        create(entity);
    }

    private List<CriterionListingCountStatisticEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CriterionListingCountStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (stats.deleted = false)",
                CriterionListingCountStatisticEntity.class);
        return query.getResultList();
    }

    private CriterionListingCountStatisticEntity getEntityById(Long id) throws EntityRetrievalException {
        CriterionListingCountStatisticEntity entity = null;
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CriterionListingCountStatisticEntity stats "
                + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
                + "LEFT OUTER JOIN FETCH cce.certificationEdition "
                + "WHERE (stats.deleted = false) "
                + "AND (stats.id = :id)",
                CriterionListingCountStatisticEntity.class);
        query.setParameter("id", id);
        List<CriterionListingCountStatisticEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }
        return entity;
    }
}
