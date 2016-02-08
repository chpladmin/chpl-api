package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.AdditionalSoftwareDTO;

import java.util.List;

public interface AdditionalSoftwareDAO {
	
	public AdditionalSoftwareDTO create(AdditionalSoftwareDTO dto) throws EntityCreationException;
	public void delete(Long id);
	public void deleteByCertifiedProduct(Long productId);
	public List<AdditionalSoftwareDTO> findAll();
	public List<AdditionalSoftwareDTO> findByCertifiedProductId(Long id);
	public List<AdditionalSoftwareDTO> findByCertificationResultId(Long id);
	public List<AdditionalSoftwareDTO> findByCQMResultId(Long id);
	public AdditionalSoftwareDTO getById(Long id) throws EntityRetrievalException;
	public AdditionalSoftwareDTO getByName(String name);
	public void update(AdditionalSoftwareDTO dto) throws EntityRetrievalException;

}
