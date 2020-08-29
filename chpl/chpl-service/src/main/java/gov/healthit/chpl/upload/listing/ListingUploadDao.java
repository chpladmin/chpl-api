package gov.healthit.chpl.upload.listing;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Repository("listingUploadDao")
@Log4j2
public class ListingUploadDao extends BaseDAOImpl {

    public void create(ListingUpload uploadMetadata) {
        ListingUploadEntity toCreate = new ListingUploadEntity();
        toCreate.setCertificationBodyId(uploadMetadata.getAcb().getId());
        toCreate.setChplProductNumber(uploadMetadata.getChplProductNumber());
        toCreate.setErrorCount(uploadMetadata.getErrorCount());
        toCreate.setWarningCount(uploadMetadata.getWarningCount());
        String fileContents = null;
        try {
            fileContents = printToString(uploadMetadata.getRecords());
        } catch (IOException ex) {
            LOGGER.error("Could not print record list to string for " + uploadMetadata.getChplProductNumber(), ex);
        }
        toCreate.setFileContents(fileContents);
        toCreate.setDeleted(false);
        toCreate.setLastModifiedUser(AuthUtil.getAuditId());
        create(toCreate);
    }

    public void delete(Long listingUploadId) {
        ListingUploadEntity entity = entityManager.find(ListingUploadEntity.class, listingUploadId);
        if (entity != null) {
            entity.setDeleted(true);
        }
        update(entity);
    }

    public List<ListingUpload> getAll() {
        Query query = entityManager.createQuery("SELECT ul "
                + "FROM ListingUploadEntity ul "
                + "WHERE deleted = false", ListingUploadEntity.class);
        List<ListingUploadEntity> entities = query.getResultList();
        List<ListingUpload> allUploadedListings = entities.stream()
                .map(entity -> convert(entity))
                .collect(Collectors.<ListingUpload>toList());
        return allUploadedListings;
    }

    public List<ListingUpload> getAllByAcbs(List<Long> acbIds) {
        Query query = entityManager.createQuery("SELECT ul "
                + "FROM ListingUploadEntity ul "
                + "WHERE deleted = false "
                + "AND certificationBodyId IN (:acbIds)", ListingUploadEntity.class);
        query.setParameter("acbIds", acbIds);
        List<ListingUploadEntity> entities = query.getResultList();
        List<ListingUpload> allUploadedListings = entities.stream()
                .map(entity -> convert(entity))
                .collect(Collectors.<ListingUpload>toList());
        return allUploadedListings;
    }

    public ListingUpload getByChplProductNumber(String chplProductNumber) {
        Query query = entityManager.createQuery("SELECT ul "
                + "FROM ListingUploadEntity ul "
                + "WHERE deleted = false "
                + "AND chplProductNumber = :chplProductNumber ", ListingUploadEntity.class);
        query.setParameter("chplProductNumber", chplProductNumber);
        List<ListingUploadEntity> entities = query.getResultList();
        if (entities != null && entities.size() > 0) {
            return convert(entities.get(0));
        }
        return null;
    }

    private ListingUpload convert(ListingUploadEntity entity) {
        ListingUpload listingUpload = new ListingUpload();
        listingUpload.setId(entity.getId());
        CertificationBody acb = new CertificationBody();
        acb.setId(entity.getCertificationBodyId());
        if (entity.getCertificationBody() != null) {
            acb.setName(entity.getCertificationBody().getName());
        }
        listingUpload.setAcb(acb);
        listingUpload.setChplProductNumber(entity.getChplProductNumber());
        listingUpload.setErrorCount(entity.getErrorCount());
        listingUpload.setWarningCount(entity.getWarningCount());
        return listingUpload;
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
