package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.svap.domain.Svap;
import gov.healthit.chpl.svap.manager.SvapManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "svaps")
@RestController
@RequestMapping("/svaps")
public class SvapController {

    private SvapManager svapManager;

    @Autowired
    public SvapController(SvapManager svapManager) {
        this.svapManager = svapManager;
    }

    @ApiOperation(value = "Update an SVAP.",
            notes = "NEED TO ADD DESCRIPTION"
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.")
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = "application/json; charset=utf-8")
    public @ResponseBody Svap updateSvap(@RequestBody(required = true) Svap svap) throws EntityRetrievalException, ValidationException {
        return svapManager.update(svap);
    }

    @ApiOperation(value = "Create an SVAP.",
            notes = "NEED TO ADD DESCRIPTION"
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.")
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = "application/json; charset=utf-8")
    public @ResponseBody Svap createSvap(@RequestBody(required = true) Svap svap) throws EntityRetrievalException {
        return svapManager.create(svap);
    }

    @ApiOperation(value = "Delete an SVAP.",
            notes = "NEED TO ADD DESCRIPTION"
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.")
    @RequestMapping(value = "", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = "application/json; charset=utf-8")
    public void deleteSvap(@RequestBody(required = true) Svap svap) throws EntityRetrievalException, ValidationException {
        svapManager.delete(svap);
    }

    @ApiOperation(value = "Update an SVAP.",
            notes = "NEED TO ADD DESCRIPTION"
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<Svap> getAllSvaps() {
        return svapManager.getAll();
    }
}
