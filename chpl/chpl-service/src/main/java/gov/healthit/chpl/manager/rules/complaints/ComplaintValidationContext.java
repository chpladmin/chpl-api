package gov.healthit.chpl.manager.rules.complaints;

import gov.healthit.chpl.dao.ComplaintDAO;
import gov.healthit.chpl.dao.ComplaintDTO;

public class ComplaintValidationContext {
    private ComplaintDAO complaintDAO;
    private ComplaintDTO complaintDTO;

    public ComplaintValidationContext(final ComplaintDTO complaintDTO, final ComplaintDAO complaintDAO) {
        this.complaintDTO = complaintDTO;
        this.complaintDAO = complaintDAO;
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
}
