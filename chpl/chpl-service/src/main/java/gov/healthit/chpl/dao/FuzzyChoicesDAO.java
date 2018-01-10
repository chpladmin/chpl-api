package gov.healthit.chpl.dao;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.entity.FuzzyType;

public interface FuzzyChoicesDAO {
    FuzzyChoicesDTO create(FuzzyChoicesDTO acb) throws EntityRetrievalException, EntityCreationException, JsonParseException, JsonMappingException, IOException;
    FuzzyChoicesDTO getByType(FuzzyType type) throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException;
    FuzzyChoicesDTO update(FuzzyChoicesDTO fuzzyChoicesDTO) throws EntityRetrievalException, EntityCreationException, JsonParseException, JsonMappingException, IOException;
    List<FuzzyChoicesDTO> findAllTypes() throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException;
}
