package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
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
import gov.healthit.chpl.dao.CertificationEditionDAO;
import gov.healthit.chpl.dto.CertificationEditionDTO;
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
public class CertificationEditionTest extends TestCase {

    @Autowired
    private CertificationEditionDAO ceDao;
    private static JWTAuthenticatedUser authUser;

    @Rule
    @Autowired
    public UnitTestRules cacheInvalidationRule;

    @BeforeClass
    public static void setUpClass() throws Exception {
        authUser = new JWTAuthenticatedUser();
        authUser.setFullName("Admin");
        authUser.setId(-2L);
        authUser.getPermissions().add(new GrantedPermission("ROLE_ADMIN"));
    }

    @Test
    @Transactional
    public void getCertificationEditionByYear() {
        CertificationEditionDTO dto = ceDao.getByYear("2014");
        assertNotNull(dto);
    }

    @Test
    @Transactional
    public void testGetCertificationEditionsForListings() {
        List<Long> listingIds = new ArrayList<Long>();
        listingIds.add(1L); // 2014
        listingIds.add(2L); // 2014
        listingIds.add(3L); // 2011

        List<CertificationEditionDTO> editions = ceDao.getEditions(listingIds);
        assertNotNull(editions);
        assertEquals(2, editions.size());
        for (CertificationEditionDTO edition : editions) {
            switch (edition.getYear()) {
            case "2014":
            case "2011":
                break;
            default:
                fail("Expected 2011, 2014 but found " + edition.getYear());
            }
        }
    }
}
