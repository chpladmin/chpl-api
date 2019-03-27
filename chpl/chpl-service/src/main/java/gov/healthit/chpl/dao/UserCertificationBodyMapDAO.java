package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface UserCertificationBodyMapDAO {
    UserCertificationBodyMapDTO create(UserCertificationBodyMapDTO dto) throws EntityRetrievalException;

    void delete(UserCertificationBodyMapDTO dto) throws EntityRetrievalException;

    List<UserCertificationBodyMapDTO> getByUserId(Long userId);

    List<UserCertificationBodyMapDTO> getByAcbId(Long acbId);

    UserCertificationBodyMapDTO getById(Long id);
}
