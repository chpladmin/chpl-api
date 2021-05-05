package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.ComplaintSurveillanceMap;
import gov.healthit.chpl.domain.complaint.ComplainantType;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.domain.complaint.ComplaintCriterionMap;
import gov.healthit.chpl.domain.complaint.ComplaintListingMap;
import gov.healthit.chpl.entity.ComplainantTypeEntity;
import gov.healthit.chpl.entity.ComplaintCriterionMapEntity;
import gov.healthit.chpl.entity.ComplaintEntity;
import gov.healthit.chpl.entity.ComplaintListingMapEntity;
import gov.healthit.chpl.entity.ComplaintSurveillanceMapEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceBasicEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class ComplaintDAO extends BaseDAOImpl {
    private static final String GET_COMPLAINTS_HQL = "SELECT DISTINCT c "
            + "FROM ComplaintEntity c "
            + "LEFT JOIN FETCH c.listings "
            + "LEFT JOIN FETCH c.surveillances "
            + "LEFT JOIN FETCH c.criteria "
            + "JOIN FETCH c.certificationBody acb "
            + "LEFT JOIN FETCH acb.address "
            + "JOIN FETCH c.complainantType "
            + "WHERE c.deleted = false ";

    private ChplProductNumberUtil chplProductNumberUtil;
    private CertificationCriterionDAO certificationCriterionDAO;

    @Autowired
    public ComplaintDAO(ChplProductNumberUtil chplProductNumberUtil,
            CertificationCriterionDAO certificationCriterionDAO) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.certificationCriterionDAO = certificationCriterionDAO;
    }

    public List<ComplainantType> getComplainantTypes() {
        List<ComplainantTypeEntity> entities = getComplainantTypeEntities();
        return entities.stream()
                .map(entity -> entity.buildComplainantType())
                .collect(Collectors.toList());
    }

    public List<Complaint> getAllComplaints() {
        Query query = entityManager.createQuery(GET_COMPLAINTS_HQL, ComplaintEntity.class);
        List<ComplaintEntity> results = query.getResultList();
        return populateComplaints(results);
    }

    public List<Complaint> getAllComplaintsBetweenDates(Long acbId, Date startDate, Date endDate) {
        Query query = entityManager.createQuery(GET_COMPLAINTS_HQL + " AND c.certificationBodyId = :acbId "
                + "AND c.receivedDate <= :endDate " + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate)",
                ComplaintEntity.class);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<ComplaintEntity> results = query.getResultList();
        return populateComplaints(results);
    }

    public Complaint getComplaint(Long complaintId) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintId);
        return entity.buildComplaint();
    }

    public Complaint create(Complaint complaint) throws EntityRetrievalException {
        ComplaintEntity entity = new ComplaintEntity();

        entity.setCertificationBodyId(complaint.getCertificationBody().getId());
        entity.setComplainantTypeId(complaint.getComplainantType().getId());
        entity.setComplainantTypeOther(complaint.getComplainantTypeOther());
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
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());

        create(entity);

        complaint.setId(entity.getId());
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
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);

        saveListings(complaint);
        saveCritiera(complaint);
        saveSurveillances(complaint);

        ComplaintEntity updatedEntity = getEntityById(complaint.getId());
        return updatedEntity.buildComplaint();
    }

    public void delete(Complaint complaint) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaint.getId());
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        entityManager.merge(entity);
        entityManager.flush();
        entityManager.clear();
    }

    private ComplaintEntity getEntityById(Long id) throws EntityRetrievalException {
        ComplaintEntity entity = null;
        Query query = entityManager.createQuery(GET_COMPLAINTS_HQL + " AND c.id = :complaintId", ComplaintEntity.class);
        query.setParameter("complaintId", id);
        List<ComplaintEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate complaint id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
            for (ComplaintListingMapEntity complaintListing : entity.getListings()) {
                populateChplProductNumber(complaintListing);
            }
            for (ComplaintCriterionMapEntity complaintCriteria : entity.getCriteria()) {
                try {
                    populateCertificationCriterion(complaintCriteria);
                } catch (Exception e) {
                    LOGGER.error("Error retreiving CertificationCriterion. - " + e.getMessage(), e);
                }
            }
            for (ComplaintSurveillanceMapEntity complaintSurveillance : entity.getSurveillances()) {
                try {
                    populateSurveillance(complaintSurveillance);
                } catch (Exception e) {
                    LOGGER.error("Error retreiving Surveillance. - " + e.getMessage(), e);
                }
            }
        }
        return entity;
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
        entity.setCreationDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        create(entity);
    }

    private void deleteSurveillanceToComplaint(long id) throws EntityRetrievalException {
        ComplaintSurveillanceMapEntity entity = getComplaintSurveillanceMapEntity(id);
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        update(entity);
    }

    private ComplaintSurveillanceMapEntity getComplaintSurveillanceMapEntity(long id)
            throws EntityRetrievalException {
        ComplaintSurveillanceMapEntity entity = null;

        Query query = entityManager.createQuery("FROM ComplaintSurveillanceMapEntity c " + "WHERE c.deleted = false "
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
        Query query = entityManager.createQuery("FROM ComplaintSurveillanceMapEntity c " + "WHERE c.deleted = false "
                + "AND c.complaintId = :complaintId", ComplaintSurveillanceMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintSurveillanceMapEntity> result = query.getResultList();

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
        entity.setCreationDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        create(entity);
    }

    private void deleteListingToComplaint(long id) throws EntityRetrievalException {
        ComplaintListingMapEntity entity = getComplaintListingMapEntity(id);
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        update(entity);
    }

    private ComplaintListingMapEntity getComplaintListingMapEntity(long id) throws EntityRetrievalException {
        ComplaintListingMapEntity entity = null;

        Query query = entityManager.createQuery("FROM ComplaintListingMapEntity c "
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
        Query query = entityManager.createQuery(
                "FROM ComplaintListingMapEntity c " + "WHERE c.deleted = false " + "AND c.complaintId = :complaintId",
                ComplaintListingMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintListingMapEntity> result = query.getResultList();

        return result;
    }

    private void saveCritiera(Complaint complaint) throws EntityRetrievalException {
        // Get the existing listing for this complaint
        List<ComplaintCriterionMapEntity> existingListings = getComplaintCriterionMapEntities(complaint.getId());

        deleteMissingCriteria(complaint, existingListings);
        addNewCriteria(complaint, existingListings);
    }

    private void addNewCriteria(Complaint complaint, List<ComplaintCriterionMapEntity> existingCriteria)
            throws EntityRetrievalException {
        // If there is a listing passed in and it does not exist in the DB, add it
        for (ComplaintCriterionMap passedIn : complaint.getCriteria()) {
            ComplaintCriterionMapEntity found = IterableUtils.find(existingCriteria,
                    new Predicate<ComplaintCriterionMapEntity>() {
                        @Override
                        public boolean evaluate(ComplaintCriterionMapEntity object) {
                            return object.getCertificationCriterionId().equals(passedIn.getCertificationCriterion().getId());
                        }
                    });
            // Wasn't found in the list from DB, add it to the DB
            if (found == null) {
                addCriterionToComplaint(complaint.getId(), passedIn.getCertificationCriterion().getId());
            }
        }
    }

    private void deleteMissingCriteria(Complaint complaint,
            List<ComplaintCriterionMapEntity> existingCriteria) throws EntityRetrievalException {
        // If the existing listing does not exist in the new list, delete it
        for (ComplaintCriterionMapEntity existing : existingCriteria) {
            ComplaintCriterionMap found = IterableUtils.find(complaint.getCriteria(),
                    new Predicate<ComplaintCriterionMap>() {
                        @Override
                        public boolean evaluate(ComplaintCriterionMap existingComplaintCriterion) {
                            return existingComplaintCriterion.getCertificationCriterionId()
                                    .equals(existing.getCertificationCriterionId());
                        }
                    });
            // Wasn't found in the list passed in, delete it from the DB
            if (found == null) {
                deleteCriterionToComplaint(existing.getId());
            }
        }

    }

    private void addCriterionToComplaint(long complaintId, long criterionId) {
        ComplaintCriterionMapEntity entity = new ComplaintCriterionMapEntity();
        entity.setComplaintId(complaintId);
        entity.setCertificationCriterionId(criterionId);
        entity.setDeleted(false);
        entity.setCreationDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        create(entity);
    }

    private void deleteCriterionToComplaint(long id) throws EntityRetrievalException {
        ComplaintCriterionMapEntity entity = getComplaintCriterionMapEntity(id);
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        update(entity);
    }

    private ComplaintCriterionMapEntity getComplaintCriterionMapEntity(long id) throws EntityRetrievalException {
        ComplaintCriterionMapEntity entity = null;

        Query query = entityManager.createQuery("FROM ComplaintCriterionMapEntity c "
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
        Query query = entityManager.createQuery("FROM ComplaintCriterionMapEntity c "
                + "WHERE c.deleted = false "
                + "AND c.complaintId = :complaintId",
                ComplaintCriterionMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintCriterionMapEntity> result = query.getResultList();

        return result;
    }

    private List<Complaint> populateComplaints(List<ComplaintEntity> complaintEntities) {
        List<Complaint> complaints = new ArrayList<Complaint>();
        for (ComplaintEntity entity : complaintEntities) {
            for (ComplaintListingMapEntity complaintListing : entity.getListings()) {
                populateChplProductNumber(complaintListing);
            }
            for (ComplaintCriterionMapEntity complaintCriteria : entity.getCriteria()) {
                try {
                    populateCertificationCriterion(complaintCriteria);
                } catch (Exception e) {
                    LOGGER.error("Error retreiving CertificationCriterion. - " + e.getMessage(), e);
                }
            }
            for (ComplaintSurveillanceMapEntity complaintSurveillance : entity.getSurveillances()) {
                try {
                    populateSurveillance(complaintSurveillance);
                } catch (Exception e) {
                    LOGGER.error("Error retreiving Surveillance. - " + e.getMessage(), e);
                }
            }
            complaints.add(entity.buildComplaint());
        }

        return complaints;
    }

    void populateChplProductNumber(ComplaintListingMapEntity complaintListing) {
        String chplProductNumber = chplProductNumberUtil.generate(complaintListing.getListingId());
        complaintListing.setChplProductNumber(chplProductNumber);
    }

    private void populateCertificationCriterion(ComplaintCriterionMapEntity complaintCriterion)
            throws EntityRetrievalException {
        complaintCriterion.setCertificationCriterion(
                certificationCriterionDAO.getEntityById(complaintCriterion.getCertificationCriterionId()));
    }

    private void populateSurveillance(ComplaintSurveillanceMapEntity complaintSurveillance)
            throws EntityRetrievalException {
        complaintSurveillance.setSurveillance(getSurveillanceBasicEntity(complaintSurveillance.getSurveillanceId()));
        String chplProductNumber = chplProductNumberUtil
                .generate(complaintSurveillance.getSurveillance().getCertifiedProductId());
        complaintSurveillance.getSurveillance().setChplProductNumber(chplProductNumber);
    }

    private SurveillanceBasicEntity getSurveillanceBasicEntity(long id) throws EntityRetrievalException {
        SurveillanceBasicEntity entity = null;

        Query query = entityManager.createQuery("FROM SurveillanceBasicEntity s "
                + "WHERE s.deleted = false "
                + "AND s.id = :surveillanceId",
                SurveillanceBasicEntity.class);
        query.setParameter("surveillanceId", id);
        List<SurveillanceBasicEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate surveillance id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }
}
