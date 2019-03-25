package gov.healthit.chpl.manager.impl;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobTypeDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.job.NoJobTypeException;
import gov.healthit.chpl.job.RunnableJob;
import gov.healthit.chpl.job.RunnableJobFactory;
import gov.healthit.chpl.manager.JobManager;
import gov.healthit.chpl.permissions.ResourcePermissions;

@Service
public class JobManagerImpl extends SecuredManager implements JobManager {
    private static final Logger LOGGER = LogManager.getLogger(JobManagerImpl.class);
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    @Autowired
    private Environment env;
    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private RunnableJobFactory jobFactory;
    @Autowired
    private JobDAO jobDao;

    @Autowired
    private ResourcePermissions resourcePermissions;

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).JOB, "
            + "T(gov.healthit.chpl.permissions.domains.JobDomainPermissions).CREATE)")
    public JobDTO createJob(final JobDTO job) throws EntityCreationException, EntityRetrievalException {
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
    public JobDTO getJobById(final Long jobId) {
        return jobDao.getById(jobId);
    }

    /**
     * Gets the jobs that are either currently running or have completed within
     * a configurable window of time.
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<JobDTO> getAllJobs() {
        String completedJobThresholdDaysStr = env.getProperty("jobThresholdDays").trim();
        Integer completedJobThresholdDays = 0;
        try {
            completedJobThresholdDays = Integer.parseInt(completedJobThresholdDaysStr);
        } catch (final NumberFormatException ex) {
            LOGGER.error(
                    "Could not format " + completedJobThresholdDaysStr + " as an integer. Defaulting to 0 instead.");
        }
        Long earliestCompletedJobMillis = System.currentTimeMillis() - (completedJobThresholdDays * MILLIS_PER_DAY);

        Long userId = null;
        if (!resourcePermissions.isUserRoleAdmin()) {
            userId = Util.getCurrentUser().getId();
        }
        return jobDao.findAllRunningAndCompletedBetweenDates(new Date(earliestCompletedJobMillis), new Date(), userId);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).JOB, "
            + "T(gov.healthit.chpl.permissions.domains.JobDomainPermissions).GET_BY_USER)")
    public List<JobDTO> getJobsForUser(final UserDTO user) throws EntityRetrievalException {
        if (user == null || user.getId() == null) {
            throw new EntityRetrievalException("A user is required.");
        }
        return jobDao.getByUser(user.getId());
    }

    @Override
    @Transactional
    public List<JobTypeDTO> getAllJobTypes() {
        return jobDao.findAllTypes();
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).JOB, "
            + "T(gov.healthit.chpl.permissions.domains.JobDomainPermissions).START)")
    public boolean start(final JobDTO job) throws EntityRetrievalException {
        RunnableJob runnableJob = null;
        try {
            runnableJob = jobFactory.getRunnableJob(job);
        } catch (final NoJobTypeException ex) {
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

        taskExecutor.execute(runnableJob);
        return true;
    }
}
