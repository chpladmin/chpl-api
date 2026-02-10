package gov.healthit.chpl.web.controller;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.astpai.AmazonTokenResponse;
import gov.healthit.chpl.astpai.AstpAiAuthenticationService;
import gov.healthit.chpl.astpai.AstpAiRequestFailedException;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingResultsUrlValidationRequest;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUploadResponse;
import gov.healthit.chpl.realworldtesting.manager.RealWorldTestingManager;
import gov.healthit.chpl.util.ServerEnvironment;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "real-world-testing", description = "Allows upload of a Real World Testing file.")
@RestController
@RequestMapping("/real-world-testing")
public class RealWorldTestingController {

    private AstpAiAuthenticationService authService;
    private RealWorldTestingManager realWorldTestingManager;
    private FF4j ff4j;
    private ServerEnvironment serverEnvironment;

    @Autowired
    public RealWorldTestingController(RealWorldTestingManager realWorldTestingManager,
            AstpAiAuthenticationService authService,
            FF4j ff4j,
            @Value("${server.environment}") String serverEnvironment) {
        this.realWorldTestingManager = realWorldTestingManager;
        this.authService = authService;
        this.ff4j = ff4j;
        this.serverEnvironment = serverEnvironment != null ? ServerEnvironment.getByName(serverEnvironment) : null;
    }

    @Operation(summary = "Upload a file with real world testing data for certified products.",
            description = "Accepts a CSV file with very specific fields to update listings with real world testing data. "
                    + "The file will be processed in the background and the user who submitted the file will be "
                    + "notified via email with the results"
                    + "Security Restrictions: User must have either role chpl-admin, chpl-onc, or chpl-onc-acb"
                    + " and administrative authority on the ONC-ACB(s) responsible for the product(s) in the file.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ResponseEntity<RealWorldTestingUploadResponse> upload(@RequestParam("file") final MultipartFile file)
            throws ValidationException, SchedulerException, UserRetrievalException {

        RealWorldTestingUploadResponse response = realWorldTestingManager.uploadRealWorldTestingCsv(file);
        return new ResponseEntity<RealWorldTestingUploadResponse>(response, HttpStatus.OK);
    }

    @Operation(summary = "Create and run a background job that fetches Real World Testing validation information "
            + "about any URL. The validation is expecting an RWT Results URL. Validation data will be emailed to the "
            + "logged-in user.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/validate-results-url", method = RequestMethod.POST)
    public @ResponseBody ChplOneTimeTrigger createAiValidationJob(@RequestBody RealWorldTestingResultsUrlValidationRequest request)
            throws UserRetrievalException, SchedulerException, ValidationException {
        if (!ff4j.check(FeatureList.RWT_AI_INTEGRATION)
                || this.serverEnvironment == null
                || !this.serverEnvironment.equals(ServerEnvironment.PRODUCITON)) {
            throw new NotImplementedException("This method has not been implemented");
        }
        return realWorldTestingManager.validateResultsUrlAsBackgroundJob(request);
    }

    @Operation(summary = "Create and run a background job that fetches Real World Testing validation information "
            + "about any URL. The validation is expecting an RWT Results URL. Validation data will be emailed to the "
            + "logged-in user.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/astp-ai-auth", method = RequestMethod.POST)
    public @ResponseBody AmazonTokenResponse authenticateToAspAi(@RequestBody RealWorldTestingResultsUrlValidationRequest request)
            throws AstpAiRequestFailedException {

        return authService.authenticate();
    }
}
