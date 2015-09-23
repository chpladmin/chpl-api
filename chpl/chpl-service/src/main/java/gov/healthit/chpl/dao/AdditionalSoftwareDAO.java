package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.AdditionalSoftwareDTO;

import java.util.List;

public interface AdditionalSoftwareDAO {
	
	public void create(AdditionalSoftwareDTO dto) throws EntityCreationException;
	public void delete(Long id);
	public List<AdditionalSoftwareDTO> findAll();
	public List<AdditionalSoftwareDTO> findByCertifiedProductId(Long id);
	public AdditionalSoftwareDTO getById(Long id) throws EntityRetrievalException;
	public AdditionalSoftwareDTO getByName(String name);
	public void update(AdditionalSoftwareDTO dto) throws EntityRetrievalException;

}
