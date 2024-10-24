package gov.healthit.chpl.complaint;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.complaint.domain.ComplainantType;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.complaint.domain.ComplaintCriterionMap;
import gov.healthit.chpl.complaint.domain.ComplaintListingMap;
import gov.healthit.chpl.complaint.domain.ComplaintType;
import gov.healthit.chpl.complaint.entity.ComplainantTypeEntity;
import gov.healthit.chpl.complaint.entity.ComplaintCriterionMapEntity;
import gov.healthit.chpl.complaint.entity.ComplaintEntity;
import gov.healthit.chpl.complaint.entity.ComplaintListingMapEntity;
import gov.healthit.chpl.complaint.entity.ComplaintSurveillanceMapEntity;
import gov.healthit.chpl.complaint.entity.ComplaintToComplaintTypeMapEntity;
import gov.healthit.chpl.complaint.entity.ComplaintTypeEntity;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.persistence.Query;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ComplaintDAO extends BaseDAOImpl {
    private static final String GET_COMPLAINTS_HQL = "SELECT DISTINCT c "
            + "FROM ComplaintEntity c "
            + "LEFT JOIN FETCH c.listings listings "
            + "LEFT JOIN FETCH listings.listing "
            + "LEFT JOIN FETCH c.surveillances surveillances "
            + "LEFT JOIN FETCH surveillances.surveillance surv "
            + "LEFT JOIN FETCH c.complaintTypes typeMap "
            + "LEFT JOIN FETCH typeMap.complaintType "
            + "LEFT JOIN FETCH surv.surveillanceType "
            + "LEFT JOIN FETCH c.criteria criteria "
            + "LEFT JOIN FETCH criteria.certificationCriterion criterion "
            + "LEFT JOIN FETCH criterion.certificationEdition "
            + "LEFT JOIN FETCH criterion.rule "
            + "JOIN FETCH c.certificationBody acb "
            + "LEFT JOIN FETCH acb.address "
            + "JOIN FETCH c.complainantType "
            + "WHERE c.deleted = false ";

    public List<ComplaintType> getComplaintTypes() {
        List<ComplaintTypeEntity> entities = getComplaintTypeEntities();
        return entities.stream()
                .map(entity -> entity.buildComplaintType())
                .collect(Collectors.toList());
    }

    public List<ComplainantType> getComplainantTypes() {
        List<ComplainantTypeEntity> entities = getComplainantTypeEntities();
        return entities.stream()
                .map(entity -> entity.buildComplainantType())
                .collect(Collectors.toList());
    }

    @Cacheable(CacheNames.COMPLAINTS)
    public List<Complaint> getAllComplaints() {
        Query query = entityManager.createQuery(GET_COMPLAINTS_HQL, ComplaintEntity.class);
        List<ComplaintEntity> results = query.getResultList();
        return convertToComplaints(results);
    }

    public Complaint getComplaint(Long complaintId) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintId);
        return entity.buildComplaint();
    }

    public List<Complaint> getComplaintsForSurveillance(Long surveillanceId) {
        Query query = entityManager.createQuery(GET_COMPLAINTS_HQL
                + " AND surv.id = :surveillanceId ",
                ComplaintEntity.class);
        query.setParameter("surveillanceId", surveillanceId);
        List<ComplaintEntity> result = query.getResultList();

        if (!CollectionUtils.isEmpty(result)) {
            return result.stream()
                    .map(entity -> entity.buildComplaint())
                    .collect(Collectors.toList());
        }
        return new ArrayList<Complaint>();
    }

    public Complaint create(Complaint complaint) throws EntityRetrievalException {
        ComplaintEntity entity = new ComplaintEntity();

        entity.setCertificationBodyId(complaint.getCertificationBody().getId());
        entity.setComplainantTypeId(complaint.getComplainantType().getId());
        entity.setComplainantTypeOther(complaint.getComplainantTypeOther());
        entity.setComplaintTypesOther(complaint.getComplaintTypesOther());
        entity.setOncComplaintId(complaint.getOncComplaintId());
        entity.setAcbComplaintId(complaint.getAcbComplaintId());
        entity.setReceivedDate(complaint.getReceivedDate());
        entity.setSummary(complaint.getSummary());
        entity.setActions(complaint.getActions());
        entity.setComplainantContacted(complaint.isComplainantContacted());
        entity.setDeveloperContacted(complaint.isDeveloperContacted());
        entity.setOncAtlContacted(complaint.isOncAtlContacted());
        entity.setFlagForOncReview(complaint.isFlagForOncReview());
        entity.setClosedDate(complaint.getClosedDate());
        entity.setDeleted(false);

        create(entity);

        complaint.setId(entity.getId());
        saveComplaintTypes(complaint);
        saveListings(complaint);
        saveCritiera(complaint);
        saveSurveillances(complaint);

        return getEntityById(entity.getId()).buildComplaint();
    }

    public Complaint update(Complaint complaint) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaint.getId());
        entity.setCertificationBodyId(complaint.getCertificationBody().getId());
        entity.setComplainantTypeId(complaint.getComplainantType().getId());
        entity.setComplainantTypeOther(complaint.getComplainantTypeOther());
        entity.setComplaintTypesOther(complaint.getComplaintTypesOther());
        entity.setOncComplaintId(complaint.getOncComplaintId());
        entity.setAcbComplaintId(complaint.getAcbComplaintId());
        entity.setReceivedDate(complaint.getReceivedDate());
        entity.setSummary(complaint.getSummary());
        entity.setActions(complaint.getActions());
        entity.setComplainantContacted(complaint.isComplainantContacted());
        entity.setDeveloperContacted(complaint.isDeveloperContacted());
        entity.setOncAtlContacted(complaint.isOncAtlContacted());
        entity.setFlagForOncReview(complaint.isFlagForOncReview());
        entity.setClosedDate(complaint.getClosedDate());
        entity.setDeleted(false);

        update(entity);

        saveComplaintTypes(complaint);
        saveListings(complaint);
        saveCritiera(complaint);
        saveSurveillances(complaint);

        ComplaintEntity updatedEntity = getEntityById(complaint.getId());
        return updatedEntity.buildComplaint();
    }

    public void delete(Complaint complaint) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaint.getId());
        entity.setDeleted(true);

        entityManager.merge(entity);
        entityManager.flush();
        entityManager.clear();
    }

    private ComplaintEntity getEntityById(Long id) throws EntityRetrievalException {
        ComplaintEntity entity = null;
        Query query = entityManager.createQuery(GET_COMPLAINTS_HQL
                + " AND c.id = :complaintId",
                ComplaintEntity.class);
        query.setParameter("complaintId", id);
        List<ComplaintEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate complaint id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ComplaintTypeEntity> getComplaintTypeEntities() {
        Query query = entityManager.createQuery("from ComplaintTypeEntity where (NOT deleted = true) ",
                ComplaintTypeEntity.class);
        List<ComplaintTypeEntity> result = query.getResultList();
        return result;
    }

    private List<ComplainantTypeEntity> getComplainantTypeEntities() {
        Query query = entityManager.createQuery("from ComplainantTypeEntity where (NOT deleted = true) ",
                ComplainantTypeEntity.class);
        List<ComplainantTypeEntity> result = query.getResultList();
        return result;
    }

    private void saveSurveillances(Complaint complaint) throws EntityRetrievalException {
        // Get the existing surveillances for this complaint
        List<ComplaintSurveillanceMapEntity> existingSurveillances = getComplaintSurveillanceMapEntities(
                complaint.getId());

        deleteMissingSurveillances(complaint, existingSurveillances);
        addNewSurveillances(complaint, existingSurveillances);
    }

    private void addNewSurveillances(Complaint complaint,
            final List<ComplaintSurveillanceMapEntity> existingSurveillances) throws EntityRetrievalException {
        // If there is a surveillance passed in and it does not exist in the DB, add it
        for (ComplaintSurveillanceMap passedIn : complaint.getSurveillances()) {
            ComplaintSurveillanceMapEntity found = IterableUtils.find(existingSurveillances,
                    new Predicate<ComplaintSurveillanceMapEntity>() {
                        @Override
                        public boolean evaluate(ComplaintSurveillanceMapEntity fromDb) {
                            return fromDb.getSurveillanceId().equals(passedIn.getSurveillance().getId());
                        }
                    });
            // Wasn't found in the list from DB, add it to the DB
            if (found == null) {
                addSurveillanceToComplaint(complaint.getId(), passedIn.getSurveillance().getId());
            }
        }
    }

    private void deleteMissingSurveillances(Complaint complaint,
            final List<ComplaintSurveillanceMapEntity> existingSurveillances) throws EntityRetrievalException {
        // If the existing surveillance does not exist in the new list, delete it
        for (ComplaintSurveillanceMapEntity fromDb : existingSurveillances) {
            ComplaintSurveillanceMap found = IterableUtils.find(complaint.getSurveillances(),
                    new Predicate<ComplaintSurveillanceMap>() {
                        @Override
                        public boolean evaluate(ComplaintSurveillanceMap passedIn) {
                            return passedIn.getSurveillance().getId().equals(fromDb.getSurveillanceId());
                        }
                    });
            // Wasn't found in the list passed in, delete it from the DB
            if (found == null) {
                deleteSurveillanceToComplaint(fromDb.getId());
            }
        }

    }

    private void addSurveillanceToComplaint(long complaintId, long surveillanceId) {
        ComplaintSurveillanceMapEntity entity = new ComplaintSurveillanceMapEntity();
        entity.setComplaintId(complaintId);
        entity.setSurveillanceId(surveillanceId);
        entity.setDeleted(false);

        create(entity);
    }

    private void deleteSurveillanceToComplaint(long id) throws EntityRetrievalException {
        ComplaintSurveillanceMapEntity entity = getComplaintSurveillanceMapEntity(id);
        entity.setDeleted(true);

        update(entity);
    }

    private ComplaintSurveillanceMapEntity getComplaintSurveillanceMapEntity(long id)
            throws EntityRetrievalException {
        ComplaintSurveillanceMapEntity entity = null;

        Query query = entityManager.createQuery("SELECT c "
                + "FROM ComplaintSurveillanceMapEntity c "
                + "LEFT JOIN FETCH c.surveillance surveillance "
                + "LEFT JOIN FETCH surveillance.surveillanceType "
                + "WHERE c.deleted = false "
                + "AND c.id = :complaintSurveillanceMapId", ComplaintSurveillanceMapEntity.class);
        query.setParameter("complaintSurveillanceMapId", id);
        List<ComplaintSurveillanceMapEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate complaint surveillance map id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ComplaintSurveillanceMapEntity> getComplaintSurveillanceMapEntities(long complaintId) {
        Query query = entityManager.createQuery("SELECT c "
                + "FROM ComplaintSurveillanceMapEntity c "
                + "LEFT JOIN FETCH c.surveillance surveillance "
                + "LEFT JOIN FETCH surveillance.surveillanceType "
                + "WHERE c.deleted = false "
                + "AND c.complaintId = :complaintId", ComplaintSurveillanceMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintSurveillanceMapEntity> result = query.getResultList();

        return result;
    }

    private void saveComplaintTypes(Complaint complaint) throws EntityRetrievalException {
        // Get the existing complaint types for this complaint
        List<ComplaintToComplaintTypeMapEntity> existingComplaintTypes = getComplaintTypeMapEntities(complaint.getId());

        deleteMissingComplaintTypes(complaint, existingComplaintTypes);
        addNewComplaintTypes(complaint, existingComplaintTypes);
    }

    private void addNewComplaintTypes(Complaint complaint, List<ComplaintToComplaintTypeMapEntity> existingComplaintTypes)
            throws EntityRetrievalException {
        // If there is a complaint type passed in and it does not exist in the DB, add it
        for (ComplaintType passedIn : complaint.getComplaintTypes()) {
            ComplaintToComplaintTypeMapEntity found = IterableUtils.find(existingComplaintTypes,
                    new Predicate<ComplaintToComplaintTypeMapEntity>() {
                        @Override
                        public boolean evaluate(ComplaintToComplaintTypeMapEntity object) {
                            return object.getComplaintTypeId().equals(passedIn.getId());
                        }
                    });
            // Wasn't found in the list from DB, add it to the DB
            if (found == null) {
                addComplaintTypeToComplaint(complaint.getId(), passedIn.getId());
            }
        }
    }

    private void deleteMissingComplaintTypes(Complaint complaint, List<ComplaintToComplaintTypeMapEntity> existingComplaintTypes)
            throws EntityRetrievalException {
        // If the existing complaint type does not exist in the new list, delete it
        for (ComplaintToComplaintTypeMapEntity existing : existingComplaintTypes) {
            ComplaintType found = IterableUtils.find(complaint.getComplaintTypes(),
                    new Predicate<ComplaintType>() {
                        @Override
                        public boolean evaluate(ComplaintType existingComplaintType) {
                            return existingComplaintType.getId().equals(existing.getComplaintTypeId());
                        }
                    });
            // Wasn't found in the list passed in, delete it from the DB
            if (found == null) {
                deleteComplaintTypeFromComplaint(existing.getId());
            }
        }

    }

    private void addComplaintTypeToComplaint(Long complaintId, Long complaintTypeId) {
        ComplaintToComplaintTypeMapEntity entity = new ComplaintToComplaintTypeMapEntity();
        entity.setComplaintId(complaintId);
        entity.setComplaintTypeId(complaintTypeId);
        entity.setDeleted(false);
        create(entity);
    }

    private void deleteComplaintTypeFromComplaint(Long mappingId) throws EntityRetrievalException {
        ComplaintToComplaintTypeMapEntity entity = getComplaintTypeToComplaintMapEntity(mappingId);
        entity.setDeleted(true);
        update(entity);
    }

    private ComplaintToComplaintTypeMapEntity getComplaintTypeToComplaintMapEntity(Long mappingId) throws EntityRetrievalException {
        ComplaintToComplaintTypeMapEntity entity = null;

        Query query = entityManager.createQuery("SELECT c "
                + "FROM ComplaintToComplaintTypeMapEntity c "
                + "LEFT JOIN FETCH c.complaintType "
                + "WHERE c.deleted = false "
                + "AND c.id = :mappingId",
                ComplaintToComplaintTypeMapEntity.class);
        query.setParameter("mappingId", mappingId);
        List<ComplaintToComplaintTypeMapEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate complaint-to-type map id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ComplaintToComplaintTypeMapEntity> getComplaintTypeMapEntities(Long complaintId) {
        Query query = entityManager.createQuery("SELECT c "
                + "FROM ComplaintToComplaintTypeMapEntity c "
                + "LEFT JOIN FETCH c.complaintType "
                + "WHERE c.deleted = false "
                + "AND c.complaintId = :complaintId",
                ComplaintToComplaintTypeMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintToComplaintTypeMapEntity> result = query.getResultList();
        return result;
    }

    private void saveListings(Complaint complaint) throws EntityRetrievalException {
        // Get the existing listing for this complaint
        List<ComplaintListingMapEntity> existingListings = getComplaintListingMapEntities(complaint.getId());

        deleteMissingListings(complaint, existingListings);
        addNewListings(complaint, existingListings);
    }

    private void addNewListings(Complaint complaint, List<ComplaintListingMapEntity> existingListings)
            throws EntityRetrievalException {
        // If there is a listing passed in and it does not exist in the DB, add it
        for (ComplaintListingMap passedIn : complaint.getListings()) {
            ComplaintListingMapEntity found = IterableUtils.find(existingListings,
                    new Predicate<ComplaintListingMapEntity>() {
                        @Override
                        public boolean evaluate(ComplaintListingMapEntity object) {
                            return object.getListingId().equals(passedIn.getListingId());
                        }
                    });
            // Wasn't found in the list from DB, add it to the DB
            if (found == null) {
                addListingToComplaint(complaint.getId(), passedIn.getListingId());
            }
        }
    }

    private void deleteMissingListings(Complaint complaint,
            List<ComplaintListingMapEntity> existingListings) throws EntityRetrievalException {
        // If the existing listing does not exist in the new list, delete it
        for (ComplaintListingMapEntity existing : existingListings) {
            ComplaintListingMap found = IterableUtils.find(complaint.getListings(),
                    new Predicate<ComplaintListingMap>() {
                        @Override
                        public boolean evaluate(ComplaintListingMap existingComplaintListing) {
                            return existingComplaintListing.getListingId().equals(existing.getListingId());
                        }
                    });
            // Wasn't found in the list passed in, delete it from the DB
            if (found == null) {
                deleteListingToComplaint(existing.getId());
            }
        }

    }

    private void addListingToComplaint(long complaintId, long listingId) {
        ComplaintListingMapEntity entity = new ComplaintListingMapEntity();
        entity.setComplaintId(complaintId);
        entity.setListingId(listingId);
        entity.setDeleted(false);

        create(entity);
    }

    private void deleteListingToComplaint(long id) throws EntityRetrievalException {
        ComplaintListingMapEntity entity = getComplaintListingMapEntity(id);
        entity.setDeleted(true);

        update(entity);
    }

    private ComplaintListingMapEntity getComplaintListingMapEntity(long id) throws EntityRetrievalException {
        ComplaintListingMapEntity entity = null;

        Query query = entityManager.createQuery("SELECT c "
                + "FROM ComplaintListingMapEntity c "
                + "LEFT JOIN FETCH c.listing "
                + "WHERE c.deleted = false "
                + "AND c.id = :complaintListingMapId",
                ComplaintListingMapEntity.class);
        query.setParameter("complaintListingMapId", id);
        List<ComplaintListingMapEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate complaint listing map id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ComplaintListingMapEntity> getComplaintListingMapEntities(long complaintId) {
        Query query = entityManager.createQuery("SELECT c "
                + "FROM ComplaintListingMapEntity c "
                + "LEFT JOIN FETCH c.listing "
                + "WHERE c.deleted = false "
                + "AND c.complaintId = :complaintId",
                ComplaintListingMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintListingMapEntity> result = query.getResultList();

        return result;
    }

    private void saveCritiera(Complaint complaint) throws EntityRetrievalException {
        // Get the existing listing for this complaint
        List<ComplaintCriterionMapEntity> existingCriteria = getComplaintCriterionMapEntities(complaint.getId());

        deleteMissingCriteria(complaint, existingCriteria);
        addNewCriteria(complaint, existingCriteria);
    }

    private void addNewCriteria(Complaint updatedComplaint, List<ComplaintCriterionMapEntity> existingCriteria)
            throws EntityRetrievalException {
        // If there is a listing passed in and it does not exist in the DB, add it
        for (ComplaintCriterionMap updatedComplaintCriterion : updatedComplaint.getCriteria()) {
            ComplaintCriterionMapEntity found = IterableUtils.find(existingCriteria,
                    new Predicate<ComplaintCriterionMapEntity>() {
                        @Override
                        public boolean evaluate(ComplaintCriterionMapEntity existingComplaintCriterionEntity) {
                            if (updatedComplaintCriterion.getCertificationCriterionId() != null) {
                                return updatedComplaintCriterion.getCertificationCriterionId()
                                        .equals(existingComplaintCriterionEntity.getCertificationCriterionId());
                            } else if (updatedComplaintCriterion.getCertificationCriterion() != null) {
                                return updatedComplaintCriterion.getCertificationCriterion().getId()
                                        .equals(existingComplaintCriterionEntity.getCertificationCriterionId());
                            }
                            return false;
                        }
                    });
            // Wasn't found in the list from DB, add it to the DB
            if (found == null) {
                addCriterionToComplaint(updatedComplaint.getId(), updatedComplaintCriterion.getCertificationCriterion().getId());
            }
        }
    }

    private void deleteMissingCriteria(Complaint updatedComplaint,
            List<ComplaintCriterionMapEntity> existingCriteria) throws EntityRetrievalException {
        // If the existing listing does not exist in the new list, delete it
        for (ComplaintCriterionMapEntity existingComplaintCriterionEntity : existingCriteria) {
            ComplaintCriterionMap found = IterableUtils.find(updatedComplaint.getCriteria(),
                    new Predicate<ComplaintCriterionMap>() {
                        @Override
                        public boolean evaluate(ComplaintCriterionMap updatedComplaintCriterion) {
                            if (updatedComplaintCriterion.getCertificationCriterionId() != null) {
                                return updatedComplaintCriterion.getCertificationCriterionId()
                                        .equals(existingComplaintCriterionEntity.getCertificationCriterionId());
                            } else if (updatedComplaintCriterion.getCertificationCriterion() != null) {
                                return updatedComplaintCriterion.getCertificationCriterion().getId()
                                        .equals(existingComplaintCriterionEntity.getCertificationCriterionId());
                            }
                            return false;
                        }
                    });
            // Wasn't found in the list passed in, delete it from the DB
            if (found == null) {
                deleteCriterionToComplaint(existingComplaintCriterionEntity.getId());
            }
        }

    }

    private void addCriterionToComplaint(long complaintId, long criterionId) {
        ComplaintCriterionMapEntity entity = new ComplaintCriterionMapEntity();
        entity.setComplaintId(complaintId);
        entity.setCertificationCriterionId(criterionId);
        entity.setDeleted(false);

        create(entity);
    }

    private void deleteCriterionToComplaint(long id) throws EntityRetrievalException {
        ComplaintCriterionMapEntity entity = getComplaintCriterionMapEntity(id);
        entity.setDeleted(true);

        update(entity);
    }

    private ComplaintCriterionMapEntity getComplaintCriterionMapEntity(long id) throws EntityRetrievalException {
        ComplaintCriterionMapEntity entity = null;

        Query query = entityManager.createQuery("SELECT c "
                + "FROM ComplaintCriterionMapEntity c "
                + "LEFT JOIN FETCH c.certificationCriterion criterion "
                + "LEFT JOIN FETCH criterion.certificationEdition "
                + "LEFT JOIN FETCH criterion.rule "
                + "WHERE c.deleted = false "
                + "AND c.id = :complaintCriterionMapId",
                ComplaintCriterionMapEntity.class);
        query.setParameter("complaintCriterionMapId", id);
        List<ComplaintCriterionMapEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate complaint criterion map id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ComplaintCriterionMapEntity> getComplaintCriterionMapEntities(long complaintId) {
        Query query = entityManager.createQuery("SELECT c "
                + "FROM ComplaintCriterionMapEntity c "
                + "LEFT JOIN FETCH c.certificationCriterion criterion "
                + "LEFT JOIN FETCH criterion.certificationEdition "
                + "LEFT JOIN FETCH criterion.rule "
                + "WHERE c.deleted = false "
                + "AND c.complaintId = :complaintId",
                ComplaintCriterionMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintCriterionMapEntity> result = query.getResultList();
        return result;
    }

    private List<Complaint> convertToComplaints(List<ComplaintEntity> complaintEntities) {
        return complaintEntities.stream()
                .map(entity -> entity.buildComplaint())
                .collect(Collectors.toList());
    }
}
