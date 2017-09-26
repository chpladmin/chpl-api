package gov.healthit.chpl.dao.statistics;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ChartDataDTO;
import gov.healthit.chpl.dto.ChartDataStatTypeDTO;
import gov.healthit.chpl.entity.ChartDataEntity;

public interface ChartDataDAO {
	public ChartDataDTO create(ChartDataDTO acb) throws EntityRetrievalException, EntityCreationException;
	public ChartDataDTO getById(Long id) throws EntityRetrievalException;
	public ChartDataDTO update(ChartDataDTO dto) throws EntityRetrievalException;
	public List<ChartDataDTO> findAllData();
	public List<ChartDataStatTypeDTO> findAllTypes();
}
