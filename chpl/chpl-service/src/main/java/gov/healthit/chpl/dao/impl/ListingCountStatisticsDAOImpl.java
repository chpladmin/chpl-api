package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.ListingCountStatisticsDAO;
import gov.healthit.chpl.dto.ListingCountStatisticsDTO;
import gov.healthit.chpl.entity.statistics.ListingCountStatisticsEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * The implementation for ListingCountStatisticsDAO.
 * @author alarned
 *
 */
@Repository("listingCountStatisticsDAO")
public class ListingCountStatisticsDAOImpl extends BaseDAOImpl implements ListingCountStatisticsDAO {

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
    @Transactional
    public void delete(final Long id) throws EntityRetrievalException {
        ListingCountStatisticsEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            entityManager.merge(toDelete);
        }
    }

    @Override
    @Transactional
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
}
