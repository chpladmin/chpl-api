package gov.healthit.chpl.upload.listing;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.util.AuthUtil;
import lombok.extern.log4j.Log4j2;

@Repository("listingUploadDao")
@Log4j2
public class ListingUploadDao extends BaseDAOImpl {

    public void create(ListingUpload uploadMetadata, String fileContents) {
        ListingUploadEntity toCreate = new ListingUploadEntity();
        toCreate.setCertificationBodyId(uploadMetadata.getAcb().getId());
        toCreate.setChplProductNumber(uploadMetadata.getChplProductNumber());
        toCreate.setErrorCount(uploadMetadata.getErrorCount());
        toCreate.setWarningCount(uploadMetadata.getWarningCount());
        toCreate.setFileContents(fileContents);
        toCreate.setDeleted(false);
        toCreate.setLastModifiedUser(AuthUtil.getAuditId());
        create(toCreate);
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
}
