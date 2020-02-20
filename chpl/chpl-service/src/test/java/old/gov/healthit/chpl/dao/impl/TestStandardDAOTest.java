package old.gov.healthit.chpl.dao.impl;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Rollback;
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
import gov.healthit.chpl.dao.TestStandardDAO;
import gov.healthit.chpl.dto.TestStandardDTO;
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
public class TestStandardDAOTest extends TestCase {
    private static JWTAuthenticatedUser adminUser;

    @Autowired
    private TestStandardDAO tsDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(-2L);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Transactional
    public void findByNumberAndEdition() {
        String number = "170.210(h)";
        Long editionId = 2L;
        TestStandardDTO foundTs = tsDao.getByNumberAndEdition(number, editionId);
        assertNotNull(foundTs);
        assertEquals(number, foundTs.getName());
        assertEquals(editionId.longValue(), foundTs.getCertificationEditionId().longValue());
        assertEquals("2014", foundTs.getYear());
    }

    @Test
    @Transactional
    public void findByNumberAndEditionCaseInsensitive() {
        String number = "170.210(H)";
        Long editionId = 2L;
        TestStandardDTO foundTs = tsDao.getByNumberAndEdition(number, editionId);
        assertNotNull(foundTs);
        assertEquals("170.210(h)", foundTs.getName());
        assertEquals(editionId.longValue(), foundTs.getCertificationEditionId().longValue());
        assertEquals("2014", foundTs.getYear());
    }

    @Test
    @Transactional
    public void findByNumberAndEditionWithSpaces() {
        String number = "170.210 (h)";
        Long editionId = 2L;
        TestStandardDTO foundTs = tsDao.getByNumberAndEdition(number, editionId);
        assertNotNull(foundTs);
        assertEquals("170.210(h)", foundTs.getName());
        assertEquals(editionId.longValue(), foundTs.getCertificationEditionId().longValue());
        assertEquals("2014", foundTs.getYear());
    }

    @Test
    @Transactional
    public void findByNumberAndEditionWithBadCaseAndSpaces() {
        String number = "170.210 (H)  ";
        Long editionId = 2L;
        TestStandardDTO foundTs = tsDao.getByNumberAndEdition(number, editionId);
        assertNotNull(foundTs);
        assertEquals("170.210(h)", foundTs.getName());
        assertEquals(editionId.longValue(), foundTs.getCertificationEditionId().longValue());
        assertEquals("2014", foundTs.getYear());
    }

    @Test
    @Transactional
    public void findNoTestStandardByNumberAndEdition() {
        String number = "BOGUS";
        Long editionId = 2L;
        TestStandardDTO foundTs = tsDao.getByNumberAndEdition(number, editionId);
        assertNull(foundTs);
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createTestStandardWithNumberAndEdition() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        String number = "None";
        Long editionId = 2L;
        TestStandardDTO toCreate = new TestStandardDTO();
        toCreate.setName(number);
        toCreate.setCertificationEditionId(editionId);

        try {
            TestStandardDTO createdTs = tsDao.create(toCreate);
            assertNotNull(createdTs);
            assertEquals(number, createdTs.getName());
            assertNull(createdTs.getDescription());
            assertEquals(editionId.longValue(), createdTs.getCertificationEditionId().longValue());
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    @Transactional
    @Rollback(true)
    public void createTestStandardsWithSameNumberDifferentEditions() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        TestStandardDTO toCreate = new TestStandardDTO();
        toCreate.setName("None");
        toCreate.setCertificationEditionId(2L);

        TestStandardDTO toCreate2 = new TestStandardDTO();
        toCreate2.setName("None");
        toCreate2.setCertificationEditionId(3L);

        try {
            tsDao.create(toCreate);
            tsDao.create(toCreate2);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
