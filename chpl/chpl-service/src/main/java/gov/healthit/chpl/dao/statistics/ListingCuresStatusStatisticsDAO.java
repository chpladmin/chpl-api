package gov.healthit.chpl.dao.statistics;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.statistics.ListingCuresStatusStatisticDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.entity.statistics.ListingCuresStatusStatisticEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;


@Repository("listingCuresStatusStatisticsDAO")
public class ListingCuresStatusStatisticsDAO extends BaseDAOImpl {
    private List<String> activeStatusNames;

    public ListingCuresStatusStatisticsDAO() {
        activeStatusNames = Stream.of(CertificationStatusType.Active.getName(),
                CertificationStatusType.SuspendedByAcb.getName(),
                CertificationStatusType.SuspendedByOnc.getName())
                .collect(Collectors.toList());
    }

    public Long getListingCountWithCuresUpdateStatus() {
        String hql = "SELECT count(listing.id) "
                + "FROM CertifiedProductDetailsEntitySimple listing "
                + "WHERE listing.certificationStatusName IN (:statusNames) "
                + "AND listing.curesUpdate = true "
                + "AND listing.deleted = false ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("statusNames", activeStatusNames);
        return (Long) query.getSingleResult();
    }

    public Long getTotalListingCount() {
        String hql = "SELECT count(listing.id) "
                + "FROM CertifiedProductDetailsEntitySimple listing "
                + "WHERE listing.certificationStatusName IN (:statusNames) "
                + "AND listing.deleted = false ";
        Query query = entityManager.createQuery(hql);
        query.setParameter("statusNames", activeStatusNames);
        return (Long) query.getSingleResult();
    }

    public List<ListingCuresStatusStatisticDTO> findAll() {
        List<ListingCuresStatusStatisticEntity> entities = this.findAllEntities();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public List<ListingCuresStatusStatisticDTO> getStatisticsForDate(LocalDate statisticDate) {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM ListingCuresStatusStatisticEntity stats "
                + "WHERE (stats.deleted = false) "
                + "AND stats.statisticDate = :statisticDate ",
                ListingCuresStatusStatisticEntity.class);
        query.setParameter("statisticDate", statisticDate);
        List<ListingCuresStatusStatisticEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> entity.toDto())
                .collect(Collectors.toList());
    }

    public void delete(Long id) throws EntityRetrievalException {
        ListingCuresStatusStatisticEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
            update(toDelete);
        }
    }

    public void create(ListingCuresStatusStatisticDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        ListingCuresStatusStatisticEntity entity = new ListingCuresStatusStatisticEntity();
        entity.setCuresListingCount(dto.getCuresListingCount());
        entity.setTotalListingCount(dto.getTotalListingCount());
        entity.setStatisticDate(dto.getStatisticDate());
        entity.setLastModifiedUser(getUserId(User.SYSTEM_USER_ID));
        entity.setLastModifiedDate(new Date());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);

        create(entity);
    }

    private List<ListingCuresStatusStatisticEntity> findAllEntities() {
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM ListingCuresStatusStatisticEntity stats "
                + "WHERE (stats.deleted = false)",
                ListingCuresStatusStatisticEntity.class);
        return query.getResultList();
    }

    private ListingCuresStatusStatisticEntity getEntityById(Long id) throws EntityRetrievalException {
        ListingCuresStatusStatisticEntity entity = null;
        Query query = entityManager.createQuery("SELECT stats "
                + "FROM ListingCuresStatusStatisticEntity stats "
                + "WHERE (stats.deleted = false) "
                + "AND (stats.id = :id)",
                ListingCuresStatusStatisticEntity.class);
        query.setParameter("id", id);
        List<ListingCuresStatusStatisticEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        } else {
            throw new EntityRetrievalException("Data error. Did not find only one entity.");
        }
        return entity;
    }
}
