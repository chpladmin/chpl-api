package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Deprecated
public interface FilterDAO {
    FilterDTO update(FilterDTO dto) throws EntityRetrievalException;

    FilterDTO create(FilterDTO dto) throws EntityRetrievalException;

    List<FilterDTO> getByFilterType(FilterTypeDTO filterType);

    void delete(FilterDTO dto) throws EntityRetrievalException;

    FilterDTO getById(Long id) throws EntityRetrievalException;

    FilterTypeDTO getFilterTypeById(Long filterTypeId) throws EntityRetrievalException;

    List<FilterTypeDTO> getFilterTypes();
}
