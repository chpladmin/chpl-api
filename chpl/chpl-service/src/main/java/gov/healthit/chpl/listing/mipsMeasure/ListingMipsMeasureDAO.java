package gov.healthit.chpl.listing.mipsMeasure;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.ListingMipsMeasure;
import gov.healthit.chpl.domain.MipsMeasurementType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Repository("listingMipsMeasureDao")
@Log4j2
public class ListingMipsMeasureDAO extends BaseDAOImpl {
    private static final String LISTING_MEASURE_MAP_HQL_BEGIN = "SELECT listingMipsMap "
            + "FROM ListingMipsMeasureEntity listingMipsMap "
            + "JOIN FETCH listingMipsMap.type "
            + "LEFT JOIN FETCH listingMipsMap.assocaitedCriteria assocCC "
            + "LEFT JOIN FETCH assocCC.certificationEdition "
            + "JOIN FETCH listingMipsMap.measure mm "
            + "JOIN FETCH mm.domain "
            + "JOIN FETCH mm.allowedCriteria ac "
            + "JOIN FETCH ac.crierion allowedCC "
            + "JOIN FETCH allowedCC.certificationEdition "
            + "WHERE listingMipsMap.deleted = false ";

    public void createCertifiedProductMipsMapping(Long listingId, ListingMipsMeasure mm)
            throws EntityCreationException {

        ListingMipsMeasureEntity mmEntity = new ListingMipsMeasureEntity();
        mmEntity.setDeleted(false);
        mmEntity.setLastModifiedUser(AuthUtil.getAuditId());
        mmEntity.setListingId(listingId);
        mmEntity.setMeasureId(mm.getMeasure().getId());
        mmEntity.setTypeId(mm.getMeasurementType().getId());
        create(mmEntity);

        for (CertificationCriterion associatedCriterion : mm.getAssociatedCriteria()) {
            ListingMipsMeasureCriterionMapEntity mmCriterionEntity = new ListingMipsMeasureCriterionMapEntity();
            mmCriterionEntity.setCertificationCriterionId(associatedCriterion.getId());
            mmCriterionEntity.setListingMipsMeasureMapId(mmEntity.getId());
            mmCriterionEntity.setDeleted(false);
            mmCriterionEntity.setLastModifiedUser(AuthUtil.getAuditId());
            create(mmCriterionEntity);
        }
    }

    public void updateCertifiedProductMipsMapping(ListingMipsMeasure toUpdate)
            throws EntityRetrievalException {

        ListingMipsMeasureEntity existingEntity = getEntityById(toUpdate.getId());
        if (existingEntity == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + toUpdate.getId());
        }
        //TODO: update things
        existingEntity.setLastModifiedUser(AuthUtil.getAuditId());
        update(existingEntity);
    }

    public void deleteCertifiedProductMips(Long id) throws EntityRetrievalException {

        ListingMipsMeasureEntity existingMeasureMap = getEntityById(id);
        if (existingMeasureMap == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        try {
            if (existingMeasureMap.getAssociatedCriteria() != null) {
                existingMeasureMap.getAssociatedCriteria().stream()
                    .forEach(assocCriterion -> {
                        assocCriterion.setDeleted(true);
                        assocCriterion.setLastModifiedUser(AuthUtil.getAuditId());
                        update(assocCriterion);
                    });
            }
            existingMeasureMap.setDeleted(true);
            existingMeasureMap.setLastModifiedUser(AuthUtil.getAuditId());
        update(existingMeasureMap);
        } catch (Exception ex) {
            LOGGER.error("Exception marking listing-measure map with ID " + id + " as deleted.", ex);
        }
    }

    public List<ListingMipsMeasure> getMipsMeasuresByListingId(Long listingId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(LISTING_MEASURE_MAP_HQL_BEGIN,
                ListingMipsMeasureEntity.class);
        List<ListingMipsMeasureEntity> entities = query.getResultList();
        return entities.stream().map(entity -> entity.convert())
                .collect(Collectors.toList());
    }

    public ListingMipsMeasure lookupMapping(Long listingId, Long measureId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(LISTING_MEASURE_MAP_HQL_BEGIN
                + "AND listingMipsMap.listingId = :listingId "
                + "AND listingMipsMap.measureId = :measureId ",
                ListingMipsMeasureEntity.class);
        query.setParameter("listingId", listingId);
        query.setParameter("measureId", measureId);
        List<ListingMipsMeasureEntity> entities = query.getResultList();
        if (entities == null || entities.size() == 0) {
            return null;
        }
        return entities.stream().map(entity -> entity.convert())
                .collect(Collectors.toList())
                .get(0);
    }

    private ListingMipsMeasureEntity getEntityById(Long id) throws EntityRetrievalException {
        ListingMipsMeasureEntity entity = null;
        Query query = entityManager.createQuery(LISTING_MEASURE_MAP_HQL_BEGIN
                + " AND listingMipsMap.id = :entityid ",
                ListingMipsMeasureEntity.class);

        query.setParameter("entityid", id);
        List<ListingMipsMeasureEntity> result = query.getResultList();
        if (result.size() >= 1) {
            entity = result.get(0);
        }
        return entity;
    }

    public MipsMeasurementType getMeasurementType(String type) {
        Query query = entityManager.createQuery("SELECT mipsType "
                + "FROM ListingMipsMeasureTypeEntity mipsType "
                + "WHERE deleted = false"
                + "AND name = :type");
        query.setParameter("name", type);
        List<ListingMipsMeasureTypeEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0).convert();
    }
}
