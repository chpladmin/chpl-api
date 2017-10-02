package gov.healthit.chpl.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.AccurateAsOfDate;
import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.domain.concept.JobTypeConcept;
import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.JobManager;
import gov.healthit.chpl.manager.MeaningfulUseManager;
import gov.healthit.chpl.web.controller.exception.ValidationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="meaningful-use")
@RestController
@RequestMapping("/meaningful_use")
public class MeaningfulUseController {
	private static final Logger LOGGER = LogManager.getLogger(MeaningfulUseController.class);
	private final JobTypeConcept allowedJobType = JobTypeConcept.MUU_UPLOAD;
	@Autowired CertifiedProductManager cpManager;
	@Autowired MeaningfulUseManager muManager;
	@Autowired JobManager jobManager;
	@Autowired UserManager userManager;

	@ApiOperation(value="Upload a file to update the number of meaningful use users for each CHPL Product Number",
			notes="Accepts a CSV file with chpl_product_number and num_meaningful_use_users to update the number of meaningful use users for each CHPL Product Number."
					+ " The user uploading the file must have ROLE_ADMIN or ROLE_ONC_STAFF ")
	@RequestMapping(value="/upload", method = RequestMethod.POST,
			produces="application/json; charset = utf-8")
	public synchronized ResponseEntity<Job> uploadMeaningfulUseUsers(@RequestParam("file") MultipartFile file)
			throws EntityRetrievalException, EntityCreationException,
			ValidationException, MaxUploadSizeExceededException {

		if(Util.getCurrentUser() == null || Util.getCurrentUser().getId() == null) {
			return new ResponseEntity<Job>(HttpStatus.UNAUTHORIZED);
		}

		if (file.isEmpty()) {
			throw new ValidationException("You cannot upload an empty file!");
		}

		//for now we'll only accept CSV
		if(!file.getContentType().equalsIgnoreCase("text/csv") &&
				!file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
			throw new ValidationException("File must be a CSV document.");
		}

		//figure out the user
		UserDTO currentUser = null;
		try {
			currentUser = userManager.getById(Util.getCurrentUser().getId());
		} catch(final UserRetrievalException ex) {
			LOGGER.error("Error finding user with ID " + Util.getCurrentUser().getId() + ": " + ex.getMessage());
			return new ResponseEntity<Job>(HttpStatus.UNAUTHORIZED);
		}
		if(currentUser == null) {
			LOGGER.error("No user with ID " + Util.getCurrentUser().getId() + " could be found in the system.");
			return new ResponseEntity<Job>(HttpStatus.UNAUTHORIZED);
		}

		JobTypeDTO jobType = null;
		List<JobTypeDTO> jobTypes = jobManager.getAllJobTypes();
		for(JobTypeDTO jt : jobTypes) {
			if(jt.getName().equalsIgnoreCase(allowedJobType.getName())) {
				jobType = jt;
			}
		}

		//read the file into a string
		StringBuffer data = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			String line = null;
			while((line = reader.readLine()) != null) {
				if(data.length() > 0) {
					data.append(System.getProperty("line.separator"));
				}
				data.append(line);
			}
		} catch(final IOException ex) {
			String msg = "Could not read file: " + ex.getMessage();
			LOGGER.error(msg);
			throw new ValidationException(msg);
		}

		JobDTO toCreate = new JobDTO();
		toCreate.setData(data.toString());
		toCreate.setUser(currentUser);
		toCreate.setJobType(jobType);
		JobDTO insertedJob = jobManager.createJob(toCreate);
		JobDTO createdJob = jobManager.getJobById(insertedJob.getId());

		try {
			boolean isStarted = jobManager.start(createdJob);
			if(!isStarted) {
				return new ResponseEntity<Job>(new Job(createdJob), HttpStatus.BAD_REQUEST);
			} else {
				createdJob = jobManager.getJobById(insertedJob.getId());
			}
		} catch(final EntityRetrievalException ex) {
			LOGGER.error("Could not mark job " + createdJob.getId() + " as started.");
			return new ResponseEntity<Job>(new Job(createdJob), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		//query the now running job
		return new ResponseEntity<Job>(new Job(createdJob), HttpStatus.OK);
	}

	@ApiOperation(value="Get a single date value to indicate when the last meaningful use user file data is accurate as of.",
			notes="This is a single system-wide value.")
	@RequestMapping(value="/accurate_as_of", method = RequestMethod.GET,
			produces="application/json; charset = utf-8")
	public @ResponseBody AccurateAsOfDate getAccurateAsOfDate() throws EntityRetrievalException {
		MeaningfulUseAccurateAsOfDTO dto = muManager.getMeaningfulUseAccurateAsOf();
		if(dto == null) {
			throw new EntityRetrievalException("Could not retrieve entity for MeaningfulUseAccurateAsOf");
		}

		AccurateAsOfDate ar = new AccurateAsOfDate(dto.getAccurateAsOfDate().getTime());
		return ar;
	}

	@ApiOperation(value="Update the Meaningful Use Accurate As Of date.",
			notes="Accurate as of date value can be edited by a user with ROLE_ADMIN and ROLE_CMS_STAFF.")
	@RequestMapping(value="/accurate_as_of", method = RequestMethod.POST,
			produces="application/json; charset = utf-8")
	public @ResponseBody AccurateAsOfDate updateMeaningfulUseAccurateAsOf(@RequestBody(required = true) AccurateAsOfDate accurateAsOfDate) {
		MeaningfulUseAccurateAsOfDTO dto = muManager.getMeaningfulUseAccurateAsOf();
		dto.setAccurateAsOfDate(new Date(accurateAsOfDate.getAccurateAsOfDate()));
		dto = muManager.updateMeaningfulUseAccurateAsOf(dto);
		AccurateAsOfDate result = new AccurateAsOfDate(dto.getAccurateAsOfDate().getTime());
		return result;
	}
}
