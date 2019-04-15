package gov.healthit.chpl.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.dto.FilterTypeDTO;
import gov.healthit.chpl.manager.FilterManager;
import gov.healthit.chpl.web.controller.results.FilterResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "filters")
@RestController
@RequestMapping("/filters")
public class FilterController {

    private FilterManager filterManager;
    
    @Autowired
    public FilterController(final FilterManager filterManager) {
        this.filterManager = filterManager;
    }
    
    @ApiOperation(value = "List all filters based on the filter type for the current user.",
            notes = "Security Restrictions: Only filters owned by the current user will be returned")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody FilterResults getFiltersByFilterType(@RequestParam() final Long filterTypeId) {
        FilterResults results = new FilterResults();
        
        FilterTypeDTO filterTypeDTO = filterManager.getFilterType(filterTypeId);
        
        results.setResults(filterManager.getByFilterType(filterTypeDTO));
        
        return results;
    }
}
