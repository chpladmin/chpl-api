package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.dto.ComplaintDTO;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplainantTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ComplaintDAO {
    List<ComplainantTypeDTO> getComplainantTypes();

    List<ComplaintStatusTypeDTO> getComplaintStatusTypes();

    List<ComplaintDTO> getAllComplaints();

    List<ComplaintDTO> getAllComplaintsBetweenDates(Long acbId, Date startDate, Date endDate);

    ComplaintDTO getComplaint(Long complaintId) throws EntityRetrievalException;

    ComplaintDTO create(ComplaintDTO complaintDTO) throws EntityRetrievalException;

    ComplaintDTO update(ComplaintDTO complaintDTO) throws EntityRetrievalException;

    void delete(ComplaintDTO complaintDTO) throws EntityRetrievalException;
}
