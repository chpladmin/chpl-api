package gov.healthit.chpl.scheduler.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dto.auth.UserDTO;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "surveillanceUploadJobLogger")
public class SurveillanceUploadJob implements Job {
    public static final String JOB_NAME = "surveillanceUploadJob";
    public static final String FILE_CONTENTS_KEY = "fileContents";
    public static final String USER_KEY = "user";

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Surveillance Upload job. *********");

        JobDataMap jobDataMap = jobContext.getMergedJobDataMap();
        UserDTO user = (UserDTO) jobDataMap.get(USER_KEY);
        if (user == null) {
            LOGGER.fatal("No user could be found in the job data.");
        } else {
            setSecurityContext(user);
            //TODO: Upload stuff
        }
        LOGGER.info("********* Completed the Surveillance Upload job. *********");
    }

    private void setSecurityContext(UserDTO user) {
        JWTAuthenticatedUser jobUser = new JWTAuthenticatedUser();
        jobUser.setFullName(user.getFullName());
        jobUser.setId(user.getId());
        jobUser.setFriendlyName(user.getFriendlyName());
        jobUser.setSubjectName(user.getUsername());
        jobUser.getPermissions().add(user.getPermission().getGrantedPermission());

        SecurityContextHolder.getContext().setAuthentication(jobUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
