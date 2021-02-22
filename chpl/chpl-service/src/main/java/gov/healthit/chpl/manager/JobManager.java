package gov.healthit.chpl.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.job.NoJobTypeException;
import gov.healthit.chpl.job.RunnableJob;
import gov.healthit.chpl.job.RunnableJobFactory;
import gov.healthit.chpl.manager.impl.SecuredManager;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class JobManager extends SecuredManager {
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final int JOB_DELAY_MILLIS = 250;

    private Environment env;
    private TaskScheduler taskScheduler;
    private RunnableJobFactory jobFactory;
    private JobDAO jobDao;

    @Autowired
    public JobManager(JobDAO jobDao, RunnableJobFactory jobFactory,
            TaskScheduler taskScheduler, Environment env) {
        this.jobDao = jobDao;
        this.jobFactory = jobFactory;
        this.taskScheduler = taskScheduler;
        this.env = env;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).JOB, "
            + "T(gov.healthit.chpl.permissions.domains.JobDomainPermissions).CREATE)")
    public JobDTO createJob(JobDTO job) throws EntityCreationException, EntityRetrievalException {
        UserDTO user = job.getUser();
        if (user == null || user.getId() == null) {
            throw new EntityRetrievalException("A user is required.");
        }

        JobDTO created = jobDao.create(job);
        return created;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).JOB, "
            + "T(gov.healthit.chpl.permissions.domains.JobDomainPermissions).GET_BY_ID)")
    public JobDTO getJobById(Long jobId) {
        return jobDao.getById(jobId);
    }

    /**
     * Gets the jobs that are either currently running or have completed within
     * a configurable window of time.
     */
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).JOB, "
            + "T(gov.healthit.chpl.permissions.domains.JobDomainPermissions).GET_ALL)")
    public List<JobDTO> getAllJobs() {
        String completedJobThresholdDaysStr = env.getProperty("jobThresholdDays").trim();
        Integer completedJobThresholdDays = 0;
        try {
            completedJobThresholdDays = Integer.parseInt(completedJobThresholdDaysStr);
        } catch (NumberFormatException ex) {
            LOGGER.error(
                    "Could not format " + completedJobThresholdDaysStr + " as an integer. Defaulting to 0 instead.");
        }
        Long earliestCompletedJobMillis = System.currentTimeMillis() - (completedJobThresholdDays * MILLIS_PER_DAY);
        return jobDao.findAllRunningAndCompletedBetweenDates(new Date(earliestCompletedJobMillis), new Date());
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).JOB, "
            + "T(gov.healthit.chpl.permissions.domains.JobDomainPermissions).GET_BY_USER)")
    public List<JobDTO> getJobsForUser(UserDTO user) throws EntityRetrievalException {
        if (user == null || user.getId() == null) {
            throw new EntityRetrievalException("A user is required.");
        }
        return jobDao.getByUser(user.getId());
    }

    @Transactional
    public List<JobTypeDTO> getAllJobTypes() {
        return jobDao.findAllTypes();
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).JOB, "
            + "T(gov.healthit.chpl.permissions.domains.JobDomainPermissions).START)")
    public boolean start(JobDTO job) throws EntityRetrievalException {
        RunnableJob runnableJob = null;
        try {
            runnableJob = jobFactory.getRunnableJob(job);
        } catch (NoJobTypeException ex) {
            LOGGER.error("No runnable job for job type " + job.getJobType().getName() + " found.");
        }

        if (runnableJob == null) {
            try {
                jobDao.delete(job.getId());
            } catch (Exception e) {
                LOGGER.error("Unable to delete job with id {}, error {}", job.getId(), e.getMessage());
            }
            return false;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MILLISECOND, JOB_DELAY_MILLIS);
        taskScheduler.schedule(runnableJob, cal.getTime());
        return true;
    }
}
