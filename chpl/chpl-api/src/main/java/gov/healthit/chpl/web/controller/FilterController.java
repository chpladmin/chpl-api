package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.Filter;
import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.FilterManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.web.controller.results.FilterResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "filters")
@RestController
@RequestMapping("/filters")
@Loggable
public class FilterController {

    private FilterManager filterManager;
    private UserManager userManager;

    @Autowired
    public FilterController(final FilterManager filterManager, final UserManager userManager) {
        this.filterManager = filterManager;
        this.userManager = userManager;
    }

    @ApiOperation(value = "List all filters based on the filter type for the current user.",
            notes = "Security Restrictions: Only filters owned by the current user will be returned")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody FilterResults getFiltersByFilterType(@RequestParam() final Long filterTypeId)
    		throws EntityRetrievalException {
        FilterResults results = new FilterResults();
        FilterTypeDTO filterTypeDTO = filterManager.getFilterType(filterTypeId);
        List<Filter> filters = new ArrayList<Filter>();
        List<FilterDTO> dtos = filterManager.getByFilterType(filterTypeDTO);
        for (FilterDTO dto : dtos) {
            filters.add(new Filter(dto));
        }
        results.setResults(filters);
        return results;
    }

    @ApiOperation(value = "Save filter for the current user.",
            notes = "")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody Filter create(@RequestBody final Filter filter)
    		throws EntityRetrievalException, UserRetrievalException, ValidationException {
        FilterDTO dto = new FilterDTO();
        UserDTO userDTO = userManager.getById(AuthUtil.getCurrentUser().getId());
        FilterTypeDTO filterTypeDTO = filterManager.getFilterType(filter.getFilterType().getId());

        dto.setName(filter.getName());
        dto.setFilter(filter.getFilter());
        dto.setUser(userDTO);
        dto.setFilterType(filterTypeDTO);

        dto = filterManager.create(dto);
        return new Filter(dto);
    }

    @ApiOperation(value = "Deletes a filter.",
            notes = "")
    @RequestMapping(value = "/{filterId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody String deleteFilter(@PathVariable("filterId") final Long filterId)
    		throws EntityRetrievalException, ValidationException {
        FilterDTO filterDTO = filterManager.getByFilterId(filterId);
        filterManager.delete(filterDTO);
        return "{\"success\" : true}";
    }
}
