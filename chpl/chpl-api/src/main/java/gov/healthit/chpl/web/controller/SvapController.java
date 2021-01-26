package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.exception.EntityRetrievalException;
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

    @ApiOperation(value = "Get all SVAPs.",
            notes = "NEED TO ADD DESCRIPTION"
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.")
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = "application/json; charset=utf-8")
    public Svap updateSvap(Svap svap) throws EntityRetrievalException {
        return svapManager.update(svap);
    }

    @ApiOperation(value = "Update an SVAP.",
            notes = "NEED TO ADD DESCRIPTION"
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.")
    @RequestMapping(value = "", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = "application/json; charset=utf-8")
    public List<Svap> getAllSvaps() {
        return svapManager.getAll();
    }
}
