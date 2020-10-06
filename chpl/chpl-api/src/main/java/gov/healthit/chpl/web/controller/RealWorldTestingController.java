package gov.healthit.chpl.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Api(value = "real-world-testing")
@RestController
@RequestMapping("/real-world-testing")
@Log4j2
public class RealWorldTestingController {

    @ApiOperation(value = "Upload a file with real world testing data for certified products.",
            notes = "Accepts a CSV file with very specific fields to update listings with real world testing data. "
                    + "The file will be processed in the background and the user who submitted the file will be "
                    + "notified via email with the results"
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and administrative authority "
                    + "on the ACB(s) responsible for the product(s) in the file.")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ResponseEntity<?> upload(@RequestParam("file") final MultipartFile file) {
        //SurveillanceUploadResult uploadResult = pendingSurveillanceManager.uploadPendingSurveillance(file);

        return new ResponseEntity<String>("", HttpStatus.OK);
    }
}
