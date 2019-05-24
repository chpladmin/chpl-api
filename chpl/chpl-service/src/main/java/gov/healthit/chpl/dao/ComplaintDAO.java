package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ComplaintStatusTypeDTO;
import gov.healthit.chpl.dto.ComplaintTypeDTO;

public interface ComplaintDAO {
    List<ComplaintTypeDTO> getComplaintTypes();

    List<ComplaintStatusTypeDTO> getComplaintStatusTypes();
}
