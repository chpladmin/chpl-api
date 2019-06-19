package gov.healthit.chpl.manager.rules.complaints;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;

public class ComplaintValidationContext {
    private ComplaintDAO complaintDAO;
    private ComplaintDTO complaintDTO;
    private CertifiedProductDAO certifiedProductDAO;
    private ErrorMessageUtil errorMessageUtil;
    private ChplProductNumberUtil chplProductNumberUtil;

    public ComplaintValidationContext(final ComplaintDTO complaintDTO, final ComplaintDAO complaintDAO,
            final CertifiedProductDAO certifiedProductDAO, final ErrorMessageUtil errorMessageUtil,
            final ChplProductNumberUtil chplProductNumberUtil) {
        this.complaintDTO = complaintDTO;
        this.complaintDAO = complaintDAO;
        this.certifiedProductDAO = certifiedProductDAO;
        this.errorMessageUtil = errorMessageUtil;
        this.chplProductNumberUtil = chplProductNumberUtil;
    }

    public ComplaintDAO getComplaintDAO() {
        return complaintDAO;
    }

    public void setComplaintDAO(ComplaintDAO complaintDAO) {
        this.complaintDAO = complaintDAO;
    }

    public ComplaintDTO getComplaintDTO() {
        return complaintDTO;
    }

    public void setComplaintDTO(ComplaintDTO complaintDTO) {
        this.complaintDTO = complaintDTO;
    }

    public CertifiedProductDAO getCertifiedProductDAO() {
        return certifiedProductDAO;
    }

    public void setCertifiedProductDAO(CertifiedProductDAO certifiedProductDAO) {
        this.certifiedProductDAO = certifiedProductDAO;
    }

    public ErrorMessageUtil getErrorMessageUtil() {
        return errorMessageUtil;
    }

    public void setErrorMessageUtil(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    public ChplProductNumberUtil getChplProductNumberUtil() {
        return chplProductNumberUtil;
    }

    public void setChplProductNmberUtil(ChplProductNumberUtil chplProductNumberUtil) {
        this.chplProductNumberUtil = chplProductNumberUtil;
    }
}
