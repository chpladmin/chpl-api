package gov.healthit.chpl.scheduler.job;

import java.util.List;

import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.entity.TestFunctionalityEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.TestingFunctionalityManager;

public class ModifyTestFunctionalityJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("modifyTestFunctionalityJobLogger");
    private static final String B3_OLD_NUMBER = "(b)(3)(iii)";
    private static final String B3_NEW_NUMBER = "(b)(3)(i)(C)";
    private static final String B3_NEW_NAME = "Optional: 170.315(b)(3)(i)(C) For each transaction listed in paragraph "
            + "(b)(3)(i)(A) of this section, the technology must be able to receive and transmit the reason for the prescription "
            + "using the indication elements in the SIG Segment";
    private static final String E12_OLD_NUMBER = "(e)(1)(i)(A)(2)";
    private static final String E12_NEW_NUMBER = " (e)(1)(i)(A)(4)";
    private static final String E12_NEW_NAME = "Ambulatory: 170.315(e)(1)(i)(A)(4) Provider’s name and office contact "
            + "information";
    private static final String E13_OLD_NUMBER = "(e)(1)(i)(A)(3)";
    private static final String E13_NEW_NUMBER = "(e)(1)(i)(A)(5)";
    private static final String E13_NEW_NAME = "Inpatient 170.315(e)(1)(i)(A)(5) Admission and discharge dates and "
            + "locations; discharge instructions; and reason(s) for hospitalization";
    private static final Long ADMIN_ID = -2L;

    @Autowired
    private ModifiableTestFunctionalityDao modifiableTestFunctionalityDao;

    @Autowired
    private TestingFunctionalityManager testingFunctionalityManager;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        LOGGER.info("********* Starting the Test Functionality Modification job. *********");

        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        setSecurityContext();
        modifyTestFunctionality();
        testingFunctionalityManager.onApplicationEvent(null);

        LOGGER.info("********* Completed the Test Functionality Modification job. *********");
    }

    private void modifyTestFunctionality() {
        try {
            modifiableTestFunctionalityDao.update(B3_OLD_NUMBER, B3_NEW_NUMBER, B3_NEW_NAME);
            LOGGER.info("Updated Test Functionality: " + B3_OLD_NUMBER);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Unable to update Test Functionality: " + e.getMessage());
        }
        try {
            modifiableTestFunctionalityDao.update(E12_OLD_NUMBER, E12_NEW_NUMBER, E12_NEW_NAME);
            LOGGER.info("Updated Test Functionality: " + E12_OLD_NUMBER);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Unable to update Test Functionality: " + e.getMessage());
        }
        try {
            modifiableTestFunctionalityDao.update(E13_OLD_NUMBER, E13_NEW_NUMBER, E13_NEW_NAME);
            LOGGER.info("Updated Test Functionality: " + E13_OLD_NUMBER);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Unable to update Test Functionality: " + e.getMessage());
        }
    }

    private void setSecurityContext() {
        JWTAuthenticatedUser adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Admin");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));

        SecurityContextHolder.getContext().setAuthentication(adminUser);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Component("modifiableTestFunctionalityDao")
    private static class ModifiableTestFunctionalityDao extends BaseDAOImpl {

        @SuppressWarnings("unused")
        ModifiableTestFunctionalityDao() {
            super();
        }

        @Transactional
        public TestFunctionalityEntity getEntityByNumber(String number) {
            String hql = "SELECT tf "
                    + "FROM TestFunctionalityEntity tf "
                    + "WHERE tf.number = :number "
                    + "AND deleted = false";
            Query query = entityManager.createQuery(hql);
            query.setParameter("number", number);
            @SuppressWarnings("unchecked") List<TestFunctionalityEntity> tfEntities = query.getResultList();
            TestFunctionalityEntity result = null;
            if (tfEntities != null && tfEntities.size() > 0) {
                result = tfEntities.get(0);
            }
            return result;
        }

        @Transactional
        public void update(String oldNumber, String newNumber, String name) throws EntityRetrievalException {
            TestFunctionalityEntity toUpdate = this.getEntityByNumber(oldNumber);
            if (toUpdate == null) {
                throw new EntityRetrievalException("No Test Functionality exists with the number: " + oldNumber);
            }
            toUpdate.setNumber(newNumber);
            toUpdate.setName(name);
            super.update(toUpdate);
        }
    }
}
