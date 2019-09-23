package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface UserDeveloperMapDAO {
    UserDeveloperMapDTO create(UserDeveloperMapDTO dto) throws EntityRetrievalException;

    void delete(UserDeveloperMapDTO dto) throws EntityRetrievalException;

    List<UserDeveloperMapDTO> getByUserId(Long userId);

    List<DeveloperDTO> getDevelopersByUserId(Long userId);

    List<UserDeveloperMapDTO> getByDeveloperId(Long developerId);

    UserDeveloperMapDTO getById(Long id);
}
