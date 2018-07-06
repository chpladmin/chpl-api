package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ListingCountStatisticsDAO;
import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
import gov.healthit.chpl.entity.ListingCountStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for ListingCountStatisticsDAO.
 * @author alarned
 *
 */
@Repository("listingCountStatisticsDAO")
public class ListingCountStatisticsDAOImpl extends BaseDAOImpl implements ListingCountStatisticsDAO {
    private static final long MODIFIED_USER_ID = -3L;

    @Override
    public List<ListingCountStatisticsDTO> findAll() {
        List<ListingCountStatisticsEntity> result = this.findAllEntities();
        List<ListingCountStatisticsDTO> dtos = new ArrayList<ListingCountStatisticsDTO>(result.size());
        for (ListingCountStatisticsEntity entity : result) {
            dtos.add(new ListingCountStatisticsDTO(entity));
        }
        return dtos;
    }

    @Override
    public void delete(final Long id) throws EntityRetrievalException {
        ListingCountStatisticsEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedUser(getUserId());
            entityManager.merge(toDelete);
        }
    }

    @Override
    public ListingCountStatisticsEntity create(final ListingCountStatisticsDTO dto)
            throws EntityCreationException, EntityRetrievalException {
        ListingCountStatisticsEntity entity = new ListingCountStatisticsEntity();
        entity.setDeveloperCount(dto.getDeveloperCount());
        entity.setProductCount(dto.getProductCount());
        entity.setCertificationEditionId(dto.getCertificationEditionId());
        entity.setCertificationStatusId(dto.getCertificationStatusId());

        if (dto.getDeleted() != null) {
            entity.setDeleted(dto.getDeleted());
        } else {
            entity.setDeleted(false);
        }
        if (dto.getLastModifiedUser() != null) {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        } else {
            entity.setLastModifiedUser(getUserId());
        }
        if (dto.getLastModifiedDate() != null) {
            entity.setLastModifiedDate(dto.getLastModifiedDate());
        } else {
            entity.setLastModifiedDate(new Date());
        }
        if (dto.getCreationDate() != null) {
            entity.setCreationDate(dto.getCreationDate());
        } else {
            entity.setCreationDate(new Date());
        }

        entityManager.persist(entity);
        entityManager.flush();
        return entity;

    }

    private List<ListingCountStatisticsEntity> findAllEntities() {
        Query query = entityManager.createQuery("from ListingCountStatisticsEntity lcse "
                + "LEFT OUTER JOIN FETCH lcse.certificationEdition "
                + "LEFT OUTER JOIN FETCH lcse.certificationStatus "
                + "where (lcse.deleted = false)",
                ListingCountStatisticsEntity.class);
        return query.getResultList();
    }

    private ListingCountStatisticsEntity getEntityById(final Long id) throws EntityRetrievalException {
        ListingCountStatisticsEntity entity = null;

        Query query = entityManager.createQuery("from ListingCountStatisticsEntity lcse "
                + "LEFT OUTER JOIN FETCH lcse.certificationEdition "
                + "LEFT OUTER JOIN FETCH lcse.certificationStatus "
                + "where (lcse.deleted = false) AND (lcse.id = :entityid) ",
                ListingCountStatisticsEntity.class);
        query.setParameter("entityid", id);
        List<ListingCountStatisticsEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate address id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    private Long getUserId() {
        // If there is no user the current context, assume this is a system
        // process
        if (Util.getCurrentUser() == null || Util.getCurrentUser().getId() == null) {
            return MODIFIED_USER_ID;
        } else {
            return Util.getCurrentUser().getId();
        }
    }
}
