package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.ComplainantTypeDTO;
import gov.healthit.chpl.dto.ComplaintCriterionMapDTO;
import gov.healthit.chpl.dto.ComplaintDTO;
import gov.healthit.chpl.dto.ComplaintListingMapDTO;
import gov.healthit.chpl.dto.ComplaintSurveillanceMapDTO;
import gov.healthit.chpl.entity.ComplainantTypeEntity;
import gov.healthit.chpl.entity.ComplaintCriterionMapEntity;
import gov.healthit.chpl.entity.ComplaintEntity;
import gov.healthit.chpl.entity.ComplaintListingMapEntity;
import gov.healthit.chpl.entity.ComplaintSurveillanceMapEntity;
import gov.healthit.chpl.entity.surveillance.SurveillanceBasicEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;

@Component
public class ComplaintDAO extends BaseDAOImpl {
    private static final Logger LOGGER = LogManager.getLogger(ComplaintDAO.class);
    private static final String GET_COMPLAINTS_HQL = "SELECT DISTINCT c " + "FROM ComplaintEntity c "
            + "LEFT JOIN FETCH c.listings " + "LEFT JOIN FETCH c.surveillances " + "LEFT JOIN FETCH c.criteria "
            + "JOIN FETCH c.certificationBody " + "JOIN FETCH c.complainantType "
            + "WHERE c.deleted = false ";

    private ChplProductNumberUtil chplProductNumberUtil;
    private CertificationCriterionDAO certificationCriterionDAO;

    @Autowired
    public ComplaintDAO(final ChplProductNumberUtil chplProductNumberUtil,
            final CertificationCriterionDAO certificationCriterionDAO) {
        this.chplProductNumberUtil = chplProductNumberUtil;
        this.certificationCriterionDAO = certificationCriterionDAO;
    }

    public List<ComplainantTypeDTO> getComplainantTypes() {
        List<ComplainantTypeEntity> entities = getComplainantTypeEntities();
        List<ComplainantTypeDTO> dtos = new ArrayList<ComplainantTypeDTO>();
        for (ComplainantTypeEntity entity : entities) {
            dtos.add(new ComplainantTypeDTO(entity));
        }
        return dtos;
    }

    public List<ComplaintDTO> getAllComplaints() {
        Query query = entityManager.createQuery(GET_COMPLAINTS_HQL, ComplaintEntity.class);
        List<ComplaintEntity> results = query.getResultList();
        return populateComplaintDTOs(results);
    }

    public List<ComplaintDTO> getAllComplaintsBetweenDates(final Long acbId, final Date startDate, final Date endDate) {
        Query query = entityManager.createQuery(GET_COMPLAINTS_HQL + " AND c.certificationBodyId = :acbId "
                + "AND c.receivedDate <= :endDate " + "AND (c.closedDate IS NULL OR c.closedDate >= :startDate)",
                ComplaintEntity.class);
        query.setParameter("acbId", acbId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);

        List<ComplaintEntity> results = query.getResultList();
        return populateComplaintDTOs(results);
    }

