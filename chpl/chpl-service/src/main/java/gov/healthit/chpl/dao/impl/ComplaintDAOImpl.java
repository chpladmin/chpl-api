package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.dto.ComplaintListingMapDTO;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.entity.ComplaintEntity;
import gov.healthit.chpl.entity.ComplaintListingMapEntity;
import gov.healthit.chpl.entity.ComplaintStatusTypeEntity;
import gov.healthit.chpl.entity.ComplaintTypeEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil;

@Component
public class ComplaintDAOImpl extends BaseDAOImpl implements ComplaintDAO {

    private ChplProductNumberUtil chplProductNumberUtil;

    @Autowired
    public ComplaintDAOImpl(final ChplProductNumberUtil chplProductNumberUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    @Override
    public List<ComplaintTypeDTO> getComplaintTypes() {
        List<ComplaintTypeEntity> entities = getComplaintTypeEntities();
        List<ComplaintTypeDTO> dtos = new ArrayList<ComplaintTypeDTO>();
        for (ComplaintTypeEntity entity : entities) {
            dtos.add(new ComplaintTypeDTO(entity));
        }
        return dtos;
    }

    @Override
    public List<ComplaintStatusTypeDTO> getComplaintStatusTypes() {
        List<ComplaintStatusTypeEntity> entities = getComplaintStatusTypeEntities();
        List<ComplaintStatusTypeDTO> dtos = new ArrayList<ComplaintStatusTypeDTO>();
        for (ComplaintStatusTypeEntity entity : entities) {
            dtos.add(new ComplaintStatusTypeDTO(entity));
        }
        return dtos;
    }

    @Override
    public List<ComplaintDTO> getAllComplaints() throws EntityRetrievalException {
        Query query = entityManager.createQuery("SELECT DISTINCT c FROM ComplaintEntity c "
                + "LEFT JOIN FETCH c.listings " + "JOIN FETCH c.certificationBody " + "JOIN FETCH c.complaintType "
                + "JOIN FETCH c.complaintStatusType " + "WHERE c.deleted = false ", ComplaintEntity.class);
        List<ComplaintEntity> results = query.getResultList();

        List<ComplaintDTO> complaintDTOs = new ArrayList<ComplaintDTO>();
        for (ComplaintEntity entity : results) {
            for (ComplaintListingMapEntity complaintListing : entity.getListings()) {
                populateChplProductNumber(complaintListing);
            }
            complaintDTOs.add(new ComplaintDTO(entity));
        }

        return complaintDTOs;
    }

    @Override
    public ComplaintDTO getComplaint(final Long complaintId) throws EntityRetrievalException {
        ComplaintEntity entity = getEntityById(complaintId);
        return new ComplaintDTO(entity);
    }

    @Override
    public ComplaintDTO create(ComplaintDTO complaintDTO) throws EntityRetrievalException {
        ComplaintEntity entity = new ComplaintEntity();

        entity.setCertificationBodyId(complaintDTO.getCertificationBody().getId());
        entity.setComplaintTypeId(complaintDTO.getComplaintType().getId());
        entity.setComplaintStatusTypeId(complaintDTO.getComplaintStatusType().getId());
        entity.setOncComplaintId(complaintDTO.getOncComplaintId());
        entity.setAcbComplaintId(complaintDTO.getAcbComplaintId());
        entity.setReceivedDate(complaintDTO.getReceivedDate());
        entity.setSummary(complaintDTO.getSummary());
        entity.setActions(complaintDTO.getActions());
        entity.setComplainantContacted(complaintDTO.isComplainantContacted());
        entity.setDeveloperContacted(complaintDTO.isDeveloperContacted());
        entity.setOncAtlContacted(complaintDTO.isOncAtlContacted());
        entity.setFlagForOncReview(complaintDTO.isFlagForOncReview());
        entity.setClosedDate(null);
        entity.setDeleted(false);
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setCreationDate(new Date());
        entity.setLastModifiedDate(new Date());

        create(entity);

        complaintDTO.setId(entity.getId());
        saveListings(complaintDTO);

        return new ComplaintDTO(getEntityById(entity.getId()));
    }

    @Override
    public ComplaintDTO update(ComplaintDTO complaintDTO) throws EntityRetrievalException {

        ComplaintEntity entity = getEntityById(complaintDTO.getId());
        entity.setCertificationBodyId(complaintDTO.getCertificationBody().getId());
        entity.setComplaintTypeId(complaintDTO.getComplaintType().getId());
        entity.setComplaintStatusTypeId(complaintDTO.getComplaintStatusType().getId());
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

        ComplaintEntity updatedEntity = getEntityById(complaintDTO.getId());
        return new ComplaintDTO(updatedEntity);
    }

    @Override
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

        Query query = entityManager.createQuery(
                "SELECT DISTINCT c FROM ComplaintEntity c " + "LEFT JOIN FETCH c.listings "
                        + "JOIN FETCH c.certificationBody " + "JOIN FETCH c.complaintType "
                        + "JOIN FETCH c.complaintStatusType " + "WHERE c.deleted = false " + "AND c.id = :complaintId",
                ComplaintEntity.class);
        query.setParameter("complaintId", id);
        List<ComplaintEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate complaint id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
            for (ComplaintListingMapEntity complaintListing : entity.getListings()) {
                populateChplProductNumber(complaintListing);
            }
        }
        return entity;
    }

    private List<ComplaintTypeEntity> getComplaintTypeEntities() {
        Query query = entityManager.createQuery("from ComplaintTypeEntity where (NOT deleted = true) ",
                ComplaintTypeEntity.class);
        List<ComplaintTypeEntity> result = query.getResultList();
        return result;
    }

    private List<ComplaintStatusTypeEntity> getComplaintStatusTypeEntities() {
        Query query = entityManager.createQuery("from ComplaintStatusTypeEntity where (NOT deleted = true) ",
                ComplaintStatusTypeEntity.class);
        List<ComplaintStatusTypeEntity> result = query.getResultList();
        return result;
    }

    private void saveListings(ComplaintDTO complaint) throws EntityRetrievalException {
        // Get the existing listing for this complaint
        List<ComplaintListingMapEntity> existingListings = getComplaintListings(complaint.getId());
        // If the existing listing does not exist in the new list, delete it
        for (ComplaintListingMapEntity existing : existingListings) {
            ComplaintListingMapDTO found = IterableUtils.find(complaint.getListings(),
                    new Predicate<ComplaintListingMapDTO>() {
                        @Override
                        public boolean evaluate(ComplaintListingMapDTO object) {
                            return object.getId().equals(existing.getCertifiedProductId());
                        }
                    });
            // Wasn't found in the list passed in, delete it from the DB
            if (found == null) {
                deleteListingToComplaint(existing.getId());
            }
        }

        for (ComplaintListingMapDTO passedIn : complaint.getListings()) {
            ComplaintListingMapEntity found = IterableUtils.find(existingListings,
                    new Predicate<ComplaintListingMapEntity>() {
                        @Override
                        public boolean evaluate(ComplaintListingMapEntity object) {
                            return object.getCertifiedProductId().equals(passedIn.getId());
                        }
                    });
            // Wasn't found in the list from DB, add it to the DB
            if (found == null) {
                addListingToComplaint(complaint.getId(), passedIn.getCertifiedProductId());
            }
        }
    }

    private void addListingToComplaint(final long complaintId, final long listingId) {
        ComplaintListingMapEntity entity = new ComplaintListingMapEntity();
        entity.setComplaintId(complaintId);
        entity.setCertifiedProductId(listingId);
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

    private List<ComplaintListingMapEntity> getComplaintListings(final long complaintId) {
        Query query = entityManager.createQuery(
                "FROM ComplaintListingMapEntity c " + "WHERE c.deleted = false " + "AND c.complaintId = :complaintId",
                ComplaintListingMapEntity.class);
        query.setParameter("complaintId", complaintId);
        List<ComplaintListingMapEntity> result = query.getResultList();

        return result;
    }

    private void populateChplProductNumber(ComplaintListingMapEntity complaintListing) {
        String chplProductNumber = chplProductNumberUtil.generate(complaintListing.getCertifiedProductId());
        complaintListing.setChplProductNumber(chplProductNumber);
    }
}
