package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.VendorDTO;

public interface VendorManager {
	public List<VendorDTO> getAll();
	public VendorDTO getById(Long id) throws EntityRetrievalException;
	public VendorDTO update(VendorDTO vendor) throws EntityRetrievalException;
	public VendorDTO create(VendorDTO dto) throws EntityRetrievalException, EntityCreationException;
	public void delete(VendorDTO dto) throws EntityRetrievalException;
	public void delete(Long vendorId) throws EntityRetrievalException;
}
