package gov.healthit.chpl.manager.rules.complaints;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dao.ComplaintDTO;

public class ComplaintValidationContext {
    private ComplaintDAO complaintDAO;
    private ComplaintDTO complaintDTO;
    private CertifiedProductDAO certifiedProductDAO;

    public ComplaintValidationContext(final ComplaintDTO complaintDTO, final ComplaintDAO complaintDAO,
            final CertifiedProductDAO certifiedProductDAO) {
        this.complaintDTO = complaintDTO;
        this.complaintDAO = complaintDAO;
        this.setCertifiedProductDAO(certifiedProductDAO);
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
}
