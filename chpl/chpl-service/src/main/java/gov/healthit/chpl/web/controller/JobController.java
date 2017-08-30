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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.Job;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.JobDTO;
import gov.healthit.chpl.dto.JobTypeDTO;
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

	@ApiOperation(value = "Get the list of all jobs currently running in the system.")
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
			notes="ROLE_ADMIN or ROLE_ACB_ADMIN are required. ")
	@RequestMapping(value="/create", method=RequestMethod.POST,
			produces="application/json; charset=utf-8")
	public synchronized ResponseEntity<Job> createJob(
			@RequestBody(required = true) Job job, 
			@RequestParam("file") MultipartFile file) throws MaxUploadSizeExceededException, ValidationException, 
			EntityCreationException, EntityRetrievalException {

		if (file.isEmpty()) {
			throw new ValidationException("You cannot upload an empty file!");
		}
		
		//for now we'll only accept CSV
		if(!file.getContentType().equalsIgnoreCase("text/csv") &&
				!file.getContentType().equalsIgnoreCase("application/vnd.ms-excel")) {
			throw new ValidationException("File must be a CSV document.");
		}
		
		if(job.getUser() == null || job.getType() == null) {
			throw new ValidationException("Both a contact and type must be provided for the new job.");
		}
		
		//read the file into a string
		StringBuffer data = new StringBuffer();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			data.append(reader.readLine());
		} catch(IOException ex) {
			String msg = "Could not read file: " + ex.getMessage();
			logger.error(msg);
			throw new ValidationException(msg);
		}
		
		JobDTO toCreate = new JobDTO();
		toCreate.setData(data.toString());
		ContactDTO contact = new ContactDTO();
		if(job.getUser() != null) {
			contact.setId(job.getUser().getContactId());
			contact.setFirstName(job.getUser().getFirstName());
			contact.setLastName(job.getUser().getLastName());
			contact.setEmail(job.getUser().getEmail());
			contact.setPhoneNumber(job.getUser().getPhoneNumber());
			contact.setTitle(job.getUser().getTitle());
		}
		toCreate.setContact(contact);
		JobTypeDTO type = new JobTypeDTO();
		if(job.getType() != null) {
			type.setId(job.getType().getId());
			type.setName(job.getType().getName());
		}
		toCreate.setJobType(type);
		JobDTO insertedJob = jobManager.createJob(toCreate);
		JobDTO created = jobManager.getJobById(insertedJob.getId());
		jobManager.start(created);
		
		//query the now running surveillance
		created = jobManager.getJobById(insertedJob.getId());
		return new ResponseEntity<Job>(new Job(created), HttpStatus.OK);
	}
	
}
