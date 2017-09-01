package gov.healthit.chpl.job;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.auth.SendMailUtil;
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

	public JobDTO getJob() {
		return job;
	}
	public void setJob(JobDTO job) {
		this.job = job;
	}
	
	public JobDAO getJobDao() {
		return jobDao;
	}
	public void setJobDao(JobDAO jobDao) {
		this.jobDao = jobDao;
	}
	
	protected void start() {
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
		JobDTO completedJob = jobDao.getById(this.job.getId());
		this.job = completedJob;
		
		if(StringUtils.isEmpty(this.job.getContact().getEmail())) {
			logger.fatal("Cannot send email message regarding job ID " + this.job.getId() + " because email address is blank for contact id " + this.job.getContact().getId());
			return;
		} else {
			logger.info("Sending email to " + this.job.getContact().getEmail());
		}
		
		String[] to = {this.job.getContact().getEmail()};
		String subject = "Your Job '" + this.job.getJobType().getName() + "' Has Completed";
		String htmlMessage = "<h3>Job Details:</h3>"
				+ "<ul>"
				+ "<li>Started: " + this.job.getStartTime() + "</li>"
				+ "<li>Ended: " + this.job.getEndTime() + "</li>"
				+ "<li>Completion Status: " + this.job.getStatus().getStatus().toString() + "</li>"
				+ "</ul>";
		if(this.job.getMessages() != null && this.job.getMessages().size() > 0) {
			htmlMessage += "<br/><br/>"
				+ "The following messages were generated: " 
				+ "<ul>";
			for(JobMessageDTO message : this.job.getMessages()) {
				htmlMessage += "<li>" + message.getMessage() + "</li>";
			}
			htmlMessage += "</ul>";
		} else {
			htmlMessage += "<p>No messages were generated.</p>";
		}
		
		try {
			mailUtils.sendEmail(to, subject, htmlMessage, null);
		} catch(MessagingException ex) {
			logger.error("Error sending email " + ex.getMessage(), ex);
		}
	}
	
	public void run() {
		this.start();
	}
}
