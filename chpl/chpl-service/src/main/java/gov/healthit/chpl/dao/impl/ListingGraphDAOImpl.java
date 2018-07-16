package gov.healthit.chpl.dao.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ListingGraphDAO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.ListingToListingMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;

@Repository(value = "listingGraphDao")
public class ListingGraphDAOImpl extends BaseDAOImpl implements ListingGraphDAO {
    private static final Logger LOGGER = LogManager.getLogger(ListingGraphDAOImpl.class);
    @Autowired
    MessageSource messageSource;

    @Override
    public ListingToListingMapDTO createListingMap(ListingToListingMapDTO toCreate) throws EntityCreationException {
        ListingToListingMapEntity entity = new ListingToListingMapEntity();
        entity.setChildId(toCreate.getChildId());
        entity.setParentId(toCreate.getParentId());
        entity.setDeleted(false);
        entity.setLastModifiedUser(Util.getCurrentUser().getId());
        try {
            entityManager.persist(entity);
            entityManager.flush();
            entityManager.clear();
        } catch (Exception ex) {
            String msg = String
                    .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badIcsRelative"),
                            LocaleContextHolder.getLocale()), toCreate.getParentId(), toCreate.getChildId());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }
        entity = getListingMapEntity(toCreate.getChildId(), toCreate.getParentId());
        return new ListingToListingMapDTO(entity);
    }

    @Override
    public void deleteListingMap(ListingToListingMapDTO toDelete) {
        ListingToListingMapEntity entity = null;

        if (toDelete.getId() != null) {
            entity = entityManager.find(ListingToListingMapEntity.class, toDelete.getId());
        } else if (toDelete.getParentId() != null && toDelete.getChildId() != null) {
            entity = getListingMapEntity(toDelete.getChildId(), toDelete.getParentId());
        }

        if (entity != null) {
            entity.setDeleted(true);
            entity.setLastModifiedUser(Util.getCurrentUser().getId());
            entityManager.merge(entity);
            entityManager.flush();
        }
    }

    /**
     * Get the maximum ICS value from the proposed parent nodes
     * 
     * @param listingIds
     *            proposed parent nodes
     * @return largest ICS value
     */
    @Override
    public Integer getLargestIcs(List<Long> listingIds) {
        Query query = entityManager
                .createQuery(
                        "SELECT MAX(to_number(listing.icsCode, '99')) " + "FROM CertifiedProductEntity listing "
                                + "WHERE listing.id IN (:listingIds) " + "AND listing.deleted <> true",
                        BigDecimal.class);
        query.setParameter("listingIds", listingIds);
        BigDecimal result = (BigDecimal) query.getSingleResult();
        if(result == null) {
            return null;
        }
        return new Integer(result.intValue());
    }

    /**
     * Find the first-level parent nodes of a specific listing
     * 
     * @param listingId
     *            Listing to find the parents of
     * @return The first-level parents (can have multiple)
     */
    @Override
    @Transactional
    public List<CertifiedProductDetailsDTO> getParents(Long listingId) {
        Query query = entityManager.createQuery(
                "SELECT listingMap.parent " + "FROM ListingToListingMapEntity listingMap "
                        + "WHERE listingMap.childId = :childId " + "AND listingMap.deleted <> true ",
                CertifiedProductDetailsEntity.class);
        query.setParameter("childId", listingId);
        List<CertifiedProductDetailsEntity> parentEntities = query.getResultList();

        List<CertifiedProductDetailsDTO> result = new ArrayList<CertifiedProductDetailsDTO>();
        for (CertifiedProductDetailsEntity parentEntity : parentEntities) {
            result.add(new CertifiedProductDetailsDTO(parentEntity));
        }
        return result;
    }

    /**
     * Find the first-level child nodes of a specific listing
     * 
     * @param listingId
     *            Listing to find the children of
     * @return The first-level children (can have multiple)
     */
    @Override
    @Transactional
    public List<CertifiedProductDetailsDTO> getChildren(Long listingId) {
        Query query = entityManager.createQuery(
                "SELECT listingMap.child " + "FROM ListingToListingMapEntity listingMap "
                        + "WHERE listingMap.parentId = :parentId " + "AND listingMap.deleted <> true",
                CertifiedProductDetailsEntity.class);
        query.setParameter("parentId", listingId);
        List<CertifiedProductDetailsEntity> childEntities = query.getResultList();

        List<CertifiedProductDetailsDTO> result = new ArrayList<CertifiedProductDetailsDTO>();
        for (CertifiedProductDetailsEntity childEntity : childEntities) {
            result.add(new CertifiedProductDetailsDTO(childEntity));
        }
        return result;
    }

    @Override
    public ListingToListingMapDTO getListingMap(Long childId, Long parentId) {
        ListingToListingMapEntity mapEntity = getListingMapEntity(childId, parentId);

        ListingToListingMapDTO result = null;
        if (mapEntity != null) {
            result = new ListingToListingMapDTO(mapEntity);
        }
        return result;
    }

    private ListingToListingMapEntity getListingMapEntity(Long childId, Long parentId) {
        Query query = entityManager.createQuery(
                "SELECT listingMap " + "FROM ListingToListingMapEntity listingMap " + "JOIN FETCH listingMap.child "
                        + "JOIN FETCH listingMap.parent " + "WHERE listingMap.parentId = :parentId "
                        + "AND listingMap.childId = :childId " + "AND listingMap.deleted <> true",
                ListingToListingMapEntity.class);
        query.setParameter("parentId", parentId);
        query.setParameter("childId", childId);
        List<ListingToListingMapEntity> mapEntities = query.getResultList();

        if (mapEntities != null && mapEntities.size() > 0) {
            return mapEntities.get(0);
        }
        return null;
    }
}
