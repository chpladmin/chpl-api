package gov.healthit.chpl.scheduler.job;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.sharedstore.user.SharedUserStoreProvider;
import gov.healthit.chpl.user.cognito.CognitoApiWrapper;
import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "cognitoUserCacheRefreshJobLogger")
public class CognitoUserCacheRefreshJob extends QuartzJob  {
    public static final String JOB_NAME = "cognitoUserCacheRefresh";
    public static final String JOB_GROUP = "systemJobs";

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private SharedUserStoreProvider sharedUserStoreProvider;

    @Autowired
    private CognitoApiWrapper cognitoApiWrapper;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the User Cache Refresh Job. *********");
        LOGGER.info("Querying Cognito for all enabled users with access to this environment.");
        List<User> users = cognitoApiWrapper.getAllUsersNoCache();
        LOGGER.info("Got " + users.size() + " users from Cognito.");
        replaceSharedStore(users);
        LOGGER.info("********* Completed the User Cache Refresh Job. *********");
    }

    private void replaceSharedStore(List<User> users) {
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    LOGGER.info("In transaction - removing all users from shared store.");
                    sharedUserStoreProvider.removeAll();
                    LOGGER.info("Removed all users from shared store.");

                    LOGGER.info("Putting " + users.size() + " users into the shared store");
                    sharedUserStoreProvider.putAll(users);
                    LOGGER.info("Completed putting users in the shared store");
                } catch (Exception e) {
                    LOGGER.catching(e);
                }
            }
        });
    }
}
