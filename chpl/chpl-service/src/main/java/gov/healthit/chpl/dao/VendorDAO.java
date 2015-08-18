package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.VendorDTO;

import java.util.List;

public interface VendorDAO {
	
	public void create(VendorDTO dto) throws EntityCreationException, EntityRetrievalException;

	public void update(VendorDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id);
	
	public List<VendorDTO> findAll();
	
	public VendorDTO getById(Long id) throws EntityRetrievalException;
	
}
