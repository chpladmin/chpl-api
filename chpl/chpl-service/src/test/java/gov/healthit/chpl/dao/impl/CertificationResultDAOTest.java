package gov.healthit.chpl.dao.impl;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
import gov.healthit.chpl.dao.CertificationResultDAO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
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
public class CertificationResultDAOTest extends TestCase {

    @Autowired
    private CertificationResultDAO certificationResultDAO;

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

    /**
     * Tests that the getTestToolsForCertificationResult() returns Given that a
     * user requests for a Certified Product Details When the
     * getTestToolsForCertificationResult() method is called for a
     * CertificationResult with more than one associated test tool Then a valid
     * result is returned
     * @throws EntityRetrievalException
     * @throws EntityCreationException
     */
    @Test
    @Transactional
    public void test_getTestToolsForCertificationResult() throws EntityRetrievalException, EntityCreationException {
        SecurityContextHolder.getContext().setAuthentication(adminUser);

        System.out.println("Running getTestToolsForCertificationResult() test");
        Long certResultId = 1L; // certResultId = 1 is associated with two
                                // certificationResultTestTools (id=1 & id=3)
        List<CertificationResultTestToolDTO> certCritDTOs = certificationResultDAO
                .getTestToolsForCertificationResult(certResultId);
        assertTrue("getTestToolsForCertificationResult() should return two CertificationResultTestTool but returned "
                + certCritDTOs.size(), certCritDTOs.size() == 2);
    }

}
