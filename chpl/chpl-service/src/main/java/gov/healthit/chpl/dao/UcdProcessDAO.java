package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Interface for database access to UCD Processes.
 * @author kekey
 *
 */
public interface UcdProcessDAO {

    UcdProcessDTO update(UcdProcessDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    UcdProcessDTO findOrCreate(Long id, String name) throws EntityCreationException;
    List<UcdProcessDTO> findAll();

    UcdProcessDTO getById(Long id);

    UcdProcessDTO getByName(String name);
}
