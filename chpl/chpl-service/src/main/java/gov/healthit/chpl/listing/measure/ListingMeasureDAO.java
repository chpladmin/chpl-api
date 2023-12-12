package gov.healthit.chpl.listing.measure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Repository("listingMeasureDao")
@Log4j2
public class ListingMeasureDAO extends BaseDAOImpl {
    private static final String LISTING_MEASURE_MAP_HQL_BEGIN = "SELECT DISTINCT listingMeasureMap "
            + "FROM ListingMeasureEntity listingMeasureMap "
            + "JOIN FETCH listingMeasureMap.type "
            + "LEFT JOIN FETCH listingMeasureMap.associatedCriteria assocCCMap "
            + "LEFT JOIN FETCH assocCCMap.criterion assocCC "
            + "LEFT JOIN FETCH assocCC.certificationEdition "
            + "LEFT JOIN FETCH assocCC.rule "
            + "JOIN FETCH listingMeasureMap.measure mm "
            + "JOIN FETCH mm.domain "
            + "JOIN FETCH mm.allowedCriteria ac "
            + "JOIN FETCH ac.criterion allowedCC "
            + "LEFT JOIN FETCH allowedCC.certificationEdition "
            + "LEFT JOIN FETCH allowedCC.rule "
            + "WHERE listingMeasureMap.deleted = false ";

    public void createCertifiedProductMeasureMapping(Long listingId, ListingMeasure mm) throws EntityCreationException {
        try {
            ListingMeasureEntity mmEntity = new ListingMeasureEntity();
            mmEntity.setDeleted(false);
            mmEntity.setListingId(listingId);
            mmEntity.setMeasureId(mm.getMeasure().getId());
            mmEntity.setTypeId(mm.getMeasureType().getId());
            create(mmEntity);

            for (CertificationCriterion associatedCriterion : mm.getAssociatedCriteria()) {
                ListingMeasureCriterionMapEntity mmCriterionEntity = new ListingMeasureCriterionMapEntity();
                mmCriterionEntity.setCertificationCriterionId(associatedCriterion.getId());
                mmCriterionEntity.setListingMeasureMapId(mmEntity.getId());
                mmCriterionEntity.setDeleted(false);
                create(mmCriterionEntity);
            }
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
        }
    }

    public void updateCertifiedProductMeasureMapping(ListingMeasure toUpdate)
            throws EntityRetrievalException {

        ListingMeasureEntity existingEntity = getEntityById(toUpdate.getId());
        if (existingEntity == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + toUpdate.getId());
        }
        existingEntity.setMeasureId(toUpdate.getMeasure().getId());
        existingEntity.setTypeId(toUpdate.getMeasureType().getId());
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

        removed.stream().forEach(removedCriterion -> deleteRemovedCriterion(existingEntity, removedCriterion));
        added.stream().forEach(addedCriterion -> saveAddedCriterion(existingEntity, addedCriterion));
    }

    private void saveAddedCriterion(ListingMeasureEntity listingMeasureEntity, CertificationCriterion addedCriterion) {
        ListingMeasureCriterionMapEntity addedEntity = new ListingMeasureCriterionMapEntity();
        addedEntity.setCertificationCriterionId(addedCriterion.getId());
        addedEntity.setDeleted(false);
        addedEntity.setListingMeasureMapId(listingMeasureEntity.getId());
        create(addedEntity);
    }

    private void deleteRemovedCriterion(ListingMeasureEntity listingMeasureEntity, CertificationCriterion removedCriterion) {
        Optional<ListingMeasureCriterionMapEntity> removedEntityOpt =
                listingMeasureEntity.getAssociatedCriteria().stream()
                    .filter(existingCrit -> existingCrit.getCriterion().getId().equals(removedCriterion.getId()))
                    .findAny();
        if (removedEntityOpt.isPresent()) {
            ListingMeasureCriterionMapEntity removedEntity = removedEntityOpt.get();
            removedEntity.setDeleted(true);
            update(removedEntity);
        }
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

    public void deleteCertifiedProductMeasure(Long id) throws EntityRetrievalException {

        ListingMeasureEntity existingMeasureMap = getEntityById(id);
        if (existingMeasureMap == null) {
            throw new EntityRetrievalException("Could not find mapping with id " + id);
        }
        try {
            if (existingMeasureMap.getAssociatedCriteria() != null) {
                existingMeasureMap.getAssociatedCriteria().stream()
                    .forEach(assocCriterion -> deleteAssociatedCriterion(assocCriterion));
            }
            existingMeasureMap.setDeleted(true);
        update(existingMeasureMap);
        } catch (Exception ex) {
            LOGGER.error("Exception marking listing-measure map with ID " + id + " as deleted.", ex);
        }
    }

    private void deleteAssociatedCriterion(ListingMeasureCriterionMapEntity assocCriterion) {
        assocCriterion.setDeleted(true);
        update(assocCriterion);
    }

    public List<ListingMeasure> getMeasuresByListingId(Long listingId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(LISTING_MEASURE_MAP_HQL_BEGIN
                + " AND listingMeasureMap.listingId = :listingId ",
                ListingMeasureEntity.class);
        query.setParameter("listingId", listingId);
        List<ListingMeasureEntity> entities = query.getResultList();
        return entities.stream().map(entity -> entity.convert())
                .collect(Collectors.toList());
    }

    public ListingMeasure lookupMapping(Long listingId, Long measureId)
            throws EntityRetrievalException {
        Query query = entityManager.createQuery(LISTING_MEASURE_MAP_HQL_BEGIN
                + "AND listingMeasureMap.listingId = :listingId "
                + "AND listingMeasureMap.measureId = :measureId ",
                ListingMeasureEntity.class);
        query.setParameter("listingId", listingId);
        query.setParameter("measureId", measureId);
        List<ListingMeasureEntity> entities = query.getResultList();
        if (entities == null || entities.size() == 0) {
            return null;
        }
        return entities.stream().map(entity -> entity.convert())
                .collect(Collectors.toList())
                .get(0);
    }

    private ListingMeasureEntity getEntityById(Long id) throws EntityRetrievalException {
        ListingMeasureEntity entity = null;
        Query query = entityManager.createQuery(LISTING_MEASURE_MAP_HQL_BEGIN
                + " AND listingMeasureMap.id = :entityid ",
                ListingMeasureEntity.class);

        query.setParameter("entityid", id);
        List<ListingMeasureEntity> result = query.getResultList();
        if (result.size() >= 1) {
            entity = result.get(0);
        }
        return entity;
    }

    public Set<MeasureType> getMeasureTypes() {
        Query query = entityManager.createQuery("SELECT measureType "
                + "FROM ListingMeasureTypeEntity measureType "
                + "WHERE deleted = false");
        List<ListingMeasureTypeEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.stream().map(result -> result.convert())
                .collect(Collectors.toSet());
    }

    public ListingMeasureTypeEntity getMeasureTypeEntity(String name) {
        Query query = entityManager.createQuery("SELECT measureType "
                + "FROM ListingMeasureTypeEntity measureType "
                + "WHERE deleted = false "
                + "AND name = :name");
        query.setParameter("name", name);
        List<ListingMeasureTypeEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);
    }
}
