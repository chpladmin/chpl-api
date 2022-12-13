package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.Filter;
import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.FilterManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import gov.healthit.chpl.web.controller.results.FilterResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Deprecated
@Tag(name = "filters", description = "Allows management of user filters.")
@RestController
@RequestMapping("/filters")
public class FilterController {

    private FilterManager filterManager;
    private UserManager userManager;

    @Autowired
    public FilterController(final FilterManager filterManager, final UserManager userManager) {
        this.filterManager = filterManager;
        this.userManager = userManager;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/filters",
            message = "The filters functionality, also known as Saved Searches, is deprecated and will be removed from the CHPL.",
            removalDate = "2023-06-01")
    @Operation(summary = "List all filters based on the filter type for the current user.",
            description = "Security Restrictions: Only filters owned by the current user will be returned",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody FilterResults getFiltersByFilterType(@RequestParam() final Long filterTypeId) throws EntityRetrievalException {
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

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/filters",
            message = "The filters functionality, also known as Saved Searches, is deprecated and will be removed from the CHPL.",
            removalDate = "2023-06-01",
            httpMethod = "POST")
    @Operation(summary = "Save filter for the current user.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody Filter create(@RequestBody final Filter filter) throws EntityRetrievalException, UserRetrievalException, ValidationException {
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

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/filters/{filterId}",
            message = "The filters functionality, also known as Saved Searches, is deprecated and will be removed from the CHPL.",
            removalDate = "2023-06-01",
            httpMethod = "DELETE")
    @Operation(summary = "Deletes a filter.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{filterId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody String deleteFilter(@PathVariable("filterId") final Long filterId) throws EntityRetrievalException, ValidationException {
        FilterDTO filterDTO = filterManager.getByFilterId(filterId);
        filterManager.delete(filterDTO);
        return "{\"success\" : true}";
    }
}
