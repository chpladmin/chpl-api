package old.gov.healthit.chpl.dao.impl;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.caching.UnitTestRules;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        old.gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CertificationStatusDAOTest extends TestCase {
    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @Autowired
    private CertificationStatusDAO certStatusDao;

    private static JWTAuthenticatedUser adminUser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    /**
     * Tests that getByStatusName() completes without error for each status name.
     *
     * @throws EntityRetrievalException
     * @throws EntityCreationException
     */
    @Test
    @Transactional
    public void testgetByStatusName_Active() throws EntityRetrievalException, EntityCreationException {
        System.out.println("Running testgetByStatusName() test");
        CertificationStatusDTO dto = certStatusDao.getByStatusName(CertificationStatusType.Active.getName());
        assertNotNull(dto);
        dto = certStatusDao.getByStatusName(CertificationStatusType.Retired.getName());
        assertNotNull(dto);
        dto = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByDeveloper.getName());
        assertNotNull(dto);
        dto = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByDeveloperUnderReview.getName());
        assertNotNull(dto);
        dto = certStatusDao.getByStatusName(CertificationStatusType.WithdrawnByAcb.getName());
        assertNotNull(dto);
        dto = certStatusDao.getByStatusName(CertificationStatusType.SuspendedByAcb.getName());
        assertNotNull(dto);
        dto = certStatusDao.getByStatusName(CertificationStatusType.SuspendedByOnc.getName());
        assertNotNull(dto);
        dto = certStatusDao.getByStatusName(CertificationStatusType.TerminatedByOnc.getName());
        assertNotNull(dto);
    }

}
