package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.AccessibilityStandardDTO;

public interface AccessibilityStandardDAO {

	public AccessibilityStandardDTO create(AccessibilityStandardDTO dto) throws EntityCreationException, EntityRetrievalException;
	public AccessibilityStandardDTO update(AccessibilityStandardDTO dto) throws EntityRetrievalException;
	public void delete(Long id) throws EntityRetrievalException;

	public List<AccessibilityStandardDTO> findAll();
	public AccessibilityStandardDTO getById(Long id) throws EntityRetrievalException;
	public AccessibilityStandardDTO getByName(String name) ;
	public AccessibilityStandardDTO findOrCreate(Long id, String name) throws EntityCreationException;
}
