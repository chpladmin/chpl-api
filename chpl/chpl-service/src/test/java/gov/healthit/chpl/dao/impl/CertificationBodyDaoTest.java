package gov.healthit.chpl.dao.impl;

import java.util.Date;
import java.util.List;

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
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import junit.framework.TestCase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        gov.healthit.chpl.CHPLTestConfig.class
})
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:data/testData.xml")
public class CertificationBodyDaoTest extends TestCase {

    @Autowired
    private CertificationBodyDAO acbDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

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

    @Test
    @Transactional
    public void testGetMaxAcbCode() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        String maxCode = acbDao.getMaxCode();
        assertNotNull(maxCode);
        assertEquals("08", maxCode);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    public void testGetAllAcbs() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        List<CertificationBodyDTO> acbs = acbDao.findAll();
        assertNotNull(acbs);
        assertEquals(8, acbs.size());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testCreateAcbWithAddress() throws EntityCreationException, EntityRetrievalException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertificationBodyDTO acb = new CertificationBodyDTO();
        acb.setName("ACB TEST 2");
        acb.setWebsite("http://www.google.com");
        AddressDTO address = new AddressDTO();
        address.setStreetLineOne("Some Street");
        address.setCity("Baltimore");
        address.setState("MD");
        address.setZipcode("21228");
        address.setCountry("USA");
        address.setDeleted(false);
        address.setLastModifiedDate(new Date());
        address.setLastModifiedUser(AuthUtil.getAuditId());
        acb.setAddress(address);
        acb = acbDao.create(acb);

        assertNotNull(acb);
        assertNotNull(acb.getId());
        assertTrue(acb.getId() > 0L);
        assertNotNull(acb.getAddress());
        assertNotNull(acb.getAddress().getId());
        assertTrue(acb.getAddress().getId() > 0L);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testUpdateAcb() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertificationBodyDTO toUpdate = acbDao.findAll().get(0);
        toUpdate.setName("UPDATED NAME");

        CertificationBodyDTO result = null;
        try {
            result = acbDao.update(toUpdate);
        } catch (Exception ex) {
            fail("could not update acb!");
            ex.printStackTrace();
        }
        assertNotNull(result);

        try {
            CertificationBodyDTO updatedAcb = acbDao.getById(toUpdate.getId());
            assertEquals("UPDATED NAME", updatedAcb.getName());
        } catch (Exception ex) {
            fail("could not find acb!");
            ex.printStackTrace();
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    @Transactional
    @Rollback
    public void testRetireAcb() {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        CertificationBodyDTO toUpdate = acbDao.findAll().get(0);
        toUpdate.setRetired(true);

        CertificationBodyDTO result = null;
        try {
            result = acbDao.update(toUpdate);
        } catch (Exception ex) {
            fail("could not update acb!");
            ex.printStackTrace();
        }
        assertNotNull(result);

        try {
            CertificationBodyDTO updatedAcb = acbDao.getById(toUpdate.getId());
            assertTrue(updatedAcb.isRetired());
        } catch (Exception ex) {
            fail("could not find acb!");
            ex.printStackTrace();
        }
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
