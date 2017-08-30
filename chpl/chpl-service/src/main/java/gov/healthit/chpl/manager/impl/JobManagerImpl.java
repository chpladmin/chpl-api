package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.JobDTO;
import gov.healthit.chpl.dto.JobTypeDTO;
import gov.healthit.chpl.job.MeaningfulUseUploadJob;
import gov.healthit.chpl.job.NoJobTypeException;
import gov.healthit.chpl.job.RunnableJob;
import gov.healthit.chpl.job.RunnableJobFactory;
import gov.healthit.chpl.manager.JobManager;

@Service
public class JobManagerImpl extends ApplicationObjectSupport implements JobManager {
	private static final Logger logger = LogManager.getLogger(JobManagerImpl.class);
	private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
	
	@Autowired private Environment env;
	@Autowired private TaskExecutor taskExecutor;
	@Autowired private RunnableJobFactory jobFactory;
	@Autowired private JobDAO jobDao;
	@Autowired private ContactDAO contactDao;
	
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
	public JobDTO createJob(JobDTO job) throws EntityCreationException, EntityRetrievalException {
		ContactDTO contact = job.getContact();
		if(contact != null && contact.getId() == null) {
			contact = contactDao.getByValues(job.getContact());
		}
		if(contact == null || contact.getId() == null) {
			throw new EntityRetrievalException("No contact could be found with the provided information.");
		}
		
		JobDTO created = jobDao.create(job);
		return created;
	}
	
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public JobDTO getJobById(Long jobId) {
		return jobDao.getById(jobId);
	}
	
	/**
	 * Gets the jobs that are either currently running or have completed within a configurable window of time
	 */
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public List<JobDTO> getAllJobs() {
		String completedJobThresholdDaysStr = env.getProperty("jobThresholdDays");
		Integer completedJobThresholdDays = 0;
		try {
			completedJobThresholdDays = Integer.parseInt(completedJobThresholdDaysStr);
		} catch(NumberFormatException ex) {
			logger.error("Could not format " + completedJobThresholdDaysStr + " as an integer. Defaulting to 0 instead.");
		}
		Long earliestCompletedJobMillis = System.currentTimeMillis() - (completedJobThresholdDays * MILLIS_PER_DAY);
		return jobDao.findAllRunningAndCompletedBetweenDates(new Date(earliestCompletedJobMillis), new Date());
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<JobDTO> getAllRunningJobs() {
		return jobDao.findAllRunning();
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<JobDTO> getJobsForUser(ContactDTO user) throws EntityRetrievalException {
		if(user != null && user.getId() == null) {
			user = contactDao.getByValues(user);
		}
		if(user == null || user.getId() == null) {
			throw new EntityRetrievalException("No contact could be found with the provided information.");
		}
		return jobDao.getByUser(user.getId());
	}
	
	@Override
	@Transactional
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public List<JobTypeDTO> getAllJobTypes() {
		return jobDao.findAllTypes();
	}
	
	@Override
	public boolean start(JobDTO job) {
		RunnableJob runnableJob = null;
		try {
			runnableJob = jobFactory.getRunnableJob(job);
		} catch(NoJobTypeException ex) {
			logger.error("No runnable job for job type " + job.getJobType().getName() + " found.");
		}
		
		if(runnableJob == null) {
			try {
				jobDao.delete(job.getId());
			} catch(Exception ignore) {}
			return false;
		}
		
		taskExecutor.execute(runnableJob);
		return true;
	}
}

