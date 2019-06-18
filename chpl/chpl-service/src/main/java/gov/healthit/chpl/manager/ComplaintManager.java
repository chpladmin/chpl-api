package gov.healthit.chpl.manager;

import java.util.List;
import java.util.Set;

import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;

public interface ComplaintManager {
    Set<KeyValueModel> getComplaintTypes();

    Set<KeyValueModel> getComplaintStatusTypes();

    List<Complaint> getAllComplaints();

    Complaint create(Complaint complaint) throws EntityRetrievalException, ValidationException;

    Complaint update(Complaint complaint) throws EntityRetrievalException, ValidationException;

    void delete(Long complaintId) throws EntityRetrievalException;

    ComplaintStatusTypeDTO getComplaintStatusType(String name);
}
