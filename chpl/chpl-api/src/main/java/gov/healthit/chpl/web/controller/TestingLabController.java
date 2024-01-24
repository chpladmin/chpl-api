package gov.healthit.chpl.web.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.TestingLabManager;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.TestingLabResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "atls", description = "Allows management of testing labs (ONC-ATLs).")
@RestController
@RequestMapping("/atls")
public class TestingLabController {
    private TestingLabManager atlManager;

    @Autowired
    public TestingLabController(TestingLabManager atlManager) {
        this.atlManager = atlManager;
    }

    @Operation(summary = "List all testing labs (ATLs).",
            description = "ROLE_ADMIN and ROLE_ONC can view and edit ONC-ATLs.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody TestingLabResults getAtls(
            @RequestParam(required = false, defaultValue = "false") boolean editable) {
        TestingLabResults results = new TestingLabResults();
        results.getAtls().addAll(atlManager.getAll());
        return results;
    }

    @Operation(summary = "Get details about a specific testing lab (ATL).",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{atlId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody TestingLab getAtlById(@PathVariable("atlId") final Long atlId)
            throws EntityRetrievalException {
        return atlManager.getById(atlId);
    }

    @Operation(summary = "Create a new testing lab.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC can create a new testing lab.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public TestingLab createAtl(@RequestBody final TestingLab atlInfo)
            throws InvalidArgumentsException, UserRetrievalException, EntityRetrievalException,
            EntityCreationException, JsonProcessingException {
        TestingLab toCreate = new TestingLab();
        toCreate.setAtlCode(atlInfo.getAtlCode());
        if (StringUtils.isEmpty(atlInfo.getName())) {
            throw new InvalidArgumentsException("A name is required for a testing lab");
        }
        toCreate.setName(atlInfo.getName());
        toCreate.setWebsite(atlInfo.getWebsite());

        if (atlInfo.getAddress() == null) {
            throw new InvalidArgumentsException("An address is required for a new testing lab");
        }
        toCreate.setAddress(atlInfo.getAddress());
        TestingLab created = atlManager.create(toCreate);
        return created;
    }

    @Operation(summary = "Update an existing ATL.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{atlId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ResponseEntity<TestingLab> updateAtl(@RequestBody TestingLab updatedAtl)
            throws InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException {
        TestingLab existingAtl = atlManager.getById(updatedAtl.getId());
        if (updatedAtl.isRetired()) {
            // we are retiring this ATL and no other changes can be made
            existingAtl.setRetired(true);
            existingAtl.setRetirementDay(updatedAtl.getRetirementDay());
            atlManager.retire(existingAtl);
        } else {
            if (existingAtl.isRetired()) {
                // unretire the ATL
                atlManager.unretire(updatedAtl.getId());
            }
            TestingLab toUpdate = new TestingLab();
            toUpdate.setId(updatedAtl.getId());
            toUpdate.setAtlCode(updatedAtl.getAtlCode());
            toUpdate.setRetired(false);
            toUpdate.setRetirementDay(null);
            if (StringUtils.isEmpty(updatedAtl.getName())) {
                throw new InvalidArgumentsException("A name is required for a testing lab");
            }
            toUpdate.setName(updatedAtl.getName());
            toUpdate.setWebsite(updatedAtl.getWebsite());

            if (updatedAtl.getAddress() == null) {
                throw new InvalidArgumentsException("An address is required to update the testing lab");
            }
            toUpdate.setAddress(updatedAtl.getAddress());
            atlManager.update(toUpdate);
        }

        TestingLab result = atlManager.getById(updatedAtl.getId());
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        return new ResponseEntity<TestingLab>(result, responseHeaders, HttpStatus.OK);
    }
}
