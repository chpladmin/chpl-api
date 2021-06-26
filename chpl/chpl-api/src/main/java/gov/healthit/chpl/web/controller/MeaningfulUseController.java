package gov.healthit.chpl.web.controller;

import java.io.IOException;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.MeaningfulUseManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "meaningful-use", description = "Allows upload of meaningful use file.")
@RestController
@RequestMapping("/meaningful_use")
@Loggable
public class MeaningfulUseController {
    private MeaningfulUseManager muuManager;

    @Autowired
    public MeaningfulUseController(MeaningfulUseManager muuManager) {
        this.muuManager = muuManager;
    }

    @Operation(summary = "Upload a file to update the number of meaningful use users for each CHPL Product Number",
            description = "Accepts a CSV file with chpl_product_number and num_meaningful_use_users to update the number of meaningful use users for each CHPL Product Number."
                    + " The user uploading the file must have ROLE_ADMIN, ROLE_ONC. ")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")

    public ChplOneTimeTrigger uploadMeaningfulUseUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam("accurate_as_of") Long accurateAsOfDate)
            throws EntityRetrievalException, EntityCreationException, ValidationException,
            SchedulerException, IOException, MaxUploadSizeExceededException {
        return muuManager.processUploadAsJob(file, accurateAsOfDate);
    }
}
