package gov.healthit.chpl.dao;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.PromotingInteroperabilityUser;
import gov.healthit.chpl.entity.PromotingInteroperabilityUserEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("promotingInteroperabilityUserDao")
public class PromotingInteroperabilityUserDAO extends BaseDAOImpl {

    @Transactional
    public void create(Long listingId, PromotingInteroperabilityUser toCreate)
            throws EntityCreationException {
        PromotingInteroperabilityUserEntity entity = new PromotingInteroperabilityUserEntity();
        entity.setListingId(listingId);
        entity.setUserCount(toCreate.getUserCount());
        entity.setUserCountDate(toCreate.getUserCountDate());
        entity.setDeleted(false);
        create(entity);
    }

    public void update(PromotingInteroperabilityUser toUpdate) throws EntityRetrievalException {
        PromotingInteroperabilityUserEntity entity = getEntityById(toUpdate.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Promoting Interoperability User entry with id " + toUpdate.getId() + " does not exist");
        }
        entity.setUserCount(toUpdate.getUserCount());
        entity.setUserCountDate(toUpdate.getUserCountDate());
        update(entity);
    }

    public void delete(Long id) throws EntityRetrievalException {
        PromotingInteroperabilityUserEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            update(toDelete);
        }
    }

    public PromotingInteroperabilityUser getById(Long id) throws EntityRetrievalException {
        PromotingInteroperabilityUserEntity entity = getEntityById(id);
        if (entity == null) {
            throw new EntityRetrievalException("Promoting Interoperability User entry with id " + id + " does not exist");
        }
        return entity.toDomain();
    }

    public List<PromotingInteroperabilityUser> findByListingId(Long listingId) {
        List<PromotingInteroperabilityUserEntity> entities = getEntitiesByListingId(listingId);
        return entities.stream().map(entity -> entity.toDomain()).collect(Collectors.toList());
    }

    private PromotingInteroperabilityUserEntity getEntityById(Long id) throws EntityRetrievalException {
        Query query = entityManager.createQuery("SELECT piuEntity "
                + "FROM PromotingInteroperabilityUserEntity piuEntity "
                + "WHERE piuEntity.id = :id "
                + "AND (NOT piuEntity.deleted = true)",
                PromotingInteroperabilityUserEntity.class);
        query.setParameter("id", id);

        PromotingInteroperabilityUserEntity entity = null;
        List<PromotingInteroperabilityUserEntity> result = query.getResultList();
        if (result != null && result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<PromotingInteroperabilityUserEntity> getEntitiesByListingId(Long listingId) {
        Query query = entityManager.createQuery("SELECT piuEntity "
                + "FROM PromotingInteroperabilityUserEntity piuEntity "
                + "WHERE piuEntity.listingId = :listingId "
                + "AND (NOT piuEntity.deleted = true)",
                PromotingInteroperabilityUserEntity.class);
        query.setParameter("listingId", listingId);
        return query.getResultList();
    }
}
