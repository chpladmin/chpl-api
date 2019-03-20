package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface UserTestingLabMapDAO {
    UserTestingLabMapDTO create(UserTestingLabMapDTO dto) throws EntityRetrievalException;

    UserTestingLabMapDTO update(UserTestingLabMapDTO dto) throws EntityRetrievalException;

    void delete(UserTestingLabMapDTO dto) throws EntityRetrievalException;

    List<UserTestingLabMapDTO> getByUserId(Long userId);

    List<UserTestingLabMapDTO> getByAtlId(Long acbId);

    UserTestingLabMapDTO getById(Long id);
}
