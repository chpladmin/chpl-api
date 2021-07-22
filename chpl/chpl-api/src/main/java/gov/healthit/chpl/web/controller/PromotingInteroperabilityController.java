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
import gov.healthit.chpl.manager.PromotingInteroperabilityManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Api(value = "promoting-interoperability")
@RestController
@Loggable
@Log4j2
public class PromotingInteroperabilityController {
    private PromotingInteroperabilityManager piuManager;

    @Autowired
    public PromotingInteroperabilityController(PromotingInteroperabilityManager piuManager) {
        this.piuManager = piuManager;
    }

    @ApiOperation(value = "Upload a file to update the number of promoting interoperability users "
            + "for each CHPL Product Number",
            notes = "Accepts a CSV file with chpl_product_number and user_count columns to update "
                    + "the number of promoting interoperability users for each CHPL Product Number."
                    + " The user uploading the file must have ROLE_ADMIN, ROLE_ONC. ")
    @RequestMapping(value = "/promoting-interoperability/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger uploadPromotingInteroperabilityUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam("accurate_as_of") Long accurateAsOfDate)
            throws EntityRetrievalException, EntityCreationException, ValidationException,
            SchedulerException, IOException, MaxUploadSizeExceededException {
        return piuManager.processUploadAsJob(file, accurateAsOfDate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED. Use /promoting-interoperability/upload instead."
            + "Upload a file to update the number of meaningful use users for each CHPL Product Number",
            notes = "Accepts a CSV file with chpl_product_number and num_meaningful_use_users to update the number of meaningful use users for each CHPL Product Number."
                    + " The user uploading the file must have ROLE_ADMIN, ROLE_ONC. ")
    @RequestMapping(value = "/meaningful_use/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger uploadMeaningfulUseUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam("accurate_as_of") Long accurateAsOfDate)
            throws EntityRetrievalException, EntityCreationException, ValidationException,
            SchedulerException, IOException, MaxUploadSizeExceededException {
        return piuManager.processUploadAsJob(file, accurateAsOfDate);
    }
}
