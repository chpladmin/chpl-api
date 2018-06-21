package gov.healthit.chpl.upload.certifiedProduct;

import java.util.List;

import org.apache.commons.csv.CSVRecord;

import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.InvalidArgumentsException;

public interface CertifiedProductUploadHandler {
    public PendingCertifiedProductEntity handle() throws InvalidArgumentsException;

    public List<CQMCriterion> getApplicableCqmCriterion(List<CQMCriterion> allCqms);

    public List<CSVRecord> getRecord();

    public void setRecord(final List<CSVRecord> record);

    public CSVRecord getHeading();

    public void setHeading(final CSVRecord heading);
    
    public UploadTemplateVersionDTO getUploadTemplateVersion();
    
    public void setUploadTemplateVersion(UploadTemplateVersionDTO template);
}
