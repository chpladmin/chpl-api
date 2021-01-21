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
import gov.healthit.chpl.dto.job.JobDTO;

@Component
public class JobInitializer {
    private static final Logger LOGGER = LogManager.getLogger(JobInitializer.class);

    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private RunnableJobFactory jobFactory;
    @Autowired
    private JobDAO jobDao;

    @PostConstruct
    @Async
    public void initialize() {
        List<JobDTO> runningJobs = jobDao.findAllRunning();

        LOGGER.info("Found " + runningJobs.size() + " jobs to start.");
        for (JobDTO job : runningJobs) {
            RunnableJob runnableJob = null;
            try {
                runnableJob = jobFactory.getRunnableJob(job);
            } catch (final NoJobTypeException ex) {
                LOGGER.error("No runnable job type found for " + job.getJobType().getName());
            }
            if (runnableJob != null) {
                LOGGER.info("Starting job with ID " + job.getId() + " for user " + job.getUser().getUsername());
                taskExecutor.execute(runnableJob);
            }
        }
    }

}
