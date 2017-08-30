package gov.healthit.chpl.job;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dto.JobDTO;

@Component
public class JobInitializer {
	private static final Logger logger = LogManager.getLogger(JobInitializer.class);
	
	@Autowired private TaskExecutor taskExecutor;
	@Autowired private JobDAO jobDao;
	
	@PostConstruct
	@Async
	public void initialize() {
		List<JobDTO> runningJobs = jobDao.findAllRunning();
		
		logger.info("Found " + runningJobs.size() + " jobs to start.");
		for(JobDTO job : runningJobs) {
			logger.info("Starting job with ID " + job.getId() + " for " + job.getContact().getFirstName());
			taskExecutor.execute(new MeaningfulUseUploadJob(job));
		}
	}
	
}
