package gov.healthit.chpl.manager.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.FilterDAO;
import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.FilterManager;

@Component("filterManager")
public class FilterManagerImpl implements FilterManager {
    private FilterDAO filterDAO;

    @Autowired
    public FilterManagerImpl(final FilterDAO filterDAO) {
        this.filterDAO = filterDAO;
    }

    @Override
    public List<FilterDTO> getByFilterType(FilterTypeDTO filterTypeDTO) {
        return filterDAO.getByFilterType(filterTypeDTO);
    }

    @Override
    public FilterDTO create(FilterDTO filterDTO) throws EntityRetrievalException, ValidationException {
        Set<String> errors = validateForCreate(filterDTO);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }
        return null;
    }

    private Set<String> validateForCreate(FilterDTO filterDTO) {
        Set<String> errors = new HashSet<String>();
        if (filterDTO == null) {
            errors.add("No filter object was present to be saved.");
            return errors;
        }

        if (StringUtils.isEmpty(filterDTO.getFilter())) {
            errors.add("Filter cannot be empty.");
        }

        if (filterDTO.getUser() == null || filterDTO.getUser().getId() == null) {
            errors.add("The filter does not have an associated user.");
        }

        if (filterDTO.getFilterType() == null || filterDTO.getFilterType().getId() == null) {
            errors.add("The filter does not have an associated filter type.");
        }
        return errors;
    }

}
