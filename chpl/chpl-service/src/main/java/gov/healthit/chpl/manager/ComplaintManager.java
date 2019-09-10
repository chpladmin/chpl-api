package gov.healthit.chpl.manager;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.KeyValueModel;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;

public interface ComplaintManager {
    Set<KeyValueModel> getComplainantTypes();

    Set<KeyValueModel> getComplaintStatusTypes();

    List<Complaint> getAllComplaints();

    List<Complaint> getAllComplaintsBetweenDates(CertificationBodyDTO acb, Date startDate, Date endDate);

    Complaint create(Complaint complaint)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException;

    Complaint update(Complaint complaint)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException;

    void delete(Long complaintId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    ComplaintStatusTypeDTO getComplaintStatusType(String name);
}
