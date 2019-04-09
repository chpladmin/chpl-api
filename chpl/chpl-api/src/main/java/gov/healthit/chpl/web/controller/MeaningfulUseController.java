package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.auth.AuthUtil;
import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.JobManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.util.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "meaningful-use")
@RestController
@RequestMapping("/meaningful_use")
public class MeaningfulUseController {
    private static final Logger LOGGER = LogManager.getLogger(MeaningfulUseController.class);
    private final JobTypeConcept allowedJobType = JobTypeConcept.MUU_UPLOAD;
    @Autowired
    private JobManager jobManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private FileUtils fileUtils;

    @ApiOperation(value = "Upload a file to update the number of meaningful use users for each CHPL Product Number",
            notes = "Accepts a CSV file with chpl_product_number and num_meaningful_use_users to update the number of meaningful use users for each CHPL Product Number."
                    + " The user uploading the file must have ROLE_ADMIN, ROLE_ONC. ")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public synchronized ResponseEntity<Job> uploadMeaningfulUseUsers(
            @RequestParam("file") final MultipartFile file,
            @RequestParam("accurate_as_of") final Long date)
            throws EntityRetrievalException, EntityCreationException, ValidationException,
            MaxUploadSizeExceededException {

        if (AuthUtil.getCurrentUser() == null || AuthUtil.getCurrentUser().getId() == null) {
            return new ResponseEntity<Job>(HttpStatus.UNAUTHORIZED);
        }

        if (file.isEmpty()) {
            throw new ValidationException("You cannot upload an empty file!");
        }

        // for now we'll only accept CSV
        if (!file.getContentType().equalsIgnoreCase("text/csv")
                && !file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
            throw new ValidationException("File must be a CSV document.");
        }

        // figure out the user
        UserDTO currentUser = null;
        try {
            currentUser = userManager.getById(AuthUtil.getCurrentUser().getId());
        } catch (final UserRetrievalException ex) {
            LOGGER.error("Error finding user with ID " + AuthUtil.getCurrentUser().getId() + ": " + ex.getMessage());
            return new ResponseEntity<Job>(HttpStatus.UNAUTHORIZED);
        }
        if (currentUser == null) {
            LOGGER.error("No user with ID " + AuthUtil.getCurrentUser().getId() + " could be found in the system.");
            return new ResponseEntity<Job>(HttpStatus.UNAUTHORIZED);
        }

        JobTypeDTO jobType = null;
        List<JobTypeDTO> jobTypes = jobManager.getAllJobTypes();
        for (JobTypeDTO jt : jobTypes) {
            if (jt.getName().equalsIgnoreCase(allowedJobType.getName())) {
                jobType = jt;
            }
        }

        String data = fileUtils.readFileAsString(file);
        JobDTO toCreate = new JobDTO();
        toCreate.setData(date.toString() + ";" + data);
        toCreate.setUser(currentUser);
        toCreate.setJobType(jobType);
        JobDTO insertedJob = jobManager.createJob(toCreate);
        JobDTO createdJob = jobManager.getJobById(insertedJob.getId());

        try {
            boolean isStarted = jobManager.start(createdJob);
            if (!isStarted) {
                return new ResponseEntity<Job>(new Job(createdJob), HttpStatus.BAD_REQUEST);
            } else {
                createdJob = jobManager.getJobById(insertedJob.getId());
            }
        } catch (final EntityRetrievalException ex) {
            LOGGER.error("Could not mark job " + createdJob.getId() + " as started.");
            return new ResponseEntity<Job>(new Job(createdJob), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // query the now running job
        return new ResponseEntity<Job>(new Job(createdJob), HttpStatus.OK);
    }
}
