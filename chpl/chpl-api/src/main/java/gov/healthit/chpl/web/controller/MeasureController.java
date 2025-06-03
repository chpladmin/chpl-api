package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.domain.MeasureType;
import gov.healthit.chpl.listing.measure.MeasureManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "measures", description = "Endpoints related to Measures.")
@RestController
@RequestMapping("/measures")
public class MeasureController {
    private MeasureManager measureManager;

    @Autowired
    public MeasureController(MeasureManager measureManager) {
        this.measureManager = measureManager;
    }

    @Operation(summary = "Retrieve all current Measures. ",
            description = "Returns all of the Measures that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<Measure> getAllTargetedUsers() {
        return measureManager.getAll().stream().collect(Collectors.toList());
    }

    @Operation(summary = "Retrieve all current Measure Types. ",
            description = "Returns all of the Measures that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/measure-types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<MeasureType> getAllMeasureTypes() {
        return measureManager.getMeasureTypes().stream().collect(Collectors.toList());
    }
}
