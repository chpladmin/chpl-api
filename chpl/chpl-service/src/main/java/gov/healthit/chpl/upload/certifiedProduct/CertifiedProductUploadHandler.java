package gov.healthit.chpl.upload.certifiedProduct;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.InvalidArgumentsException;

public interface CertifiedProductUploadHandler {
    PendingCertifiedProductEntity handle() throws InvalidArgumentsException;

    List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms);

    List<CSVRecord> getRecord();

    void setRecord(List<CSVRecord> record);

    CSVRecord getHeading();

    void setHeading(CSVRecord heading);

    UploadTemplateVersionDTO getUploadTemplateVersion();

    void setUploadTemplateVersion(UploadTemplateVersionDTO template);
}
