package gov.healthit.chpl.scheduler.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.TestToolDAO;
import gov.healthit.chpl.dto.TestToolDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import net.sf.ehcache.CacheManager;

public class CreateTestToolJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("createTestToolJobLogger");
    private static final String TEST_TOOL = "Inferno";

    @Autowired
    private TestToolDAO testToolDao;

    @Autowired
    private JpaTransactionManager txnMgr;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Create Test Tool job. *********");

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setSecurityContext();

        TestToolDTO existing = testToolDao.getByName(TEST_TOOL);
        if (existing != null) {
            LOGGER.error("Cannot create test tool " + TEST_TOOL + "; it already exists");
        } else {
            try {
                addTestTool();
            } catch (EntityCreationException | EntityRetrievalException e) {
                LOGGER.error("Unable to create the test tool " + TEST_TOOL + ": " + e.getMessage());
            }
        }

        CacheManager.getInstance().clearAll();
        LOGGER.info("********* Completed the Create Test Tool job. *********");
    }

    private void addTestTool() throws EntityCreationException, EntityRetrievalException {
        // Since there is no manager, we need to handle the transaction.
        // @Transactional does not work in Quartz jobs, since Spring context
        // is injected after method has started.
        TransactionTemplate txnTemplate = new TransactionTemplate(txnMgr);
        txnTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                TestToolDTO tool = new TestToolDTO();
                tool.setName(TEST_TOOL);
                try {
                    testToolDao.create(tool);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    status.setRollbackOnly();
                }
            }
        });
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}
