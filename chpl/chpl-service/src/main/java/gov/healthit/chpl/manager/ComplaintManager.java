package gov.healthit.chpl.manager;

import java.util.List;
import java.util.Set;

import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ComplaintManager {
    Set<KeyValueModel> getComplaintTypes();

    Set<KeyValueModel> getComplaintStatusTypes();

    List<ComplaintDTO> getAllComplaints();

    ComplaintDTO create(ComplaintDTO complaintDTO);

    ComplaintDTO update(ComplaintDTO complaintDTO) throws EntityRetrievalException;

    void delete(ComplaintDTO complaintDTO) throws EntityRetrievalException;
}
