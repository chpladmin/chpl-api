package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.entity.DeveloperEntity;

import java.util.List;

public interface DeveloperDAO {
	
	public DeveloperDTO create(DeveloperDTO dto) throws EntityCreationException, EntityRetrievalException;
	public DeveloperACBMapDTO createTransparencyMapping(DeveloperACBMapDTO dto);
	
	public DeveloperEntity update(DeveloperDTO dto) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;
	
	public List<DeveloperDTO> findAll();
	
	public DeveloperDTO getById(Long id) throws EntityRetrievalException;
	public DeveloperDTO getByName(String name);
	public DeveloperDTO getByCode(String code);
	public DeveloperDTO getByCertifiedProduct(CertifiedProductDTO cpDto) throws EntityRetrievalException;
	
	public DeveloperACBMapDTO updateTransparencyMapping(DeveloperACBMapDTO dto);
	public void deleteTransparencyMapping(Long vendorId, Long acbId);
	public DeveloperACBMapDTO getTransparencyMapping(Long vendorId, Long acbId);
}
