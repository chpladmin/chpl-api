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
import gov.healthit.chpl.manager.PromotingInteroperabilityManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "promoting-interoperability", description = "Allows upload of promoting interoperability user data.")
@RestController
@Log4j2
public class PromotingInteroperabilityController {
    private PromotingInteroperabilityManager piuManager;

    @Autowired
    public PromotingInteroperabilityController(PromotingInteroperabilityManager piuManager) {
        this.piuManager = piuManager;
    }

    @Operation(summary = "Upload a file to update the number of promoting interoperability users "
            + "for each CHPL Product Number",
            description = "Accepts a CSV file with chpl_product_number and user_count columns to update "
                    + "the number of promoting interoperability users for each CHPL Product Number."
                    + " The user uploading the file must have ROLE_ADMIN, ROLE_ONC. ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/promoting-interoperability/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger uploadPromotingInteroperabilityUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam("accurate_as_of") Long accurateAsOfDate)
            throws EntityRetrievalException, EntityCreationException, ValidationException,
            SchedulerException, IOException, MaxUploadSizeExceededException {
        return piuManager.processUploadAsJob(file, accurateAsOfDate);
    }

    @Deprecated
    @Operation(summary = "DEPRECATED. Use /promoting-interoperability/upload instead."
            + "Upload a file to update the number of meaningful use users for each CHPL Product Number",
            description = "Accepts a CSV file with chpl_product_number and num_meaningful_use_users to update the number of meaningful use users for each CHPL Product Number."
                    + " The user uploading the file must have ROLE_ADMIN, ROLE_ONC.",
            deprecated = true,
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/meaningful_use/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger uploadMeaningfulUseUsers(
            @RequestParam("file") MultipartFile file,
            @RequestParam("accurate_as_of") Long accurateAsOfDate)
            throws EntityRetrievalException, EntityCreationException, ValidationException,
            SchedulerException, IOException, MaxUploadSizeExceededException {
        return piuManager.processUploadAsJob(file, accurateAsOfDate);
    }
}
