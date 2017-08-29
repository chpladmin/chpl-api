package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.ContactDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.JobDTO;
import gov.healthit.chpl.manager.JobManager;

@Service
public class JobManagerImpl extends ApplicationObjectSupport implements JobManager {

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
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
	public JobDTO getJobById(Long jobId) {
		return jobDao.getById(jobId);
	}
	
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMIN')")
	public List<JobDTO> getAllJobs() {
		return jobDao.findAll();
	}
	
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
	public List<JobDTO> getAllRunningJobs() {
		return jobDao.findAllRunning();
	}
	
	@Transactional
	@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ACB_ADMIN')")
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
	public void start(JobDTO job) {
		//TODO
	}
}

