package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ActivityConceptDTO;

public interface ActivityConceptDAO {

    ActivityConceptDTO create(ActivityConceptDTO dto) throws EntityCreationException, EntityRetrievalException;

    ActivityConceptDTO update(ActivityConceptDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    ActivityConceptDTO getById(Long id) throws EntityRetrievalException;

    List<ActivityConceptDTO> findAll();

}
