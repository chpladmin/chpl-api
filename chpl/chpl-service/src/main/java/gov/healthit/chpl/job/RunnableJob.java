package gov.healthit.chpl.job;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.JobDAO;
import gov.healthit.chpl.dto.job.JobDTO;
import gov.healthit.chpl.dto.job.JobMessageDTO;
import gov.healthit.chpl.entity.job.JobStatusType;

@Component
public class RunnableJob implements Runnable {
	private static final Logger logger = LogManager.getLogger(RunnableJob.class);

	@Autowired protected SendMailUtil mailUtils;
	@Autowired protected JobDAO jobDao;
	protected JobDTO job;
	protected User user; //run this job as this user

	public JobDTO getJob() {
		return job;
	}
	public void setJob(JobDTO job) {
		this.job = job;
	}

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	public JobDAO getJobDao() {
		return jobDao;
	}
	public void setJobDao(JobDAO jobDao) {
		this.jobDao = jobDao;
	}

	protected void start() {
		SecurityContextHolder.getContext().setAuthentication(this.user);
		logger.info("Starting " + job.getJobType().getName() + " job for " + job.getUser().getSubjectName());
		try {
			jobDao.markStarted(this.job);
		} catch(Exception ex) {
			logger.error("Could not mark the job " + this.job.getId() + " as started.", ex);
		}
	}

	protected void updateStatus(double percentComplete, JobStatusType statusType) {
		try {
			jobDao.updateStatus(this.job, (int)percentComplete, statusType);
		} catch(Exception ex) {
			logger.error("Could not update the job status " + this.job.getId() + ". Error was: " + ex.getMessage(), ex);
		}
	}

	protected void addJobMessage(String message) {
		try {
			jobDao.addJobMessage(this.job, message);
		} catch(Exception ex) {
			logger.error("Could not add message " + message + " to job " + this.job.getId() + ". Error was: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Create an email to send to the user responsible for this job.
	 * Email should say the job is done and include any status or messages from the job execution.
	 */
	public void complete() {
		updateStatus(100, JobStatusType.Complete);

		JobDTO completedJob = jobDao.getById(this.job.getId());
		this.job = completedJob;

		if(this.job.getUser() == null || StringUtils.isEmpty(this.job.getUser().getEmail())) {
			logger.fatal("Cannot send email message regarding job ID " + this.job.getId() + " because email address is blank.");
			return;
		} else {
			logger.info("Sending email to " + this.job.getUser().getEmail());
		}

		String[] to = {this.job.getUser().getEmail()};
		String subject = this.job.getJobType().getSuccessMessage();
		String htmlMessage = "<h3>Job Details:</h3>"
				+ "<ul>"
				+ "<li>Started: " + this.job.getStartTime() + "</li>"
				+ "<li>Ended: " + this.job.getEndTime() + "</li>"
				+ "<li>Status: " + this.job.getStatus().getStatus().toString() + "</li>"
				+ "</ul>";
		if(this.job.getMessages() != null && this.job.getMessages().size() > 0) {
			htmlMessage += "<h4>The following messages were generated: </h4>"
				+ "<ul>";
			for(JobMessageDTO message : this.job.getMessages()) {
				htmlMessage += "<li>" + message.getMessage() + "</li>";
			}
			htmlMessage += "</ul>";
		} else {
			htmlMessage += "<p>No messages were generated.</p>";
		}

		try {
			mailUtils.sendEmail(to, null, subject, htmlMessage);
		} catch(MessagingException ex) {
			logger.error("Error sending email " + ex.getMessage(), ex);
		} finally {
			logger.info("Completed " + job.getJobType().getName() + " job for " + job.getUser().getSubjectName());
			SecurityContextHolder.getContext().setAuthentication(null);
		}
	}

	public void run() {
		this.start();
	}
}
