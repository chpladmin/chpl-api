package gov.healthit.chpl.web.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;
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
import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.manager.JobManager;
import gov.healthit.chpl.web.controller.exception.ValidationException;
import gov.healthit.chpl.web.controller.results.JobResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value="jobs")
@RestController
@RequestMapping("/jobs")
public class JobController {
	
	private static final Logger logger = LogManager.getLogger(JobController.class);
	@Autowired JobManager jobManager;
	@Autowired UserManager userManager;
	
	@ApiOperation(value = "Get the list of all jobs currently running in the system and those"
			+ "that have completed within a configurable amount of time (usually a short window like the last 7 days).")
	@RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
	public @ResponseBody JobResults getAllRunningJobs() {
		List<JobDTO> jobDtos = jobManager.getAllRunningJobs();
		
		List<Job> jobs = new ArrayList<Job>();
		for(JobDTO jobDto : jobDtos) {
			Job job = new Job(jobDto);
			jobs.add(job);
		}
		
		JobResults results = new JobResults();
		results.setResults(jobs);
		return results;
	}
	
	@ApiOperation(value="Creates and starts a new job.", 
			notes="The 'jobType' URL path parameter may be any of the names returned in the /data/job_types call."
					+ "User must be logged in to make this API call.")
	@RequestMapping(value="/create", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public synchronized ResponseEntity<Job> createJob(
			@RequestParam(value = "jobType", required = true) String jobTypeName,
			@RequestParam("file") MultipartFile file) throws MaxUploadSizeExceededException, ValidationException, 
			EntityCreationException, EntityRetrievalException {
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
		
		//determine the job type
		if(StringUtils.isEmpty(jobTypeName)) {
			logger.error("Job Type was not specified.");
			return new ResponseEntity<Job>(HttpStatus.BAD_REQUEST);
		}
		JobTypeDTO jobType = null;
		List<JobTypeDTO> jobTypes = jobManager.getAllJobTypes();
		for(JobTypeDTO jt : jobTypes) {
			if(jt.getName().equalsIgnoreCase(jobTypeName)) {
				jobType = jt;
			}
		}
		if(jobType == null) {
			logger.error("Job Type " + jobTypeName + " is not recognized.");
			return new ResponseEntity<Job>(HttpStatus.BAD_REQUEST);
		}
		
		//figure out the user
		UserDTO currentUser = null;
		try {
			currentUser = userManager.getById(Util.getCurrentUser().getId());
		} catch(UserRetrievalException ex) {
			logger.error("Error finding user with ID " + Util.getCurrentUser().getId() + ": " + ex.getMessage());
			return new ResponseEntity<Job>(HttpStatus.UNAUTHORIZED);
		}
		if(currentUser == null) {
			logger.error("No user with ID " + Util.getCurrentUser().getId() + " could be found in the system.");
			return new ResponseEntity<Job>(HttpStatus.UNAUTHORIZED);
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
		} catch(IOException ex) {
			String msg = "Could not read file: " + ex.getMessage();
			logger.error(msg);
			throw new ValidationException(msg);
		}
		
		JobDTO toCreate = new JobDTO();
		toCreate.setData(data.toString());
		ContactDTO contact = new ContactDTO();
		contact.setFirstName(currentUser.getFirstName());
		contact.setLastName(currentUser.getLastName());
		contact.setEmail(currentUser.getEmail());
		contact.setPhoneNumber(currentUser.getPhoneNumber());
		contact.setTitle(currentUser.getTitle());
		toCreate.setContact(contact);
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
		} catch(EntityRetrievalException ex) {
			logger.error("Could not mark job " + createdJob.getId() + " as started.");
			return new ResponseEntity<Job>(new Job(createdJob), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		//query the now running surveillance
		return new ResponseEntity<Job>(new Job(createdJob), HttpStatus.OK);
	}
	
}
