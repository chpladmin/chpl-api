package gov.healthit.chpl.manager;

import java.util.List;
import java.util.Set;

import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;

public interface ComplaintManager {
    Set<KeyValueModel> getComplaintTypes();

    Set<KeyValueModel> getComplaintStatusTypes();

    List<ComplaintDTO> getAllComplaints();

    ComplaintDTO create(ComplaintDTO complaintDTO) throws EntityRetrievalException, ValidationException;

    ComplaintDTO update(ComplaintDTO complaintDTO) throws EntityRetrievalException, ValidationException;

    void delete(Long complaintId) throws EntityRetrievalException;

    ComplaintStatusTypeDTO getComplaintStatusType(String name);
}
