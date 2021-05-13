package gov.healthit.chpl.manager.rules.complaints;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.Data;

@Data
public class ComplaintValidationContext {
    private ComplaintDAO complaintDAO;
    private Complaint complaint;
    private CertifiedProductDAO certifiedProductDAO;
    private ErrorMessageUtil errorMessageUtil;
    private ChplProductNumberUtil chplProductNumberUtil;

    public ComplaintValidationContext(Complaint complaint, ComplaintDAO complaintDAO,
            CertifiedProductDAO certifiedProductDAO, ErrorMessageUtil errorMessageUtil,
            ChplProductNumberUtil chplProductNumberUtil) {
        this.complaint = complaint;
        this.complaintDAO = complaintDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.errorMessageUtil = errorMessageUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
    }
}
