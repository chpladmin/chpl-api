package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplaintTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ComplaintDAO {
    List<ComplaintTypeDTO> getComplaintTypes();

    List<ComplaintStatusTypeDTO> getComplaintStatusTypes();

    List<ComplaintDTO> getAllComplaints() throws EntityRetrievalException;

    ComplaintDTO getComplaint(Long complaintId) throws EntityRetrievalException;

    ComplaintDTO create(ComplaintDTO complaintDTO) throws EntityRetrievalException;

    ComplaintDTO update(ComplaintDTO complaintDTO) throws EntityRetrievalException;

    void delete(ComplaintDTO complaintDTO) throws EntityRetrievalException;
}
