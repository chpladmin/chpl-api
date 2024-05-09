package gov.healthit.chpl.upload.listing;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Repository("listingUploadDao")
@Log4j2
public class ListingUploadDao extends BaseDAOImpl {
    private static final String GET_ENTITY_HQL_BEGIN = "SELECT ul "
            + "FROM ListingUploadEntity ul "
            + "LEFT JOIN FETCH ul.certificationBody acb "
            + "LEFT JOIN FETCH acb.address ";

    public ListingUpload create(ListingUpload uploadMetadata) {
        ListingUploadEntity toCreate = new ListingUploadEntity();
        toCreate.setCertificationBodyId(uploadMetadata.getAcb() != null ? uploadMetadata.getAcb().getId() : null);
        toCreate.setChplProductNumber(uploadMetadata.getChplProductNumber());
        toCreate.setCertificationDate(uploadMetadata.getCertificationDate());
        toCreate.setDeveloperName(uploadMetadata.getDeveloper());
        toCreate.setProductName(uploadMetadata.getProduct());
        toCreate.setVersionName(uploadMetadata.getVersion());
        toCreate.setErrorCount(uploadMetadata.getErrorCount());
        toCreate.setWarningCount(uploadMetadata.getWarningCount());
        toCreate.setStatus(ListingUploadStatus.UPLOAD_PROCESSING);
        String fileContents = null;
        try {
            fileContents = printToString(uploadMetadata.getRecords());
        } catch (IOException ex) {
            LOGGER.error("Could not print record list to string for " + uploadMetadata.getChplProductNumber(), ex);
        }
        toCreate.setFileContents(fileContents);
        toCreate.setDeleted(false);
        create(toCreate);

        return toCreate.toDomain();
    }

    public void updateErrorAndWarningCounts(ListingUpload listingUpload) {
        ListingUploadEntity entity = entityManager.find(ListingUploadEntity.class, listingUpload.getId());
        if (entity != null) {
            entity.setStatus(listingUpload.getStatus());
            entity.setErrorCount(listingUpload.getErrorCount());
            entity.setWarningCount(listingUpload.getWarningCount());
            update(entity);
        }
    }

    public void updateStatus(Long listingUploadId, ListingUploadStatus status) {
        ListingUploadEntity entity = entityManager.find(ListingUploadEntity.class, listingUploadId);
        if (entity != null) {
            entity.setStatus(status);
            update(entity);
        }
    }

    public void updateConfirmedListingId(Long listingUploadId, Long confirmedListingId) {
        ListingUploadEntity entity = entityManager.find(ListingUploadEntity.class, listingUploadId);
        if (entity != null) {
            entity.setCertifiedProductId(confirmedListingId);
            update(entity);
        }
    }

    public void delete(Long listingUploadId) {
        ListingUploadEntity entity = entityManager.find(ListingUploadEntity.class, listingUploadId);
        if (entity != null) {
            entity.setDeleted(true);
            update(entity);
        }
    }

    public boolean isAvailableForProcessing(Long id) {
        Query query = entityManager.createQuery("SELECT ul "
                + "FROM ListingUploadEntity ul "
                + "WHERE ul.id = :id "
                + "AND ul.status = :status "
                + "AND ul.deleted = false", ListingUploadEntity.class);
        query.setParameter("id", id);
        query.setParameter("status", ListingUploadStatus.UPLOAD_SUCCESS);
        List<ListingUploadEntity> availableUploadedListingsWithId = query.getResultList();
        return CollectionUtils.isNotEmpty(availableUploadedListingsWithId);
    }

