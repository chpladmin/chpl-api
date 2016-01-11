package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.VendorACBMapDTO;
import gov.healthit.chpl.dto.VendorDTO;
import gov.healthit.chpl.entity.VendorEntity;

import java.util.List;

public interface VendorDAO {
	
	public VendorDTO create(VendorDTO dto) throws EntityCreationException, EntityRetrievalException;
	public VendorACBMapDTO createTransparencyMapping(VendorACBMapDTO dto);
	
	public VendorEntity update(VendorDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<VendorDTO> findAll();
	
	public VendorDTO getById(Long id) throws EntityRetrievalException;
	public VendorDTO getByName(String name);
	public VendorDTO getByCertifiedProduct(CertifiedProductDTO cpDto) throws EntityRetrievalException;
	
	public VendorACBMapDTO updateTransparencyMapping(VendorACBMapDTO dto);
	public void deleteTransparencyMapping(Long vendorId, Long acbId);
	public VendorACBMapDTO getTransparencyMapping(Long vendorId, Long acbId);
}
