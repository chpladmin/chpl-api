package gov.healthit.chpl.job;

import java.util.ArrayList;
import java.util.Arrays;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobMessageDTO;
import gov.healthit.chpl.entity.job.JobStatusType;
import gov.healthit.chpl.util.EmailBuilder;

@Component
public class RunnableJob implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(RunnableJob.class);

    @Autowired
    protected Environment env;

    @Autowired
    protected JobDAO jobDao;
    protected JobDTO job;
    protected User user; // run this job as this user

    public JobDTO getJob() {
        return job;
    }

    public void setJob(final JobDTO job) {
        this.job = job;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public JobDAO getJobDao() {
        return jobDao;
    }

    public void setJobDao(final JobDAO jobDao) {
        this.jobDao = jobDao;
    }

    @Transactional
    protected void start() {
        SecurityContextHolder.getContext().setAuthentication(this.user);
        LOGGER.info("Starting " + job.getJobType().getName() + " job for " + job.getUser().getSubjectName());
        try {
            jobDao.markStarted(this.job);
        } catch (Exception ex) {
            LOGGER.error("Could not mark the job " + this.job.getId() + " as started.", ex);
        }
    }

    @Transactional
    protected void updateStatus(double percentComplete, JobStatusType statusType) {
        try {
            jobDao.updateStatus(this.job, (int) percentComplete, statusType);
        } catch (Exception ex) {
            LOGGER.error("Could not update the job status " + this.job.getId() + ". Error was: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    protected void addJobMessage(String message) {
        try {
            jobDao.addJobMessage(this.job, message);
        } catch (Exception ex) {
            LOGGER.error("Could not add message " + message + " to job " + this.job.getId() + ". Error was: "
                    + ex.getMessage(), ex);
        }
    }

    /**
     * Create an email to send to the user responsible for this job. Email
     * should say the job is done and include any status or messages from the
     * job execution.
     */
    @Transactional
    public void complete() {
        updateStatus(100, JobStatusType.Complete);

        JobDTO completedJob = jobDao.getById(this.job.getId());
        this.job = completedJob;

        if (this.job.getUser() == null || StringUtils.isEmpty(this.job.getUser().getEmail())) {
            LOGGER.fatal("Cannot send email message regarding job ID " + this.job.getId()
                    + " because email address is blank.");
            return;
        } else {
            LOGGER.info("Sending email to " + this.job.getUser().getEmail());
        }

        String[] to = {
                this.job.getUser().getEmail()
        };
        String subject = this.job.getJobType().getSuccessMessage();
        StringBuilder htmlMessage = new StringBuilder();
        htmlMessage.append("<h3>Job Details:</h3><ul><li>Started: ") 
        .append(this.job.getStartTime())
        .append("</li><li>Ended: ")
        .append(this.job.getEndTime())
        .append("</li><li>Status: ")
        .append(this.job.getStatus().getStatus().toString())
        .append("</li></ul>");
        if (this.job.getMessages() != null && this.job.getMessages().size() > 0) {
            htmlMessage.append("<h4>The following messages were generated: </h4><ul>");
            for (JobMessageDTO message : this.job.getMessages()) {
                htmlMessage.append("<li>")
                .append(message.getMessage())
                .append("</li>");
            }
            htmlMessage.append("</ul>");
        } else {
            htmlMessage.append("<p>No messages were generated.</p>");
        }

        try {
            EmailBuilder emailBuilder = new EmailBuilder(env);
            emailBuilder.recipients(new ArrayList<String>(Arrays.asList(to)))
                            .subject(subject)
                            .htmlMessage(htmlMessage.toString())
                            .sendEmail();
        } catch (final MessagingException ex) {
            LOGGER.error("Error sending email " + ex.getMessage(), ex);
        } finally {
            LOGGER.info("Completed " + job.getJobType().getName() + " job for " + job.getUser().getSubjectName());
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    public void run() {
        this.start();
    }
}
