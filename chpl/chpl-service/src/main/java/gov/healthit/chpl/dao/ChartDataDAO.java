package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.ChartDataDTO;

public interface ChartDataDAO {
	public ChartDataDTO create(ChartDataDTO acb) throws EntityRetrievalException, EntityCreationException;
	public ChartDataDTO getById(Long id) throws EntityRetrievalException;
	public ChartDataDTO update(ChartDataDTO dto) throws EntityRetrievalException;
	public List<ChartDataDTO> findAll();
}
