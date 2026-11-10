package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.Measure;
import gov.healthit.chpl.listing.measure.MeasureManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "measures", description = "Endpoints related to Measures.")
@RestController
@RequestMapping("/measures")
public class MeasureController {
    private MeasureManager measureManager;
    private FF4j ff4j;

    @Autowired
    public MeasureController(MeasureManager measureManager,
            FF4j ff4j) {
        this.measureManager = measureManager;
        this.ff4j = ff4j;
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/measures", httpMethod = "GET", message = "This will be removed.",
        removalDate = "2027-01-01")
    @Operation(summary = "Retrieve all current Measures. ",
            description = "Returns all of the Measures that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<Measure> getAllMeasures() {
        if (ff4j.check(FeatureList.HTI_5_2027_01_01)) {
            throw new NotImplementedException();
        }

        return measureManager.getAll().stream().collect(Collectors.toList());
    }
}
