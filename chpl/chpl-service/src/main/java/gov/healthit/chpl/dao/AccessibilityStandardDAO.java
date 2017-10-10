package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.AccessibilityStandardDTO;

public interface AccessibilityStandardDAO {

    AccessibilityStandardDTO create(AccessibilityStandardDTO dto)
            throws EntityCreationException, EntityRetrievalException;

    AccessibilityStandardDTO update(AccessibilityStandardDTO dto) throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<AccessibilityStandardDTO> findAll();

    AccessibilityStandardDTO getById(Long id) throws EntityRetrievalException;

    AccessibilityStandardDTO getByName(String name);

    AccessibilityStandardDTO findOrCreate(Long id, String name) throws EntityCreationException;
}
