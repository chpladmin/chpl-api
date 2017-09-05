package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ChartDataDTO;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ChartDataManager {
	public ChartDataDTO create(ChartDataDTO cd) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public ChartDataDTO update(ChartDataDTO cd) throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;
	public List<ChartDataDTO> getAll();
}
