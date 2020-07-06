package gov.healthit.chpl.upload.listing;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
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
}
