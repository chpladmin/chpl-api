package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.DeveloperStatusDTO;

public interface DeveloperStatusDAO {
	public List<DeveloperStatusDTO> findAll();
	public DeveloperStatusDTO getById(Long id);
	public DeveloperStatusDTO getByName(String name);
}
