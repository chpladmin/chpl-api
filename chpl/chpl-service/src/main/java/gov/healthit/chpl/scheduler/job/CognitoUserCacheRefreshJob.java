package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "cognitoUserCacheRefreshJobLogger")
public class CognitoUserCacheRefreshJob extends QuartzJob  {
    public static final String JOB_NAME = "cognitoUserCacheRefresh";
    public static final String JOB_GROUP = "systemJobs";

    @Autowired
    private CognitoApiWrapper cognitoApiWrapper;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the User Cache Refresh Job. *********");
        List<User> users = cognitoApiWrapper.getAllUsersNoCache();

        //TODO Put all users in the shared store - no method for this currently, have to add one
        //TODO do we remove all the existing users in the shared store? If so the removal + input all needs to be done
        //in a single transaction.
        //Is this the only place they would get put into the shared store? We could also do it
        //entirely within the getAllUsersNoCache method...

        LOGGER.info("Put " + users.size() + " users from Cognito in the shared store.");
        LOGGER.info("********* Completed the User Cache Refresh Job. *********");
    }
}
