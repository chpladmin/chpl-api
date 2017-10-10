package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.DeveloperStatusDTO;

public interface DeveloperStatusDAO {
    List<DeveloperStatusDTO> findAll();

    DeveloperStatusDTO getById(Long id);

    DeveloperStatusDTO getByName(String name);
}