    public List<ListingUpload> getAllProcessingAndAvailable() {
        Query query = entityManager.createQuery(GET_ENTITY_HQL_BEGIN
                + "WHERE ul.status NOT IN (:statuses) "
                + "AND ul.deleted = false", ListingUploadEntity.class);
        query.setParameter("statuses",
                Stream.of(ListingUploadStatus.CONFIRMED, ListingUploadStatus.REJECTED).collect(Collectors.toList()));
        List<ListingUploadEntity> entities = query.getResultList();
        List<ListingUpload> availableUploadedLisitngs = entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.<ListingUpload>toList());
        return availableUploadedLisitngs;
    }

    public List<ListingUpload> getAll() {
        Query query = entityManager.createQuery(GET_ENTITY_HQL_BEGIN
                + "WHERE ul.certifiedProductId IS NULL "
                + "AND ul.deleted = false", ListingUploadEntity.class);
        List<ListingUploadEntity> entities = query.getResultList();
        List<ListingUpload> allUploadedListings = entities.stream()
                .map(entity -> entity.toDomain())
                .collect(Collectors.<ListingUpload>toList());
        return allUploadedListings;
    }

    public ListingUpload getById(Long id) throws EntityRetrievalException {
        Query query = entityManager.createQuery(GET_ENTITY_HQL_BEGIN
                + "WHERE ul.id = :id "
                + "AND ul.deleted = false", ListingUploadEntity.class);
        query.setParameter("id", id);
        List<ListingUploadEntity> entities = query.getResultList();
        if (entities == null || entities.size() == 0) {
            throw new EntityRetrievalException();
        }
        return entities.get(0).toDomain();
    }

    ListingUploadEntity getEntityByIdIncludingDeleted(Long id) throws EntityRetrievalException {
        Query query = entityManager.createQuery(GET_ENTITY_HQL_BEGIN
                + "WHERE ul.id = :id ", ListingUploadEntity.class);
        query.setParameter("id", id);
        List<ListingUploadEntity> entities = query.getResultList();
        if (entities == null || entities.size() == 0) {
            throw new EntityRetrievalException();
        }
        return entities.get(0);
    }

    public ListingUpload getByIdIncludingRecords(Long id) throws EntityRetrievalException {
        Query query = entityManager.createQuery(GET_ENTITY_HQL_BEGIN
                + "WHERE ul.id = :id "
                + "AND ul.deleted = false", ListingUploadEntity.class);
        query.setParameter("id", id);
        List<ListingUploadEntity> entities = query.getResultList();
        if (entities == null || entities.size() == 0) {
            throw new EntityRetrievalException();
        }
        return entities.get(0).toDomainWithRecords();
    }

    public ListingUpload getByConfirmedListingIdIncludingRecords(Long confirmedListingId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(GET_ENTITY_HQL_BEGIN
                + "WHERE ul.certifiedProductId = :confirmedListingId "
                + "AND ul.deleted = false", ListingUploadEntity.class);
        query.setParameter("confirmedListingId", confirmedListingId);
        List<ListingUploadEntity> entities = query.getResultList();
        if (entities == null || entities.size() == 0) {
            throw new EntityRetrievalException("There is no upload file associated with listing ID " + confirmedListingId);
        }
        return entities.get(0).toDomainWithRecords();
    }

    public ListingUpload getByChplProductNumber(String chplProductNumber) {
        Query query = entityManager.createQuery(GET_ENTITY_HQL_BEGIN
                + "WHERE ul.deleted = false "
                + "AND ul.chplProductNumber = :chplProductNumber ", ListingUploadEntity.class);
        query.setParameter("chplProductNumber", chplProductNumber);
        List<ListingUploadEntity> entities = query.getResultList();
        if (entities != null && entities.size() > 0) {
            return entities.get(0).toDomain();
        }
        return null;
    }

    private String printToString(List<CSVRecord> csvRecords) throws IOException {
        StringWriter writer = new StringWriter();
        CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
        csvRecords.stream().forEach(record -> {
            try {
                printer.printRecord(record);
            } catch (IOException ex) {
                LOGGER.error("Could not print record " + record + " to stringwriter.", ex);
            }
        });
        printer.flush();
        printer.close();
        return writer.toString();
    }
}
