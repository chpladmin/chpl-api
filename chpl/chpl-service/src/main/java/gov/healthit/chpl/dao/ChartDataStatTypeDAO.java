package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.ChartDataStatTypeDTO;

import java.util.List;

public interface ChartDataStatTypeDAO {
	public ChartDataStatTypeDTO getById(Long id) throws EntityRetrievalException;
	public List<ChartDataStatTypeDTO> getAll();
	public ChartDataStatTypeDTO getByName(String typeName);
}
