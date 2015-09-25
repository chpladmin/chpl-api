package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.VendorEntity;

import java.util.List;

public interface VendorDAO {
	
	public VendorDTO create(VendorDTO dto) throws EntityCreationException, EntityRetrievalException;

	public VendorEntity update(VendorDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<VendorDTO> findAll();
	
	public VendorDTO getById(Long id) throws EntityRetrievalException;
	public VendorDTO getByName(String name);
	
}
