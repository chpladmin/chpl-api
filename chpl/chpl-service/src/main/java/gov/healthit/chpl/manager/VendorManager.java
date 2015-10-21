package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.VendorDTO;

public interface VendorManager {
	public List<VendorDTO> getAll();
	public VendorDTO getById(Long id) throws EntityRetrievalException;
	public VendorDTO update(VendorDTO vendor) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public VendorDTO create(VendorDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public void delete(VendorDTO dto) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public void delete(Long vendorId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public VendorDTO merge(List<Long> vendorIdsToMerge, VendorDTO vendorToCreate) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
}
