package gov.healthit.chpl.dao.impl;

import java.util.List;

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
import gov.healthit.chpl.dao.CertifiedProductTestingLabDAO;
import gov.healthit.chpl.dto.CertifiedProductTestingLabDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import junit.framework.TestCase;

/**
 * Tests for Certified Product Testing Lab DAO mapping.
 * @author alarned
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { gov.healthit.chpl.CHPLTestConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class})
@DatabaseSetup("classpath:data/testData.xml")
public class CertifiedProductTestingLabDaoTest extends TestCase {
    @Autowired
    private CertifiedProductTestingLabDAO cptlDao;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    private static JWTAuthenticatedUser adminUser;
    private static final long ADMIN_ID = -2L;
    private static final long LISTING_WITH_ONE_ATL = 4L;
    private static final long LISTING_WITH_TWO_ATLS = 17L;

    /**
     * Log in user as admin.
     * @throws Exception if cannot grant permission
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        adminUser = new JWTAuthenticatedUser();
        adminUser.setFullName("Administrator");
        adminUser.setId(ADMIN_ID);
        adminUser.setFriendlyName("Administrator");
        adminUser.setSubjectName("admin");
        adminUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    /**
     * Ensure Listing with single ATL has single ATL.
     * @throws EntityRetrievalException if cannot retrieve entity
     */
    @Test
    @Transactional
    public void testGetTestingLabWithOneATL() throws EntityRetrievalException {
        List<CertifiedProductTestingLabDTO> testingLabs =
                cptlDao.getTestingLabsByCertifiedProductId(LISTING_WITH_ONE_ATL);
        assertEquals(1, testingLabs.size());
        assertEquals("My Testing Lab", testingLabs.get(0).getTestingLabName());
    }

    /**
     * Ensure Listing with two ATLs has both.
     * @throws EntityRetrievalException if cannot retrieve entity
     */
    @Test
    @Transactional
    public void testGetTestingLabWIthTwoATLs() throws EntityRetrievalException {
        int expectedTestingLabCount = 2;
        List<CertifiedProductTestingLabDTO> testingLabs =
                cptlDao.getTestingLabsByCertifiedProductId(LISTING_WITH_TWO_ATLS);
        assertEquals(expectedTestingLabCount, testingLabs.size());
        //we don't know what order the testing labs will be in
        //in the result so just make sure we found them both
        int foundCount = 0;
        String[] expectedTestingLabNames = {"ICSA Labs", "Drummond Group"};
        for (CertifiedProductTestingLabDTO foundTestingLab : testingLabs) {
            for (String expectedTestingLabName : expectedTestingLabNames) {
                if (foundTestingLab.getTestingLabName().equals(expectedTestingLabName)) {
                    foundCount++;
                }
            }
        }
        assertEquals(expectedTestingLabCount, foundCount);
    }
}
