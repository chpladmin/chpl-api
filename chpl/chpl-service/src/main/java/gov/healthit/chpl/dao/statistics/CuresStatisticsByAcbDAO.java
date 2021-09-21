package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.statistics.CuresStatisticsByAcb;
import gov.healthit.chpl.entity.statistics.CuresStatisticsByAcbEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Repository("curesStatisticsByAcbDAO")
@Log4j2
public class CuresStatisticsByAcbDAO extends BaseDAOImpl {
//    private List<String> activeStatusNames;

    public CuresStatisticsByAcbDAO() {
//        activeStatusNames = Stream.of(CertificationStatusType.Active.getName(),
//                CertificationStatusType.SuspendedByAcb.getName(),
//                CertificationStatusType.SuspendedByOnc.getName())
//                .collect(Collectors.toList());
    }

//        public Long getListingCountForCriterion(Long certificationCriterionId) {
//            String hql = "SELECT count(distinct listing.id) "
//                    + "FROM CertifiedProductDetailsEntitySimple listing, CertificationResultEntity cre "
//                    + "WHERE listing.id = cre.certifiedProductId "
//                    + "AND listing.certificationStatusName IN (:statusNames) "
//                    + "AND cre.certificationCriterionId = :criterionId "
//                    + "AND cre.success = true "
//                    + "AND cre.deleted = false "
//                    + "AND listing.deleted = false ";
//            Query query = entityManager.createQuery(hql);
//            query.setParameter("statusNames", activeStatusNames);
//            query.setParameter("criterionId", certificationCriterionId);
//            Long result = 0L;
//            try {
//                result = (Long) query.getSingleResult();
//            } catch (NoResultException ex) {
//                LOGGER.debug("0 active listings attest to criterion ID " + certificationCriterionId);
//            }
//            return result;
//        }

//        public List<CriterionListingCountStatistic> findAll() {
//            List<CriterionListingCountStatisticEntity> entities = this.findAllEntities();
//            return entities.stream()
//                    .map(entity -> entity.toDomain())
//                    .collect(Collectors.toList());
//        }


    public LocalDate getDateOfMostRecentStatistics() {
        LocalDate result = null;
        Query query = entityManager.createQuery("SELECT max(statisticDate) "
                + "FROM CuresStatisticsByAcbEntity stats "
                + "WHERE (stats.deleted = false) ",
                LocalDate.class);
        Object queryResult = query.getSingleResult();
        if (queryResult instanceof LocalDate) {
            result = (LocalDate) queryResult;
        }
        return result;
    }

    public void delete(Long id) throws EntityRetrievalException {
        CuresStatisticsByAcbEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            update(toDelete);
        }
    }

    public List<CuresStatisticsByAcb> getStatisticsForDate(LocalDate statisticDate) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CuresStatisticsByAcbEntity stats "
                + "JOIN FETCH stats.certificationBody cb "
                + "LEFT OUTER JOIN FETCH stats.originalCriterion oc "
                + "LEFT OUTER JOIN FETCH stats.curesCriterion cc "
                + "WHERE (stats.deleted = false) "
                + "AND stats.statisticDate = :statisticDate ",
                CuresStatisticsByAcbEntity.class);
        query.setParameter("statisticDate", statisticDate);
        List<CuresStatisticsByAcbEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> new CuresStatisticsByAcb(entity))
                .collect(Collectors.toList());
    }

    public void create(List<CuresStatisticsByAcb> domains) {
        domains.stream()
                .forEach(domain -> create(domain));

    }

    public void create(CuresStatisticsByAcb domain) {
        CuresStatisticsByAcbEntity entity = new CuresStatisticsByAcbEntity(domain);
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);

        create(entity);
    }

//        private List<CriterionListingCountStatisticEntity> findAllEntities() {
//            Query query = entityManager.createQuery("SELECT stats "
//                    + "FROM CriterionListingCountStatisticEntity stats "
//                    + "LEFT OUTER JOIN FETCH stats.certificationCriterion cce "
//                    + "LEFT OUTER JOIN FETCH cce.certificationEdition "
//                    + "WHERE (stats.deleted = false)",
//                    CriterionListingCountStatisticEntity.class);
//            return query.getResultList();
//        }

    private CuresStatisticsByAcbEntity getEntityById(Long id) throws EntityRetrievalException {
        CuresStatisticsByAcbEntity entity = null;
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM CuresStatisticsByAcbEntity stats "
                + "JOIN FETCH stats.certificationBody cb "
                + "LEFT OUTER JOIN FETCH stats.originalCriterion oc "
                + "LEFT OUTER JOIN FETCH stats.curesCriterion cc "
                + "WHERE stats.deleted = false "
                + "AND stats.id = :id",
                CuresStatisticsByAcbEntity.class);
        query.setParameter("id", id);
        List<CuresStatisticsByAcbEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }
        return entity;
    }

}
