package gov.healthit.chpl.dao.statistics;

import java.text.ParseException;
import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ChartDataDTO;
import gov.healthit.chpl.dto.ChartDataStatTypeDTO;
import gov.healthit.chpl.entity.ChartDataEntity;

public interface ChartDataDAO {
	public ChartDataDTO create(ChartDataDTO acb) throws EntityRetrievalException, EntityCreationException, ParseException;
	public ChartDataDTO getById(Long id) throws EntityRetrievalException, ParseException;
	public ChartDataDTO update(ChartDataDTO dto) throws EntityRetrievalException, ParseException;
	public List<ChartDataDTO> findAllData() throws ParseException;
	public List<ChartDataStatTypeDTO> findAllTypes();
}
