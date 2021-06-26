package gov.healthit.chpl.web.controller;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUploadResponse;
import gov.healthit.chpl.realworldtesting.manager.RealWorldTestingManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "real-world-testing", description = "Allows upload of RWT file.")
@RestController
@RequestMapping("/real-world-testing")
@Loggable
public class RealWorldTestingController {

    private RealWorldTestingManager realWorldTestingManager;

    @Autowired
    public RealWorldTestingController(RealWorldTestingManager realWorldTestingManager) {
        this.realWorldTestingManager = realWorldTestingManager;
    }

    @Operation(summary = "Upload a file with real world testing data for certified products.",
            description = "Accepts a CSV file with very specific fields to update listings with real world testing data. "
                    + "The file will be processed in the background and the user who submitted the file will be "
                    + "notified via email with the results"
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative authority "
                    + "on the ACB(s) responsible for the product(s) in the file.")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ResponseEntity<RealWorldTestingUploadResponse> upload(@RequestParam("file") final MultipartFile file)
            throws ValidationException, SchedulerException, UserRetrievalException {

        RealWorldTestingUploadResponse response = realWorldTestingManager.uploadRealWorldTestingCsv(file);
        return new ResponseEntity<RealWorldTestingUploadResponse>(response, HttpStatus.OK);
    }
}
