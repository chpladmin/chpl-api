package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;

public interface FilterManager {
    List<FilterDTO> getByFilterType(FilterTypeDTO filterTypeDTO);

    FilterDTO getByFilterId(Long filterId) throws EntityRetrievalException;

    FilterDTO create(FilterDTO filterDTO) throws EntityRetrievalException, ValidationException;

    FilterDTO update(FilterDTO filterDTO) throws EntityRetrievalException, ValidationException;

    void delete(FilterDTO filterDTO) throws EntityRetrievalException, ValidationException;

    FilterTypeDTO getFilterType(Long filterTypeId) throws EntityRetrievalException;
}
