package gov.healthit.chpl.manager.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.FilterDAO;
import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.FilterManager;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("filterManager")
public class FilterManagerImpl extends SecuredManager implements FilterManager {
    private FilterDAO filterDAO;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public FilterManagerImpl(final FilterDAO filterDAO, final ErrorMessageUtil errorMessageUtil) {
        this.filterDAO = filterDAO;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    @Transactional
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FILTER, "
            + "T(gov.healthit.chpl.permissions.domains.FilterDomainPermissions).GET_BY_FILTER_TYPE, filterObject)")
    public List<FilterDTO> getByFilterType(FilterTypeDTO filterTypeDTO) {
        return filterDAO.getByFilterType(filterTypeDTO);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FILTER, "
            + "T(gov.healthit.chpl.permissions.domains.FilterDomainPermissions).CREATE, #filterDTO)")
    public FilterDTO create(FilterDTO filterDTO) throws EntityRetrievalException, ValidationException {
        Set<String> errors = validateForCreate(filterDTO);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }
        return filterDAO.create(filterDTO);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FILTER, "
            + "T(gov.healthit.chpl.permissions.domains.FilterDomainPermissions).UPDATE, #filterDTO)")
    public FilterDTO update(FilterDTO filterDTO) throws EntityRetrievalException, ValidationException {
        Set<String> errors = validateForUpdate(filterDTO);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }
        return filterDAO.update(filterDTO);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FILTER, "
            + "T(gov.healthit.chpl.permissions.domains.FilterDomainPermissions).DELETE, #filterDTO)")
    public void delete(FilterDTO filterDTO) throws EntityRetrievalException, ValidationException {
        Set<String> errors = validateForDelete(filterDTO);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }
        filterDAO.delete(filterDTO);
    }

    @Override
    @Transactional
    public FilterTypeDTO getFilterType(Long filterTypeId) throws EntityRetrievalException {
        return filterDAO.getFilterTypeById(filterTypeId);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).FILTER, "
            + "T(gov.healthit.chpl.permissions.domains.FilterDomainPermissions).GET_BY_ID, #filterId)")
    public FilterDTO getByFilterId(Long filterId) throws EntityRetrievalException {
        return filterDAO.getById(filterId);
    }

    private Set<String> validateForCreate(FilterDTO filterDTO) {
        if (filterDTO == null) {
            Set<String> errors = new HashSet<String>();
            errors.add(errorMessageUtil.getMessage("filter.notPresent"));
            return errors;
        }
        return validate(filterDTO);
    }

    private Set<String> validateForUpdate(FilterDTO filterDTO) {
        if (filterDTO == null || filterDTO.getId() == null) {
            Set<String> errors = new HashSet<String>();
            errors.add(errorMessageUtil.getMessage("filter.notPresent"));
            return errors;
        }
        return validate(filterDTO);
    }

    private Set<String> validateForDelete(FilterDTO filterDTO) {
        Set<String> errors = new HashSet<String>();
        if (filterDTO == null || filterDTO.getId() == null) {
            errors.add(errorMessageUtil.getMessage("filter.notPresent"));
        }
        return errors;
    }

    private Set<String> validate(FilterDTO filterDTO) {
        Set<String> errors = new HashSet<String>();
        if (StringUtils.isEmpty(filterDTO.getFilter())) {
            errors.add(errorMessageUtil.getMessage("filter.empty"));
        }
        if (filterDTO.getUser() == null || filterDTO.getUser().getId() == null) {
            errors.add(errorMessageUtil.getMessage("filter.noUser"));
        }
        if (filterDTO.getFilterType() == null || filterDTO.getFilterType().getId() == null) {
            errors.add(errorMessageUtil.getMessage("filter.noFilterType"));
        }
        return errors;
    }
}
