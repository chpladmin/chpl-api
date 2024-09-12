package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "cognitoMassRequirePasswordChangeJobLogger")
public class CognitoMassRequirePasswordChangeJob extends QuartzJob  {
    @Autowired
    private CognitoApiWrapper cognitoApiWrapper;

    @Value("#{'${cognitoMassRequirePasswordChangeJob.includedUsers}'.split(',')}")
    private List<String> includedUsers;

    public CognitoMassRequirePasswordChangeJob() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Cognito Mass Require Password Change job. *********");

        List<User> users = cognitoApiWrapper.getAllUsers();

        users.stream()
                .filter(user -> CollectionUtils.isEmpty(includedUsers)
                        || includedUsers.contains(user.getEmail()))
                .forEach(user -> updateUserPasswordResetRequired(user));

        LOGGER.info("********* Completed the Cognito Mass Require Password Change job. *********");
    }

    private void updateUserPasswordResetRequired(User user) {
        user.setPasswordResetRequired(true);
        try {
            cognitoApiWrapper.updateUser(user);
            LOGGER.info("Updated user: {}", user.getEmail());
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not update user: {}", user.getEmail(), e);
        }
    }
}
