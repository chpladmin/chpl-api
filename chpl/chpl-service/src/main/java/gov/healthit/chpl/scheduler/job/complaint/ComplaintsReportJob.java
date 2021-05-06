package gov.healthit.chpl.scheduler.job.complaint;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.manager.ComplaintManager;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "complaintsReportJobLogger")
public class ComplaintsReportJob implements Job {
    public static final String JOB_NAME = "complaintsReportJob";
    public static final String USER_KEY = "user";

    @Autowired
    private ComplaintManager complaintManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Complaint Report Generation job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            try {
                setSecurityContext(user);

                List<Complaint> allComplaints = complaintManager.getAllComplaints();

            } catch (Exception ex) {
                LOGGER.fatal("Unexpected exception was caught. All listing uploads may not have been processed.", ex);
            }
        }
        LOGGER.info("********* Completed the Complaint Report Generation job. *********");
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser splitUser = new JWTAuthenticatedUser();
        splitUser.setFullName(user.getFullName());
        splitUser.setId(user.getId());
        splitUser.setFriendlyName(user.getFriendlyName());
        splitUser.setSubjectName(user.getUsername());
        splitUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(splitUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
