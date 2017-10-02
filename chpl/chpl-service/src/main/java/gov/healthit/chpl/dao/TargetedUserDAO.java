package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.TargetedUserDTO;

public interface TargetedUserDAO {

    TargetedUserDTO create(TargetedUserDTO dto) throws EntityCreationException, EntityRetrievalException;

    TargetedUserDTO update(TargetedUserDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<TargetedUserDTO> findAll();

    TargetedUserDTO getById(Long id);

    TargetedUserDTO getByName(String name);

    TargetedUserDTO findOrCreate(Long id, String name) throws EntityCreationException;
}
