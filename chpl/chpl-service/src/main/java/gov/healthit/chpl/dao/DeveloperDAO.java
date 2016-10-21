package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;

import java.util.List;

public interface DeveloperDAO {

	public DeveloperDTO create(DeveloperDTO dto) throws EntityCreationException, EntityRetrievalException;
	public DeveloperACBMapDTO createTransparencyMapping(DeveloperACBMapDTO dto);

	public DeveloperDTO update(DeveloperDTO dto) throws EntityRetrievalException;
	public DeveloperDTO updateStatus(DeveloperDTO toUpdate) throws EntityRetrievalException;
	
	public void delete(Long id) throws EntityRetrievalException;

	public List<DeveloperDTO> findAll();
	public List<DeveloperDTO> findAllIncludingDeleted();

	public DeveloperDTO getById(Long id) throws EntityRetrievalException;
	public DeveloperDTO getByName(String name);
	public DeveloperDTO getByCode(String code);
	public DeveloperDTO getByVersion(Long productVersionId) throws EntityRetrievalException;
	public DeveloperACBMapDTO updateTransparencyMapping(DeveloperACBMapDTO dto);
	public void deleteTransparencyMapping(Long vendorId, Long acbId);
	public DeveloperACBMapDTO getTransparencyMapping(Long vendorId, Long acbId);
	public List<DeveloperACBMapDTO> getAllTransparencyMappings();
}