    public ComplaintDTO getComplaint(final Long complaintId) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintId);
        return new ComplaintDTO(entity);
    }

    public ComplaintDTO create(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        ComplaintEntity entity = new ComplaintEntity();

        entity.setCertificationBodyId(complaintDTO.getCertificationBody().getId());
        entity.setComplainantTypeId(complaintDTO.getComplainantType().getId());
        entity.setComplainantTypeOther(complaintDTO.getComplainantTypeOther());
        entity.setOncComplaintId(complaintDTO.getOncComplaintId());
        entity.setAcbComplaintId(complaintDTO.getAcbComplaintId());
        entity.setReceivedDate(complaintDTO.getReceivedDate());
        entity.setSummary(complaintDTO.getSummary());
        entity.setActions(complaintDTO.getActions());
        entity.setComplainantContacted(complaintDTO.isComplainantContacted());
        entity.setDeveloperContacted(complaintDTO.isDeveloperContacted());
        entity.setOncAtlContacted(complaintDTO.isOncAtlContacted());
        entity.setFlagForOncReview(complaintDTO.isFlagForOncReview());
        entity.setClosedDate(complaintDTO.getClosedDate());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());

        create(entity);

        complaintDTO.setId(entity.getId());
        saveListings(complaintDTO);
        saveCritiera(complaintDTO);
        saveSurveillances(complaintDTO);

        return new ComplaintDTO(getEntityById(entity.getId()));
    }

    public ComplaintDTO update(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintDTO.getId());
        entity.setCertificationBodyId(complaintDTO.getCertificationBody().getId());
        entity.setComplainantTypeId(complaintDTO.getComplainantType().getId());
        entity.setComplainantTypeOther(complaintDTO.getComplainantTypeOther());
        entity.setOncComplaintId(complaintDTO.getOncComplaintId());
        entity.setAcbComplaintId(complaintDTO.getAcbComplaintId());
        entity.setReceivedDate(complaintDTO.getReceivedDate());
        entity.setSummary(complaintDTO.getSummary());
        entity.setActions(complaintDTO.getActions());
        entity.setComplainantContacted(complaintDTO.isComplainantContacted());
        entity.setDeveloperContacted(complaintDTO.isDeveloperContacted());
        entity.setOncAtlContacted(complaintDTO.isOncAtlContacted());
        entity.setFlagForOncReview(complaintDTO.isFlagForOncReview());
        entity.setClosedDate(complaintDTO.getClosedDate());
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        update(entity);

        saveListings(complaintDTO);
        saveCritiera(complaintDTO);
        saveSurveillances(complaintDTO);

        ComplaintEntity updatedEntity = getEntityById(complaintDTO.getId());
        return new ComplaintDTO(updatedEntity);
    }

    public void delete(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintDTO.getId());
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

    private void saveSurveillances(final ComplaintDTO complaint) throws EntityRetrievalException {
        // Get the existing surveillances for this complaint
        List<ComplaintSurveillanceMapEntity> existingSurveillances = getComplaintSurveillanceMapEntities(
                complaint.getId());

        deleteMissingSurveillances(complaint, existingSurveillances);
        addNewSurveillances(complaint, existingSurveillances);
    }

    private void addNewSurveillances(final ComplaintDTO complaint,
            final List<ComplaintSurveillanceMapEntity> existingSurveillances) throws EntityRetrievalException {
        // If there is a surveillance passed in and it does not exist in the DB, add it
        for (ComplaintSurveillanceMapDTO passedIn : complaint.getSurveillances()) {
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

    private void deleteMissingSurveillances(final ComplaintDTO complaint,
            final List<ComplaintSurveillanceMapEntity> existingSurveillances) throws EntityRetrievalException {
        // If the existing surveillance does not exist in the new list, delete it
        for (ComplaintSurveillanceMapEntity fromDb : existingSurveillances) {
            ComplaintSurveillanceMapDTO found = IterableUtils.find(complaint.getSurveillances(),
                    new Predicate<ComplaintSurveillanceMapDTO>() {
                        @Override
                        public boolean evaluate(ComplaintSurveillanceMapDTO passedIn) {
                            return passedIn.getSurveillance().getId().equals(fromDb.getSurveillanceId());
                        }
                    });
            // Wasn't found in the list passed in, delete it from the DB
            if (found == null) {
                deleteSurveillanceToComplaint(fromDb.getId());
            }
        }

    }

    private void addSurveillanceToComplaint(final long complaintId, final long surveillanceId) {
        ComplaintSurveillanceMapEntity entity = new ComplaintSurveillanceMapEntity();
        entity.setComplaintId(complaintId);
        entity.setSurveillanceId(surveillanceId);
        entity.setDeleted(false);
        entity.setCreationDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        create(entity);
    }

    private void deleteSurveillanceToComplaint(final long id) throws EntityRetrievalException {
        ComplaintSurveillanceMapEntity entity = getComplaintSurveillanceMapEntity(id);
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        update(entity);
    }

    private ComplaintSurveillanceMapEntity getComplaintSurveillanceMapEntity(final long id)
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

    private List<ComplaintSurveillanceMapEntity> getComplaintSurveillanceMapEntities(final long complaintId) {
        Query query = entityManager.createQuery("FROM ComplaintSurveillanceMapEntity c " + "WHERE c.deleted = false "
                + "AND c.complaintId = :complaintId", ComplaintSurveillanceMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintSurveillanceMapEntity> result = query.getResultList();

        return result;
    }

    private void saveListings(final ComplaintDTO complaint) throws EntityRetrievalException {
        // Get the existing listing for this complaint
        List<ComplaintListingMapEntity> existingListings = getComplaintListingMapEntities(complaint.getId());

        deleteMissingListings(complaint, existingListings);
        addNewListings(complaint, existingListings);
    }

    private void addNewListings(final ComplaintDTO complaint, final List<ComplaintListingMapEntity> existingListings)
            throws EntityRetrievalException {
        // If there is a listing passed in and it does not exist in the DB, add it
        for (ComplaintListingMapDTO passedIn : complaint.getListings()) {
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

    private void deleteMissingListings(final ComplaintDTO complaint,
            final List<ComplaintListingMapEntity> existingListings) throws EntityRetrievalException {
        // If the existing listing does not exist in the new list, delete it
        for (ComplaintListingMapEntity existing : existingListings) {
            ComplaintListingMapDTO found = IterableUtils.find(complaint.getListings(),
                    new Predicate<ComplaintListingMapDTO>() {
                        @Override
                        public boolean evaluate(ComplaintListingMapDTO existingComplaintListing) {
                            return existingComplaintListing.getListingId().equals(existing.getListingId());
                        }
                    });
            // Wasn't found in the list passed in, delete it from the DB
            if (found == null) {
                deleteListingToComplaint(existing.getId());
            }
        }

    }

    private void addListingToComplaint(final long complaintId, final long listingId) {
        ComplaintListingMapEntity entity = new ComplaintListingMapEntity();
        entity.setComplaintId(complaintId);
        entity.setListingId(listingId);
        entity.setDeleted(false);
        entity.setCreationDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        create(entity);
    }

    private void deleteListingToComplaint(final long id) throws EntityRetrievalException {
        ComplaintListingMapEntity entity = getComplaintListingMapEntity(id);
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        update(entity);
    }

    private ComplaintListingMapEntity getComplaintListingMapEntity(final long id) throws EntityRetrievalException {
        ComplaintListingMapEntity entity = null;

        Query query = entityManager.createQuery(
                "FROM ComplaintListingMapEntity c " + "WHERE c.deleted = false " + "AND c.id = :complaintListingMapId",
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

    private List<ComplaintListingMapEntity> getComplaintListingMapEntities(final long complaintId) {
        Query query = entityManager.createQuery(
                "FROM ComplaintListingMapEntity c " + "WHERE c.deleted = false " + "AND c.complaintId = :complaintId",
                ComplaintListingMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintListingMapEntity> result = query.getResultList();

        return result;
    }

    private void saveCritiera(final ComplaintDTO complaint) throws EntityRetrievalException {
        // Get the existing listing for this complaint
        List<ComplaintCriterionMapEntity> existingListings = getComplaintCriterionMapEntities(complaint.getId());

        deleteMissingCriteria(complaint, existingListings);
        addNewCriteria(complaint, existingListings);
    }

    private void addNewCriteria(final ComplaintDTO complaint, final List<ComplaintCriterionMapEntity> existingCriteria)
            throws EntityRetrievalException {
        // If there is a listing passed in and it does not exist in the DB, add it
        for (ComplaintCriterionMapDTO passedIn : complaint.getCriteria()) {
            ComplaintCriterionMapEntity found = IterableUtils.find(existingCriteria,
                    new Predicate<ComplaintCriterionMapEntity>() {
                        @Override
                        public boolean evaluate(ComplaintCriterionMapEntity object) {
                            return object.getCertificationCriterionId().equals(passedIn.getCertificationCriterionId());
                        }
                    });
            // Wasn't found in the list from DB, add it to the DB
            if (found == null) {
                addCriterionToComplaint(complaint.getId(), passedIn.getCertificationCriterionId());
            }
        }
    }

    private void deleteMissingCriteria(final ComplaintDTO complaint,
            final List<ComplaintCriterionMapEntity> existingCriteria) throws EntityRetrievalException {
        // If the existing listing does not exist in the new list, delete it
        for (ComplaintCriterionMapEntity existing : existingCriteria) {
            ComplaintCriterionMapDTO found = IterableUtils.find(complaint.getCriteria(),
                    new Predicate<ComplaintCriterionMapDTO>() {
                        @Override
                        public boolean evaluate(ComplaintCriterionMapDTO existingComplaintCriterion) {
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

    private void addCriterionToComplaint(final long complaintId, final long criterionId) {
        ComplaintCriterionMapEntity entity = new ComplaintCriterionMapEntity();
        entity.setComplaintId(complaintId);
        entity.setCertificationCriterionId(criterionId);
        entity.setDeleted(false);
        entity.setCreationDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        create(entity);
    }

    private void deleteCriterionToComplaint(final long id) throws EntityRetrievalException {
        ComplaintCriterionMapEntity entity = getComplaintCriterionMapEntity(id);
        entity.setDeleted(true);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        update(entity);
    }

    private ComplaintCriterionMapEntity getComplaintCriterionMapEntity(final long id) throws EntityRetrievalException {
        ComplaintCriterionMapEntity entity = null;

        Query query = entityManager.createQuery("FROM ComplaintCriterionMapEntity c " + "WHERE c.deleted = false "
                + "AND c.id = :complaintCriterionMapId", ComplaintCriterionMapEntity.class);
        query.setParameter("complaintCriterionMapId", id);
        List<ComplaintCriterionMapEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate complaint criterion map id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<ComplaintCriterionMapEntity> getComplaintCriterionMapEntities(final long complaintId) {
        Query query = entityManager.createQuery(
                "FROM ComplaintCriterionMapEntity c " + "WHERE c.deleted = false " + "AND c.complaintId = :complaintId",
                ComplaintCriterionMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintCriterionMapEntity> result = query.getResultList();

        return result;
    }

    private List<ComplaintDTO> populateComplaintDTOs(final List<ComplaintEntity> complaintEntities) {
        List<ComplaintDTO> complaintDTOs = new ArrayList<ComplaintDTO>();
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
            complaintDTOs.add(new ComplaintDTO(entity));
        }

        return complaintDTOs;
    }

    private void populateChplProductNumber(final ComplaintListingMapEntity complaintListing) {
        String chplProductNumber = chplProductNumberUtil.generate(complaintListing.getListingId());
        complaintListing.setChplProductNumber(chplProductNumber);
    }

    private void populateCertificationCriterion(final ComplaintCriterionMapEntity complaintCriterion)
            throws EntityRetrievalException {
        complaintCriterion.setCertificationCriterion(
                certificationCriterionDAO.getEntityById(complaintCriterion.getCertificationCriterionId()));
    }

    private void populateSurveillance(final ComplaintSurveillanceMapEntity complaintSurveillance)
            throws EntityRetrievalException {
        complaintSurveillance.setSurveillance(getSurveillanceBasicEntity(complaintSurveillance.getSurveillanceId()));
        String chplProductNumber = chplProductNumberUtil
                .generate(complaintSurveillance.getSurveillance().getCertifiedProductId());
        complaintSurveillance.getSurveillance().setChplProductNumber(chplProductNumber);
    }

    private SurveillanceBasicEntity getSurveillanceBasicEntity(final long id) throws EntityRetrievalException {
        SurveillanceBasicEntity entity = null;

        Query query = entityManager.createQuery(
                "FROM SurveillanceBasicEntity s " + "WHERE s.deleted = false " + "AND s.id = :surveillanceId",
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
