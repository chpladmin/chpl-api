package gov.healthit.chpl.complaint.rules;

import gov.healthit.chpl.complaint.ComplaintDAO;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.dao.CertifiedProductDAO;
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
