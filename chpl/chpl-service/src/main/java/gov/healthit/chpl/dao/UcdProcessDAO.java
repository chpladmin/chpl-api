package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.UcdProcessDTO;

public interface UcdProcessDAO {

    UcdProcessDTO create(UcdProcessDTO dto) throws EntityCreationException;

    UcdProcessDTO update(UcdProcessDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<UcdProcessDTO> findAll();

    UcdProcessDTO getById(Long id);

    UcdProcessDTO getByName(String name);
}
