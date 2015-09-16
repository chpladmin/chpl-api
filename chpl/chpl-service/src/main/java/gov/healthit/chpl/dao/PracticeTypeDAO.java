package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.PracticeTypeDTO;

import java.util.List;

public interface PracticeTypeDAO {
	
	public void create(PracticeTypeDTO dto) throws EntityCreationException, EntityRetrievalException;

	public void update(PracticeTypeDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id);
	
	public List<PracticeTypeDTO> findAll();
	
	public PracticeTypeDTO getById(Long id) throws EntityRetrievalException;
	
	public PracticeTypeDTO getByName(String name);
	
}
