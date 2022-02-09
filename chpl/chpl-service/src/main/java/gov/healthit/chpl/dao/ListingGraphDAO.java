package gov.healthit.chpl.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.ListingToListingMapDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.entity.listing.ListingToListingMapEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Repository(value = "listingGraphDao")
@Log4j2
public class ListingGraphDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public ListingGraphDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public Long createListingMap(Long childId, Long parentId) throws EntityCreationException {
        try {
            ListingToListingMapEntity entity = new ListingToListingMapEntity();
            entity.setChildId(childId);
            entity.setParentId(parentId);
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            create(entity);
            return entity.getId();
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public ListingToListingMapDTO createListingMap(ListingToListingMapDTO toCreate) throws EntityCreationException {
        ListingToListingMapEntity entity = new ListingToListingMapEntity();
        entity.setChildId(toCreate.getChildId());
        entity.setParentId(toCreate.getParentId());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        try {
            entityManager.persist(entity);
            entityManager.flush();
            entityManager.clear();
        } catch (Exception ex) {
            String msg = msgUtil.getMessage("listing.badIcsRelative", toCreate.getParentId(), toCreate.getChildId());
            LOGGER.error(msg, ex);
            throw new EntityCreationException(msg);
        }
        entity = getListingMapEntity(toCreate.getChildId(), toCreate.getParentId());
        return new ListingToListingMapDTO(entity);
    }

    public void deleteListingMap(ListingToListingMapDTO toDelete) {
        ListingToListingMapEntity entity = null;

        if (toDelete.getId() != null) {
            entity = entityManager.find(ListingToListingMapEntity.class, toDelete.getId());
        } else if (toDelete.getParentId() != null && toDelete.getChildId() != null) {
            entity = getListingMapEntity(toDelete.getChildId(), toDelete.getParentId());
        }

        if (entity != null) {
            entity.setDeleted(true);
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            update(entity);
        }
    }

    public Integer getLargestIcs(List<Long> listingIds) {
        Query query = entityManager
                .createQuery(
                        "SELECT MAX(to_number(listing.icsCode, '99')) " + "FROM CertifiedProductEntity listing "
                                + "WHERE listing.id IN (:listingIds) " + "AND listing.deleted <> true",
                        BigDecimal.class);
        query.setParameter("listingIds", listingIds);
        BigDecimal result = (BigDecimal) query.getSingleResult();
        if (result == null) {
            return null;
        }
        return Integer.valueOf(result.intValue());
    }

    public List<CertifiedProductDTO> getParents(Long listingId) {
        Query query = entityManager.createQuery(
                    "SELECT listingMap.parentId "
                    + "FROM ListingToListingMapEntity listingMap "
                    + "WHERE listingMap.childId = :childId "
                    + "AND listingMap.deleted <> true ",
                Long.class);
        query.setParameter("childId", listingId);
        List<Long> parentIds = query.getResultList();

        //Retrieve the CertifiedProduct for each parent listing
        List<CertifiedProductDTO> result = new ArrayList<CertifiedProductDTO>();
        for (Long parentId : parentIds) {
            Query query2 = entityManager.createQuery(
                            "SELECT certifiedProduct "
                            + "FROM CertifiedProductEntity certifiedProduct "
                            + "WHERE certifiedProduct.id = :id "
                            + "AND certifiedProduct.deleted <> true ",
                    CertifiedProductEntity.class);
            query2.setParameter("id", parentId);
            List<CertifiedProductEntity> parentEntities = query2.getResultList();
            result.add(new CertifiedProductDTO(parentEntities.get(0)));
        }
        return result;
    }

    public List<CertifiedProductDTO> getChildren(Long listingId) {
        Query query = entityManager.createQuery(
                    "SELECT listingMap.childId "
                    + "FROM ListingToListingMapEntity listingMap "
                    + "WHERE listingMap.parentId = :parentId "
                    + "AND listingMap.deleted <> true ",
                Long.class);
        query.setParameter("parentId", listingId);
        List<Long> childIds = query.getResultList();

        //Retrieve the CertifiedProduct for each child listing
        List<CertifiedProductDTO> result = new ArrayList<CertifiedProductDTO>();
        for (Long childId : childIds) {
            Query query2 = entityManager.createQuery(
                            "SELECT certifiedProduct "
                            + "FROM CertifiedProductEntity certifiedProduct "
                            + "WHERE certifiedProduct.id = :id "
                            + "AND certifiedProduct.deleted <> true ",
                    CertifiedProductEntity.class);
            query2.setParameter("id", childId);
            List<CertifiedProductEntity> parentEntities = query2.getResultList();
            result.add(new CertifiedProductDTO(parentEntities.get(0)));
        }
        return result;
    }

    public ListingToListingMapDTO getListingMap(Long childId, Long parentId) {
        ListingToListingMapEntity mapEntity = getListingMapEntity(childId, parentId);

        ListingToListingMapDTO result = null;
        if (mapEntity != null) {
            result = new ListingToListingMapDTO(mapEntity);
        }
        return result;
    }

    private ListingToListingMapEntity getListingMapEntity(Long childId, Long parentId) {
        Query query = entityManager.createQuery("SELECT listingMap "
                + "FROM ListingToListingMapEntity listingMap "
                + "JOIN FETCH listingMap.child "
                + "JOIN FETCH listingMap.parent "
                + "WHERE listingMap.parentId = :parentId "
                + "AND listingMap.childId = :childId "
                + "AND listingMap.deleted <> true",
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
