package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.domain.FuzzyChoices;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface FuzzyChoicesManager {
    String getTopFuzzyChoice(String query, FuzzyType type);

    List<String> getFuzzyChoicesByType(FuzzyType type)
            throws JsonParseException, JsonMappingException, EntityRetrievalException, IOException;

    FuzzyChoicesDTO getByType(FuzzyType type)
            throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException;

    Set<FuzzyChoices> getFuzzyChoices() throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException;

    FuzzyChoices updateFuzzyChoices(FuzzyChoicesDTO fuzzyChoicesDTO)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, IOException;
}
