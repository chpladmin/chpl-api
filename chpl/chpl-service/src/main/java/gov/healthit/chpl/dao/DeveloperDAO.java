package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;

public interface DeveloperDAO {

	public DeveloperDTO create(DeveloperDTO dto) throws EntityCreationException, EntityRetrievalException;
	public DeveloperACBMapDTO createTransparencyMapping(DeveloperACBMapDTO dto);

	public DeveloperDTO update(DeveloperDTO dto) throws EntityRetrievalException, EntityCreationException;
	public void updateStatus(DeveloperStatusEventDTO newStatusHistory) throws EntityCreationException;	
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
	public List<DecertifiedDeveloperDTO> getDecertifiedDevelopers();
}
