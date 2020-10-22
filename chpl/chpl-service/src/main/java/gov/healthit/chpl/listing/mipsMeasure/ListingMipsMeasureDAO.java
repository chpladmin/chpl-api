package gov.healthit.chpl.listing.mipsMeasure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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
    private static final String LISTING_MEASURE_MAP_HQL_BEGIN = "SELECT DISTINCT listingMipsMap "
            + "FROM ListingMipsMeasureEntity listingMipsMap "
            + "JOIN FETCH listingMipsMap.type "
            + "LEFT JOIN FETCH listingMipsMap.associatedCriteria assocCCMap "
            + "LEFT JOIN FETCH assocCCMap.criterion assocCC "
            + "LEFT JOIN FETCH assocCC.certificationEdition "
            + "JOIN FETCH listingMipsMap.measure mm "
            + "JOIN FETCH mm.domain "
            + "JOIN FETCH mm.allowedCriteria ac "
            + "JOIN FETCH ac.criterion allowedCC "
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
        existingEntity.setMeasureId(toUpdate.getMeasure().getId());
        existingEntity.setTypeId(toUpdate.getMeasurementType().getId());
        existingEntity.setLastModifiedUser(AuthUtil.getAuditId());
        update(existingEntity);

        Optional<List<CertificationCriterion>> updatedAssociatedCriteria = Optional
                .ofNullable(new ArrayList(toUpdate.getAssociatedCriteria()));
        Optional<List<CertificationCriterion>> exsitingAssociatedCriteria = Optional
                .ofNullable(existingEntity.getAssociatedCriteria().stream()
                        .map(assocCriterion -> assocCriterion.convert())
                        .collect(Collectors.toList()));

        List<CertificationCriterion> removed = getRemovedAssociatedCriteria(updatedAssociatedCriteria,
                exsitingAssociatedCriteria);

        List<CertificationCriterion> added = getAddedAssociatedCriteria(updatedAssociatedCriteria,
                exsitingAssociatedCriteria);

        removed.stream().forEach(removedCriterion -> {
            Optional<ListingMipsMeasureCriterionMapEntity> removedEntityOpt =
                    existingEntity.getAssociatedCriteria().stream()
                        .filter(existingCrit -> existingCrit.getCriterion().getId().equals(removedCriterion.getId()))
                        .findAny();
            if (removedEntityOpt.isPresent()) {
                ListingMipsMeasureCriterionMapEntity removedEntity = removedEntityOpt.get();
                removedEntity.setDeleted(true);
                removedEntity.setLastModifiedUser(AuthUtil.getAuditId());
                update(removedEntity);
            }
        });

        added.stream().forEach(addedCriterion -> {
            ListingMipsMeasureCriterionMapEntity addedEntity = new ListingMipsMeasureCriterionMapEntity();
            addedEntity.setCertificationCriterionId(addedCriterion.getId());
            addedEntity.setDeleted(false);
            addedEntity.setLastModifiedUser(AuthUtil.getAuditId());
            addedEntity.setListingMipsMeasureMapId(existingEntity.getId());
            create(addedEntity);
        });
    }

    private List<CertificationCriterion> getRemovedAssociatedCriteria(
            Optional<List<CertificationCriterion>> listA,
            Optional<List<CertificationCriterion>> listB) {
        //gets items in list B that are not in list A
        return subtractLists(
                listB.isPresent() ? listB.get() : new ArrayList<CertificationCriterion>(),
                listA.isPresent() ? listA.get() : new ArrayList<CertificationCriterion>());
    }

    private List<CertificationCriterion> getAddedAssociatedCriteria(
            Optional<List<CertificationCriterion>> listA,
            Optional<List<CertificationCriterion>> listB) {
        //get items in list A that are not in list B
        return subtractLists(
                listA.isPresent() ? listA.get() : new ArrayList<CertificationCriterion>(),
                listB.isPresent() ? listB.get() : new ArrayList<CertificationCriterion>());
    }

    private List<CertificationCriterion> subtractLists(List<CertificationCriterion> listA,
            List<CertificationCriterion> listB) {

        Predicate<CertificationCriterion> notInListB = certFromA -> !listB.stream()
                .anyMatch(cert -> certFromA.equals(cert));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
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
        Query query = entityManager.createQuery(LISTING_MEASURE_MAP_HQL_BEGIN
                + " AND listingMipsMap.listingId = :listingId ",
                ListingMipsMeasureEntity.class);
        query.setParameter("listingId", listingId);
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
